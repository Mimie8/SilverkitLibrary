package com.amelie.silverkit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Point
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.amelie.silverkit.datamanager.SkAnalysisData
import com.amelie.silverkit.datamanager.SkClicksData
import com.amelie.silverkit.datamanager.SkCoordsData
import com.amelie.silverkit.datamanager.SkHardwareData
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.round

class SkInit {

    fun init(activity: Activity){

        initViewsCoordinates(activity)

        val prefs: SharedPreferences? = activity.baseContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstStart = prefs?.getBoolean("firstStart", true)
        val editor: SharedPreferences.Editor? = prefs?.edit()

        Log.d("info", "SharedPreferences firstStart : $firstStart ")

        if(firstStart==true){
            initHardwareInfo(activity)

            editor?.putBoolean("firstStart", false)
            editor?.apply()
        }
    }


    fun applyCorrections(activity: Activity){

        val context = activity.baseContext
        val dbHelper =  DatabaseHelper(context)

        // Look if it's time to analyse
        if(dbHelper.isAnalysisTime()){

            // Analyse data and save to bd
            analyseData(dbHelper, activity)

            // Apply tactics if necessary
            applyTactics(dbHelper, activity)
        }
    }

    // -------------------- TACTICS
    private fun applyTactics(dbHelper : DatabaseHelper, activity: Activity){

        // Get data from table and see if it's necessary to apply tactics, if yes apply tactics
        val analysisDataOfActivity = dbHelper.getAnalysisData(activity.localClassName)
        Log.d("info", "Analysis Data : $analysisDataOfActivity ")

        //applyColorContrastTactic(analysisDataOfActivity)
        //applySizeTactic(analysisDataOfActivity)
        //applyGravityCenterTactic(analysisDataOfActivity)
    }


    // -------------------- ANALYSE DATA
    private fun analyseData(dbHelper : DatabaseHelper, activity: Activity){

        // Get data from DB
        val clicks = dbHelper.getClicksData()
        val views = dbHelper.getViewsData()
        val deviceData = dbHelper.getDeviceData()

        if(clicks.isNotEmpty() && views.isNotEmpty()){

            // Analyse data from all the views in this activity and save to table in DB
            val skViewsID = getSkViewsID(activity)
            for (viewID in skViewsID){
                val analysisData = analyseViewData(viewID, activity.localClassName, clicks, views, deviceData)
                if(analysisData != null){
                    dbHelper.addAnalysisData(analysisData)
                }
            }


        }
    }

    private fun analyseViewData(viewID : String, activity: String, clicksData : MutableList<SkClicksData>, viewsData : MutableList<SkCoordsData>, deviceData : List<Any>) : SkAnalysisData?{

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

        if(pointsDistance.isEmpty()){
            pointsDistance.add(0f)
        }

        val result : Float = pointsDistance.sum() / pointsDistance.size.toFloat()

        Log.d("info", "getAverageDistanceFromBorder : $result ")
        return result

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
        val num : Float = ( ((x2-x1) * (y1-y.toFloat())) - ((x1 - x.toFloat()) * (y2 - y1))).absoluteValue

        val den : Float = sqrt( (x2 - x1).pow(2) + (y2 - y1).pow(2) )

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

        val result = sqrt(((centerOfView[0] - gravityX).pow(2) - (centerOfView[1] - gravityY).pow(2)).absoluteValue)

        Log.d("info", "getDistGravityCenter : $result ")
        return result
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

            val inDistance = (tl_x - maxDistance <= x) && (tl_y - maxDistance <= y) and (x <= dr_x + maxDistance) and (y <= dr_y + maxDistance)
            val inView = (tl_x < x) && (tl_y < y) && (x < dr_x) && (y < dr_y)

            if( inDistance && !inView){
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


    // -------------------- INIT


    private fun initHardwareInfo(activity: Activity){
        saveHardwareData(activity)
    }

    private fun initViewsCoordinates(activity: Activity) {

        val prefs: SharedPreferences? = activity.baseContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val listActivities = prefs?.getStringSet("listActivities", HashSet<String>())
        val editor: SharedPreferences.Editor? = prefs?.edit()

        //get the root view
        val rv: ViewGroup? =
            activity.window?.decorView?.findViewById(android.R.id.content) as ViewGroup?

        //if root view is not null and if the activity isn't already saved in shared pref
        if (rv != null && !(listActivities?.contains(activity.localClassName))!!) {

            //get all the views of the root view
            val allChildren : List<View> = getAllChildren(rv)

            //Check for every view in the activity if it's a Silverkit view, if yes save it in the csv
            for (v in allChildren) {

                if (v is SkTools) {

                    val viewID = getViewID(v)
                    val viewLocal = getViewLocal(v)
                    val coord = getViewCoord(v)
                    val viewData = SkCoordsData(viewID, viewLocal, coord[0], coord[1])

                    //Save view coordinates in CSV file if the view coordinates aren't already saved
                    saveCoordinates(v, viewData)
                }

            }

            //Save new shared prefs by adding the activity name
            val hash = HashSet<String>(listActivities)
            hash.add(activity.localClassName)
            editor?.putStringSet("listActivities", hash)
            editor?.apply()

            Log.d("info", "SharedPreferences get specialized views of : ${activity.localClassName} ")
        }
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

    private fun getViewCoord(view: View): List<List<Int>>{

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val tl_x = location[0]
        val tl_y = location[1]
        val coordTL = listOf(tl_x, tl_y)

        val width = view.measuredWidth
        val height = view.measuredHeight
        val dr_x = tl_x + width
        val dr_y = tl_y + height
        val coordDR = listOf(dr_x, dr_y)

        return listOf<List<Int>>(coordTL, coordDR)
    }

    private fun getViewID(view: View): String {

        // view doesn't have an id
        if (view.id == View.NO_ID) {
            view.id = View.generateViewId()
        }

        return getViewType(view).toString() + "-" + view.id

    }

    private fun getViewType(view: View) : SkTools.ViewType {

        if(view is SkTools){
            return view.getType()
        } else {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
            return SkTools.ViewType.NONE
        }

    }

    private fun getViewLocal(view: View) : String {
        return view.context.javaClass.simpleName
    }

    /*
    private fun saveCoordinates(view:View, viewData: SkCoordsData){

        //Create CSV if it doesn't exist
        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/CoordinatesData.csv"
        FileWriter(str, true)

        //Read CSV
        val data:MutableList<List<String>> = readCSVCoordsData(str)

        //If the coords aren't saved, saved them
        val viewToSave = listOf(viewData.viewLocal, viewData.coordTL?.get(0).toString(), viewData.coordTL?.get(1).toString(), viewData.coordDR?.get(0).toString(), viewData.coordDR?.get(1).toString())

        if(!data.contains(viewToSave)){

            try {

                val writer = FileWriter(str, true)

                val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

                val coordTL = viewData.coordTL
                val tl_x = coordTL?.get(0)
                val tl_y = coordTL?.get(1)

                val coordDR = viewData.coordDR
                val dr_x = coordDR?.get(0)
                val dr_y = coordDR?.get(1)

                csvPrinter.printRecord(viewData.viewID, viewData.viewLocal, tl_x, tl_y, dr_x, dr_y)

                csvPrinter.flush()
                csvPrinter.close()

                println("Write coordinates data in CSV successfully!")

            } catch (e: Exception) {

                println("Writing coordinates data in CSV error!")
                e.printStackTrace()

            }

        }


    }
    */

    /*
    private fun readCSVCoordsData(path: String): MutableList<List<String>>{

        val data:MutableList<List<String>> = mutableListOf()

        var fileReader: BufferedReader? = null

        try {

            var line: String?

            fileReader = BufferedReader(FileReader(path))

            // Read CSV header
            line = fileReader.readLine()

            // Read the file line by line starting from the first line
            while (line != null) {
                val tokens = line.split(",")
                if (tokens.isNotEmpty()) {

                    val activity = tokens[1]
                    val coordTLX = tokens[2]
                    val coordTLY = tokens[3]
                    val coordDRX = tokens[4]
                    val coordDRY = tokens[5]
                    data.add(listOf(activity,coordTLX,coordTLY,coordDRX,coordDRY))

                }

                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }

        return data
    }
    */

    private fun saveCoordinates(view: View, viewData: SkCoordsData){

        val context = view.context

        val dbHelper =  DatabaseHelper(context)
        dbHelper.addViewData(viewData)

    }

    /*
    private fun saveHardwareData(activity: Activity){

        var fileReader: BufferedReader? = null

        //Create CSV if it doesn't exist
        val path = activity.baseContext.getExternalFilesDir(null)?.absolutePath
        val str = "$path/HardwareData.csv"
        FileWriter(str, true)

        try {

            var line: String?

            fileReader = BufferedReader(FileReader(str))

            // Read CSV first line
            line = fileReader.readLine()

            // if there's not even a line in the file, write in it the hardware info
            if (line == null) {

                //Data needs to be saved
                try {

                    val writer = FileWriter(str, true)

                    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

                    val hardwareData = getHardwareData(activity)

                    csvPrinter.printRecord(hardwareData.screenWidth, hardwareData.screenHeight)

                    csvPrinter.flush()
                    csvPrinter.close()

                    println("Write hardware data in CSV successfully!")

                } catch (e: Exception) {

                    println("Writing hardware data in CSV error!")
                    e.printStackTrace()

                }

            }

        } catch (e: Exception) {
            println("Reading HardwareData CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing HardwareData CSV fileReader Error!")
                e.printStackTrace()
            }
        }
    }
    */

    private fun saveHardwareData(activity: Activity){

        val hardwareData = getHardwareData(activity)

        val context = activity.baseContext

        val dbHelper =  DatabaseHelper(context)
        val time = Timestamp(System.currentTimeMillis())
        dbHelper.addDeviceData(hardwareData.screenWidth, hardwareData.screenHeight, time.toString())

    }

    private fun getHardwareData(activity: Activity): SkHardwareData {

        val hardwareData = SkHardwareData()

        //Get Screen width and height
        val screenDimensions = getScreenSizeIncludingTopBottomBar(activity.baseContext)
        hardwareData.screenWidth = screenDimensions[0]
        hardwareData.screenHeight = screenDimensions[1]

        return hardwareData

    }

    @SuppressLint("ServiceCast")
    private fun getScreenSizeIncludingTopBottomBar(context: Context): IntArray {
        val screenDimensions = IntArray(2) // width[0], height[1]
        val x: Int
        val y: Int
        val orientation = context.resources.configuration.orientation
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val screenSize = Point()
        display.getRealSize(screenSize)
        x = screenSize.x
        y = screenSize.y
        screenDimensions[0] = if (orientation == Configuration.ORIENTATION_PORTRAIT) x else y // width
        screenDimensions[1] = if (orientation == Configuration.ORIENTATION_PORTRAIT) y else x // height


        Log.d("info","getScreenSizeIncludingTopBottomBar : width ${screenDimensions[0]} height : ${screenDimensions[1]}")


        return screenDimensions
    }

}