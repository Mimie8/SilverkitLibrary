import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.amelie.silverkit.datamanager.SkAnalysisData
import com.amelie.silverkit.datamanager.SkClicksData
import com.amelie.silverkit.datamanager.SkCoordsData
import com.amelie.silverkit.datamanager.SkTacticsData
import java.io.*
import java.sql.SQLException


class TestDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    private val myContext: Context = context
    private var outFileName = ""
    private val DB_PATH: String
    private var db: SQLiteDatabase? = null

    companion object {
        val TAG = TestDatabaseHelper::class.java.simpleName
        var flag = 0

        // Exact Name of you db file that you put in assets folder with extension.
        var DB_NAME = "SkDatabase.db"

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
        const val C_BASE_COLOR = "BASE_COLOR"
        const val C_BASE_SIZE_WIDTH = "BASE_SIZE_WIDTH"
        const val C_BASE_SIZE_HEIGHT = "BASE_SIZE_HEIGHT"

        const val T_DEVICE_DATA = "DEVICE_DATA_TABLE"
        const val C_DEVICE_DATA_ID = "ID"
        const val C_SCREEN_WIDTH = "SCREEN_WIDTH"
        const val C_SCREEN_HEIGHT = "SCREEN_HEIGHT"

        const val T_ANALYSIS_DATA = "ANALYSIS_DATA_TABLE"
        const val C_ANALYSIS_DATA_ID = "ID"
        // VIEW ID AND VIEW ACTIVITY
        const val C_ERROR_RATIO = "ERROR_RATIO"
        const val C_AVERAGE_DIST_FROM_BORDER = "AVERAGE_DIST_FROM_BORDER"
        const val C_DIST_GRAVITY_CENTER = "DIST_GRAVITY_CENTER"
        const val C_GRAVITY_CENTER_X = "GRAVITY_CENTER_X"
        const val C_GRAVITY_CENTER_Y = "GRAVITY_CENTER_Y"

        const val T_TACTICS_DATA = "TACTICS_DATA_TABLE"
        const val C_TACTICS_DATA_ID = "ID"
        const val C_VIEW_COLOR = "VIEW_COLOR"
        const val C_PADDING_START = "PADDING_START"
        const val C_PADDING_END = "PADDING_END"
        const val C_PADDING_TOP = "PADDING_TOP"
        const val C_PADDING_BOTTOM = "PADDING_BOTTOM"
        const val C_OLD_PADDING_START = "OLD_PADDING_START"
        const val C_OLD_PADDING_END = "OLD_PADDING_END"
        const val C_OLD_PADDING_TOP = "OLD_PADDING_TOP"
        const val C_OLD_PADDING_BOTTOM = "OLD_PADDING_BOTTOM"
        const val C_VIEW_WIDTH = "VIEW_WIDTH"
        const val C_VIEW_HEIGHT = "VIEW_HEIGHT"

        const val T_ANALYSIS_TIMESTAMPS = "ANALYSIS_TIMESTAMP"
        const val C_ANALYSIS_TIMESTAMP_ID = "ID"
        const val C_ACTIVITY = "ACTIVITY"
        const val C_CORRECTIONS_TIMESTAMP = "CORRECTIONS_TIMESTAMP"
    }
    init {
        val cw = ContextWrapper(context)
        DB_PATH = cw.filesDir.absolutePath + "/databases/"
        Log.e(TAG, "TestDatabaseHelper: DB_PATH $DB_PATH")
        outFileName = DB_PATH + DB_NAME
        val file = File(DB_PATH)
        Log.e(TAG, "TestDatabaseHelper: " + file.exists())
        if (!file.exists()) {
            file.mkdir()
        }
    }


    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist) {
            //do nothing - database already exist
        } else {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.readableDatabase
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            checkDB = SQLiteDatabase.openDatabase(outFileName, null, SQLiteDatabase.OPEN_READWRITE)
        } catch (e: SQLiteException) {
            try {
                copyDataBase()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        checkDB?.close()
        return checkDB != null
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    @Throws(IOException::class)
    private fun copyDataBase() {
        Log.i(
            "Database",
            "New database is being copied to device!"
        )
        val buffer = ByteArray(1024)
        var myOutput: OutputStream? = null
        var length: Int
        // Open your local db as the input stream
        var myInput: InputStream? = null
        try {
            myInput = myContext.getAssets().open(DB_NAME)
            // transfer bytes from the inputfile to the
            // outputfile
            myOutput = FileOutputStream(DB_PATH + DB_NAME)
            while (myInput.read(buffer).also { length = it } > 0) {
                myOutput.write(buffer, 0, length)
            }
            myOutput.close()
            myOutput.flush()
            myInput.close()
            Log.i(
                "Database",
                "New database has been copied to device!"
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(SQLException::class)
    fun openDataBase() : SQLiteDatabase{
        //Open the database
        val myPath = DB_PATH + DB_NAME
        Log.e(TAG, "openDataBase: Open " + db!!.isOpen)

        return SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @Synchronized
    override fun close() {
        if (db != null) db!!.close()
        super.close()
    }

    override fun onCreate(arg0: SQLiteDatabase) {}
    override fun onUpgrade(arg0: SQLiteDatabase, arg1: Int, arg2: Int) {}

    // =============================================================================================

    // Add click to db
    fun addClickEvent(db : SQLiteDatabase, click_data : SkClicksData) : Boolean {

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
    fun addViewData(db : SQLiteDatabase, view_data: SkCoordsData) : Boolean{

        // Check if view data is already saved before saving
        if(!isDataAlreadyInDB(db, T_VIEW_DATA, C_VIEW_ID, view_data.viewID)){

            val cv = ContentValues()

            cv.put(C_VIEW_ID, view_data.viewID)
            cv.put(C_VIEW_ACTIVTY, view_data.viewLocal)
            cv.put(C_TOPLEFT_X, view_data.coordTL?.get(0))
            cv.put(C_TOPLEFT_Y, view_data.coordTL?.get(1))
            cv.put(C_BOTTOMRIGHT_X, view_data.coordDR?.get(0))
            cv.put(C_BOTTOMRIGHT_Y, view_data.coordDR?.get(1))
            cv.put(C_BASE_COLOR, view_data.baseColor)
            cv.put(C_BASE_SIZE_WIDTH, view_data.baseSizeWidth)
            cv.put(C_BASE_SIZE_HEIGHT, view_data.baseSizeHeight)

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
    fun addDeviceData(db : SQLiteDatabase, screen_width:Int, screen_height:Int) : Boolean{

        val cv = ContentValues()

        cv.put(C_SCREEN_WIDTH, screen_width)
        cv.put(C_SCREEN_HEIGHT, screen_height)

        val result = db.insert(T_DEVICE_DATA, null, cv)

        return if(result == -1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE DEVICE DATA")
            Log.d("info", "DATABASE SK : $screen_width, $screen_height")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE DEVICE DATA")
            true
        }

    }

    // Check if data already in DB or not
    private fun isDataAlreadyInDB(db : SQLiteDatabase, table_name : String, field_name: String, field_value: String?) : Boolean{

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
    fun addAnalysisData(db : SQLiteDatabase, analysisData : SkAnalysisData){
        // Check if analysis data for this view already exist, if yes update data else add new row of data

        val result = analysisDataExists(db, analysisData)
        if(result != -1){
            // Update analysis data in row of id = result
            updateAnalysisData(db, result, analysisData)
        } else {
            // Create row to add analysis data
            addNewAnalysisData(db, analysisData)
        }
    }

    private fun updateAnalysisData(db : SQLiteDatabase, id: Int, analysisData : SkAnalysisData) : Boolean{

        val cv = ContentValues()
        cv.put(C_ERROR_RATIO, roundTo2Decimal(analysisData.errorRatio))
        cv.put(C_AVERAGE_DIST_FROM_BORDER, roundTo2Decimal(analysisData.averageDistFromBorder))
        cv.put(C_DIST_GRAVITY_CENTER, roundTo2Decimal(analysisData.distGravityCenter))
        cv.put(C_GRAVITY_CENTER_X, analysisData.gravityCenterX)
        cv.put(C_GRAVITY_CENTER_Y, analysisData.gravityCenterY)

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

    private fun addNewAnalysisData(db : SQLiteDatabase, analysisData : SkAnalysisData) : Boolean{

        val cv = ContentValues()

        cv.put(C_VIEW_ID, analysisData.viewID)
        cv.put(C_VIEW_ACTIVTY, analysisData.viewLocal)
        cv.put(C_ERROR_RATIO, roundTo2Decimal(analysisData.errorRatio))
        cv.put(C_AVERAGE_DIST_FROM_BORDER, roundTo2Decimal(analysisData.averageDistFromBorder))
        cv.put(C_DIST_GRAVITY_CENTER, roundTo2Decimal(analysisData.distGravityCenter))
        cv.put(C_GRAVITY_CENTER_X, analysisData.gravityCenterX)
        cv.put(C_GRAVITY_CENTER_Y, analysisData.gravityCenterY)

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
    private fun analysisDataExists(db : SQLiteDatabase, analysisData : SkAnalysisData) : Int{

        val viewID = analysisData.viewID
        val activity = analysisData.viewLocal

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
    fun isAnalysisTime(activity: String) : Boolean{
        // Change condition of analysis start

        // Ex : If last analysis timestamp of this activity is 5 days ago
        // Do not forget to check if the line in table T_ANALYSIS_TIMESTAMPS exists with this activity before getting the timestamp
        return true
    }

    fun getClicksDataOfActivity(db : SQLiteDatabase, activity : String, lastCorrectionTimestamp : String?) : MutableList<SkClicksData> {

        val query = if(lastCorrectionTimestamp == null){
            "SELECT * FROM $T_CLICK_EVENTS WHERE $C_VIEW_ACTIVTY = '$activity'"
        } else {
            "SELECT * FROM $T_CLICK_EVENTS WHERE $C_TIMESTAMP > \'$lastCorrectionTimestamp\' AND $C_VIEW_ACTIVTY = '$activity'"
        }

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

    fun getViewsDataOfActivity(db : SQLiteDatabase, activity: String) : MutableList<SkCoordsData>{

        val query = "SELECT * FROM $T_VIEW_DATA WHERE $C_VIEW_ACTIVTY = \'$activity\'"

        val viewsData = mutableListOf<SkCoordsData>()

        try{
            val cursor = db.rawQuery(query, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val viewID = cursor.getString(1)
                        val viewActivity = cursor.getString(2)
                        val tl_x = cursor.getInt(3)
                        val tl_y = cursor.getInt(4)
                        val dr_x = cursor.getInt(5)
                        val dr_y = cursor.getInt(6)
                        val baseColor = cursor.getInt(7)
                        val baseWidth = cursor.getInt(8)
                        val baseHeight = cursor.getInt(9)

                        val data = SkCoordsData(viewID, viewActivity, listOf(tl_x, tl_y), listOf(dr_x, dr_y), baseColor, baseWidth, baseHeight)
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

    fun getViewData(db : SQLiteDatabase, viewID:String, activity: String): SkCoordsData?{

        val query = "SELECT * FROM $T_VIEW_DATA WHERE $C_VIEW_ID = \'$viewID\' AND $C_VIEW_ACTIVTY = \'$activity\'"

        return try{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val id = cursor.getString(1)
                val viewActivity = cursor.getString(2)
                val tl_x = cursor.getInt(3)
                val tl_y = cursor.getInt(4)
                val dr_x = cursor.getInt(5)
                val dr_y = cursor.getInt(6)
                val color = cursor.getInt(7)
                val width = cursor.getInt(8)
                val height = cursor.getInt(9)

                val result = SkCoordsData(id, viewActivity, listOf(tl_x, tl_y), listOf(dr_x, dr_y), color, width, height)

                cursor.close()
                result
            } else {
                cursor.close()
                null
            }
        } catch (e: Exception){
            null
        }
    }

    fun getDeviceData(db : SQLiteDatabase) : List<Any>{

        val query = "SELECT * FROM $T_DEVICE_DATA"

        return try{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val width = cursor.getInt(1)
                val height = cursor.getInt(2)
                cursor.close()
                listOf(width, height)
            } else {
                cursor.close()
                listOf()
            }
        } catch (e: Exception){
            listOf()
        }
    }

    fun getAnalysisData(db : SQLiteDatabase, viewID: String, activity: String): SkAnalysisData?{

        val query = "SELECT * FROM $T_ANALYSIS_DATA WHERE $C_VIEW_ACTIVTY = \'$activity\' AND $C_VIEW_ID = \'$viewID\'"

        return try{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val viewid = cursor.getString(1)
                val viewActivity = cursor.getString(2)
                val errorRatio = cursor.getString(3)
                val averageDistFromBorder = cursor.getString(4)
                val distGravityCenter = cursor.getString(5)
                val gravityX = cursor.getInt(6)
                val gravityY = cursor.getInt(7)

                cursor.close()
                SkAnalysisData(viewid, viewActivity, errorRatio.toFloat(), averageDistFromBorder.toFloat(), distGravityCenter.toFloat(), gravityX, gravityY)
            } else {
                cursor.close()
                null
            }
        } catch (e: Exception){
            null
        }

    }

    fun updateLastCorrectionTimestamp(db : SQLiteDatabase, newTimestamp : String, activity: String) : Boolean{

        // Check if correction timestamp data for this activity already exist, if yes update data else add new row of data

        val result = isDataAlreadyInDB(db, T_ANALYSIS_TIMESTAMPS, C_ACTIVITY, activity)
        if(result){

            // Update correction timestamp data in activity

            val cv = ContentValues()
            cv.put(C_CORRECTIONS_TIMESTAMP, newTimestamp)

            val where = "${C_ACTIVITY}=?"
            val whereArgs = arrayOf(activity)

            return try{
                db.update(T_ANALYSIS_TIMESTAMPS, cv, where, whereArgs)
                Log.d("info", "DATABASE SK : SUCCESSFULLY UPDATED THE LAST CORRECTION TIMESTAMP")
                true
            } catch (e: Exception){
                Log.d("info", "DATABASE SK : ERROR WHILE UPDATING THE LAST CORRECTION TIMESTAMP")
                false
            }

        } else {
            // Create row to add correction timestamp data

            val cv = ContentValues()

            cv.put(C_ACTIVITY, activity)
            cv.put(C_CORRECTIONS_TIMESTAMP, newTimestamp)

            val resultAdd = db.insert(T_ANALYSIS_TIMESTAMPS, null, cv)

            return if(resultAdd == -1L){
                Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE CORRECTION TIMESTAMP OF $activity")
                false
            } else {
                Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE CORRECTION TIMESTAMP OF $activity")
                true
            }
        }

    }

    fun getLastCorrectionTimestampOfActivity(db : SQLiteDatabase, activity: String) : String?{

        val query = "SELECT * FROM ${T_ANALYSIS_TIMESTAMPS} WHERE ${C_ACTIVITY} = \'$activity\'"

        return try{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val time = cursor.getString(2)
                cursor.close()
                time
            } else {
                cursor.close()
                null
            }
        } catch (e: Exception){
            null
        }
    }

    fun getViewBaseColor(db : SQLiteDatabase, viewID : String, activity: String) : Int?{

        val query = "SELECT * FROM ${T_VIEW_DATA} WHERE ${C_VIEW_ID} = \'$viewID\' AND ${C_VIEW_ACTIVTY} = '$activity'"

        return try{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val color = cursor.getInt(7)
                cursor.close()
                color
            } else {
                cursor.close()
                null
            }
        } catch (e: Exception){
            null
        }
    }

    private fun roundTo2Decimal(d: Float): String {
        return "%.2f".format(d)
    }

    fun saveTacticsData(db : SQLiteDatabase, data: SkTacticsData){
        val result = tacticsDataExists(db, data)
        if(result != -1){
            // Update analysis data in row of id = result
            updateTacticsData(db, result, data)
        } else {
            // Create row to add analysis data
            addTacticsData(db, data)
        }
    }

    private fun addTacticsData(db : SQLiteDatabase, data : SkTacticsData) : Boolean{

        val cv = ContentValues()

        cv.put(C_VIEW_ID, data.viewID)
        cv.put(C_VIEW_ACTIVTY, data.viewLocal)
        cv.put(C_VIEW_COLOR, data.color)
        cv.put(C_PADDING_START, data.paddingStart)
        cv.put(C_PADDING_END, data.paddingEnd)
        cv.put(C_PADDING_TOP, data.paddingTop)
        cv.put(C_PADDING_BOTTOM, data.paddingBottom)
        cv.put(C_OLD_PADDING_START, data.oldPaddingStart)
        cv.put(C_OLD_PADDING_END, data.oldPaddingEnd)
        cv.put(C_OLD_PADDING_TOP, data.oldPaddingTop)
        cv.put(C_OLD_PADDING_BOTTOM, data.oldPaddingBottom)
        cv.put(C_VIEW_WIDTH, data.viewWidth)
        cv.put(C_VIEW_HEIGHT, data.viewHeight)

        val result = db.insert(T_TACTICS_DATA, null, cv)

        return if(result == -1L){
            Log.d("info", "DATABASE SK : ERROR WHILE SAVING THE TACTICS DATA OF ${data.viewID}")
            false
        } else {
            Log.d("info", "DATABASE SK : SUCCESSFULLY SAVED THE TACTICS DATA OF ${data.viewID}")
            true
        }
    }

    private fun updateTacticsData(db : SQLiteDatabase, id:Int, data : SkTacticsData) : Boolean{

        val cv = ContentValues()
        cv.put(C_VIEW_COLOR, data.color)
        cv.put(C_PADDING_START, data.paddingStart)
        cv.put(C_PADDING_END, data.paddingEnd)
        cv.put(C_PADDING_TOP, data.paddingTop)
        cv.put(C_PADDING_BOTTOM, data.paddingBottom)
        cv.put(C_OLD_PADDING_START, data.oldPaddingStart)
        cv.put(C_OLD_PADDING_END, data.oldPaddingEnd)
        cv.put(C_OLD_PADDING_TOP, data.oldPaddingTop)
        cv.put(C_OLD_PADDING_BOTTOM, data.oldPaddingBottom)
        cv.put(C_VIEW_WIDTH, data.viewWidth)
        cv.put(C_VIEW_HEIGHT, data.viewHeight)

        val where = "id=?"
        val whereArgs = arrayOf(java.lang.String.valueOf(id))

        return try{
            db.update(T_TACTICS_DATA, cv, where, whereArgs)
            Log.d("info", "DATABASE SK : SUCCESSFULLY UPDATED THE TACTICS DATA OF ${data.viewID}")
            true
        } catch (e: Exception){
            Log.d("info", "DATABASE SK : ERROR WHILE UPDATING THE TACTICS DATA OF ${data.viewID}")
            false
        }
    }

    private fun tacticsDataExists(db : SQLiteDatabase, data : SkTacticsData) : Int{

        val viewID = data.viewID
        val activity = data.viewLocal

        val query = "SELECT * FROM ${T_TACTICS_DATA} WHERE ${C_VIEW_ID} = \"$viewID\" AND ${C_VIEW_ACTIVTY} = \"$activity\" "
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

    fun getTacticsDataOfActivity(db : SQLiteDatabase, activity: String): MutableList<SkTacticsData>{

        val query = "SELECT * FROM ${T_TACTICS_DATA} WHERE ${C_VIEW_ACTIVTY} = \'$activity\'"

        val tacticsData = mutableListOf<SkTacticsData>()

        try{
            val cursor = db.rawQuery(query, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val viewID = cursor.getString(1)
                        val viewActivity = cursor.getString(2)
                        val color = cursor.getInt(3)
                        val paddingStart = cursor.getInt(4)
                        val paddingEnd = cursor.getInt(5)
                        val paddingTop = cursor.getInt(6)
                        val paddingBottom = cursor.getInt(7)
                        val oldPaddingStart = cursor.getInt(8)
                        val oldPaddingEnd = cursor.getInt(9)
                        val oldPaddingTop = cursor.getInt(10)
                        val oldPaddingBottom = cursor.getInt(11)
                        val viewWidth = cursor.getInt(12)
                        val viewHeight = cursor.getInt(13)

                        val data = SkTacticsData(viewID, viewActivity, color, paddingStart, paddingEnd, paddingTop, paddingBottom, oldPaddingStart, oldPaddingEnd, oldPaddingTop,oldPaddingBottom, viewWidth, viewHeight)
                        tacticsData.add(data)
                    }
                }
                cursor.close() // close your cursor when you don't need it anymore
            }
            return tacticsData
        } catch (e: Exception){
            return mutableListOf()
        }
    }

    fun getTacticsDataOfView(db : SQLiteDatabase, viewID: String, activity:String): SkTacticsData?{

        val query = "SELECT * FROM ${T_TACTICS_DATA} WHERE ${C_VIEW_ACTIVTY} = \'$activity\' AND ${C_VIEW_ID} = \'$viewID\'"

        val cursor = db.rawQuery(query, null)
        return if(cursor.moveToFirst()){
            val id = cursor.getString(1)
            val viewActivity = cursor.getString(2)
            val color = cursor.getInt(3)
            val paddingStart = cursor.getInt(4)
            val paddingEnd = cursor.getInt(5)
            val paddingTop = cursor.getInt(6)
            val paddingBottom = cursor.getInt(7)
            val oldPaddingStart = cursor.getInt(8)
            val oldPaddingEnd = cursor.getInt(9)
            val oldPaddingTop = cursor.getInt(10)
            val oldPaddingBottom = cursor.getInt(11)
            val viewWidth = cursor.getInt(12)
            val viewHeight = cursor.getInt(13)

            val data = SkTacticsData(id, viewActivity, color, paddingStart, paddingEnd, paddingTop, paddingBottom, oldPaddingStart, oldPaddingEnd, oldPaddingTop,oldPaddingBottom, viewWidth, viewHeight)
            cursor.close()
            data
        } else {
            cursor.close()
            null
        }
    }

}