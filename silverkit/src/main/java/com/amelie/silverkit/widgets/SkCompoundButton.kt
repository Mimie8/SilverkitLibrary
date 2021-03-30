package com.amelie.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.CompoundButton
import com.amelie.silverkit.SkTools

class SkCompoundButton : CompoundButton, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        Log.d("info", "SILVERKIT CompoundButton : adjusted size")
    }

    fun getType(): SkTools.ViewType {
        return SkTools.ViewType.COMPOUNDBUTTON
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}