package com.example.silverkit

import android.util.Log
import android.view.MotionEvent
import android.view.View

interface SkOnTouchEvent : View.OnTouchListener {

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {

        val values = IntArray(2)
        view?.getLocationOnScreen(values)
        val x = values[0]
        val y = values[1]

        Log.d("info", "SILVERKIT (OnTouch): x = $x | y = $y")

        return true

    }

}