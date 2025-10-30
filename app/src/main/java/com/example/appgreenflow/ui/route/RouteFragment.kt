package com.example.appgreenflow.ui.route

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.databinding.FragmentRouteBinding
import com.google.android.gms.location.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class RouteFragment : Fragment() {

    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RouteViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: GeoPoint? = null
    private val markers = mutableListOf<Marker>()
    private lateinit var roadManager: OSRMRoadManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))
        _binding = FragmentRouteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(21.0285, 105.8542))
        }

        roadManager = OSRMRoadManager(requireContext(), "GreenFlowApp/1.0")

        requestLocationPermission()
        observeViewModel()
        viewModel.loadTrashBins((requireActivity() as MainActivity).getUserRole())
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = GeoPoint(it.latitude, it.longitude)
                binding.map.controller.setCenter(currentLocation)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.trashBins.observe(viewLifecycleOwner) { bins ->
            markers.forEach { binding.map.overlays.remove(it) }
            markers.clear()
            bins.forEach { bin ->
                val point = GeoPoint(bin.lat, bin.lng)
                val marker = Marker(binding.map).apply {
                    position = point
                    title = "${bin.location} (${bin.percent}%)"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        currentLocation?.let { start ->
                            val waypoints = arrayListOf(start, point)
                            Thread {
                                val road = roadManager.getRoad(waypoints)
                                activity?.runOnUiThread {
                                    drawRoute(road.mRouteHigh)
                                }
                            }.start()
                        }
                        true
                    }
                }
                markers.add(marker)
                binding.map.overlays.add(marker)
            }
            binding.map.invalidate()
        }
    }

    private fun drawRoute(points: List<GeoPoint>) {
        val route = Polyline().apply {
            setPoints(points)
            color = 0xFF00FF00.toInt()
            width = 10f
        }
        binding.map.overlays.add(route)
        binding.map.controller.animateTo(points.last())
        binding.map.invalidate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}