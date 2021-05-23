package com.amelie.silverkit.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import com.amelie.silverkit.SkTools

class SkMultiAutoCompleteTextView : AppCompatMultiAutoCompleteTextView, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT MultiAutoCompleteTextView : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.MULTIAUTOCOMPLETETEXTVIEW
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}