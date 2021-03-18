package com.example.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View
import mu.KotlinLogging

val kotLog = KotlinLogging.logger{}

interface SkOnTouchEvent : View.OnTouchListener {

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        val x = event?.getX()
        val y = event?.getY()
        kotLog.debug{ "SILVERKIT : x = $x | y = $y" }
        Log.d("info", "SILVERKIT (Log.d): x = $x | y = $y")

        return onTouch(view, event)
    }


}