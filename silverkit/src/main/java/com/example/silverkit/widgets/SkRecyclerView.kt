package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.example.silverkit.SkTools

class SkRecyclerView : RecyclerView, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        Log.d("info", "SILVERKIT (recyclerview): adjusted size")
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}