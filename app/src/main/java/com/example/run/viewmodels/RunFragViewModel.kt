package com.example.run.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.run.repository.Repository

class RunFragViewModel: ViewModel() {

    var liveDistance = MutableLiveData<Double>()

    init
    {
        liveDistance.postValue(0.0)
    }

    fun getLiveDistance(){
        val distance = Repository.repoLiveAccuDistanceKilometers
        liveDistance.postValue(distance)
    }

}