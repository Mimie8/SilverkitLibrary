package com.amelie.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageSwitcher
import com.amelie.silverkit.SkTools

class SkImageSwitcher : ImageSwitcher, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT ImageSwitcher : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.IMAGESWITCHER
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}