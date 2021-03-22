package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.example.silverkit.SkOnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SkFloatingActionButton : FloatingActionButton, SkOnClick {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        //Adjust floating button size
        Log.d("info", "SILVERKIT (adjustSize)")
    }

    override fun setOnClickListener(l: OnClickListener?) {
        Log.d("info", "SILVERKIT (onClickListener)")
        skOnClick(this)
        super.setOnClickListener(l)
    }

}