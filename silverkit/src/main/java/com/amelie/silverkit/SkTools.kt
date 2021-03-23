package com.amelie.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.amelie.silverkit.widgets.SkFloatingActionButton
import com.amelie.silverkit.widgets.SkRecyclerView
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface SkTools {

    enum class ViewType {
        FAB, RV, NONE
    }

    fun toolOnTouch(view: View, event: MotionEvent) {

        val pressure = event.getPressure()
        val rawX = event.getRawX()
        val rawY = event.getRawY()
        val timestamp = Timestamp(System.currentTimeMillis())


        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //PRESS ON THE VIEW
            val viewType = getViewType(view)
            Log.d("info", "SILVERKIT TOOL ONTOUCH : VIEW = $viewType | X = $rawX | Y = $rawY | PRESSURE = $pressure | TIMESTAMP : $timestamp")
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

}