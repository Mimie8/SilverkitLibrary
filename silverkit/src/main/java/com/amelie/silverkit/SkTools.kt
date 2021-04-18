package com.amelie.silverkit

import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.amelie.silverkit.datamanager.SkOnTouchData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
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
            val pressure = event.getPressure()
            val rawX = event.getRawX()
            val rawY = event.getRawY()
            val timestamp = Timestamp(System.currentTimeMillis())

            val touchData = SkOnTouchData(viewType, viewLocal, pressure, rawX, rawY, timestamp)

            Log.d("info", "SILVERKIT TOOL ONTOUCH : VIEW = $viewType | X = $rawX | Y = $rawY | PRESSURE = $pressure | TIMESTAMP : $timestamp")

            //saveOnTouchData(view, touchData)
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
        return view.context.toString()
    }

    private fun saveOnTouchData(view: View, touchData: SkOnTouchData){

        var fileWriter: FileWriter? = null

        try {

            //val path = view.context.filesDir.canonicalFile
            val path = view.context.getExternalFilesDir(null)?.absolutePath
            val file = File("$path/FileOnTouchData.csv")

            fileWriter = FileWriter(file, true)

            //If the file doesn't exist set the column
            if(!file.exists()){
                //fileWriter.append(CSV_HEADER)
                fileWriter.append('\n')
            }

            //Save the on touch event info
            fileWriter.append(touchData.viewType.toString())
            fileWriter.append(',')
            fileWriter.append(touchData.viewLocal.toString())
            fileWriter.append(',')
            fileWriter.append(touchData.pressure.toString())
            fileWriter.append(',')
            fileWriter.append(touchData.rawX.toString())
            fileWriter.append(',')
            fileWriter.append(touchData.rawY.toString())
            fileWriter.append(',')
            fileWriter.append(touchData.timestamp.toString())
            fileWriter.append('\n')

            Log.d("info", "SILVERKIT TOOL ONTOUCH : Write CSV successfully in \n$path/FileOnTouchData.csv)")

        } catch (e: Exception) {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : Writing CSV error!)")
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
                Log.d("info", "SILVERKIT TOOL ONTOUCH : Flushing/closing error!)")
                e.printStackTrace()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData(view: View, touchData: SkOnTouchData){

        val path = view.context.getExternalFilesDir(null)
        val file = File("$path/FileOnTouchData.csv")

        try {
            
            val fileWriter = Files.newBufferedWriter(file.toPath(), StandardOpenOption.APPEND, StandardOpenOption.CREATE)
            val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("VIEW_TYPE", "VIEW_ACTIVITY", "PRESSURE", "X", "Y", "TIMESTAMP"))

            csvPrinter.printRecord(touchData.viewType, touchData.viewLocal, touchData.pressure, touchData.rawX, touchData.rawY, touchData.timestamp)

            csvPrinter.flush()
            csvPrinter.close()

            println("Write CSV successfully!")

        } catch (e: Exception) {

            println("Writing CSV error!")
            e.printStackTrace()

        }

        /*

        finally {

            try {
                fileWriter!!.flush()
                fileWriter.close()
                csvPrinter!!.close()
            } catch (e: IOException) {
                println("Flushing/closing error!")
                e.printStackTrace()
            }

        }
        */


    }

}