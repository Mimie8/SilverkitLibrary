package com.example.silverkit

import android.util.Log
import android.view.View

interface SkOnClick : View.OnClickListener {

    override fun onClick(view: View?) {

        val values = IntArray(2)
        view?.getLocationOnScreen(values)
        val x = values[0]
        val y = values[1]

        Log.d("info", "SILVERKIT (OnClick): x = $x | y = $y")

        onClick(view)
    }

}