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

        Log.d("info", "SILVERKIT (OnTouch)" + view.toString() + ": x = $x | y = $y")

    }

}