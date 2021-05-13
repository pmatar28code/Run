package com.example.run

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.run.databinding.ActivityMainBinding
import com.example.run.interfaces.MainInterface
import com.example.run.repository.Repository
import com.mapbox.mapboxsdk.Mapbox

class MainActivity() : AppCompatActivity(), MainInterface {
    private var MAPBOX_KEY = "pk.eyJ1IjoicG1hdGFyMjhjb2RlIiwiYSI6ImNrbnJ4anpzYTBuMzkyb3Bob3lwNjI3bTcifQ.1vv5YfZsK6KtKLd_cG7CQw"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, MAPBOX_KEY)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        swapFragments(MainFragment())
    }

    private fun swapFragments(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .addToBackStack("back")
                .replace(R.id.fragment_container, fragment)
                .commit()
    }

    override fun forSwapingFragments(fragment: Fragment) {
        swapFragments(fragment)
    }
}




