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

    /*
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
    */

    private fun saveClicks(view: View, clickData: SkClicksData){

        val context = view.context

        val db_helper =  DatabaseHelper(context)
        db_helper.addClickEvent(clickData)

    }


    // -------------------------------- TACTIQUES CORRECTIVES

    fun applyCorrections(activity: Activity){

        val context = activity.baseContext
        val dbHelper =  DatabaseHelper(context)

        // Look if it's time to analyse
        if(dbHelper.isAnalysisTime()){

            // Get data from DB
            val clicks = dbHelper.getClicksData()
            val views = dbHelper.getViewsData()
            val deviceData = dbHelper.getDeviceData()

            if(clicks.isNotEmpty() && views.isNotEmpty()){
                // Analyse data from all the views in this activity and save to table in DB
                val skViewsID = getSkViewsID(activity)
                for (viewID in skViewsID){
                    val analysisData = analyseData(viewID, activity.toString(), clicks, views, deviceData)
                    if(analysisData != null){
                        dbHelper.addAnalysisData(analysisData)
                    }
                }
            }

            // Get data from table and see if it's necessary to apply tactics
            // Apply tactics
        }
    }
    
    // -------------------- CORRECTIONS

    private fun analyseData(viewID : String, activity: String, clicksData : MutableList<SkClicksData>, viewsData : MutableList<SkCoordsData>, deviceData : List<Any>) : SkAnalysisData?{

        val viewDelimitations = viewDelimitations(viewID, activity, viewsData)
        val maxDistance = getMaxDistance(deviceData)

        if(viewDelimitations.isNotEmpty() && maxDistance != 0){
            val clicksOnView = clicksOnView(viewDelimitations, clicksData)
            val clicksAroundView = clicksAroundView(viewDelimitations, maxDistance, clicksData)
            val centerOfView = centerOfView(viewDelimitations)

            val errorRatio = getErrorRatio(clicksOnView, clicksAroundView)
            val averageDistFromBorder = getAverageDistanceFromBorder(viewDelimitations, clicksAroundView)
            val distGravityCenter = getDistGravityCenter(clicksOnView, clicksAroundView, centerOfView)

            return SkAnalysisData(viewID, activity, errorRatio, averageDistFromBorder, distGravityCenter)
        }

        return null

    }

    private fun getErrorRatio(clicksOnView : MutableList<SkClicksData>, clicksAroundView : MutableList<SkClicksData>) : Float{
        // Compute error ratio

        val missClicks = clicksAroundView.size
        val totalClicks = clicksOnView.size + missClicks

        return missClicks.toFloat().div(totalClicks.toFloat())
    }

    private fun getAverageDistanceFromBorder(viewDelimitations : List<Int>, clicksAroundView : MutableList<SkClicksData>) : Float{
        // Compute average distance from border

        val pointsDistance = mutableListOf<Float>()

        for (click in clicksAroundView){
            val x = click.rawX
            val y = click.rawY
            val distance = getDistanceFromView(x, y, viewDelimitations)
            pointsDistance.add(distance)
        }

        return pointsDistance.sum() / pointsDistance.size
    }

    private fun getDistanceFromView(x:Int, y:Int, viewDelimitations: List<Int>) : Float{

        val tl_x = viewDelimitations[0].toFloat()
        val tl_y = viewDelimitations[1].toFloat()
        val dr_x = viewDelimitations[2].toFloat()
        val dr_y = viewDelimitations[3].toFloat()

        val view_TL = listOf(tl_x, tl_y)
        val view_DR = listOf(dr_x, dr_y)
        val view_TR = listOf(dr_x, tl_y)
        val view_DL = listOf(tl_x, dr_y)

        var P1 = emptyList<Float>()
        var P2 = emptyList<Float>()

        if (x <= tl_x){
            P1 = view_TL
            P2 = view_DL
        }

        if (dr_x <= x){
            P1 = view_TR
            P2 = view_DR
        }

        if  (y <= tl_y){
            P1 = view_TL
            P2 = view_TR
        }

        if (dr_y <= y){
            P1 = view_DL
            P2 = view_DR
        }

        val x1 = P1[0]
        val y1 = P1[1]
        val x2 = P2[0]
        val y2 = P2[1]

        // distance(x,y,x1,y1,x2,y2) = |(x2 - x1)(y1 - y) - (x1 - x)(y2 - y1)|   /    sqrt( (x2 - x1)² + (y2 - y1)² )
        val num : Float = ( ((x2-x1) * (y1-y)) - ((x1 - x) * (y2 - y1))).absoluteValue

        val den = sqrt( (x2 - x1).pow(2) + (y2 - y1).pow(2) )

        return num / den

    }

    private fun getDistGravityCenter(clicksOnView : MutableList<SkClicksData>, clicksAroundView : MutableList<SkClicksData>, centerOfView : List<Int>) : Float{
        // Compute gravity center
        // Compute distance between gravity center and center of view

        val total_x = mutableListOf<Int>()
        val total_y = mutableListOf<Int>()

        for (click in clicksOnView){
            total_x.add(click.rawX)
            total_y.add(click.rawY)
        }

        for (click in clicksAroundView){
            total_x.add(click.rawX)
            total_y.add(click.rawY)
        }

        val gravityX : Float = total_x.sum().toFloat().div(total_x.size.toFloat())
        val gravityY : Float = total_y.sum().toFloat().div(total_y.size.toFloat())

        return sqrt(((centerOfView[0] - gravityX).pow(2) - (centerOfView[1] - gravityY).pow(2)).absoluteValue)
    }

    private fun viewDelimitations(viewID: String, activity: String, viewsData: MutableList<SkCoordsData>) : List<Int>{

        for (views in viewsData){
            if(viewID == views.viewID && activity == views.viewLocal){
                return listOf(views.coordTL!![0], views.coordTL!![1], views.coordDR!![0], views.coordDR!![1])
            }
        }

        Log.d("info", "viewDelimitations : ERROR WHILE GETTING VIEW DELIMITATION OF $viewID IN ACTIVITY $activity")
        return listOf()

    }

    private fun getMaxDistance(deviceData: List<Any>) : Int {

        val width : Int = deviceData[0] as Int
        val height : Int = deviceData[1] as Int

        if(width < height){
            return (width / 100) * 10
        }
        if (height >= width){
            return (height / 100) * 10
        }
        return 0
    }

    private fun clicksOnView(viewDelimitations : List<Int>, clicksData : MutableList<SkClicksData>) : MutableList<SkClicksData>{

        val clicksOnView = mutableListOf<SkClicksData>()

        val tl_x = viewDelimitations[0]
        val tl_y = viewDelimitations[1]
        val dr_x = viewDelimitations[2]
        val dr_y = viewDelimitations[3]

        for (click in clicksData){

            val x = click.rawX
            val y = click.rawY

            if( tl_x < x && tl_y < y && x < dr_x && y < dr_y){
                clicksOnView.add(click)
            }

        }

        return clicksOnView
    }

    private fun clicksAroundView(viewDelimitations : List<Int>, maxDistance : Int, clicksData : MutableList<SkClicksData>) : MutableList<SkClicksData>{

        val clicksAroundView = mutableListOf<SkClicksData>()

        val tl_x = viewDelimitations[0]
        val tl_y = viewDelimitations[1]
        val dr_x = viewDelimitations[2]
        val dr_y = viewDelimitations[3]

        for (click in clicksData){

            val x = click.rawX
            val y = click.rawY

            if( (tl_x - maxDistance <= x) && (tl_y - maxDistance <= y) and (x <= dr_x + maxDistance) and (y <= dr_y + maxDistance)){
                clicksAroundView.add(click)
            }

        }

        return clicksAroundView

    }

    private fun centerOfView(viewDelimitations : List<Int>) : List<Int>{
        val tl_x = viewDelimitations[0]
        val tl_y = viewDelimitations[1]
        val dr_x = viewDelimitations[2]
        val dr_y = viewDelimitations[3]

        val center_x = tl_x + ((dr_x-tl_x)/2)
        val center_y = tl_y + ((dr_y - tl_y)/2)

        return listOf(center_x, center_y)

    }

    private fun getSkViewsID(activity: Activity) : List<String>{

        val skViewsID : MutableList<String> = mutableListOf()

        //get the root view
        val rv: ViewGroup? = activity.window?.decorView?.findViewById(android.R.id.content) as ViewGroup?

        //get all the views of the root view
        if(rv != null){
            val allChildren : List<View> = getAllChildren(rv)

            //Check for every view in the activity if it's a Silverkit view, if yes add viewID to list
            for (v in allChildren) {

                if (v is SkTools) {

                    val viewID = getViewID(v)
                    skViewsID.add(viewID)
                }
            }

        } else {
            Log.d("info", "SkInit : ERROR WHILE GETTING ROOT VIEW OF ACTIVITY $activity")
        }

        return skViewsID
    }

    fun getAllChildren(v: View): List<View> {
        if (v !is ViewGroup) {
            return ArrayList()
        }

        val result = ArrayList<View>()
        val viewGroup = v as ViewGroup
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            //Add parents then detect its child elements
            result.add(child)
            result.addAll(getAllChildren(child))
        }
        return result
    }



}