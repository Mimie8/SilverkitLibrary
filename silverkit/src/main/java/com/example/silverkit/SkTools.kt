package com.example.silverkit

import android.view.MotionEvent
import android.view.View
import mu.KotlinLogging

val kotLog = KotlinLogging.logger{}

class SkTools {

    fun initTools(){
        SktGetClicks()
    }

    class SktGetClicks : View.OnTouchListener{

        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            val x = event?.getX()
            val y = event?.getY()
            kotLog.debug{ "SILVERKIT : x = $x | y = $y" }

            return event!=null
        }


    }

}