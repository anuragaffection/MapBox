package com.example.mapone

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.gestures
import org.json.JSONObject


var mapView: MapView? = null
var annotationApi : AnnotationPlugin? = null

lateinit var annotationConfig : AnnotationConfig

const val layerID = "map_annotation"

var pointAnnotationManager : PointAnnotationManager? = null
var markerList :ArrayList<PointAnnotationOptions> = ArrayList()

var latitudeList : ArrayList<Double> = ArrayList()
var longitudeList : ArrayList<Double> = ArrayList()



class MainActivity : AppCompatActivity(){

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        createLatLongForMarker()

        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS) {
            zoomCamera()
            addAnnotationToMap()

            annotationApi = mapView?.annotations

            annotationConfig = AnnotationConfig(
                layerId = layerID
            )
            pointAnnotationManager = annotationApi?.createPointAnnotationManager(annotationConfig)!!

            createMarkerList()

            try {
                mapView!!.gestures.pitchEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        val homeView :ImageButton = findViewById(R.id.homeView)
        val outdoorView :ImageButton = findViewById(R.id.outdoorView)
        val satelliteView :ImageButton = findViewById(R.id.satelliteView)
        val streetView :ImageButton = findViewById(R.id.streetView)
        val trafficView :ImageButton = findViewById(R.id.trafficView)

        homeView.setOnClickListener {
            mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
        }

        outdoorView.setOnClickListener {
            mapView?.getMapboxMap()?.loadStyleUri(Style.OUTDOORS)
        }

        satelliteView.setOnClickListener {
            mapView?.getMapboxMap()?.loadStyleUri(Style.SATELLITE_STREETS)
        }

        streetView.setOnClickListener {
            mapView?.getMapboxMap()?.loadStyleUri(Style.DARK)
        }

        trafficView.setOnClickListener {
            mapView?.getMapboxMap()?.loadStyleUri(Style.TRAFFIC_DAY)
        }


    }



    private fun addAnnotationToMap() {

        bitmapFromDrawableRes(this@MainActivity, R.drawable.red_marker)?.let {

            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()

                .withPoint(Point.fromLngLat(77.216721, 28.644800))
                .withIconImage(it)

            pointAnnotationManager?.create(pointAnnotationOptions)
        }
    }


    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))


    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {

            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }



    private fun createLatLongForMarker(){
        latitudeList.add(27.176670)
        longitudeList.add(78.008072)

        latitudeList.add( 28.535517)
        longitudeList.add( 77.391029)

        latitudeList.add(28.9676)
        longitudeList.add(76.0534)

        latitudeList.add(27.276670)
        longitudeList.add(78.708072)

        latitudeList.add( 28.835517)
        longitudeList.add( 77.791029)

        latitudeList.add(28.0676)
        longitudeList.add(76.8534)

    }


    private fun zoomCamera(){
        mapView!!.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(	77.216721,28.644800))
                .zoom(11.0)
                .build()
        )
    }



    private fun createMarkerList(){

        clearAnnotation()

        pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener {
                annotation: PointAnnotation ->
                onMarkerItemClick(annotation)
                true
        })

        markerList =  ArrayList()

        val bitmap = convertDrawableToBitmap(AppCompatResources.getDrawable(this, R.drawable.red_marker))

        for (i in 0 until  6){

            val mObject = JSONObject()
            mObject.put("Index", i)

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitudeList[i], latitudeList[i]))
                .withData(Gson().fromJson(mObject.toString(), JsonElement::class.java))
                .withIconImage(bitmap!!)

            markerList.add(pointAnnotationOptions)
        }
        pointAnnotationManager?.create(markerList)

    }


    private fun clearAnnotation(){
        markerList = ArrayList()
        pointAnnotationManager?.deleteAll()
    }


    private fun onMarkerItemClick(marker: PointAnnotation) {

        val jsonElement: JsonElement? = marker.getData()

        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.marker_details)
        dialog.setCancelable(false)

        val viewLatitude = dialog.findViewById<TextView>(R.id.viewLatitude)
        val viewLongitude = dialog.findViewById<TextView>(R.id.viewLongitude)
        val btnDelete = dialog.findViewById<Button>(R.id.btnDelete)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        viewLatitude.text = "Latitude = " + jsonElement.toString()
        viewLongitude.text = "Longitude = " + jsonElement.toString()

        btnCancel.setOnClickListener {
            Toast.makeText(this@MainActivity, "It is safe", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        btnDelete.setOnClickListener {
            clearAnnotation()
            dialog.dismiss()
        }
        dialog.show()
    }





}


