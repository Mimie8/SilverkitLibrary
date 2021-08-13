package com.amelie.silverkit.datamanager

class SkTacticsData {

    var viewID : String? = null
    var viewLocal : String? = null
    var color: Int? = null
    var paddingStart : Int = 0
    var paddingEnd : Int = 0
    var paddingTop : Int = 0
    var paddingBottom : Int = 0
    var oldPaddingStart : Int = 0
    var oldPaddingEnd : Int = 0
    var oldPaddingTop : Int = 0
    var oldPaddingBottom : Int = 0

    constructor()
    constructor(viewID: String, viewLocal: String, color: Int?, paddingStart: Int, paddingEnd: Int, paddingTop : Int, paddingBottom : Int, oldPaddingStart: Int, oldPaddingEnd: Int, oldPaddingTop : Int, oldPaddingBottom : Int){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.color = color
        this.paddingStart = paddingStart
        this.paddingEnd = paddingEnd
        this.paddingTop = paddingTop
        this.paddingBottom = paddingBottom
        this.oldPaddingStart = oldPaddingStart
        this.oldPaddingEnd = oldPaddingEnd
        this.oldPaddingTop = oldPaddingTop
        this.oldPaddingBottom = oldPaddingBottom
    }

    override fun toString(): String {
        return "SkClicksData(viewID=$viewID, viewLocal=$viewLocal, color=$color, paddingStart=$paddingStart, paddingEnd=$paddingEnd, paddingTop=$paddingTop, paddingBottom = $paddingBottom, oldPaddingStart=$oldPaddingStart, oldPaddingEnd=$oldPaddingEnd, oldPaddingTop=$oldPaddingTop, oldPaddingBottom=$oldPaddingBottom)"
    }

}