package com.example.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View

interface SkTools {

    fun toolOnTouch(view: View, event: MotionEvent) {

        val values = IntArray(2)
        view.getLocationOnScreen(values)
        val x = values[0]
        val y = values[1]
        
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            Log.d("info", "SILVERKIT (OnTouch): x = $x | y = $y")
        }

    }

}