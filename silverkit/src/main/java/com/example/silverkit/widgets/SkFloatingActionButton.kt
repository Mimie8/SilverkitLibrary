package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.example.silverkit.SkOnTouchEvent
import com.example.silverkit.kotLog
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SkFloatingActionButton : FloatingActionButton, SkOnTouchEvent {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        //Adjust floating button size
        kotLog.debug{ "SILVERKIT : test" }
        Log.d("info", "SILVERKIT (func Log.d): x = $x | y = $y")
    }

}