package com.amelie.silverkit.datamanager

import com.amelie.silverkit.SkTools
import java.sql.Timestamp

class SkAnalysisData {

    var viewID : String = ""
    var viewLocal : String = ""
    var errorRatio : Float = 0.0f
    var averageDistFromBorder : Float = 0.0f
    var distGravityCenter : Float = 0.0f

    constructor()
    constructor(viewID: String, viewLocal: String, errorRatio: Float, averageDistFromBorder: Float, distGravityCenter: Float){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.errorRatio = errorRatio
        this.averageDistFromBorder = averageDistFromBorder
        this.distGravityCenter = distGravityCenter
    }

    override fun toString(): String {
        return "SkClicksData(viewID=$viewID, viewLocal=$viewLocal, errorRatio=$errorRatio, averageDistFromBorder=$averageDistFromBorder, distGravityCenter=$distGravityCenter)"
    }

}