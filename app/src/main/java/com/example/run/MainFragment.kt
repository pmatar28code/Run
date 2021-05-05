package com.example.run

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.run.databinding.FragmentMainBinding
import com.example.run.viewmodels.MainFragViewModel

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentMainBinding.bind(view)
        var mainAct = activity as MainActivity
        val mainViewModel : MainFragViewModel by viewModels()

        binding.apply {
            mainStartRunButton.setOnClickListener{
                mainTitleText.isVisible = mainViewModel.visibilityFalse
                mainTextIntro.isVisible = mainViewModel.visibilityFalse
                mainImage.isVisible =  mainViewModel.visibilityFalse
                mainStartRunButton.isVisible = mainViewModel.visibilityFalse
                mainViewModel.swapingViaInterface(mainAct)
            }
        }
    }
}