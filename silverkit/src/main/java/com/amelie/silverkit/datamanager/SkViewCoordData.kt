package com.amelie.silverkit.datamanager

class SkViewCoordData {

    var viewID : String? = null
    var viewLocal : String? = null
    var coordTL: List<Int>? = null
    var coordDR: List<Int>? = null

    constructor()
    constructor(viewID: String, viewLocal: String, coordTL: List<Int>, coordDR: List<Int>){
        this.viewID = viewID
        this.viewLocal = viewLocal
        this.coordTL = coordTL
        this.coordDR = coordDR
    }

    override fun toString(): String {
        return "SkViewCoordData(viewID=$viewID, viewLocal=$viewLocal, coordTL=$coordTL, coordDR=$coordDR)"
    }
}