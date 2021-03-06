package com.example.run

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.fragment.app.Fragment
import com.example.run.databinding.FragmentResultsBinding
import com.example.run.repository.Repository
import com.mapbox.mapboxsdk.maps.Style
import java.math.BigDecimal
import java.math.RoundingMode

class ResultsFragment: Fragment(R.layout.fragment_results) {
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentResultsBinding.bind(view)

        Repository.repoMapboxMap?.getStyle {  deleteRouteAndPin(it) }

        binding.apply {
            mapScreeshot.setImageBitmap(Repository.screenShotRep)
            distanceText.text = Repository.roundMiles().toString() + " Mi"
            buttonMiles.setOnClickListener {
            distanceText.text = Repository.roundMiles().toString() + " Mi"
            }
            buttonKilometers.setOnClickListener {
                distanceText.text = Repository.roundKilometers().toString() + " KM"
            }
                shareFab.setOnClickListener {
                share(Repository.screenShotRep!!)
            }
        }
    }

    private fun share(bitmap: Bitmap) {
        val ctx = requireContext()
        val pathofBmp: String = MediaStore.Images.Media.insertImage(ctx.contentResolver,
        bitmap, "title", null)
        val uri = Uri.parse(pathofBmp)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Star App")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "")
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        ctx.startActivity(Intent.createChooser(shareIntent, "hello hello"))
    }

    private fun deleteRouteAndPin(style: Style){
        style.removeLayer(Repository.lineLayer)
        style.removeSource(Repository.lineSource)
        style.apply {
            removeLayer(RunFragment.PIN_LOCATION_SYMBOL)
            removeSource(RunFragment.PIN_LOCATION_SOURCE)
        }
        RunFragment.pinLastLocation = null
        RunFragment.pointList.clear()
        RunFragment.s = 0
        RunFragment.f = 1
        RunFragment.distanceKilometers = 0.0
        RunFragment.distanceMiles = 0.0
        RunFragment.accumulatedDistanceKilometers = 0.0
        RunFragment.accumulatedDistanceMiles = 0.0
        RunFragment.turfPointFrom = null
        RunFragment.turfPointTo = null
        Repository.routeCoordinates.clear()
        Repository.locationComponentDisabled = false
        Repository.lineSource +="0"
        Repository.lineLayer +="1"

    }
}