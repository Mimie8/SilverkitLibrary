package com.amelie.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.amelie.silverkit.datamanager.SkOnTouchData
import com.amelie.silverkit.widgets.SkFloatingActionButton
import com.amelie.silverkit.widgets.SkRecyclerView
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.sql.Timestamp

private val CSV_HEADER = "view type, view activity, pressure, x, y, timestamp"

interface SkTools {

    enum class ViewType {
        NONE, FAB, RV
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

            saveOnTouchData(view, touchData)
        }

    }

    private fun getViewType(view: View) : ViewType{

        when (view) {
            is SkFloatingActionButton -> return ViewType.FAB
            is SkRecyclerView -> return ViewType.RV
            else -> {
                Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
                return ViewType.NONE
            }
        }

    }

    private fun getViewLocal(view: View) : String {
        return view.context.toString()
    }

    private fun saveOnTouchData(view: View, touchData: SkOnTouchData){

        var fileWriter: FileWriter? = null

        try {

            //val path = view.context.filesDir.canonicalFile
            //val file = File("$path/FileOnTouchData.csv")
            val file = File("FileOnTouchData.csv")
            fileWriter = FileWriter(file, true)

            //If the file doesn't exist set the column
            if(!file.exists()){
                fileWriter.append(CSV_HEADER)
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

            Log.d("info", "SILVERKIT TOOL ONTOUCH : Write CSV successfully!)")

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



}