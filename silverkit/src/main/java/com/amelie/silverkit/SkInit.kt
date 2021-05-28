package com.amelie.silverkit

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.amelie.silverkit.datamanager.SkCoordsData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class SkInit {

    fun initActivityLayout(activity: Activity) {
        val prefs: SharedPreferences = activity.baseContext.getSharedPreferences("activities", Context.MODE_PRIVATE)
        val firstStart = prefs.getStringSet("FirstStart", HashSet<String>())
        val editor: SharedPreferences.Editor = prefs.edit()

        val rv: ViewGroup? =
            activity.window.decorView.findViewById(android.R.id.content) as ViewGroup?

        //if root view is not null and if the activity isn't already saved in shared pref
        if (rv != null && !(firstStart?.contains(activity.localClassName))!!) {

            val allChildren : List<View> = getAllChildren(rv)
            Log.d("info", "all children : " + allChildren.toString())

            //Check for every view in the activity if it's a Sk view, if yes save it in the csv
            for (v in allChildren) {

                if (v is SkTools) {

                    Log.d("info", "View is SkTools view")

                    val viewID = getViewID(v)
                    val viewLocal = getViewLocal(v)
                    val coord = getViewCoord(v)
                    val viewData = SkCoordsData(viewID, viewLocal, coord[0], coord[1])

                    //Save view coordinates in CSV file if the view coordinates aren't already saved
                    saveCoordinates(v, viewData)
                }

            }

            //Save new shared pref by adding the activity
            val hash = HashSet<String>(firstStart)
            hash.add(activity.localClassName)
            editor.putStringSet("activities", hash)
            editor.apply()
        }
    }

    private fun getAllChildren(v: View): List<View> {
        if (v !is ViewGroup) {
            val viewArrayList = ArrayList<View>()
            viewArrayList.add(v)
            Log.d("info", "child not vg : " + v.toString())
            return viewArrayList
        }
        val result = ArrayList<View>()
        val viewGroup = v as ViewGroup
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            Log.d("info", "child : " + child.toString())

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

        return getViewType(view).toString() + view.id

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

}