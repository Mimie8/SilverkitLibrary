package com.amelie.silverkit.datamanager

class SkCoordsData {

    var viewID : String? = null
    var viewLocal : String? = null
    var coordTL: List<Int>? = null
    var coordDR: List<Int>? = null
    var baseColor : Int? = null
    var baseSizeWidth : Int? = null
    var baseSizeHeight : Int? = null

    constructor()
    constructor(viewID: String, viewLocal: String, coordTL: List<Int>, coordDR: List<Int>, baseColor : Int?, baseSizeWidth : Int, baseSizeHeight : Int){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.coordTL = coordTL
        this.coordDR = coordDR
        this.baseColor = baseColor
        this.baseSizeWidth = baseSizeWidth
        this.baseSizeHeight = baseSizeHeight
    }

    override fun toString(): String {
        return "SkCoordsData(viewID=$viewID, viewLocal=$viewLocal, coordTL=$coordTL, coordDR=$coordDR, baseColor=$baseColor, baseSizeWidth=$baseSizeWidth, baseSizeHeight=$baseSizeHeight)"
    }
}