package com.example.run.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.run.MainActivity
import com.example.run.RunFragment

class MainFragViewModel: ViewModel() {
    var visibilityFalse = false

    fun swapingViaInterface(activity: Activity){
        var mainAct = activity as MainActivity
        mainAct.forSwapingFragments(RunFragment())
    }
}