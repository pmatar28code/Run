package com.example.run

import android.util.Log
import android.widget.Toast
import com.example.run.repository.Respository
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import java.lang.ref.WeakReference


class MainActivityLocationCallBack(activity: RunFragment): LocationEngineCallback<LocationEngineResult> {

    private var activity: RunFragment? = null
    var activityWeakReference = object : WeakReference<RunFragment>(activity) {}
    var point:Point ? = null

    override fun onSuccess(result: LocationEngineResult?) {
         activity = activityWeakReference.get()

        if (activity != null) {
            val location = result!!.lastLocation ?: return

// Create a Toast which displays the new location's coordinates
            //Toast.makeText(activity!!.requireContext(), String.format(activity!!.getString(R.string.cancel),
               //     java.lang.String.valueOf(result.lastLocation!!.latitude), java.lang.String.valueOf(result.lastLocation!!.longitude)),
                //    Toast.LENGTH_SHORT).show()

// Pass the new location to the Maps SDK's LocationComponent
            if (activity!!.map != null && result.lastLocation != null) {
                activity!!.map?.getLocationComponent()?.forceLocationUpdate(result.lastLocation)
                point = Point.fromLngLat(result.lastLocation!!.longitude, result.lastLocation!!.latitude)
                Respository.routeCoordinates.add(point!!)
                //activity!!.map?.getLocationComponent()?.setCameraMode(CameraMode.TRACKING,6000L,12.0,null,null,null)

               //Toast.makeText(this.activity!!.requireContext(),"this is the location: ${Respository.routeCoordinates}",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFailure(exception: Exception) {
        Log.d("LocationChangeActivity", exception.localizedMessage)
        val activity: RunFragment? = activityWeakReference.get()
        if (activity != null) {
            //Toast.makeText(activity.requireContext(), exception.localizedMessage,
                   // Toast.LENGTH_SHORT).show()
        }
    }
}