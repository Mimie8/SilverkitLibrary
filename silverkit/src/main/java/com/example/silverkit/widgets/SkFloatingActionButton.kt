package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.example.silverkit.SkOnTouch
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SkFloatingActionButton : FloatingActionButton, SkOnTouch {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        //Adjust floating button size
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        Log.d("info", "SILVERKIT (onTouch)")
        skOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }



}