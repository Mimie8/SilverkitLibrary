package com.amelie.silverkit

import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.amelie.silverkit.datamanager.SkOnTouchData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.sql.Timestamp

interface SkTools {

    enum class ViewType {
        NONE, ABSSEEKBAR, ABSSPINNER, ACTIONMENUVIEW, AUTOCOMPLETETEXTVIEW, BUTTON,
        CALENDARVIEW, CHECKBOX, CHECKEDTEXTVIEW, CHRONOMETER, COMPOUNDBUTTON, DATEPICKER, EDITTEXT,
        EXPANDABLELISTVIEW, FAB, GRIDLAYOUT, GRIDVIEW, HORIZONTALSCROLLVIEW, IMAGEBUTTON, IMAGESWITCHER,
        IMAGEVIEW, LINEARLAYOUT, LISTVIEW, MEDIACONTROLLER, MULTIAUTOCOMPLETETEXTVIEW, NUMBERPICKER,
        PROGRESSBAR, QUICKCONTACTBADGE, RADIOBUTTON, RADIOGROUP, RATINGBAR, RECYCLERVIEW, RELATIVELAYOUT, SCROLLVIEW,
        SEARCHVIEW, SEEKBAR, SPINNER, STACKVIEW, SWITCH, TABLELAYOUT, TABLEROW, TABWIDGET, TEXTCLOCK,
        TEXTSWITCHER, TEXTVIEW, TIMEPICKER, TOGGLEBUTTON, VIDEOVIEW, VIEWANIMATOR, VIEWFLIPPER,
        VIEWSWITCHER
    }

    fun toolOnTouch(view: View, event: MotionEvent) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //PRESS ON THE VIEW

            val viewType = getViewType(view)
            val viewLocal = getViewLocal(view)
            val rawX = event.getRawX().toInt()
            val rawY = event.getRawY().toInt()
            val timestamp = Timestamp(System.currentTimeMillis())

            val touchData = SkOnTouchData(viewType, viewLocal, rawX, rawY, timestamp)

            Log.d("info", "SILVERKIT TOOL ONTOUCH : VIEW = $viewType | X = $rawX | Y = $rawY | TIMESTAMP : $timestamp")

            saveData(view, touchData)
        }

    }

    private fun getViewType(view: View) : ViewType{

        if(view is SkTools){
            return view.getType()
        } else {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
            return ViewType.NONE
        }

    }

    fun getType() : ViewType

    private fun getViewLocal(view: View) : String {
        return view.context.javaClass.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData(view: View, touchData: SkOnTouchData){

        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/FileOnTouchData.csv"
        val file = File(str)

        try {

            val writer = FileWriter(str, true)

            var csvPrinter:CSVPrinter? = null
            csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

            csvPrinter.printRecord(touchData.viewType, touchData.viewLocal, touchData.rawX, touchData.rawY, touchData.timestamp)

            csvPrinter.flush()
            csvPrinter.close()

            println("Write CSV successfully!")

        } catch (e: Exception) {

            println("Writing CSV error!")
            e.printStackTrace()

        }


    }

}