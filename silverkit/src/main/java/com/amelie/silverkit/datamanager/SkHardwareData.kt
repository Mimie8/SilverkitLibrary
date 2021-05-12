package com.amelie.silverkit.datamanager

import com.amelie.silverkit.SkTools
import java.sql.Timestamp

class SkHardwareData {
    var screenWidth: Int = 0
    var screenHeight: Int = 0

    constructor()
    constructor(screenWidth:Int, screenHeight:Int){
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }

    override fun toString(): String {
        return "SkHardwareData(screenWidth=$screenWidth, screenHeight=$screenHeight)"
    }

}