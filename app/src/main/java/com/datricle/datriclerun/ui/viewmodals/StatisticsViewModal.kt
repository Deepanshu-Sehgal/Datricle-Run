package com.datricle.datriclerun.ui.viewmodals

import androidx.lifecycle.ViewModel
import com.datricle.datriclerun.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModal @Inject constructor(
    mainRepository: MainRepository
) : ViewModel() {
    var totalTimeRun = mainRepository.getTotalTimeInMillis()
    var totalDistance = mainRepository.getTotalDistance()
    var totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    var totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    var runsSortedByDate = mainRepository.getAllRunsSortedByDate()

}