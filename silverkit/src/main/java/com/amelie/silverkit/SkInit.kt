package com.amelie.silverkit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.ColorUtils.HSLToColor
import androidx.core.graphics.ColorUtils.colorToHSL
import com.amelie.silverkit.datamanager.*
import java.sql.Timestamp
import kotlin.math.*


class SkInit {

    fun init(activity: Activity){

        initViewsCoordinates(activity)

        val prefs: SharedPreferences? = activity.baseContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstStart = prefs?.getBoolean("firstStart", true)
        val editor: SharedPreferences.Editor? = prefs?.edit()

        Log.d("info", "SharedPreferences firstStart : $firstStart ")

        if(firstStart==true){
            initHardwareInfo(activity)

            editor?.putBoolean("firstStart", false)
            editor?.apply()
        }

        restoreTactics(activity)
    }

    private fun restoreTactics(activity: Activity){

        val dbHelper =  DatabaseHelper(activity.baseContext)
        val viewsData = dbHelper.getViewsDataOfActivity(activity.localClassName)
        val tacticsData = dbHelper.getTacticsDataOfActivity(activity.localClassName)

        for (view in viewsData){

            val viewID = view.viewID
            val resourceID = activity.baseContext.resources.getIdentifier(viewID, "layout", activity.packageName)
            val viewElement = activity.window?.decorView?.findViewById(resourceID) as View

            val index = tacticsData.indexOfFirst{
                it.viewID == view.viewID && it.viewLocal == view.viewLocal
            }
            // The view has tactics information
            if(index != -1){
                // Restore the tactics on the view
                val viewTactics = tacticsData[index]

                val color = viewTactics.color
                val padStart = viewTactics.paddingStart
                val padEnd = viewTactics.paddingEnd
                val padTop = viewTactics.paddingTop
                val padBottom = viewTactics.paddingBottom

                if(color != null){
                    viewElement.setBackgroundColor(color)
                }

                if(padStart != null && padEnd != null && padTop != null && padBottom != null){
                    viewElement.setPadding(padStart, padTop, padEnd, padBottom)
                }
            }
        }

    }


    fun applyCorrections(activity: Activity){

        val context = activity.baseContext
        val activityStr = activity.localClassName
        val dbHelper =  DatabaseHelper(context)

        val deviceData = dbHelper.getDeviceData()

        // Get views of this activity
        val viewsData = dbHelper.getViewsDataOfActivity(activityStr)

        // Get clicks data done since last correction in this activity
        val lastCorrectionTimestamp = deviceData[2] as String
        val clicks = dbHelper.getClicksDataOfActivity(activityStr, lastCorrectionTimestamp)

        // Look if it's time to analyse
        if(dbHelper.isAnalysisTime()){

            Log.d("info", "applyCorrections : ANALYSING DATA ... ")

            if(viewsData.isNotEmpty()){

                if(clicks.isNotEmpty()) {

                    // Get all sk views in activity
                    for (view in viewsData) {

                        val viewDelimitations = viewDelimitations(view.viewID, view.viewLocal, viewsData)
                        val maxDistance = getMaxDistance(deviceData)
                        val centerOfView = centerOfView(viewDelimitations)

                        if (viewDelimitations != null && maxDistance != null && centerOfView != null) {

                            val clicksOnView = clicksOnView(viewDelimitations, clicks)
                            val clicksAroundView = clicksAroundView(viewDelimitations, maxDistance, clicks)

                            // If at least 5 clicks were made on or around the view since last analyse
                            if (clicksOnView.size + clicksAroundView.size >= 5) {

                                // Analyse data, save to bd and return old analysis data to compare
                                val oldAnalysisData = analyseViewData(dbHelper, view.viewID!!, activityStr, viewDelimitations, centerOfView, clicksOnView, clicksAroundView)

                                //Get new analysis data
                                val newAnalysisData = dbHelper.getAnalysisData(view.viewID!!, activityStr)

                                // Apply tactics if necessary and save tactic modification of each view in bd
                                applyTactics(activity, oldAnalysisData, newAnalysisData)

                            } else {
                                Log.d(
                                    "info",
                                    "applyCorrections : NOT ENOUGH CLICKS ON VIEW ${view.viewID} IN ACTIVITY ${view.viewLocal} SINCE LAST ANALYSE TO ANALYSE "
                                )
                            }
                        } else {
                            Log.d("info", "applyCorrections : ERROR WHILE GETTING VIEW DELIMITATIONS, MAX DISTANCE FROM BORDER OR CENTER OF VIEW OF : VIEW ${view.viewID} IN ACTIVITY ${view.viewLocal}")
                        }
                    }

                } else {
                    Log.d("info", "applyCorrections : NO CLICKS DONE SINCE LAST ANALYSE ")
                }
            } else {
                Log.d("info", "applyCorrections : NO SILVERKIT VIEWS IN THIS APP ")
            }

            // Change last correction date in DB
            val time = Timestamp(System.currentTimeMillis())
            dbHelper.updateLastCorrectionTimestamp(time.toString())

            Log.d("info", "applyCorrections : DATA ANALYSED ")

        }
        dbHelper.close()

    }

    // -------------------- TACTICS
    private fun applyTactics(activity: Activity, oldAnalysisData : SkAnalysisData?, newAnalysisData : SkAnalysisData?){

        // See if it's necessary to apply tactics for the view, if yes apply tactics
        if(newAnalysisData != null){

            applyColorContrastTactic(activity, newAnalysisData, oldAnalysisData)
            //applyResizeTactic(data)
            applyGravityCenterTactic(activity, newAnalysisData, oldAnalysisData)

        }
    }

    private fun applyColorContrastTactic(activity: Activity, newAnalysisData: SkAnalysisData, oldAnalysisData: SkAnalysisData?){

        val viewID = newAnalysisData.viewID
        val resourceID = activity.baseContext.resources.getIdentifier(viewID, "layout", activity.packageName)
        val view = activity.window?.decorView?.findViewById(resourceID) as View

        // Get color of view
        val viewColor = getViewColor(view)
        Log.d("info", " viewColor : $viewColor ")

        // Get color of the view behind
        val viewBehindColor = getViewBehindColor(view)
        Log.d("info", " viewBehindColor : $viewBehindColor ")

        // If condition is ok to apply the tactic, apply it
        if(checkColorContrastTacticCdt(newAnalysisData, oldAnalysisData, view, viewColor, viewBehindColor, activity)){

            // Change brightness level of the view based and the view behind color
            if(viewColor != null){
                val result = getBrightnessLevel(viewColor, viewBehindColor)

                if(result != null){
                    changeBrightnessLevel(view, viewColor, result, viewID, activity)
                    Log.d("info", " Apply Color Contrast Tactic : SUCCESSFUL ")
                }
            } else {
                Log.d("info", " Apply Color Contrast Tactic : ERROR (NO BACKGROUND COLOR TO CHANGE) ")
            }


        } else {
            Log.d("info", " Apply Color Contrast Tactic : NOT NECESSARY ")
        }
    }

    // verifier si l'application de la tactique a améliorer les resultats depuis la dernire application
    // Si oui mais tjr < seuil : continuer
    // Si oui > seuil : pas appliquer de tactiques
    // Si empirer annuler l'application de la tactique

    /**
     * if oldErrorRatio > newErrorRatio : tactic fonctionne , sinon fonctionne pas
     *
     */
    private fun checkColorContrastTacticCdt(newAnalysisData: SkAnalysisData, oldAnalysisData: SkAnalysisData?, view:View, viewColor: Int?, viewBehindColor: Int?, activity: Activity) : Boolean{

        if(oldAnalysisData != null){
            // If there is old data than compare to new
            val oldRatio : Float = oldAnalysisData.errorRatio
            val newRatio : Float = newAnalysisData.errorRatio

            // Get basic color of view
            val context = activity.baseContext
            val dbHelper =  DatabaseHelper(context)
            val baseColor = dbHelper.getViewBaseColor(newAnalysisData.viewID, activity.localClassName)
            Log.d("info", " Remove Color Contrast Tactic : VIEW ${newAnalysisData.viewID} BASE COLOR $baseColor ")

            Log.d("info", " checkColorContrastTacticCdt : oldRatio $oldRatio newRatio $newRatio")

            return if((oldRatio >= newRatio)){
                // La tactique fonctionne
                // Si newRatio > 0.1 on continue d'appliquer sinon on arrête d'appliquer la tactique
                Log.d("info", " checkColorContrastTacticCdt : CONTINUE TO APPLY TACTIC ${newRatio > 0.1f}")
                newRatio > 0.1f
            } else {
                // La tactique fonctionne pas
                if(baseColor != viewColor){
                    // Tactic précédemment appliquée
                    if(oldRatio + 0.3f <= newRatio){
                        // Si les resultats empire plus d'un certains seuil (de 0.3 de ratio) on enleve la tactique
                        reduceColorContrastTactic(newAnalysisData.viewID, activity, view, viewColor, viewBehindColor)
                        Log.d("info", " checkColorContrastTacticCdt : REDUCE TACTIC ")
                        false
                    } else {
                        // On amplifie la tactic car on se dit que c'était surement pas assez pour avoir un impact
                        Log.d("info", " checkColorContrastTacticCdt : AMPLIFY TACTIC ${true}")
                        true
                    }
                } else {
                    // Apply tactic car la view est dans son état initial
                    Log.d("info", " checkColorContrastTacticCdt : APPLY TACTIC ${true}")
                    true
                }
            }

        } else {
            // If there isn't any old data than it's the first time we need to apply tactic if error ratio > 0.1
            Log.d("info", " checkColorContrastTacticCdt : APPLY TACTIC : ${newAnalysisData.errorRatio > 0.1f} ")
            return newAnalysisData.errorRatio > 0.1f
        }
    }

    private fun reduceColorContrastTactic(viewID : String, activity : Activity, view: View, viewColor: Int?, viewBehindColor: Int?){

        // Reduce tactics application if the view color isn't the base one
        if(viewColor != null){

            val result = getBrightnessLevel(viewColor, viewBehindColor)
            if(result != null){
                changeBrightnessLevel(view, viewColor, !result, viewID, activity)
                Log.d("info", " Reduce Color Contrast Tactic : REDUCE COLOR TACTIC ")
            }
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
            Log.d("info", " Remove Color Contrast Tactic : SET VIEW COLOR TO TRANSPARENT COLOR ")
        }
    }

    private fun getViewBehindColor(view:View) : Int?{

        var color : Int?

        var parent = view.parent as View
        // Get the first parent to have a color else null
        while (parent != view.rootView as View){
            color = getViewColor(parent)
            if(color != null){
                return color
            }
            parent = parent.parent as View
        }
        return null
    }

    private fun getViewColor(view:View) : Int?{

        return try{
            val background = view.background as ColorDrawable
            return background.color
        } catch (e:Exception){
            null
        }

    }

    private fun lightenColor(color: Int, value: Float): Int {
        val hsl: FloatArray = floatArrayOf(1f,1f,1f)
        colorToHSL(color, hsl)
        hsl[2] += value / 100
        hsl[2] = max(0f, min(hsl[2], 1f))
        return HSLToColor(hsl)
    }

    private fun darkenColor(color: Int, value: Float): Int {
        val hsl: FloatArray = floatArrayOf(1f,1f,1f)
        colorToHSL(color, hsl)
        hsl[2] -= value / 100
        hsl[2] = max(0f, min(hsl[2], 1f))
        return HSLToColor(hsl)
    }

    /**
     * return true if viewColor > viewBehindColor (lighten) else return false (darken)
     */
    private fun getBrightnessLevel(viewColor: Int, viewBehindColor:Int?) : Boolean? {

        var viewBColor = viewBehindColor

        // If the view behind doesn't have a background color set it to white full opacity
        if(viewBColor == null){
            viewBColor = Color.WHITE
        }
        // Get brightness level of both views
        val viewHSL = floatArrayOf(1f,1f,1f)
        val viewBehindHSL = floatArrayOf(1f,1f,1f)
        colorToHSL(viewColor, viewHSL)
        colorToHSL(viewBColor, viewBehindHSL)
        val viewL = viewHSL[2]
        val viewBehindL = viewBehindHSL[2]

        // If view behind brightness > view brightness : darkened the view, else : lightened the view
        return viewL > viewBehindL
    }

    private fun changeBrightnessLevel(view:View, viewColor: Int, lighten:Boolean, viewID: String, activity: Activity){

        val db =  DatabaseHelper(activity.baseContext)


        val newColor : Int
        if(lighten){
            newColor = lightenColor(viewColor, 10f)
            Log.d("info", " Apply Color Contrast Tactic : LIGHTEN COLOR = $newColor ")
        } else {
            newColor = darkenColor(viewColor, 10f)
            Log.d("info", " Apply Color Contrast Tactic : DARKEN COLOR = $newColor ")
        }

        view.setBackgroundColor(newColor)

        val tacticsData = db.getTacticsDataOfView(viewID, activity.localClassName)
        var padS = view.paddingStart
        var padE = view.paddingEnd
        var padT = view.paddingTop
        var padB = view.paddingBottom
        var oPadS = 0
        var oPadE = 0
        var oPadT = 0
        var oPadB = 0

        if(tacticsData != null){
            padS = tacticsData.paddingStart!!
            padE = tacticsData.paddingEnd!!
            padT = tacticsData.paddingTop!!
            padB = tacticsData.paddingBottom!!

            oPadS = tacticsData.oldPaddingStart!!
            oPadE = tacticsData.oldPaddingEnd!!
            oPadT = tacticsData.oldPaddingTop!!
            oPadB = tacticsData.oldPaddingBottom!!
        }

        val data = SkTacticsData(viewID, activity.localClassName, newColor, padS, padE, padT, padB, oPadS, oPadE, oPadT, oPadB)
        db.saveTacticsData(data)
        db.close()

    }

    private fun applyResizeTactic(viewAnalysisData: SkAnalysisData){

    }

    private fun applyGravityCenterTactic(activity: Activity, newAnalysisData: SkAnalysisData, oldAnalysisData: SkAnalysisData?){

        val viewID = newAnalysisData.viewID
        val resourceID = activity.baseContext.resources.getIdentifier(viewID, "layout", activity.packageName)
        val view = activity.window?.decorView?.findViewById(resourceID) as View

        val db =  DatabaseHelper(activity.baseContext)
        val viewsData = db.getViewsDataOfActivity(activity.localClassName)

        val lp = view.layoutParams
        val isWrapContent = (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT || lp.width == ViewGroup.LayoutParams.WRAP_CONTENT)
        val isMatchParent = (lp.height == ViewGroup.LayoutParams.MATCH_PARENT || lp.width == ViewGroup.LayoutParams.MATCH_PARENT)
        val isFillParent = (lp.height == ViewGroup.LayoutParams.FILL_PARENT || lp.width == ViewGroup.LayoutParams.FILL_PARENT)


        // view need to have a fixed size
        if(!isWrapContent && !isMatchParent && !isFillParent){
            // If condition for applying tactics are met
            if(checkGravityCenterTactic(view, activity, newAnalysisData, oldAnalysisData)){

                val gravityCenterX = newAnalysisData.gravityCenterX
                val gravityCenterY = newAnalysisData.gravityCenterY
                val delimitations = viewDelimitations(newAnalysisData.viewID, activity.localClassName, viewsData)

                if(delimitations != null){

                    val centerOfView = centerOfView(delimitations)

                    if(centerOfView != null){
                        var paddingStart = view.paddingStart
                        var paddingEnd = view.paddingEnd
                        var paddingTop = view.paddingTop
                        var paddingBottom = view.paddingBottom

                        if(gravityCenterX < centerOfView[0]){
                            // left : move right
                            paddingStart += 4
                            Log.d("info", " Apply Color Contrast Tactic : MOVE RIGHT ")
                        }
                        if(gravityCenterX > centerOfView[0]){
                            // right : move left
                            paddingEnd += 4
                            Log.d("info", " Apply Color Contrast Tactic : MOVE LEFT ")
                        }
                        if(gravityCenterY < centerOfView[1]){
                            // top : move bottom
                            paddingTop += 4
                            Log.d("info", " Apply Color Contrast Tactic : MOVE BOTTOM ")
                        }
                        if(gravityCenterY > centerOfView[1]){
                            // bottom : move top
                            paddingBottom += 4
                            Log.d("info", " Apply Color Contrast Tactic : MOVE TOP ")
                        }

                        view.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
                        val tacticsData = SkTacticsData(viewID, activity.localClassName, getViewColor(view), paddingStart, paddingEnd, paddingTop, paddingBottom, view.paddingStart, view.paddingEnd, view.paddingTop, view.paddingBottom)
                        db.saveTacticsData(tacticsData)

                        Log.d("info", " Apply Color Contrast Tactic : SUCCESSFUL ")

                    } else {
                        Log.d("info", " Apply Color Contrast Tactic : ERROR WHILE GETTING VIEW CENTER ")
                    }

                } else {
                    Log.d("info", " Apply Color Contrast Tactic : ERROR WHILE GETTING VIEW DELIMITATIONS ")
                }

            } else {
                Log.d("info", " Apply Gravity Center Tactic : NOT NECESSARY ")
            }
        } else {
            Log.d("info", " Apply Gravity Center Tactic : IMPOSSIBLE TO APPLY TACTIC : VIEW DOESN'T HAVE A FIXED SIZE")
        }

        db.close()
    }

    private fun checkGravityCenterTactic(view:View, activity : Activity, newAnalysisData: SkAnalysisData, oldAnalysisData: SkAnalysisData?): Boolean{

        val distGravityCenter = newAnalysisData.distGravityCenter

        // If there is old data compare
        if(oldAnalysisData != null){
            val oldDistGravityCenter = oldAnalysisData.distGravityCenter

            // See if it's necessary to apply tactic
            if(distGravityCenter <= oldDistGravityCenter){
                // Tactics works
                // If distance between gravity center and widget center is more than 15 pixels, apply else don't apply
                Log.d("info", " checkGravityCenterTactic : APPLY TACTIC : ${distGravityCenter > 15} ")
                return distGravityCenter > 15

            } else {
                // Tactics doesn't works
                // If the gravity center is way worse than before last correction, get back to last correction
                return if(oldDistGravityCenter + 20 <= distGravityCenter){

                    reduceGravityCenterTactic(newAnalysisData.viewID, activity, view, newAnalysisData)
                    
                    false
                } else {
                    // On amplifie la tactic car on se dit que c'était surement pas assez pour avoir un impact
                    Log.d("info", " checkGravityCenterTactic : AMPLIFY TACTIC ${true}")
                    true
                }
            }
        }
        // See if it's necessary to apply tactic
        else {
            // If there isn't any old data than it's the first time we need to apply tactic if distGravityCenter > 15
            Log.d("info", " checkGravityCenterTactic : APPLY TACTIC : ${distGravityCenter > 15} ")
            return distGravityCenter > 15
        }

    }

    private fun reduceGravityCenterTactic(viewID : String, activity: Activity, view:View, newAnalysisData: SkAnalysisData){
        val db =  DatabaseHelper(activity.baseContext)

        val data = db.getTacticsDataOfView(newAnalysisData.viewID, newAnalysisData.viewLocal)

        if(data != null){
            val padS = data.oldPaddingStart
            val padE = data.oldPaddingEnd
            val padT = data.oldPaddingTop
            val padB = data.oldPaddingBottom

            view.setPadding(padS, padT, padE, padB)
            val newData = SkTacticsData(viewID, activity.localClassName, data.color, padS, padE, padT, padB, data.paddingStart, data.paddingEnd, data.paddingTop, data.paddingBottom)
            db.saveTacticsData(newData)

            Log.d("info", " checkGravityCenterTactic : REDUCE TACTIC ")
        } else {

            Log.d("info", " checkGravityCenterTactic : IMPOSSIBLE TO REDUCE TACTIC, ERROR GETTING OLD VIEW PADDINGS")
        }
        db.close()
    }


    // -------------------- ANALYSE DATA

    private fun analyseViewData(db : DatabaseHelper, viewID : String, activityStr : String, viewDelimitations : List<Int>, centerOfView : List<Int>, clicksOnView : MutableList<SkClicksData>, clicksAroundView: MutableList<SkClicksData>) : SkAnalysisData?{

        val errorRatio = getErrorRatio(clicksOnView, clicksAroundView)
        val averageDistFromBorder = getAverageDistanceFromBorder(viewDelimitations, clicksAroundView)
        val gravityCenter = getGravityCenter(clicksOnView, clicksAroundView)
        val distGravityCenter = getDistGravityCenter(gravityCenter, centerOfView)

        val newAnalysisData = SkAnalysisData(viewID, activityStr, errorRatio, averageDistFromBorder, distGravityCenter, gravityCenter[0], gravityCenter[1])
        val oldAnalysisData = db.getAnalysisData(viewID, activityStr)

        db.addAnalysisData(newAnalysisData)
        return oldAnalysisData
    }

    private fun getErrorRatio(clicksOnView : MutableList<SkClicksData>, clicksAroundView : MutableList<SkClicksData>) : Float{
        // Compute error ratio

        val missClicks = clicksAroundView.size
        val totalClicks = clicksOnView.size + missClicks

        return missClicks.toFloat().div(totalClicks.toFloat())
    }

    private fun getAverageDistanceFromBorder(viewDelimitations : List<Int>, clicksAroundView : MutableList<SkClicksData>) : Float{
        // Compute average distance from border

        val pointsDistance = mutableListOf<Float>()

        for (click in clicksAroundView){
            val x = click.rawX
            val y = click.rawY
            val distance = getDistanceFromView(x, y, viewDelimitations)
            pointsDistance.add(distance)
        }

        if(pointsDistance.isEmpty()){
            pointsDistance.add(0f)
        }

        val result : Float = pointsDistance.sum() / pointsDistance.size.toFloat()

        Log.d("info", "getAverageDistanceFromBorder : $result ")
        return result

    }

    private fun getDistanceFromView(x:Int, y:Int, viewDelimitations: List<Int>) : Float{

        val tl_x = viewDelimitations[0].toFloat()
        val tl_y = viewDelimitations[1].toFloat()
        val dr_x = viewDelimitations[2].toFloat()
        val dr_y = viewDelimitations[3].toFloat()

        val view_TL = listOf(tl_x, tl_y)
        val view_DR = listOf(dr_x, dr_y)
        val view_TR = listOf(dr_x, tl_y)
        val view_DL = listOf(tl_x, dr_y)

        var P1 = emptyList<Float>()
        var P2 = emptyList<Float>()

        if (x <= tl_x){
            P1 = view_TL
            P2 = view_DL
        }

        if (dr_x <= x){
            P1 = view_TR
            P2 = view_DR
        }

        if  (y <= tl_y){
            P1 = view_TL
            P2 = view_TR
        }

        if (dr_y <= y){
            P1 = view_DL
            P2 = view_DR
        }

        val x1 = P1[0]
        val y1 = P1[1]
        val x2 = P2[0]
        val y2 = P2[1]

        // distance(x,y,x1,y1,x2,y2) = |(x2 - x1)(y1 - y) - (x1 - x)(y2 - y1)|   /    sqrt( (x2 - x1)² + (y2 - y1)² )
        val num : Float = ( ((x2-x1) * (y1-y.toFloat())) - ((x1 - x.toFloat()) * (y2 - y1))).absoluteValue

        val den : Float = sqrt( (x2 - x1).pow(2) + (y2 - y1).pow(2) )

        return num / den

    }

    private fun getGravityCenter(clicksOnView : MutableList<SkClicksData>, clicksAroundView : MutableList<SkClicksData>) : List<Int>{
        val total_x = mutableListOf<Int>()
        val total_y = mutableListOf<Int>()

        for (click in clicksOnView){
            total_x.add(click.rawX)
            total_y.add(click.rawY)
        }

        for (click in clicksAroundView){
            total_x.add(click.rawX)
            total_y.add(click.rawY)
        }

        val gravityX : Int = total_x.sum().div(total_x.size)
        val gravityY : Int = total_y.sum().div(total_y.size)

        Log.d("info", "gravityCenter : $gravityX $gravityY ")
        return listOf(gravityX, gravityY)
    }

    private fun getDistGravityCenter(gravityCenter : List<Int>, centerOfView : List<Int>) : Float{

        val result = sqrt(((centerOfView[0] - gravityCenter[0].toFloat()).pow(2) - (centerOfView[1] - gravityCenter[1].toFloat()).pow(2)).absoluteValue)

        Log.d("info", "getDistGravityCenter : $result ")
        return result
    }

    private fun viewDelimitations(viewID: String?, activity: String?, viewsData: MutableList<SkCoordsData>) : List<Int>?{

        if(viewID != null && activity != null){
            for (views in viewsData){
                if(viewID == views.viewID && activity == views.viewLocal){
                    return listOf(views.coordTL!![0], views.coordTL!![1], views.coordDR!![0], views.coordDR!![1])
                }
            }
        }

        Log.d("info", "viewDelimitations : ERROR WHILE GETTING VIEW DELIMITATION OF $viewID IN ACTIVITY $activity")
        return null

    }

    private fun getMaxDistance(deviceData: List<Any>) : Int? {

        try{
            val width : Int = deviceData[0] as Int
            val height : Int = deviceData[1] as Int

            if(width < height){
                return (width / 100) * 10
            }
            if (height >= width){
                return (height / 100) * 10
            }
            return null
        } catch (e:Exception){
            return null
        }

    }

    private fun clicksOnView(viewDelimitations : List<Int>, clicksData : MutableList<SkClicksData>) : MutableList<SkClicksData>{

        val clicksOnView = mutableListOf<SkClicksData>()

        val tl_x = viewDelimitations[0]
        val tl_y = viewDelimitations[1]
        val dr_x = viewDelimitations[2]
        val dr_y = viewDelimitations[3]

        for (click in clicksData){

            val x = click.rawX
            val y = click.rawY

            if( tl_x < x && tl_y < y && x < dr_x && y < dr_y){
                clicksOnView.add(click)
            }

        }

        return clicksOnView
    }

    private fun clicksAroundView(viewDelimitations : List<Int>, maxDistance : Int, clicksData : MutableList<SkClicksData>) : MutableList<SkClicksData>{

        val clicksAroundView = mutableListOf<SkClicksData>()

        val tl_x = viewDelimitations[0]
        val tl_y = viewDelimitations[1]
        val dr_x = viewDelimitations[2]
        val dr_y = viewDelimitations[3]

        for (click in clicksData){

            val x = click.rawX
            val y = click.rawY

            val inDistance = (tl_x - maxDistance <= x) && (tl_y - maxDistance <= y) and (x <= dr_x + maxDistance) and (y <= dr_y + maxDistance)
            val inView = (tl_x < x) && (tl_y < y) && (x < dr_x) && (y < dr_y)

            if( inDistance && !inView){
                clicksAroundView.add(click)
            }

        }

        return clicksAroundView

    }

    private fun centerOfView(viewDelimitations : List<Int>?) : List<Int>?{

        if(viewDelimitations != null){
            val tl_x = viewDelimitations[0]
            val tl_y = viewDelimitations[1]
            val dr_x = viewDelimitations[2]
            val dr_y = viewDelimitations[3]

            val center_x = tl_x + ((dr_x-tl_x)/2)
            val center_y = tl_y + ((dr_y - tl_y)/2)

            return listOf(center_x, center_y)
        }
        return null
    }


    // -------------------- INIT


    private fun initHardwareInfo(activity: Activity){
        saveHardwareData(activity)
    }

    private fun initViewsCoordinates(activity: Activity) {

        val prefs: SharedPreferences? = activity.baseContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val listActivities = prefs?.getStringSet("listActivities", HashSet<String>())
        val editor: SharedPreferences.Editor? = prefs?.edit()

        //get the root view
        val rv: ViewGroup? =
            activity.window?.decorView?.findViewById(android.R.id.content) as ViewGroup?

        //if root view is not null and if the activity isn't already saved in shared pref
        if (rv != null && !(listActivities?.contains(activity.localClassName))!!) {

            //get all the views of the root view
            val allChildren : List<View> = getAllChildren(rv)

            //Check for every view in the activity if it's a Silverkit view, if yes save it in the csv
            for (v in allChildren) {

                if (v is SkTools) {

                    val viewID = getViewID(v)
                    val viewLocal = getViewLocal(v)
                    val coord = getViewCoord(v)
                    val color = getViewColor(v)
                    val width = v.width
                    val height = v.height
                    val viewData = SkCoordsData(viewID, viewLocal, coord[0], coord[1], color, width, height)

                    //Save view coordinates in CSV file if the view coordinates aren't already saved
                    saveCoordinates(v, viewData)
                }

            }

            //Save new shared prefs by adding the activity name
            val hash = HashSet<String>(listActivities)
            hash.add(activity.localClassName)
            editor?.putStringSet("listActivities", hash)
            editor?.apply()

            Log.d("info", "SharedPreferences get specialized views of : ${activity.localClassName} ")
        }
    }

     private fun getAllChildren(v: View): List<View> {
        if (v !is ViewGroup) {
            return ArrayList()
        }

        val result = ArrayList<View>()
        val viewGroup = v as ViewGroup
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            //Add parents then detect its child elements
            result.add(child)
            result.addAll(getAllChildren(child))
        }
        return result
    }

    private fun getViewCoord(view: View): List<List<Int>>{

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val tl_x = location[0]
        val tl_y = location[1]
        val coordTL = listOf(tl_x, tl_y)

        val width = view.measuredWidth
        val height = view.measuredHeight
        val dr_x = tl_x + width
        val dr_y = tl_y + height
        val coordDR = listOf(dr_x, dr_y)

        return listOf<List<Int>>(coordTL, coordDR)
    }

    private fun getViewID(view: View): String {

        // view doesn't have an id
        Log.d("info", "VIEW ID : ${view.id}")
        if (view.id == View.NO_ID) {
            view.id = View.generateViewId()
            Log.d("info", "RETURNED VIEW ID : ${getViewType(view).toString() + "-" + view.id}")
            return getViewType(view).toString() + "-" + view.id
        }
        Log.d("info", "RETURNED VIEW ID : ${view.id}")
        return view.id.toString()

    }

    private fun getViewType(view: View) : SkTools.ViewType {

        return if(view is SkTools){
            view.getType()
        } else {
            Log.d("info", "SILVERKIT TOOL ONTOUCH : not a sk view)")
            SkTools.ViewType.NONE
        }

    }

    private fun getViewLocal(view: View) : String {
        return view.context.javaClass.simpleName
    }

    private fun saveCoordinates(view: View, viewData: SkCoordsData){

        val context = view.context

        val dbHelper =  DatabaseHelper(context)
        dbHelper.addViewData(viewData)

    }

    private fun saveHardwareData(activity: Activity){

        val hardwareData = getHardwareData(activity)

        val context = activity.baseContext

        val dbHelper =  DatabaseHelper(context)
        val time = Timestamp(System.currentTimeMillis())
        dbHelper.addDeviceData(hardwareData.screenWidth, hardwareData.screenHeight, time.toString())

    }

    private fun getHardwareData(activity: Activity): SkHardwareData {

        val hardwareData = SkHardwareData()

        //Get Screen width and height
        val screenDimensions = getScreenSizeIncludingTopBottomBar(activity.baseContext)
        hardwareData.screenWidth = screenDimensions[0]
        hardwareData.screenHeight = screenDimensions[1]

        return hardwareData

    }

    @SuppressLint("ServiceCast")
    private fun getScreenSizeIncludingTopBottomBar(context: Context): IntArray {
        val screenDimensions = IntArray(2) // width[0], height[1]
        val x: Int
        val y: Int
        val orientation = context.resources.configuration.orientation
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val screenSize = Point()
        display.getRealSize(screenSize)
        x = screenSize.x
        y = screenSize.y
        screenDimensions[0] = if (orientation == Configuration.ORIENTATION_PORTRAIT) x else y // width
        screenDimensions[1] = if (orientation == Configuration.ORIENTATION_PORTRAIT) y else x // height


        Log.d("info","getScreenSizeIncludingTopBottomBar : width ${screenDimensions[0]} height : ${screenDimensions[1]}")


        return screenDimensions
    }

}