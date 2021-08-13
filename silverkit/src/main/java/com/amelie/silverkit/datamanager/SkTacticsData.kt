package com.amelie.silverkit.datamanager

class SkTacticsData {

    var viewID : String? = null
    var viewLocal : String? = null
    var color: Int? = null
    var paddingStart : Int? = null
    var paddingEnd : Int? = null
    var paddingTop : Int? = null
    var paddingBottom : Int? = null

    constructor()
    constructor(viewID: String, viewLocal: String, color: Int?, paddingStart: Int, paddingEnd: Int, paddingTop : Int, paddingBottom : Int){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.color = color
        this.paddingStart = paddingStart
        this.paddingEnd = paddingEnd
        this.paddingTop = paddingTop
        this.paddingBottom = paddingBottom
    }

    override fun toString(): String {
        return "SkClicksData(viewID=$viewID, viewLocal=$viewLocal, color=$color, paddingStart=$paddingStart, paddingEnd=$paddingEnd, paddingTop=$paddingTop, paddingBottom = $paddingBottom)"
    }

}