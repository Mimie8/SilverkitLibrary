package com.amelie.silverkit.widgets

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ActionMenuView
import androidx.annotation.RequiresApi
import com.amelie.silverkit.SkTools

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SkActionMenuView : ActionMenuView, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT ActionMenuView : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.ACTIONMENUVIEW
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}