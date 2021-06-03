package com.amelie.silverkit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.amelie.silverkit.datamanager.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.sql.Timestamp


interface SkTools {

    enum class ViewType {
        NONE, ABSSEEKBAR, ABSSPINNER, ACTIONMENUVIEW, AUTOCOMPLETETEXTVIEW, BUTTON,
        CALENDARVIEW, CHECKBOX, CHECKEDTEXTVIEW, CHRONOMETER, COMPOUNDBUTTON, CONSTRAINTLAYOUT,
        DATEPICKER, EDITTEXT, EXPANDABLELISTVIEW, FAB, GRIDLAYOUT, GRIDVIEW, HORIZONTALSCROLLVIEW,
        IMAGEBUTTON, IMAGESWITCHER, IMAGEVIEW, LINEARLAYOUT, LISTVIEW, MEDIACONTROLLER,
        MULTIAUTOCOMPLETETEXTVIEW, NUMBERPICKER, PROGRESSBAR, QUICKCONTACTBADGE, RADIOBUTTON,
        RADIOGROUP, RATINGBAR, RECYCLERVIEW, RELATIVELAYOUT, SCROLLVIEW, SEARCHVIEW, SEEKBAR,
        SPINNER, STACKVIEW, SWITCH, TABLELAYOUT, TABLEROW, TABWIDGET, TEXTCLOCK, TEXTSWITCHER,
        TEXTVIEW, TIMEPICKER, TOGGLEBUTTON, VIDEOVIEW, VIEWANIMATOR, VIEWFLIPPER, VIEWSWITCHER
    }

    fun toolOnTouch(view: View, event: MotionEvent) {

        if(event.action == MotionEvent.ACTION_DOWN){
            //PRESS ON THE VIEW

            val viewID = getViewID(view)
            val viewType = getViewType(view)
            val viewLocal = getViewLocal(view)
            val rawX = event.rawX.toInt()
            val rawY = event.rawY.toInt()
            val timestamp = Timestamp(System.currentTimeMillis())

            val touchData = SkClicksData(viewID, viewType, viewLocal, rawX, rawY, timestamp)

            //Save touch data in CSV file
            saveClicks(view, touchData)

            //Save hardware info in CSV file if the info isn't already saved
            saveHardwareData(view)

            Log.d("info", "SILVERKIT TOOL ONTOUCH : ID = $viewID | VIEW = $viewType |LOCAL = $viewLocal | X = $rawX | Y = $rawY | TIMESTAMP : $timestamp")
        }

    }

    fun getType() : ViewType

    fun getViewID(view: View): String {

        // view doesn't have an id
        if (view.id == View.NO_ID) {
            view.id = View.generateViewId()
        }

        return getViewType(view).toString() + "-" + view.id

    }

    private fun getViewType(view: View) : ViewType{

        if(view is SkTools){
            return view.getType()
        } else {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
            return ViewType.NONE
        }

    }

    fun getViewLocal(view: View) : String {
        return view.context.javaClass.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveClicks(view: View, touchData: SkClicksData){

        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/ClicksData.csv"

        try {

            val writer = FileWriter(str, true)

            val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

            csvPrinter.printRecord(touchData.viewID, touchData.viewType, touchData.viewLocal, touchData.rawX, touchData.rawY, touchData.timestamp)

            csvPrinter.flush()
            csvPrinter.close()

            println("Write click data in CSV successfully!")

        } catch (e: Exception) {

            println("Writing click data in CSV error!")
            e.printStackTrace()

        }


    }



}