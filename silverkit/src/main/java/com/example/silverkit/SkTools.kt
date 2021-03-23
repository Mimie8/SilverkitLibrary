package com.example.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.silverkit.widgets.SkFloatingActionButton
import com.example.silverkit.widgets.SkRecyclerView

interface SkTools {

    fun toolOnTouch(view: View, event: MotionEvent) {

        val values = IntArray(2)
        view.getLocationOnScreen(values)
        val x = values[0]
        val y = values[1]

        val screenX = event.getX()
        val screenY = event.getY()

        val rawX = event.getRawX()
        val rawY = event.getRawY()

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //PRESS ON THE VIEW
            Log.d("info", "SILVERKIT TOOL ONTOUCH : x = $x | y = $y")
            Log.d("info", "SILVERKIT TOOL ONTOUCH : getX = $screenX | getY = $screenY")
            Log.d("info", "SILVERKIT TOOL ONTOUCH : rawX = $rawX | rawY = $rawY")

            /*
            when (view) {
                is SkFloatingActionButton -> view.adjustSize()
                is SkRecyclerView -> view.adjustSize()
                else -> {
                    Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
                }
            }
            */


        }

    }

}