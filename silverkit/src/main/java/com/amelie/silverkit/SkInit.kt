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
import com.amelie.silverkit.datamanager.SkAnalysisData
import com.amelie.silverkit.datamanager.SkClicksData
import com.amelie.silverkit.datamanager.SkCoordsData
import com.amelie.silverkit.datamanager.SkHardwareData
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
    }


    fun applyCorrections(activity: Activity){

        val context = activity.baseContext
        val dbHelper =  DatabaseHelper(context)

        // Look if it's time to analyse
        if(dbHelper.isAnalysisTime()){

            // Analyse data, save to bd and return old analysis data to compare
            val oldAnalysisData = analyseData(dbHelper, activity)

            //Get new analysis data
            val newAnalysisData = dbHelper.getAnalysisData(activity.localClassName)

            // Apply tactics if necessary
            applyTactics(dbHelper, activity, oldAnalysisData, newAnalysisData)
        }
    }

    // -------------------- TACTICS
    private fun applyTactics(dbHelper : DatabaseHelper, activity: Activity, oldAnalysisData : List<SkAnalysisData>, newAnalysisData : List<SkAnalysisData>){

        // See if it's necessary to apply tactics for the view, if yes apply tactics
        for (data in newAnalysisData){

            val viewOldAnalysisData = getViewOldData(data.viewID, activity.localClassName, oldAnalysisData)

            applyColorContrastTactic(activity, data, viewOldAnalysisData)
            applyResizeTactic(data)
            applyGravityCenterTactic(data)
        }
    }

    private fun getViewOldData(viewID: String, activity:String, oldAnalysisData:List<SkAnalysisData>):SkAnalysisData?{

        for(data in oldAnalysisData){
            if(data.viewID == viewID && data.viewLocal == activity){
                return data
            }
        }
        return null

    }

    private fun applyColorContrastTactic(activity: Activity, viewAnalysisData: SkAnalysisData, viewOldAnalysisData: SkAnalysisData?){

        val viewID = viewAnalysisData.viewID
        val resourceID = activity.baseContext.resources.getIdentifier(viewID, "layout", activity.packageName)
        val view = activity.window?.decorView?.findViewById(resourceID) as View

        // If condition is ok to apply the tactic, apply it
        if(checkColorContrastTacticCdt(viewAnalysisData, viewOldAnalysisData, view, activity)){

            // Get color of view
            val viewColor = getViewColor(view)
            Log.d("info", " viewColor : $viewColor ")

            // Get color of the view behind
            val viewBehindColor = getViewBehindColor(view)
            Log.d("info", " viewBehindColor : $viewBehindColor ")

            // Change brightness level of the view based and the view behind color
            val result = changeBrightnessLevel(view, viewColor, viewBehindColor)
            if(result){
                Log.d("info", " Apply Color Contrast Tactic : SUCCESSFUL ")
            } else {
                Log.d("info", " Apply Color Contrast Tactic : ERROR ")
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
    private fun checkColorContrastTacticCdt(viewAnalysisData: SkAnalysisData, viewOldAnalysisData: SkAnalysisData?, view:View, activity: Activity) : Boolean{

        if(viewOldAnalysisData != null){
            // If there is old data than compare to new
            val oldRatio : Float = viewOldAnalysisData.errorRatio
            val newRatio : Float = viewAnalysisData.errorRatio
            val oldDistFromBorder : Float = viewOldAnalysisData.averageDistFromBorder
            val newDistFromBorder : Float = viewAnalysisData.averageDistFromBorder

            Log.d("info", " checkColorContrastTacticCdt : oldRatio $oldRatio newRatio $newRatio oldDistFromBorder $oldDistFromBorder newDistFromBorder $newDistFromBorder")

            return if((oldRatio > newRatio) || (oldDistFromBorder > newDistFromBorder)){
                // La tactique fonctionne
                // Si newRatio > 0.1 on continue d'appliquer sinon on arrête d'appliquer la tactique
                Log.d("info", " checkColorContrastTacticCdt : CONTINUE TO APPLY TACTIC : newRatio $newRatio ${newRatio > 0.1f} ")
                newRatio > 0.1f
            } else {
                // La tactique fonctionne pas et empire les résultats
                if(oldRatio + 0.3f <= newRatio){
                    // Si les resultats empire plus d'un certains seuil (de 0.3 de ratio) on enleve la tactique
                    removeColorContrastTactic(viewAnalysisData.viewID, view, activity)
                    Log.d("info", " checkColorContrastTacticCdt : REMOVE TACTIC ")
                    false
                } else {
                    // On amplifie la tactic car on se dit que c'était surement pas assez pour avoir un impact
                    Log.d("info", " checkColorContrastTacticCdt : CONTINUE TO APPLY TACTIC : ${true} ")
                    true
                }
            }

        } else {
            // If there isn't any old data than it's the first time we need to apply tactic if error ratio > 0.1
            Log.d("info", " checkColorContrastTacticCdt : APPLY TACTIC : ${viewAnalysisData.errorRatio > 0.1f} ")
            return viewAnalysisData.errorRatio > 0.1f
        }
    }

    private fun removeColorContrastTactic(viewID : String, view: View, viewActivity : Activity){
        // Get basic color of view
        val context = viewActivity.baseContext
        val dbHelper =  DatabaseHelper(context)

        val color = dbHelper.getViewBaseColor(viewID, viewActivity.localClassName)
        Log.d("info", " Remove Color Contrast Tactic : VIEW $viewID BASE COLOR $color ")

        // Apply basic color of view
        if(color != null){
            view.setBackgroundColor(color)
            Log.d("info", " Remove Color Contrast Tactic : SET VIEW COLOR TO BASE COLOR ")
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
        /*
        val viewBackground = view.background
        if (viewBackground is ColorDrawable){
            return viewBackground.color
            /*
            val viewColorHex = java.lang.String.format("#%06X", 0xFFFFFF and viewColor)
            val viewColorRGB = Color.parseColor(viewColorHex)

            val red = Color.red(viewColor)
            val blue = Color.blue(viewColor)
            val green = Color.green(viewColor)
            val viewColorOpacity = Color.alpha(viewColor)
            val viewColorHSL = rgbToHsl(red, green, blue)

            Log.d("info", "getViewColor HSLA: ${viewColorHSL[0]} ${viewColorHSL[1]} ${viewColorHSL[2]} $viewColorOpacity")
            return listOf(viewColorHSL[0], viewColorHSL[1], viewColorHSL[2], viewColorOpacity.toDouble())
            */
        }
        return null
        */
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

    /*
    /**
     * Converts an HSL color value to RGB. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes h, s, and l are contained in the set [0, 1] and
     * returns r, g, and b in the set [0, 255].
     *
     * @param   {number}  h       The hue
     * @param   {number}  s       The saturation
     * @param   {number}  l       The lightness
     * @return  {Array}           The RGB representation
     */
    private fun hslToRgb(h : Double, s : Double, l : Double) : List<Double>{
        val r : Double
        val g : Double
        val b : Double

        if(s == (0).toDouble()){
            // achromatic
            r = l
            g = l
            b = l
        }else{

            val q = if(l < 0.5){
                l * (1 + s)
            } else {
                l + s - l * s
            }

            val p = 2 * l - q

            r = hue2rgb(p, q, h + 1/3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1/3)
        }

        return listOf(Math.round(r * 255).toDouble(), Math.round(g * 255).toDouble(), Math.round(b * 255).toDouble())
    }

    /**
     * Converts an RGB color value to HSL. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes r, g, and b are contained in the set [0, 255] and
     * returns h, s, and l in the set [0, 1].
     *
     * @param   {number}  r       The red color value
     * @param   {number}  g       The green color value
     * @param   {number}  b       The blue color value
     * @return  {Array}           The HSL representation
     */
    private fun rgbToHsl(r : Int, g : Int, b : Int): List<Double>{

        val R : Double = r / (255).toDouble()
        val G : Double = g / (255).toDouble()
        val B : Double = b / (255).toDouble()

        val maximum = max(max(R, G), B)
        val minimum = min(min(R, G), B)
        var h : Double = (maximum + minimum) / 2
        var s : Double = (maximum + minimum) / 2
        val l : Double = (maximum + minimum) / 2

        if(maximum == minimum){
            // achromatic
            h = (0).toDouble()
            s = (0).toDouble()
        }else{
            val d = maximum - minimum
            s = if(l > 0.5){
                d / (2 - maximum - minimum)
            } else {
                d / (maximum + minimum)
            }

            val temp = if(g < b){
                (6).toDouble()
            } else {
                (0).toDouble()
            }

            when(maximum){
                R -> h = (g - b) / d + temp
                G -> h = (b - r) / d + 2
                B -> h = (r - g) / d + 4
            }

            h /= 6
        }

        return listOf(h, s, l)
    }

    private fun hue2rgb(p : Double, q : Double, t: Double) : Double{

        var a = t

        if(a < 0) a += 1
        if(a > 1) a -= 1
        if(a < 1/6) return p + (q - p) * 6 * a
        if(a < 1/2) return q
        if(a < 2/3) return p + (q - p) * (2/3 - a) * 6
        return p
    }
    */


    private fun changeBrightnessLevel(view:View, viewColor: Int?, viewBehindColor:Int?) : Boolean {

        var viewBColor = viewBehindColor

        // If view has a background color
        if(viewColor != null){
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
            var newColor : Int? = null
            if(viewL > viewBehindL){
                // lighten
                newColor = lightenColor(viewColor, 20f)
            } else {
                // darken
                newColor = darkenColor(viewColor, 20f)
            }

            // Apply new color on view
            view.setBackgroundColor(newColor)
            Log.d("info", " Apply Color Contrast Tactic : NEW COLOR = $newColor ")

            return true
        }
        return false
    }

    private fun applyResizeTactic(viewAnalysisData: SkAnalysisData){

    }

    private fun applyGravityCenterTactic(viewAnalysisData: SkAnalysisData){

    }


    // -------------------- ANALYSE DATA
    /**
     * Recompute the analysis data for each view
     * Change last correction date
     * return old analysis data to compare with new
     */
    private fun analyseData(dbHelper : DatabaseHelper, activity: Activity) : List<SkAnalysisData>{

        // Get data from DB
        val views = dbHelper.getViewsData()
        val deviceData = dbHelper.getDeviceData()

        // Get previous analysis data of activity since last analyse
        val oldAnalysisData = dbHelper.getAnalysisData(activity.localClassName)

        // Get clicks data done since last correction
        val lastCorrectionTimestamp = deviceData[2] as String
        val clicks = dbHelper.getClicksDataSinceLastAnalysis(lastCorrectionTimestamp)

        // Analyse Data for each view in activity
        if(clicks.isNotEmpty() && views.isNotEmpty()){

            // Analyse data from all the views in this activity and save to table in DB
            val skViewsID = getSkViewsID(activity)
            for (viewID in skViewsID){
                val analysisData = analyseViewData(viewID, activity.localClassName, clicks, views, deviceData)
                if(analysisData != null){
                    // Update analysis data
                    dbHelper.addAnalysisData(analysisData)
                }
            }
        }

        // Change last correction date in DB
        val time = Timestamp(System.currentTimeMillis())
        dbHelper.updateLastCorrectionTimestamp(time.toString())

        return oldAnalysisData
    }

    private fun analyseViewData(viewID : String, activity: String, clicksData : MutableList<SkClicksData>, viewsData : MutableList<SkCoordsData>, deviceData : List<Any>) : SkAnalysisData?{

        val viewDelimitations = viewDelimitations(viewID, activity, viewsData)
        val maxDistance = getMaxDistance(deviceData)

        if(viewDelimitations.isNotEmpty() && maxDistance != 0){
            val clicksOnView = clicksOnView(viewDelimitations, clicksData)
            val clicksAroundView = clicksAroundView(viewDelimitations, maxDistance, clicksData)
            val centerOfView = centerOfView(viewDelimitations)

            val errorRatio = getErrorRatio(clicksOnView, clicksAroundView)
            val averageDistFromBorder = getAverageDistanceFromBorder(viewDelimitations, clicksAroundView)
            val distGravityCenter = getDistGravityCenter(clicksOnView, clicksAroundView, centerOfView)

            return SkAnalysisData(viewID, activity, errorRatio, averageDistFromBorder, distGravityCenter)
        }

        return null

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

    private fun getDistGravityCenter(clicksOnView : MutableList<SkClicksData>, clicksAroundView : MutableList<SkClicksData>, centerOfView : List<Int>) : Float{
        // Compute gravity center
        // Compute distance between gravity center and center of view

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

        val gravityX : Float = total_x.sum().toFloat().div(total_x.size.toFloat())
        val gravityY : Float = total_y.sum().toFloat().div(total_y.size.toFloat())

        val result = sqrt(((centerOfView[0] - gravityX).pow(2) - (centerOfView[1] - gravityY).pow(2)).absoluteValue)

        Log.d("info", "getDistGravityCenter : $result ")
        return result
    }

    private fun viewDelimitations(viewID: String, activity: String, viewsData: MutableList<SkCoordsData>) : List<Int>{

        for (views in viewsData){
            if(viewID == views.viewID && activity == views.viewLocal){
                return listOf(views.coordTL!![0], views.coordTL!![1], views.coordDR!![0], views.coordDR!![1])
            }
        }

        Log.d("info", "viewDelimitations : ERROR WHILE GETTING VIEW DELIMITATION OF $viewID IN ACTIVITY $activity")
        return listOf()

    }

    private fun getMaxDistance(deviceData: List<Any>) : Int {

        val width : Int = deviceData[0] as Int
        val height : Int = deviceData[1] as Int

        if(width < height){
            return (width / 100) * 10
        }
        if (height >= width){
            return (height / 100) * 10
        }
        return 0
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

    private fun centerOfView(viewDelimitations : List<Int>) : List<Int>{
        val tl_x = viewDelimitations[0]
        val tl_y = viewDelimitations[1]
        val dr_x = viewDelimitations[2]
        val dr_y = viewDelimitations[3]

        val center_x = tl_x + ((dr_x-tl_x)/2)
        val center_y = tl_y + ((dr_y - tl_y)/2)

        return listOf(center_x, center_y)

    }

    private fun getSkViewsID(activity: Activity) : List<String>{

        val skViewsID : MutableList<String> = mutableListOf()

        //get the root view
        val rv: ViewGroup? = activity.window?.decorView?.findViewById(android.R.id.content) as ViewGroup?

        //get all the views of the root view
        if(rv != null){
            val allChildren : List<View> = getAllChildren(rv)

            //Check for every view in the activity if it's a Silverkit view, if yes add viewID to list
            for (v in allChildren) {

                if (v is SkTools) {

                    val viewID = getViewID(v)
                    skViewsID.add(viewID)
                }
            }

        } else {
            Log.d("info", "SkInit : ERROR WHILE GETTING ROOT VIEW OF ACTIVITY $activity")
        }

        return skViewsID
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

    /*
    private fun saveCoordinates(view:View, viewData: SkCoordsData){

        //Create CSV if it doesn't exist
        val path = view.context.getExternalFilesDir(null)?.absolutePath
        val str = "$path/CoordinatesData.csv"
        FileWriter(str, true)

        //Read CSV
        val data:MutableList<List<String>> = readCSVCoordsData(str)

        //If the coords aren't saved, saved them
        val viewToSave = listOf(viewData.viewLocal, viewData.coordTL?.get(0).toString(), viewData.coordTL?.get(1).toString(), viewData.coordDR?.get(0).toString(), viewData.coordDR?.get(1).toString())

        if(!data.contains(viewToSave)){

            try {

                val writer = FileWriter(str, true)

                val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

                val coordTL = viewData.coordTL
                val tl_x = coordTL?.get(0)
                val tl_y = coordTL?.get(1)

                val coordDR = viewData.coordDR
                val dr_x = coordDR?.get(0)
                val dr_y = coordDR?.get(1)

                csvPrinter.printRecord(viewData.viewID, viewData.viewLocal, tl_x, tl_y, dr_x, dr_y)

                csvPrinter.flush()
                csvPrinter.close()

                println("Write coordinates data in CSV successfully!")

            } catch (e: Exception) {

                println("Writing coordinates data in CSV error!")
                e.printStackTrace()

            }

        }


    }
    */

    /*
    private fun readCSVCoordsData(path: String): MutableList<List<String>>{

        val data:MutableList<List<String>> = mutableListOf()

        var fileReader: BufferedReader? = null

        try {

            var line: String?

            fileReader = BufferedReader(FileReader(path))

            // Read CSV header
            line = fileReader.readLine()

            // Read the file line by line starting from the first line
            while (line != null) {
                val tokens = line.split(",")
                if (tokens.isNotEmpty()) {

                    val activity = tokens[1]
                    val coordTLX = tokens[2]
                    val coordTLY = tokens[3]
                    val coordDRX = tokens[4]
                    val coordDRY = tokens[5]
                    data.add(listOf(activity,coordTLX,coordTLY,coordDRX,coordDRY))

                }

                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }

        return data
    }
    */

    private fun saveCoordinates(view: View, viewData: SkCoordsData){

        val context = view.context

        val dbHelper =  DatabaseHelper(context)
        dbHelper.addViewData(viewData)

    }

    /*
    private fun saveHardwareData(activity: Activity){

        var fileReader: BufferedReader? = null

        //Create CSV if it doesn't exist
        val path = activity.baseContext.getExternalFilesDir(null)?.absolutePath
        val str = "$path/HardwareData.csv"
        FileWriter(str, true)

        try {

            var line: String?

            fileReader = BufferedReader(FileReader(str))

            // Read CSV first line
            line = fileReader.readLine()

            // if there's not even a line in the file, write in it the hardware info
            if (line == null) {

                //Data needs to be saved
                try {

                    val writer = FileWriter(str, true)

                    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

                    val hardwareData = getHardwareData(activity)

                    csvPrinter.printRecord(hardwareData.screenWidth, hardwareData.screenHeight)

                    csvPrinter.flush()
                    csvPrinter.close()

                    println("Write hardware data in CSV successfully!")

                } catch (e: Exception) {

                    println("Writing hardware data in CSV error!")
                    e.printStackTrace()

                }

            }

        } catch (e: Exception) {
            println("Reading HardwareData CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing HardwareData CSV fileReader Error!")
                e.printStackTrace()
            }
        }
    }
    */

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