package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.silverkit.SkOnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SkFloatingActionButton : FloatingActionButton, View.OnClickListener, SkOnClick {

    constructor(context : Context): super(context) { init() }
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet) { init() }
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) { init() }

    fun adjustSize(){
        //Adjust floating button size
        Log.d("info", "SILVERKIT (adjustSize)")
    }

    private fun init(){
        Log.d("info", "SILVERKIT (init)")
        setOnClickListener(this)
    }

    override fun onClick(v: View) {
        Log.d("info", "SILVERKIT (onClick)")
        countClick(v)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        Log.d("info", "SILVERKIT (onTouch)")
        return super.onTouchEvent(ev)
    }



}