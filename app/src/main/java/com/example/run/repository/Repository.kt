package com.example.run.repository

import android.graphics.Bitmap
import com.example.run.RunFragment
import com.mapbox.geojson.Point

object Repository {
    var routeCoordinates = mutableListOf<Point>()
    var screenShotRep :Bitmap ? = null
}