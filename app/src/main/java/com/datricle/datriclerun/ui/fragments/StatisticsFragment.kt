package com.datricle.datriclerun.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.datricle.datriclerun.R
import com.datricle.datriclerun.databinding.FragmentStatisticsBinding
import com.datricle.datriclerun.others.CustomMarkerView
import com.datricle.datriclerun.others.TrackingUtilities
import com.datricle.datriclerun.ui.viewmodals.StatisticsViewModal
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModal: StatisticsViewModal by viewModels()
    private lateinit var binding: FragmentStatisticsBinding 

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserves()
        setupBarChart()
    }

    private fun setupBarChart() {

        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.RED
            textColor = Color.RED
            setDrawGridLines(false)

        }
        binding.barChart.axisLeft.apply {
            axisLineColor = Color.RED
            textColor = Color.RED
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor = Color.RED
            textColor = Color.RED
            setDrawGridLines(false)
        }

        binding.barChart.apply {
            contentDescription = "Avg Speed Over Time"
            description.text = "Avg Speed Over Time"
            description.textColor = Color.RED
            legend.isEnabled = false
        }
    }

    private fun subscribeToObserves() {
        viewModal.totalDistance.observe(viewLifecycleOwner, {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "$totalDistance km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        })
        viewModal.totalTimeRun.observe(viewLifecycleOwner, {
            it?.let {
                val totalTimeRun = TrackingUtilities.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }

        })

        viewModal.totalAvgSpeed.observe(viewLifecycleOwner, {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "$avgSpeed km/h"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModal.totalCaloriesBurned.observe(viewLifecycleOwner, {
            it?.let {
                val totalCalories = "$it kcal"
                binding.tvTotalCalories.text = totalCalories
            }
        })
        viewModal.runsSortedByDate.observe(viewLifecycleOwner, {
            it?.let {
                val allAvgSpeeds =
                    it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }

                val bardataSet = BarDataSet(allAvgSpeeds, "Avg Speed over Time")
                bardataSet.apply {
                    valueTextColor = Color.BLACK
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                val lineData = BarData(bardataSet)
                binding.barChart.data = lineData
                val marker = CustomMarkerView(
                    it.reversed(),
                    requireContext(),
                    R.layout.marker_view
                )
                binding.barChart.marker = marker
                binding.barChart.invalidate()
            }
        })


    }

}