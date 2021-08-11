package com.amelie.silverkit

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.amelie.silverkit.datamanager.SkAnalysisData
import com.amelie.silverkit.datamanager.SkClicksData
import com.amelie.silverkit.datamanager.SkCoordsData
import java.math.BigDecimal


class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, "SkDatabase", null, 1) {

    // static variables
    companion object {
        const val T_CLICK_EVENTS = "CLICK_EVENTS_TABLE"
        const val C_CLICK_ID = "ID"
        const val C_VIEW_ID = "VIEW_ID"
        const val C_VIEW_TYPE = "VIEW_TYPE"
        const val C_VIEW_ACTIVTY = "VIEW_ACTIVITY"
        const val C_CLICK_X = "CLICK_X"
        const val C_CLICK_Y = "CLICK_Y"
        const val C_TIMESTAMP = "TIMESTAMP"

        const val T_VIEW_DATA = "VIEW_DATA_TABLE"
        const val C_VIEW_DATA_ID = "ID"
        // VIEW ID AND VIEW ACTIVITY
        const val C_TOPLEFT_X = "TOP_LEFT_X"
        const val C_TOPLEFT_Y = "TOP_LEFT_Y"
        const val C_BOTTOMRIGHT_X = "BOTTOM_RIGHT_X"
        const val C_BOTTOMRIGHT_Y = "BOTTOM_RIGHT_Y"

        const val T_DEVICE_DATA = "DEVICE_DATA_TABLE"
        const val C_DEVICE_DATA_ID = "ID"
        const val C_SCREEN_WIDTH = "SCREEN_WIDTH"
        const val C_SCREEN_HEIGHT = "SCREEN_HEIGHT"
        const val C_CORRECTIONS_TIMESTAMP = "CORRECTIONS_TIMESTAMP"

        const val T_ANALYSIS_DATA = "ANALYSIS_DATA_TABLE"
        const val C_ANALYSIS_DATA_ID = "ID"
        // VIEW ID AND VIEW ACTIVITY
        const val C_ERROR_RATIO = "ERROR_RATIO"
        const val C_AVERAGE_DIST_FROM_BORDER = "AVERAGE_DIST_FROM_BORDER"
        const val C_DIST_GRAVITY_CENTER = "DIST_GRAVITY_CENTER"
    }

    // This is called the first time a database is accessed. There should be code in there to create a new db
    override fun onCreate(db: SQLiteDatabase) {

        val createClickEventTable = "CREATE TABLE $T_CLICK_EVENTS ($C_CLICK_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_VIEW_ID TEXT, $C_VIEW_TYPE TEXT, $C_VIEW_ACTIVTY TEXT, $C_CLICK_X INT, $C_CLICK_Y INT, $C_TIMESTAMP TEXT)"
        val createViewDataTable = "CREATE TABLE $T_VIEW_DATA ($C_VIEW_DATA_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_VIEW_ID TEXT, $C_VIEW_ACTIVTY TEXT, $C_TOPLEFT_X INT, $C_TOPLEFT_Y INT, $C_BOTTOMRIGHT_X INT, $C_BOTTOMRIGHT_Y INT)"
        val createDeviceDataTable = "CREATE TABLE $T_DEVICE_DATA ($C_DEVICE_DATA_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_SCREEN_WIDTH INT, $C_SCREEN_HEIGHT INT, $C_CORRECTIONS_TIMESTAMP TEXT)"
        val createAnalysisDataTable = "CREATE TABLE $T_ANALYSIS_DATA ($C_ANALYSIS_DATA_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_VIEW_ID TEXT, $C_VIEW_ACTIVTY TEXT, $C_ERROR_RATIO FLOAT, $C_AVERAGE_DIST_FROM_BORDER FLOAT, $C_DIST_GRAVITY_CENTER FLOAT)"

        db.execSQL(createClickEventTable)
        db.execSQL(createViewDataTable)
        db.execSQL(createDeviceDataTable)
        db.execSQL(createAnalysisDataTable)
    }

    // It is called if the db version number changes. It prevents previous users apps from breaking when you change the db design
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS $T_CLICK_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $T_VIEW_DATA")
        db.execSQL("DROP TABLE IF EXISTS $T_DEVICE_DATA")
        db.execSQL("DROP TABLE IF EXISTS $T_ANALYSIS_DATA")

        onCreate(db)
    }


    // Add click to db
    fun addClickEvent(click_data : SkClicksData) : Boolean {

        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(C_VIEW_ID, click_data.viewID)
        cv.put(C_VIEW_TYPE, click_data.viewType.toString())
        cv.put(C_VIEW_ACTIVTY, click_data.viewLocal)
        cv.put(C_CLICK_X, click_data.rawX)
        cv.put(C_CLICK_Y, click_data.rawY)
        cv.put(C_TIMESTAMP, click_data.timestamp.toString())

        val result = db.insert(T_CLICK_EVENTS, null, cv)

        return if(result == -1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE CLICK DATA")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE CLICK DATA")
            true
        }
    }

    // Add view data to db
    fun addViewData(view_data: SkCoordsData) : Boolean{

        // Check if view data is already saved before saving
        if(!isDataAlreadyInDB(T_VIEW_DATA, C_VIEW_ID, view_data.viewID)){

            val db = this.writableDatabase
            val cv = ContentValues()

            cv.put(C_VIEW_ID, view_data.viewID)
            cv.put(C_VIEW_ACTIVTY, view_data.viewLocal)
            cv.put(C_TOPLEFT_X, view_data.coordTL?.get(0))
            cv.put(C_TOPLEFT_Y, view_data.coordTL?.get(1))
            cv.put(C_BOTTOMRIGHT_X, view_data.coordDR?.get(0))
            cv.put(C_BOTTOMRIGHT_Y, view_data.coordDR?.get(1))

            val result = db.insert(T_VIEW_DATA, null, cv)

            return if(result == -1L){
                Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE VIEW DATA")
                Log.d("info", "DATABASE SK : ${view_data.toString()}")
                false
            } else {
                Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE VIEW DATA")
                true
            }

        } else {
            Log.d("info", "DATABASE SK : VIEW DATA IS ALREADY SAVED")
            return true
        }
    }

    // Add hardware data to db
    fun addDeviceData(screen_width:Int, screen_height:Int, last_corrections:String) : Boolean{

        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(C_SCREEN_WIDTH, screen_width)
        cv.put(C_SCREEN_HEIGHT, screen_height)
        cv.put(C_CORRECTIONS_TIMESTAMP, last_corrections)

        val result = db.insert(T_DEVICE_DATA, null, cv)

        return if(result == -1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE DEVICE DATA")
            Log.d("info", "DATABASE SK : $screen_width, $screen_height, $last_corrections")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE DEVICE DATA")
            true
        }

    }

    // Check if data already in DB or not
    private fun isDataAlreadyInDB(table_name : String, field_name: String, field_value: String?) : Boolean{

        val db = this.readableDatabase
        val query = "SELECT * FROM $table_name WHERE $field_name = \"$field_value\" "
        val cursor = db.rawQuery(query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    // Add analysis data
    fun addAnalysisData(analysisData : SkAnalysisData){
        // Check if analysis data for this view already exist, if yes update data else add new row of data

        val result = analysisDataExists(analysisData)
        if(result != -1){
            // Update analysis data in row of id = result
            updateAnalysisData(result, analysisData)
        } else {
            // Create row to add analysis data
            addNewAnalysisData(analysisData)
        }
    }

    private fun updateAnalysisData(id: Int, analysisData : SkAnalysisData) : Boolean{

        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(C_ERROR_RATIO, roundTo2Decimal(analysisData.errorRatio))
        cv.put(C_AVERAGE_DIST_FROM_BORDER, roundTo2Decimal(analysisData.averageDistFromBorder))
        cv.put(C_DIST_GRAVITY_CENTER, roundTo2Decimal(analysisData.distGravityCenter))

        val where = "id=?"
        val whereArgs = arrayOf(java.lang.String.valueOf(id))

        return try{
            db.update(T_ANALYSIS_DATA, cv, where, whereArgs)
            Log.d("info", "DATABASE SK : SUCCESSFULLY UPDATED THE ANALYSIS DATA OF ${analysisData.viewID}")
            true
        } catch (e: Exception){
            Log.d("info", "DATABASE SK : ERROR WHILE UPDATING THE ANALYSIS DATA OF ${analysisData.viewID}")
            false
        }
    }

    private fun addNewAnalysisData(analysisData : SkAnalysisData) : Boolean{
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(C_VIEW_ID, analysisData.viewID)
        cv.put(C_VIEW_ACTIVTY, analysisData.viewLocal)
        cv.put(C_ERROR_RATIO, roundTo2Decimal(analysisData.errorRatio))
        cv.put(C_AVERAGE_DIST_FROM_BORDER, roundTo2Decimal(analysisData.averageDistFromBorder))
        cv.put(C_DIST_GRAVITY_CENTER, roundTo2Decimal(analysisData.distGravityCenter))

        val result = db.insert(T_ANALYSIS_DATA, null, cv)

        return if(result == -1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE ANALYSIS DATA OF ${analysisData.viewID}")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE ANALYSIS DATA OF ${analysisData.viewID}")
            true
        }
    }

    // Check if analysis data for this view already exist
    // Return -1 if it doesn't else return row id of data
    private fun analysisDataExists(analysisData : SkAnalysisData) : Int{

        val viewID = analysisData.viewID
        val activity = analysisData.viewLocal

        val db = this.readableDatabase
        val query = "SELECT $C_ANALYSIS_DATA_ID FROM $T_ANALYSIS_DATA WHERE $C_VIEW_ID = \"$viewID\" AND $C_VIEW_ACTIVTY = \"$activity\" "
        val cursor = db.rawQuery(query, null)
        return if(cursor.moveToFirst()){
            val id = cursor.getInt(0)
            cursor.close()
            id
        } else {
            cursor.close()
            -1
        }
    }

    // Start analysis
    fun isAnalysisTime() : Boolean{
        // Change condition of analysis start
        return true
    }

    fun getClicksData() : MutableList<SkClicksData> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $T_CLICK_EVENTS"

        val clicksData = mutableListOf<SkClicksData>()

        try{
            val cursor = db.rawQuery(query, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val viewID = cursor.getString(1)
                        val viewType = cursor.getString(2)
                        val viewActivity = cursor.getString(3)
                        val x = cursor.getInt(4)
                        val y = cursor.getInt(5)
                        val timestamp = cursor.getString(6)

                        val data = SkClicksData(viewID, viewType, viewActivity, x, y, timestamp)
                        clicksData.add(data)
                    }
                }
                cursor.close() // close your cursor when you don't need it anymore
            }
            return clicksData
        } catch (e: Exception){
            return mutableListOf()
        }
    }

    fun getViewsData() : MutableList<SkCoordsData>{
        val db = this.readableDatabase
        val query = "SELECT * FROM $T_VIEW_DATA"

        val viewsData = mutableListOf<SkCoordsData>()

        try{
            val cursor = db.rawQuery(query, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    cursor.move(0)
                    while (cursor.moveToNext()) {
                        val viewID = cursor.getString(1)
                        val viewActivity = cursor.getString(2)
                        val tl_x = cursor.getInt(3)
                        val tl_y = cursor.getInt(4)
                        val dr_x = cursor.getInt(5)
                        val dr_y = cursor.getInt(6)

                        val data = SkCoordsData(viewID, viewActivity, listOf(tl_x, tl_y), listOf(dr_x, dr_y))
                        viewsData.add(data)
                    }
                }
                cursor.close() // close your cursor when you don't need it anymore
            }
            return viewsData
        } catch (e: Exception){
            return mutableListOf()
        }
    }

    fun getDeviceData() : List<Any>{
        val db = this.readableDatabase
        val query = "SELECT * FROM $T_DEVICE_DATA"

        return try{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val width = cursor.getInt(1)
                val height = cursor.getInt(2)
                val correctionTimestamp = cursor.getString(3)
                cursor.close()
                listOf(width, height, correctionTimestamp)
            } else {
                cursor.close()
                listOf()
            }
        } catch (e: Exception){
            listOf()
        }
    }

    private fun roundTo2Decimal(d: Float): Float {
        val decimalPlace = 2
        var bd = BigDecimal(java.lang.Float.toString(d))
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN)
        return bd.toFloat()
    }

}