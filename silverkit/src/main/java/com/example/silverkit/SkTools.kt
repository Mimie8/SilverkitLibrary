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

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //PRESS ON THE VIEW
            Log.d("info", "SILVERKIT ONTOUCH (SkTools): x = $x | y = $y")


            when (view) {
                is SkFloatingActionButton -> view.adjustSize()
                is SkRecyclerView -> view.adjustSize()
                else -> {
                    Log.d("info", "SILVERKIT ONTOUCH (SkTools): not a sk view)")
                }
            }


        }

    }

}