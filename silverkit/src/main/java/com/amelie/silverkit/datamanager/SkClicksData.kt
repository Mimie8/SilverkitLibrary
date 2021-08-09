package com.amelie.silverkit.datamanager

import com.amelie.silverkit.SkTools
import java.sql.Timestamp

class SkClicksData {

    var viewID : String? = null
    var viewType : SkTools.ViewType? = null
    var viewLocal : String? = null
    var rawX: Int = 0
    var rawY: Int = 0
    var timestamp : Timestamp? = null

    constructor()
    constructor(viewID: String, viewType: SkTools.ViewType, viewLocal: String, rawX: Int, rawY: Int, timestamp: Timestamp){
        this.viewID = viewID
        this.viewType = viewType
        this.viewLocal = viewLocal
        this.rawX = rawX
        this.rawY = rawY
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return "SkClicksData(viewID=$viewID, viewType=$viewType, viewLocal=$viewLocal, rawX=$rawX, rawY=$rawY, timestamp=$timestamp)"
    }

}