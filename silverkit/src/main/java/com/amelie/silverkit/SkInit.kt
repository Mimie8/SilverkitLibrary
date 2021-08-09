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
import com.amelie.silverkit.datamanager.SkCoordsData
import com.amelie.silverkit.datamanager.SkHardwareData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter

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

    private fun getAllChildren(v: View): List<View> {
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