package com.amelie.silverkit

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.amelie.silverkit.datamanager.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.sql.Timestamp
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


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
            val viewType = getViewType(view).toString()
            val viewLocal = getViewLocal(view)
            val rawX = event.rawX.toInt()
            val rawY = event.rawY.toInt()
            val timestamp = Timestamp(System.currentTimeMillis()).toString()

            val clickData = SkClicksData(viewID, viewType, viewLocal, rawX, rawY, timestamp)

            //Save click data in CSV file
            saveClicks(view, clickData)

            Log.d("info", "SILVERKIT TOOL ONTOUCH : ID = $viewID | VIEW = $viewType |LOCAL = $viewLocal | X = $rawX | Y = $rawY | TIMESTAMP : $timestamp")
        }

    }

    fun getType() : ViewType

    private fun getViewID(view: View): String {

        // view doesn't have an id
        Log.d("info", "VIEW ID : ${view.id}")
        if (view.id == View.NO_ID) {
            view.id = View.generateViewId()
            Log.d("info", "RETURNED VIEW ID : ${getViewType(view).toString() + "-" + view.id}")
            return getViewType(view).toString() + "-" + view.id
        }
        Log.d("info", "RETURNED VIEW ID : ${view.id}")
        return view.id.toString()

    }

    private fun getViewType(view: View) : ViewType{

        return if(view is SkTools){
            view.getType()
        } else {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
            ViewType.NONE
        }

    }

    fun getViewLocal(view: View) : String {
        return view.context.javaClass.simpleName
    }

    private fun saveClicks(view: View, clickData: SkClicksData){

        val context = view.context

        val db_helper =  DatabaseHelper(context)
        db_helper.addClickEvent(clickData)

    }

}