package com.amelie.silverkit

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.amelie.silverkit.datamanager.SkClicksData

class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, "SkDatabase", null, 1) {

    // static variables
    companion object {
        const val T_CLICK_EVENTS = "CLICK_EVENTS_TABLE"
        const val C_ID = "ID"
        const val C_VIEW_ID = "VIEW_ID"
        const val C_VIEW_TYPE = "VIEW_TYPE"
        const val C_VIEW_ACTIVTY = "VIEW_ACTIVITY"
        const val C_CLICK_X = "CLICK_X"
        const val C_CLICK_Y = "CLICK_Y"
        const val C_TIMESTAMP = "TIMESTAMP"
    }

    // This is called the first time a database is accessed. There should be code in there to create a new db
    override fun onCreate(db: SQLiteDatabase) {

        val createTableStatement = "CREATE TABLE $T_CLICK_EVENTS ($C_ID INTEGER PRIMARY KEY AUTOINCREMENT, $C_VIEW_ID TEXT, $C_VIEW_TYPE TEXT, $C_VIEW_ACTIVTY TEXT, $C_CLICK_X INT, $C_CLICK_Y INT, $C_TIMESTAMP TEXT)"

        db.execSQL(createTableStatement)
    }

    // It is called if the db version number changes. It prevents previous users apps from breaking when you change the db design
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
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
        return result != -1L
    }

}