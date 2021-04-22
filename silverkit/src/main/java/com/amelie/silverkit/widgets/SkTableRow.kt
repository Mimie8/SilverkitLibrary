package com.amelie.silverkit.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TableRow
import com.amelie.silverkit.SkTools

class SkTableRow : TableRow, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)

    fun adjustSize(){
        Log.d("info", "SILVERKIT TableRow : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.TABLEROW
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}