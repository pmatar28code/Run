package com.example.run

import android.util.Log
import com.example.run.repository.Repository
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.modes.CameraMode
import java.lang.ref.WeakReference

class MainActivityLocationCallBack(activity: RunFragment): LocationEngineCallback<LocationEngineResult> {

    private var activity: RunFragment? = null
    var activityWeakReference = object : WeakReference<RunFragment>(activity) {}
    var point:Point ? = null

    override fun onSuccess(result: LocationEngineResult?) {
         activity = activityWeakReference.get()

        if (activity != null) {
            val location = result!!.lastLocation ?: return

// Pass the new location to the Maps SDK's LocationComponent
            if (activity!!.map != null && result.lastLocation != null) {
                activity!!.map?.getLocationComponent()?.forceLocationUpdate(result.lastLocation)
                point = Point.fromLngLat(result.lastLocation!!.longitude, result.lastLocation!!.latitude)
                Repository.routeCoordinates.add(point!!)
                activity!!.map?.getLocationComponent()?.setCameraMode(
                CameraMode.TRACKING,3000L,15.0,null,null,null)
                activity!!.map?.getStyle {
                    if(Repository.locationComponentDisabled == true){

                    }else{
                        Repository.testingRoute(it)
                    }
                }
            }
        }
    }
    override fun onFailure(exception: Exception) {
        Log.d("LocationChangeActivity", exception.localizedMessage)
        val activity: RunFragment? = activityWeakReference.get()
        if (activity != null) {

        }
    }
}