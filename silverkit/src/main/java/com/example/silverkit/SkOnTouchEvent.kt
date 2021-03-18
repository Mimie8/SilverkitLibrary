package com.example.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View

interface SkOnTouchEvent : View.OnTouchListener {

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        val x = event?.getX()
        val y = event?.getY()
        Log.d("info", "SILVERKIT (Log.d): x = $x | y = $y")

        return onTouch(view, event)
    }

}