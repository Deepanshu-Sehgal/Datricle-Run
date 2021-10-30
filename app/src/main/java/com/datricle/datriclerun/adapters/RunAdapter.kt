package com.datricle.datriclerun.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.datricle.datriclerun.R
import com.datricle.datriclerun.database.Run
import com.datricle.datriclerun.others.TrackingUtilities
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_run, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        val imageView: ImageView = holder.itemView.findViewById(R.id.ivRunImage)
        val dateTv: TextView = holder.itemView.findViewById(R.id.tvDate)
        val avgSpeedTv: TextView = holder.itemView.findViewById(R.id.tvAvgSpeed)
        val distanceTv: TextView = holder.itemView.findViewById(R.id.tvDistance)
        val timeTv: TextView = holder.itemView.findViewById(R.id.tvTime)
        val caloriesTv: TextView = holder.itemView.findViewById(R.id.tvCalories)

        holder.itemView.apply {
            Glide.with(this).load(run.img).into(imageView)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            dateTv.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH} km/h"
            avgSpeedTv.text = avgSpeed

            val distanceInKm = "${run.distanceOnMeters / 1000f}"
            distanceTv.text = distanceInKm

            timeTv.text = TrackingUtilities.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned} kcal"
            caloriesTv.text = caloriesBurned

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}