package com.amelie.silverkit.datamanager

import com.amelie.silverkit.SkTools
import java.sql.Timestamp

class SkOnTouchData {

    var viewType : SkTools.ViewType? = null
    var viewLocal : String? = null
    var pressure: Float = 0f
    var rawX: Int = 0
    var rawY: Int = 0
    var timestamp : Timestamp? = null

    constructor()
    constructor(viewType: SkTools.ViewType, viewLocal: String, pressure: Float, rawX: Int, rawY: Int, timestamp: Timestamp){
        this.viewType = viewType
        this.viewLocal = viewLocal
        this.pressure = pressure
        this.rawX = rawX
        this.rawY = rawY
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return "SkOnTouchData(viewType=$viewType, viewLocal=$viewLocal, pressure=$pressure, rawX=$rawX, rawY=$rawY, timestamp=$timestamp)"
    }

}