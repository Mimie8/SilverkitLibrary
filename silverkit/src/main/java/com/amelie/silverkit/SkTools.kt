package com.amelie.silverkit

import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.amelie.silverkit.datamanager.SkOnTouchData
import com.amelie.silverkit.datamanager.SkViewCoordData
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

            val viewID = getViewID(view)
            val viewType = getViewType(view)
            val viewLocal = getViewLocal(view)
            val rawX = event.getRawX().toInt()
            val rawY = event.getRawY().toInt()
            val timestamp = Timestamp(System.currentTimeMillis())
            val coord = getViewCoord(view)
            val coord_lt = coord[0].toString()
            val coord_dr = coord[1].toString()

            val touchData = SkOnTouchData(viewID, viewType, viewLocal, rawX, rawY, timestamp)
            val viewData = SkViewCoordData(viewID, viewLocal, coord[0], coord[1])

            //Save touch data in CSV file
            saveData(view, touchData)

            //Save view coordinates in CSV file
            saveCoordinates(view, viewData)

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
        if (view.getId() == View.NO_ID) {

            // generate an id
            var id: Int = (0..1000).random()
            var name: String = view.context.resources.getResourceEntryName(id)

            //if the id is already used, generate a new one
            while(checkIDExist(name, view.context.getPackageName())){
                // try to generate a new id
                id = (0..1000).random()
                name = view.context.resources.getResourceEntryName(id)
            }

            //assign the id
            view.setId(id)

        }

        return view.context.resources.getResourceEntryName(view.id)

    }

    private fun checkIDExist(name: String, packageName: String): Boolean{

        if (name == null || !name.startsWith(packageName)) {
            // id is not an id used by a layout element.
            return false
        }
        return true

    }

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
    private fun saveData(view: View, touchData: SkOnTouchData){

        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/FileOnTouchData.csv"

        try {

            val writer = FileWriter(str, true)

            var csvPrinter:CSVPrinter? = null
            csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

            csvPrinter.printRecord(touchData.viewID, touchData.viewType, touchData.viewLocal, touchData.rawX, touchData.rawY, touchData.timestamp)

            csvPrinter.flush()
            csvPrinter.close()

            println("Write touch data in CSV successfully!")

        } catch (e: Exception) {

            println("Writing touch data in CSV error!")
            e.printStackTrace()

        }


    }

    private fun saveCoordinates(view:View, viewData:SkViewCoordData){

        //Create CSV if it doesn't exist
        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/FileCoordinatesData.csv"
        FileWriter(str, true)

        //Read CSV
        val data:MutableList<List<String>> = readCSVCoordsData(str)

        Log.d("info", "data: $data")
        Log.d("info", "view data: ${viewData.viewID} | ${viewData.viewLocal} ")

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

        var data:MutableList<List<String>> = mutableListOf()

        var fileReader: BufferedReader? = null

        try {

            var line: String?

            fileReader = BufferedReader(FileReader(path))

            // Read CSV header
            fileReader.readLine()

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
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


}