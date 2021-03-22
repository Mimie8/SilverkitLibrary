package com.example.silverkit

import android.util.Log
import android.view.View

interface SkOnClick {

    fun countClick(view: View) {

        Log.d("info", "SILVERKIT (counted click)")

    }

}