package com.example.run.viewmodels

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.run.MainActivity
import com.example.run.RunFragment

class MainFragViewModel: ViewModel() {
    var visibilityFalse = false

    fun swapingViaInterface(activity: Activity,fragment:Fragment){
        var mainAct = activity as MainActivity
        mainAct.forSwapingFragments(fragment)
    }
}