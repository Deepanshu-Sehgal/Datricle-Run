package com.datricle.datriclerun.others


import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.datricle.datriclerun.R
import com.datricle.datriclerun.database.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pop-up window, when we click on a bar in the bar chart
 */
@SuppressLint("ViewConstructor")
class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if(e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val tvDate:TextView = findViewById(R.id.tvDate)
        val tvAvgSpeed:TextView = findViewById(R.id.tvAvgSpeed)
        val tvDistance:TextView = findViewById(R.id.tvDistance)
        val tvDuration:TextView = findViewById(R.id.tvDuration)
        val tvCaloriesBurned:TextView = findViewById(R.id.tvCaloriesBurned)
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        "${run.avgSpeedInKMH}km/h".also {
            tvAvgSpeed.text = it
        }
        "${run.distanceOnMeters / 1000f}km".also {
            tvDistance.text = it
        }
        tvDuration.text =
            TrackingUtilities.getFormattedStopWatchTime(
                run.timeInMillis
            )
        "${run.caloriesBurned}kcal".also {
            tvCaloriesBurned.text = it
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }
}