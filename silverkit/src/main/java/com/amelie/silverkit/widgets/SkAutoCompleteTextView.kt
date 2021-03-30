package com.amelie.silverkit.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.AutoCompleteTextView
import com.amelie.silverkit.SkTools

@SuppressLint("AppCompatCustomView")
class SkAutoCompleteTextView : AutoCompleteTextView, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        Log.d("info", "SILVERKIT AutoCompleteTextView : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.AUTOCOMPLETETEXTVIEW
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}