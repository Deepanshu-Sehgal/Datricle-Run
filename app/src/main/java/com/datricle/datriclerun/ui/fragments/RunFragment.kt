package com.datricle.datriclerun.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.datricle.datriclerun.R
import com.datricle.datriclerun.adapters.RunAdapter
import com.datricle.datriclerun.databinding.FragmentRunBinding
import com.datricle.datriclerun.others.Constants.REQUEST_CODE_LOCATION_BACKGROUND_PERMISSION
import com.datricle.datriclerun.others.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.datricle.datriclerun.others.SortType
import com.datricle.datriclerun.others.TrackingUtilities
import com.datricle.datriclerun.ui.viewmodals.MainViewModal
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModal: MainViewModal by viewModels()
    private lateinit var binding: FragmentRunBinding
    private lateinit var runAdapter: RunAdapter
    var permission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestLocationPermission()
        setupRecyclerView()
        when(viewModal.sortType){
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0-> viewModal.sortRuns(SortType.DATE)
                    1-> viewModal.sortRuns(SortType.RUNNING_TIME)
                    2-> viewModal.sortRuns(SortType.DISTANCE)
                    3-> viewModal.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModal.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        viewModal.runs.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
        runAdapter.onItemClick={
            val dialog = BottomSheetDialog(context)

            val view = layoutInflater.inflate(R.layout.run_item_bottomsheet, null)
            val imgView = view.findViewById<ImageView>(R.id.ivRunImageRun)
            val dateView = view.findViewById<TextView>(R.id.tvDateRun)
            val timeView = view.findViewById<TextView>(R.id.tvTimeRun)
            val distanceView = view.findViewById<TextView>(R.id.tvDistanceRun)
            val avgSpeedView = view.findViewById<TextView>(R.id.tvAvgSpeedRun)
            val kcalView = view.findViewById<TextView>(R.id.tvCaloriesRun)
            Glide.with(this).load(it.img).into(imgView)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            dateView.text = "Date\n${dateFormat.format(calendar.time)}"

            val avgSpeed = "${it.avgSpeedInKMH} km/h"
            avgSpeedView.text = "Avg Speed\n$avgSpeed"

            val distanceInKm = "${it.distanceOnMeters / 1000f}"
            distanceView.text = "Distance\n$distanceInKm"

            timeView.text = "Time\n${TrackingUtilities.getFormattedStopWatchTime(it.timeInMillis)}"

            val caloriesBurned = "${it.caloriesBurned} kcal"
            kcalView.text = "Kcal\n$caloriesBurned"

            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(view)
            dialog.show()
        }
    }

    /*private fun requestPermissions() {

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(permission, REQUEST_CODE_LOCATION_PERMISSION)
            }
            shouldShowRequestPermissionRationale() -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            showInContextUI(...)
        }
            else -> {
                // You can directly ask for the permission.
                requestPermissions(CONTEXT,
                    arrayOf(Manifest.permission.REQUESTED_PERMISSION),
                    REQUEST_CODE)
            }
        }
    }*/

    private fun requestLocationPermission() {
        if (TrackingUtilities.hasLocationPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        requestBackgroundPermission()


        /* EasyPermissions.requestPermissions(
             this,
             "This application cannot work without Location Permission.",
             REQUEST_CODE_LOCATION_PERMISSION,
             Manifest.permission.ACCESS_FINE_LOCATION,
             Manifest.permission.ACCESS_COARSE_LOCATION,

         )*/
    }

    fun requestBackgroundPermission() {
        val backPermList = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        AlertDialog.Builder(requireContext())
            .setTitle("Background location permission")
            .setMessage("Allow location permission to get location updates in background")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissions(
                    backPermList,
                    REQUEST_CODE_LOCATION_BACKGROUND_PERMISSION
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    /*private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            requireContext(),
            "Permission Granted!",
            Toast.LENGTH_SHORT
        ).show()
    }
}


