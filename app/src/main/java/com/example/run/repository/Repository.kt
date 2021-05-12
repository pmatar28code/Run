package com.example.run.repository

import android.graphics.Bitmap
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap

object Repository {
    var routeCoordinates = mutableListOf<Point>()
    var screenShotRep:Bitmap ? = null
    var repoAccumulatedDistanceMiles:Double ?= null
    var repoAccumulatedDistanceKilometers:Double ?= null
    var repoMapboxMap:MapboxMap ?= null
}