package com.amelie.silverkit.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.CheckedTextView
import com.amelie.silverkit.SkTools

@SuppressLint("AppCompatCustomView")
class SkCheckedTextView : CheckedTextView, SkTools {

    constructor(context : Context): super(context)
    constructor(context : Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context : Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    fun adjustSize(){
        Log.d("info", "SILVERKIT CheckedTextView : adjusted size")
    }

    override fun getType(): SkTools.ViewType {
        return SkTools.ViewType.CHECKEDTEXTVIEW
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        toolOnTouch(this, ev)
        return super.onTouchEvent(ev)
    }

}