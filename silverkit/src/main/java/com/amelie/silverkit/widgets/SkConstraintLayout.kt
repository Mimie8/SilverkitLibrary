package com.amelie.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.AbsSeekBar
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.amelie.silverkit.SkTools

class SkConstraintLayout : ConstraintLayout, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        Log.d("info", "SILVERKIT ConstraintLayout : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.CONSTRAINTLAYOUT
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }



}