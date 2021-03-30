package com.amelie.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ViewAnimator
import com.amelie.silverkit.SkTools

class SkViewAnimator : ViewAnimator, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT ViewAnimator : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.VIEWANIMATOR
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}