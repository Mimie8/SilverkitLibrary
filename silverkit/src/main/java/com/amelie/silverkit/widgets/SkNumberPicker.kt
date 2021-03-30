package com.amelie.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.NumberPicker
import com.amelie.silverkit.SkTools

class SkNumberPicker : NumberPicker, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT NumberPicker : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.NUMBERPICKER
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}