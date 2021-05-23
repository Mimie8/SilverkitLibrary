package com.amelie.silverkit.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextSwitcher
import com.amelie.silverkit.SkTools

class SkTextSwitcher : TextSwitcher, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT TextSwitcher : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.TEXTSWITCHER
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}