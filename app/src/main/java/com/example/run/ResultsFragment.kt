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

class ResultsFragment: Fragment(R.layout.fragment_results) {
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentResultsBinding.bind(view)

        binding.apply {
            mapScreeshot.setImageBitmap(Repository.screenShotRep)
            distanceText.text = Repository.repoAccumulatedDistanceMiles.toString() + "Miles"
            buttonMiles.setOnClickListener {
            distanceText.text = Repository.repoAccumulatedDistanceMiles.toString() +"Miles"
            }
            buttonKilometers.setOnClickListener {
                distanceText.text = Repository.repoAccumulatedDistanceKilometers.toString() + "KM"
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
}