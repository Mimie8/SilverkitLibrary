package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import com.example.silverkit.SkOnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SkFloatingActionButton : FloatingActionButton, SkOnClick {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        //Adjust floating button size
    }

}