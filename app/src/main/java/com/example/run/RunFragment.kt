package com.example.run

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper.getMainLooper
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
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
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.Style.OnStyleLoaded
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfMeasurement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class RunFragment: Fragment(R.layout.fragment_run),PermissionsListener, OnMapReadyCallback {
    companion object{
        private val PIN_IMAGE = "PIN_IMAGE"
        private val PIN_LOCATION_SOURCE = "PIN_LOCATION_SOURCE"
        private val PIN_LOCATION_SYMBOL = "PIN_LOCATION_SYMBOL"
        private val ROUTE_SOURCE = "ROUTE_SOURCE"
        private val ROUTE_LAYER = "ROUTE_LAYER"
        private var MAPBOX_KEY = "pk.eyJ1IjoicG1hdGFyMjhjb2RlIiwiYSI6ImNrbnJ4anpzYTBuMzkyb3Bob3lwNjI3bTcifQ.1vv5YfZsK6KtKLd_cG7CQw"

    }
    /*
    class screenShot (view:View){
        fun takeScreenshot(view: View): Bitmap {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            return b
        }
        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v)
        }
    }

     */

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

    private var mapSnapshotter: MapSnapshotter? = null
    private var hasStartedSnapshotGeneration = false

    private var s:Int = 0
    private var f:Int = 1
    private var accumulatedDistance = 0.0
    private var distance:Double = 0.0
    private var turfPointFrom:Point ?= null
    private var turfPointTo:Point ?= null

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentRunBinding.bind(view)

        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())




        mapView = binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync { mapboxMap ->
                map = mapboxMap
                mapboxMap.setStyle(Style.MAPBOX_STREETS){style ->
                    val pinImage = resources.getDrawable(R.drawable.ic_pin, null)
                    style.addImage(PIN_IMAGE, pinImage)

                        enableLocationComponent(style)
                        setPinOnStartingLocation(style)


                    binding.currentLocationFab.setOnClickListener {
                        testingRoute(style)
                        while (s <= Repository.routeCoordinates.size){
                            if (s <= Repository.routeCoordinates.size) {
                                if (f < Repository.routeCoordinates.size) {
                                    turfPointFrom = Repository.routeCoordinates[s]
                                    turfPointTo = Repository.routeCoordinates[f]
                                    distance = TurfMeasurement.distance(turfPointFrom!!, turfPointTo!!, "miles")
                                    ++f
                                }
                                ++s
                                accumulatedDistance += distance
                            }
                    }
                        //Toast.makeText(requireContext(),"this is the total distance $accumulatedDistance",Toast.LENGTH_LONG).show()
                        //
                       // if (!hasStartedSnapshotGeneration) {
                         //   hasStartedSnapshotGeneration = true;
                          //  Toast.makeText(requireContext(), "loading snapshot image", Toast.LENGTH_SHORT).show()
                          //  mapView?.measuredHeight?.let {
                            //    startSnapShot(
                               //         mapboxMap.projection.visibleRegion.latLngBounds,
                               //         mapView!!.measuredHeight,
                                //        mapView!!.measuredWidth)
                           // };
                       // }
                        binding.currentLocationFab.setOnClickListener {
                        /*    if (!hasStartedSnapshotGeneration) {
                                hasStartedSnapshotGeneration = true;
                                Toast.makeText(requireContext(), "loading snapshot image", Toast.LENGTH_SHORT).show()
                                startSnapShot(
                                        mapboxMap.projection.visibleRegion.latLngBounds,
                                            mapView!!.measuredHeight,
                                            mapView!!.measuredWidth)
                            }*/

                            /* this method gives screen shot without route
                            val snapShotOptions = MapSnapshotter.Options(500, 500).apply {
                                this.withStyle(styleUri)
                            }

                            snapShotOptions.withRegion(mapboxMap.projection.visibleRegion.latLngBounds)

                            snapShotOptions.withStyle(mapboxMap.style!!.url)

                            val mapSnapshotter = MapSnapshotter(requireContext(), snapShotOptions)
                            mapSnapshotter?.start { snapshot ->

                                // Display, share, or use bitmap image how you'd like

                                val bitmapOfMapSnapshotImage = snapshot.bitmap
                                Repository.screenShotRep = bitmapOfMapSnapshotImage
                            }

                             */



                            //Repository.screenShotRep  = takeScreenShot(this.rootView)//screenShot(mapView!!.rootView)
                            mapboxMap.snapshot {
                                Repository.screenShotRep = it
                                var mainAct = activity as MainActivity
                                val mainViewModel : MainFragViewModel by viewModels()
                                mainViewModel.swapingViaInterface(mainAct,ResultsFragment())
                            }

                           // val screen = screenShot(this)
                            //Repository.screenShotRep = screen.takeScreenshotOfRootView(this)



                            //val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
                            //StrictMode.setVmPolicy(builder.build())
                        }


                        //Toast.makeText(requireContext(),"list: ${Respository.routeCoordinates[0]}",Toast.LENGTH_SHORT).show()
                    }
                    //setPinOnStartingLocation(style)


                   // val locationComponent = mapboxMap.getLocationComponent()

                }

                //val routeLayer = LineLayer(ROUTE_LAYER, ROUTE_SOURCE).apply {
                   // setProperties(
                       //     PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                          //  PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                           // PropertyFactory.lineWidth(1f),
                           // PropertyFactory.lineColor(Color.BLUE)
                    //)
                //}
                map?.getStyle { style ->
                   // style.addSource(GeoJsonSource(ROUTE_SOURCE))
                   // style.addLayer(routeLayer)

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
        mapSnapshotter?.cancel()
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

    fun requestPermission(style:Style, callBack: (Boolean) -> Unit){
        val permissionsManager = PermissionsManager(this)
        permissionsManager.requestLocationPermissions(requireActivity())
        if(PermissionsManager.areLocationPermissionsGranted(requireContext())){
            enableLocationComponent(style)
            var granted = true
            callBack(PermissionsManager.areLocationPermissionsGranted(requireContext()))
        }else{
            requestPermission(style, callBack)

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

    private fun startSnapShot(latLngBounds: LatLngBounds, height: Int, width: Int) {
        map?.getStyle(OnStyleLoaded { style ->
            if (mapSnapshotter == null) {
// Initialize snapshotter with map dimensions and given bounds
                val options = MapSnapshotter.Options(width, height)
                        .withRegion(latLngBounds)
                        .withCameraPosition(map?.cameraPosition)
                        .withStyle(style.uri)
                mapSnapshotter = MapSnapshotter(requireContext(), options)
            } else {
// Reuse pre-existing MapSnapshotter instance
                mapSnapshotter!!.setSize(width, height)
                mapSnapshotter!!.setRegion(latLngBounds)
                mapSnapshotter!!.setCameraPosition(map?.cameraPosition)
            }
            mapSnapshotter!!.start { snapshot ->
                val bitmapOfMapSnapshotImage = snapshot.bitmap
                Repository.screenShotRep = snapshot.bitmap
                val bmpUri = getLocalBitmapUri(bitmapOfMapSnapshotImage)
                val shareIntent = Intent()
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
                shareIntent.type = "image/png"
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(shareIntent, "Share map image"))
                hasStartedSnapshotGeneration = false
            }
        })
    }

    private fun getLocalBitmapUri(bmp: Bitmap): Uri? {
        var bmpUri: Uri? = null
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png")
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            try {
                out.close()
            } catch (exception: IOException) {
                //exception.printStackTrace()
                Log.e("PEDro","$exception")
            }
            bmpUri = Uri.fromFile(file)
        } catch (exception: FileNotFoundException) {
           // exception.printStackTrace()
            Log.e("PEDRO2","$exception")
        }
        return bmpUri
    }

    private fun screenShot(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun takeScreenShot(view: View): Bitmap? {
        // configuramos para que la view almacene la cache en una imagen
        view.isDrawingCacheEnabled = true
        view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
        view.buildDrawingCache()
        if (view.drawingCache == null) return null // Verificamos antes de que no sea null

        // utilizamos esa cache, para crear el bitmap que tendra la imagen de la view actual
        val snapshot = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        view.destroyDrawingCache()
        return snapshot
    }



}