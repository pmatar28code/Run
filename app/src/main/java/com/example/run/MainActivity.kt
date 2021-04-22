package com.example.run

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.run.databinding.ActivityMainBinding
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

class MainActivity : AppCompatActivity() {
    companion object{
        private const val MAPBOX_KEY = "pk.eyJ1IjoicG1hdGFyMjhjb2RlIiwiYSI6ImNrbnJ4anpzYTBuMzkyb3Bob3lwNjI3bTcifQ.1vv5YfZsK6KtKLd_cG7CQw"


    }
    private var mapView: MapView? =null
    private var map:MapboxMap ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, MAPBOX_KEY)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         mapView = binding.mapBoxView.apply {
             onCreate(savedInstanceState)
             getMapAsync { mapboxMap ->
                 map = mapboxMap
                 mapboxMap.setStyle(Style.MAPBOX_STREETS){ style ->

                 }
             }

        }
    }
}