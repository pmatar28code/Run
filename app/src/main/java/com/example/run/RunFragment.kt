package com.example.run

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.location.Location
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.os.Parcel
import android.os.StrictMode
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.run.databinding.FragmentRunBinding
import com.example.run.repository.Repository
import com.example.run.viewmodels.MainFragViewModel
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfMeasurement

class RunFragment: Fragment(R.layout.fragment_run),PermissionsListener, OnMapReadyCallback {
    companion object{
        private val PIN_IMAGE = "PIN_IMAGE"
        val PIN_LOCATION_SOURCE = "PIN_LOCATION_SOURCE"
        val PIN_LOCATION_SYMBOL = "PIN_LOCATION_SYMBOL"
        var pinLastLocation : Location?= null
        var pointList = mutableListOf<Location>()
        var s:Int = 0
        var f:Int = 1
        var accumulatedDistanceMiles = 0.0
        var accumulatedDistanceKilometers = 0.0
        var distanceMiles:Double = 0.0
        var distanceKilometers:Double = 0.0
        var turfPointFrom:Point ?= null
        var turfPointTo:Point ?= null
    }

    private var mapView:MapView?=null
    var map:MapboxMap ?= null
    private var permissionsManager:PermissionsManager?=null
    private var newPinLastKnownLocation : Location ?= null
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    @SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentRunBinding.bind(view)

        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        mapView = binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync { mapboxMap ->
                map = mapboxMap
                Repository.repoMapboxMap = map
                mapboxMap.setStyle(Style.MAPBOX_STREETS){style ->
                    val pinImage = resources.getDrawable(R.drawable.ic_pin, null)
                    style.addImage(PIN_IMAGE, pinImage)

                    enableLocationComponent(style)

                    binding.currentLocationFab.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(requireContext())
                        alertDialog.setTitle(R.string.title_dialog)
                        alertDialog.setMessage(R.string.end_run_dialog)
                        alertDialog.setNegativeButton(R.string.cancel_dialog,null)
                        alertDialog.setPositiveButton(R.string.accept_dialog){ _, _ ->
                            getDistanceMiles()
                            getDistanceKilometers()
                            Repository.repoAccumulatedDistanceMiles = accumulatedDistanceMiles
                            Repository.repoAccumulatedDistanceKilometers = accumulatedDistanceKilometers
                            map?.locationComponent?.locationEngine?.removeLocationUpdates(
                            PendingIntent.readPendingIntentOrNullFromParcel(Parcel.obtain()))
                            map?.locationComponent?.setCameraMode(CameraMode.NONE)
                            map?.locationComponent?.setLocationComponentEnabled(false)
                            Repository.locationComponentDisabled = true
                            mapboxMap.snapshot {
                                Repository.screenShotRep = it
                                val mainAct = activity as MainActivity
                                val mainViewModel : MainFragViewModel by viewModels()
                                mainViewModel.swapingViaInterface(mainAct,ResultsFragment())
                            }
                        }
                        alertDialog.show()
                    }
                }
            }
        }
    }

    private fun getDistanceMiles(){
        while (s <= Repository.routeCoordinates.size){
            if (s <= Repository.routeCoordinates.size) {
                if (f < Repository.routeCoordinates.size) {
                    turfPointFrom = Repository.routeCoordinates[s]
                    turfPointTo = Repository.routeCoordinates[f]
                    distanceMiles = TurfMeasurement.distance(turfPointFrom!!,
                    turfPointTo!!, "miles")
                    ++f
                }
                ++s
                accumulatedDistanceMiles += distanceMiles
            }
        }
        s =0
        f =1
    }

    private fun getDistanceKilometers(){
        while (s <= Repository.routeCoordinates.size){
            if (s <= Repository.routeCoordinates.size) {
                if (f < Repository.routeCoordinates.size) {
                    turfPointFrom = Repository.routeCoordinates[s]
                    turfPointTo = Repository.routeCoordinates[f]
                    distanceKilometers = TurfMeasurement.distance(turfPointFrom!!,
                    turfPointTo!!, "kilometers")
                    ++f
                }
                ++s
                accumulatedDistanceKilometers += distanceKilometers
            }
        }
        s =0
        f=1
    }

    fun setPinOnStartingLocation(style:Style): Int {
        return if(pinLastLocation == null) {
            pinLastLocation = map?.locationComponent?.lastKnownLocation ?: return R.drawable.ic_pin
            val point = Point.fromLngLat(pinLastLocation!!.longitude, pinLastLocation!!.latitude)
            pointList.add(pinLastLocation!!)
            val geoJsonSource = GeoJsonSource(PIN_LOCATION_SOURCE, point)
            val symbolLayer = SymbolLayer(PIN_LOCATION_SYMBOL, PIN_LOCATION_SOURCE)
            .apply {
                withProperties(PropertyFactory.iconImage(PIN_IMAGE))
            }
            style.apply {
                addSource(geoJsonSource)
                addLayer(symbolLayer)
            }
            R.drawable.ic_stop_run
        }else{
            style.apply {
                removeLayer(PIN_LOCATION_SYMBOL)
                removeSource(PIN_LOCATION_SOURCE)
            }
            newPinLastKnownLocation = pinLastLocation
            pinLastLocation = null
            R.drawable.ic_pin
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
       AlertDialog.Builder(requireContext())
           .setMessage(R.string.accept_message)
           .setPositiveButton(android.R.string.ok,null)
           .setNegativeButton(android.R.string.cancel,null)
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        map?.getStyle {
            enableLocationComponent(it)
            if(pinLastLocation == null){
                setPinOnStartingLocation(it)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {


// Get an instance of the component
        val locationComponent:LocationComponent = map?.locationComponent!!

// Set the LocationComponent activation options
            val locationComponentActivationOptions =
            LocationComponentActivationOptions.builder(requireContext(), style)
                    .useDefaultLocationEngine(false)
                    .build();

// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            //locationComponent.setCameraMode(CameraMode.TRACKING,7000L,9.0,null,null,null);
            //locationComponent.setCameraMode(CameraMode.TRACKING,2000L,12.0,null,null,null)

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initLocationEngine()
            if(pinLastLocation == null) {
                setPinOnStartingLocation(style)
            }

        } else {
            val permissionsManager =  PermissionsManager(this)
            permissionsManager.requestLocationPermissions(requireActivity())
        }
    }

    @SuppressLint("MissingPermission")
    private  fun initLocationEngine(): Unit {
        val locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        val mainLocationCallback = MainActivityLocationCallBack(this@RunFragment)
        locationEngine.requestLocationUpdates(request, mainLocationCallback, getMainLooper())
        locationEngine.getLastLocation(mainLocationCallback)
    }
}