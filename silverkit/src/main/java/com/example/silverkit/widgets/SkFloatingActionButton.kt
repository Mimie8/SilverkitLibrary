package com.example.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.example.silverkit.SkOnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SkFloatingActionButton : FloatingActionButton, View.OnClickListener, SkOnClick {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        //Adjust floating button size
    }

    override fun onClick(view: View) {
        skOnClick(view)
    }

}