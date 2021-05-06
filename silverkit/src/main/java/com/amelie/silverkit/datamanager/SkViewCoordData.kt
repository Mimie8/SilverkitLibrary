package com.amelie.silverkit.datamanager

class SkViewCoordData {

    var viewID : String? = null
    var viewLocal : String? = null
    var coordTL: IntArray? = null
    var coordDR: IntArray? = null

    constructor()
    constructor(viewID: String, viewLocal: String, coordTL: IntArray, coordDR: IntArray){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.coordTL = coordTL
        this.coordDR = coordDR
    }

    override fun toString(): String {
        return "SkViewCoordData(viewID=$viewID, viewLocal=$viewLocal, coordTL=$coordTL, coordDR=$coordDR)"
    }
}