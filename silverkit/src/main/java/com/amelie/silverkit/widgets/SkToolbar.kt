package com.amelie.silverkit.widgets

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import com.amelie.silverkit.SkTools

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SkToolbar : Toolbar, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        Log.d("info", "SILVERKIT TOOLBAR : adjusted size")
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}