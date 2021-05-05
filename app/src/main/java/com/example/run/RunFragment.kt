package com.example.run

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.run.databinding.FragmentRunBinding
import com.example.run.repository.Repository
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RunFragment: Fragment(R.layout.fragment_run),PermissionsListener, OnMapReadyCallback {
    companion object{
        private val PIN_IMAGE = "PIN_IMAGE"
        private val PIN_LOCATION_SOURCE = "PIN_LOCATION_SOURCE"
        private val PIN_LOCATION_SYMBOL = "PIN_LOCATION_SYMBOL"
        private val ROUTE_SOURCE = "ROUTE_SOURCE"
        private val ROUTE_LAYER = "ROUTE_LAYER"
        private var MAPBOX_KEY = "pk.eyJ1IjoicG1hdGFyMjhjb2RlIiwiYSI6ImNrbnJ4anpzYTBuMzkyb3Bob3lwNjI3bTcifQ.1vv5YfZsK6KtKLd_cG7CQw"

    }

    private var mapView:MapView?=null
     var map:MapboxMap ?= null
    private var permissionsManager:PermissionsManager?=null
    private var pinLastLocation : Location?= null
    private var enabled:Boolean ? = null
    private var image:Int ?= null
    private val pointList = mutableListOf<Location>()
    private var newPinLastKnownLocation : Location ?= null
    val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentRunBinding.bind(view)


       // val locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())


        mapView = binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync { mapboxMap ->
                map = mapboxMap
                mapboxMap.setStyle(Style.MAPBOX_STREETS){style ->
                    val pinImage = resources.getDrawable(R.drawable.ic_pin, null)
                    style.addImage(PIN_IMAGE, pinImage)

                      //enableLocation(style)
                    enableLocationComponent(style)

                    binding.currentLocationFab.setOnClickListener {
                        testingRoute(style)
                        //Toast.makeText(requireContext(),"list: ${Respository.routeCoordinates[0]}",Toast.LENGTH_SHORT).show()
                    }
                    //setPinOnStartingLocation(style)


                   // val locationComponent = mapboxMap.getLocationComponent()

                }

                val routeLayer = LineLayer(ROUTE_LAYER, ROUTE_SOURCE).apply {
                    setProperties(
                            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                            PropertyFactory.lineWidth(1f),
                            PropertyFactory.lineColor(Color.BLUE)
                    )
                }
                map?.getStyle { style ->
                    style.addSource(GeoJsonSource(ROUTE_SOURCE))
                    style.addLayer(routeLayer)

                }
                //val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                  //      .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    //    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                      //  .build()
                //map?.getStyle { enableUserLocation(it) }
                //var mainLocationCallback = MainActivityLocationCallBack(this@RunFragment)
                //locationEngine.requestLocationUpdates(request,mainLocationCallback, Looper.getMainLooper())



                //locationEngine.getLastLocation(mainLocationCallback);

                //map?.getStyle { enableUserLocation(it) }
                //map?.getStyle { setPinOnStartingLocation(it) }
                //map?.addOnCameraMoveListener {
                    //map?.getStyle { enableUserLocation(it) }
                   // map?.getStyle{setPinOnStartingLocation(it)}
                    //route()
                    //for(item in pointList) {
                      //  val latlng = LatLng(item.latitude,item.longitude)
                       // val poli = mapboxMap.addPolyline(PolylineOptions()
                           //     .add(latlng)
                            //    .color(Color.parseColor("#FFFFFF"))
                            //    .width(10F))


                    //}
               // }
            }

        }




}

    private fun route(){
        val lastKnownLocation = map?.locationComponent?.lastKnownLocation ?: return
        val point = Point.fromLngLat(lastKnownLocation.longitude,lastKnownLocation.latitude)
        val lastPoint = Point.fromLngLat(newPinLastKnownLocation!!.longitude,newPinLastKnownLocation!!.latitude)
        val client = MapboxDirections.builder()
                .origin(point)
                .destination(lastPoint)
                .accessToken(MAPBOX_KEY)
                .overview(DirectionsCriteria.OVERVIEW_SIMPLIFIED)
                .profile(DirectionsCriteria.APPROACH_UNRESTRICTED )
                .build()
        client.enqueueCall(object: Callback<DirectionsResponse> {
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

            }

            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if(response.body() == null || response.body()!!.routes().size < 1){

                }else{
                    handleDirectionsResponse(response.body() as DirectionsResponse)
                }
            }

        })
    }

    private fun handleDirectionsResponse(response: DirectionsResponse){
        var currentRoute = response.routes()[0]
        map?.getStyle {style ->
            val geometry = currentRoute.geometry() ?: return@getStyle
            val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE)
            source?.setGeoJson(LineString.fromPolyline(geometry,Constants.PRECISION_6))
            style.getLayerAs<LineLayer>(ROUTE_LAYER)?.setProperties(
                    PropertyFactory.visibility(Property.VISIBLE)
            )

        }
    }

    private fun setPinOnStartingLocation(style:Style): Int {
        enableUserLocation(style)
        return if(pinLastLocation == null) {
            pinLastLocation = map?.locationComponent?.lastKnownLocation ?: return R.drawable.ic_pin
            val point = Point.fromLngLat(pinLastLocation!!.longitude, pinLastLocation!!.latitude)
            pointList.add(pinLastLocation!!)
            val geoJsonSource = GeoJsonSource(PIN_LOCATION_SOURCE, point)
            val symbolLayer = SymbolLayer(PIN_LOCATION_SYMBOL, PIN_LOCATION_SOURCE).apply {
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

    private fun enableLocation(style:Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            enableLocationComponent(style)
            setPinOnStartingLocation(style)
        } else {
            permissionsManager = PermissionsManager(this).apply {
                requestLocationPermissions(requireActivity())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation(style:Style):Boolean{
       return if(PermissionsManager.areLocationPermissionsGranted(requireContext())){
            val locationComponentOptions
            = LocationComponentOptions.builder(requireContext())
                .pulseEnabled(true)
                .build()
            val locationComponentActivationOptions
            = LocationComponentActivationOptions.builder(requireContext(),style)
                .locationComponentOptions(locationComponentOptions)
                .build()
            map?.locationComponent?.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                setCameraMode(CameraMode.TRACKING,2000L,
                12.0,null,null,null)
                renderMode = RenderMode.COMPASS
            }
           true
        }else{
            permissionsManager = PermissionsManager(this).apply {
                requestLocationPermissions(requireActivity())

            }
           false
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
            map?.getStyle { enableLocation(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
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
            locationComponent.setCameraMode(CameraMode.TRACKING);
            //locationComponent.setCameraMode(CameraMode.TRACKING,2000L,12.0,null,null,null)

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();

        } else {
            val permissionsManager =  PermissionsManager(this)
            permissionsManager.requestLocationPermissions(requireActivity())
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */

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

     fun testingRoute(style:Style){
        style.addSource(GeoJsonSource("line-source",
                FeatureCollection.fromFeatures(arrayOf<Feature>(Feature.fromGeometry(
                        LineString.fromLngLats(Repository.routeCoordinates)
                )))))

// The layer properties for our line. This is where we make the line dotted, set the
// color, etc.

// The layer properties for our line. This is where we make the line dotted, set the
// color, etc.
        style.addLayer(LineLayer("linelayer", "line-source").withProperties(
                PropertyFactory.lineDasharray(arrayOf(0.01f, 2f)),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
        ))
    }

}