package com.example.run.repository

import android.graphics.Bitmap
import android.graphics.Color
import com.example.run.RunFragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfMeasurement
import java.math.BigDecimal
import java.math.RoundingMode

object Repository {
    var routeCoordinates = mutableListOf<Point>()
    var screenShotRep:Bitmap ? = null
    var repoAccumulatedDistanceMiles:Double =0.0
    var repoAccumulatedDistanceKilometers:Double =0.0
    var repoLiveAccuDistanceKilometers = 0.0
    var repoMapboxMap:MapboxMap ?= null
    var lineSource = "0"
    var lineLayer = "1"
    var locationComponentDisabled = false

    var rs =0
    var rf = 1
    var repoTurfPointFrom:Point ?= null
    var repoTurfPointTo:Point ?= null
    var repoDistanceKilometers:Double =0.0

     fun testingRoute(style: Style){
        lineSource+="0"
        lineLayer+="1"
        style.addSource(GeoJsonSource(lineSource,
                FeatureCollection.fromFeatures(arrayOf<Feature>(Feature.fromGeometry(
                        LineString.fromLngLats(routeCoordinates)
                )))))
// The layer properties for our line. This is where we make the line dotted, set the
// color, etc.
        style.addLayer(LineLayer(lineLayer, lineSource).withProperties(
                PropertyFactory.lineDasharray(arrayOf(0.01f, 2f)),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(Color.parseColor("#0000FF"))
        ))
    }

    fun roundKilometers():BigDecimal{
        val roundKilometers =
                BigDecimal(repoAccumulatedDistanceKilometers!!).setScale(2, RoundingMode.HALF_EVEN)
        return roundKilometers
    }

    fun roundMiles():BigDecimal{
       val roundMiles=
               BigDecimal(repoAccumulatedDistanceMiles!!).setScale(2, RoundingMode.HALF_EVEN)
        return roundMiles
    }

    fun getDistanceKilometers(){
        while (rs <= routeCoordinates.size){
            if (rs <= routeCoordinates.size) {
                if (rf < routeCoordinates.size) {
                    repoTurfPointFrom = routeCoordinates[rs]
                   repoTurfPointTo = routeCoordinates[rf]
                    repoDistanceKilometers = TurfMeasurement.distance(repoTurfPointFrom!!,
                            repoTurfPointTo!!, "kilometers")
                    ++rf
                }
                ++rs
                repoLiveAccuDistanceKilometers += repoDistanceKilometers
            }
        }
        rs =0
        rf =1
    }
}