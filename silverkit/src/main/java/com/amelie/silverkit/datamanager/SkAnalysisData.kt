package com.amelie.silverkit.datamanager

import com.amelie.silverkit.SkTools
import java.sql.Timestamp

class SkAnalysisData {

    var viewID : String = ""
    var viewLocal : String = ""
    var errorRatio : Float = 0.0f
    var averageDistFromBorder : Float = 0.0f
    var distGravityCenter : Float = 0.0f
    var gravityCenterX : Int = 0
    var gravityCenterY : Int = 0

    constructor()
    constructor(viewID: String, viewLocal: String, errorRatio: Float, averageDistFromBorder: Float, distGravityCenter: Float, gravityCenterX : Int, gravityCenterY : Int){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.errorRatio = errorRatio
        this.averageDistFromBorder = averageDistFromBorder
        this.distGravityCenter = distGravityCenter
        this.gravityCenterX = gravityCenterX
        this.gravityCenterY = gravityCenterY
    }

    override fun toString(): String {
        return "SkAnalysisData(viewID=$viewID, viewLocal=$viewLocal, errorRatio=$errorRatio, averageDistFromBorder=$averageDistFromBorder, distGravityCenter=$distGravityCenter, gravityCenterX=$gravityCenterX, gravityCenterY=$gravityCenterY)"
    }

}