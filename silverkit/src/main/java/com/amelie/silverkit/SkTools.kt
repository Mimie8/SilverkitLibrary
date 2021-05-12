package com.amelie.silverkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
import java.util.*


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

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //PRESS ON THE VIEW

            val viewID = getViewID(view)
            val viewType = getViewType(view)
            val viewLocal = getViewLocal(view)
            val rawX = event.getRawX().toInt()
            val rawY = event.getRawY().toInt()
            val timestamp = Timestamp(System.currentTimeMillis())
            val coord = getViewCoord(view)
            val coord_lt = coord[0].toString()
            val coord_dr = coord[1].toString()

            val touchData = SkClicksData(viewID, viewType, viewLocal, rawX, rawY, timestamp)
            val viewData = SkCoordsData(viewID, viewLocal, coord[0], coord[1])

            //Save touch data in CSV file
            saveClicks(view, touchData)

            //Save view coordinates in CSV file
            saveCoordinates(view, viewData)

            //save hardware info if necessary (only once but hey idk how to call it only one)
            saveHardwareData(view)

            Log.d("info", "SILVERKIT TOOL ONTOUCH : ID = $viewID | VIEW = $viewType | LOCAL = $viewLocal | COORD_TL = $coord_lt | COORD_DR = $coord_dr | X = $rawX | Y = $rawY | TIMESTAMP : $timestamp")
        }

    }

    fun getType() : ViewType

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

        return getViewType(view).toString() + view.id

    }

    /*
    private fun isResourceIdInPackage(context: Context, packageName: String?, resId: Int): Boolean {
        if (packageName == null || resId == 0) {
            return false
        }
        var res: Resources? = null
        if (packageName == context.packageName) {
            res = context.resources
        } else {
            try {
                res = context.packageManager.getResourcesForApplication(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.d("info", packageName + "does not contain " + resId + " ... " + e.message)
            }
        }
        return res?.let { isResourceIdInResources(it, resId) } ?: false
    }

    private fun isResourceIdInResources(res: Resources, resId: Int): Boolean {
        return try {
            res.getResourceName(resId)
            //Didn't catch so id is in res
            true
        } catch (e: NotFoundException) {
            false
        }
    }
    */


    private fun getViewType(view: View) : ViewType{

        if(view is SkTools){
            return view.getType()
        } else {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
            return ViewType.NONE
        }

    }

    private fun getViewLocal(view: View) : String {
        return view.context.javaClass.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveClicks(view: View, touchData: SkClicksData){

        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/ClicksData.csv"

        try {

            val writer = FileWriter(str, true)

            var csvPrinter:CSVPrinter? = null
            csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

            csvPrinter.printRecord(touchData.viewID, touchData.viewType, touchData.viewLocal, touchData.rawX, touchData.rawY, touchData.timestamp)

            csvPrinter.flush()
            csvPrinter.close()

            println("Write click data in CSV successfully!")

        } catch (e: Exception) {

            println("Writing click data in CSV error!")
            e.printStackTrace()

        }


    }

    private fun saveCoordinates(view:View, viewData: SkCoordsData){

        //Create CSV if it doesn't exist
        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/CoordinatesData.csv"
        FileWriter(str, true)

        //Read CSV
        val data:MutableList<List<String>> = readCSVCoordsData(str)

        //If the coords aren't saved, saved them
        if(!data.contains(listOf(viewData.viewID, viewData.viewLocal))){

            try {

                val writer = FileWriter(str, true)

                var csvPrinter:CSVPrinter? = null
                csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

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

                    val id = tokens[0]
                    val activity = tokens[1]
                    data.add(listOf(id,activity))

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

    private fun saveHardwareData(view:View){

        var fileReader: BufferedReader? = null

        //Create CSV if it doesn't exist
        val path = view.context.getExternalFilesDir(null)?.absolutePath
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

                    var csvPrinter:CSVPrinter? = null
                    csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

                    val hardwareData = getHardwareData(view)

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

    private fun getHardwareData(view:View):SkHardwareData{

        val hardwareData = SkHardwareData()

        //Get Screen width and height
        val screenDimensions = getScreenSizeIncludingTopBottomBar(view.context)
        hardwareData.screenWidth = screenDimensions[0]
        hardwareData.screenHeight = screenDimensions[1]

        return hardwareData

    }

    @SuppressLint("ServiceCast")
    fun getScreenSizeIncludingTopBottomBar(context: Context): IntArray {
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