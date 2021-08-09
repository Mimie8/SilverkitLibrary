package com.amelie.silverkit

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.amelie.silverkit.datamanager.SkClicksData
import com.amelie.silverkit.datamanager.SkCoordsData
import java.sql.Timestamp

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
        const val C_TOPLEFT_X = "TOP_LEFT_X"
        const val C_TOPLEFT_Y = "TOP_LEFT_Y"
        const val C_BOTTOMRIGHT_X = "BOTTOM_RIGHT_X"
        const val C_BOTTOMRIGHT_Y = "BOTTOM_RIGHT_Y"

        const val T_DEVICE_DATA = "DEVICE_DATA_TABLE"
        const val C_DEVICE_DATA_ID = "DEVICE_DATA_ID"
        const val C_SCREEN_WIDTH = "SCREEN_WIDTH"
        const val C_SCREEN_HEIGHT = "SCREEN_HEIGHT"
        const val C_LAST_CORRECTIONS_DATE = "LAST_CORRECTIONS_DATE"
    }

    // This is called the first time a database is accessed. There should be code in there to create a new db
    override fun onCreate(db: SQLiteDatabase) {

        val createClickEventTable = "CREATE TABLE $T_CLICK_EVENTS ($C_CLICK_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_VIEW_ID TEXT, $C_VIEW_TYPE TEXT, $C_VIEW_ACTIVTY TEXT, $C_CLICK_X INT, $C_CLICK_Y INT, $C_TIMESTAMP TEXT)"
        val createViewDataTable = "CREATE TABLE $T_VIEW_DATA ($C_VIEW_DATA_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_VIEW_ID TEXT, $C_VIEW_ACTIVTY TEXT, $C_TOPLEFT_X INT, $C_TOPLEFT_Y INT, $C_BOTTOMRIGHT_X INT, $C_BOTTOMRIGHT_Y INT)"
        val createDeviceDataTable = "CREATE TABLE $T_DEVICE_DATA ($C_DEVICE_DATA_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_SCREEN_WIDTH INT, $C_SCREEN_HEIGHT INT, $C_LAST_CORRECTIONS_DATE TEXT)"

        db.execSQL(createClickEventTable)
        db.execSQL(createViewDataTable)
        db.execSQL(createDeviceDataTable)
    }

    // It is called if the db version number changes. It prevents previous users apps from breaking when you change the db design
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS $T_CLICK_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $T_VIEW_DATA")
        db.execSQL("DROP TABLE IF EXISTS $T_DEVICE_DATA")

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

        return if(result == 1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE CLICK DATA")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVE THE CLICK DATA")
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

            return if(result == 1L){
                Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE VIEW DATA")
                false
            } else {
                Log.d("info", "DATABASE SK : SUCCESSFULLY SAVE THE CLICK DATA")
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
        cv.put(C_LAST_CORRECTIONS_DATE, last_corrections)

        val result = db.insert(T_DEVICE_DATA, null, cv)

        return if(result == 1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE DEVICE DATA")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVE THE DEVICE DATA")
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

}