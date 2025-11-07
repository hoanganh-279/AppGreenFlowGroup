package com.example.appgreenflow.ui.route

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class RouteFragment : Fragment() {

    private lateinit var viewModel: RouteViewModel
    private lateinit var mapView: org.osmdroid.views.MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: GeoPoint? = null
    private val markers = mutableListOf<Marker>()
    private lateinit var roadManager: OSRMRoadManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))
        return inflater.inflate(R.layout.fragment_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[RouteViewModel::class.java]
        mapView = view.findViewById(R.id.map)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(21.0285, 105.8542))
        }

        roadManager = OSRMRoadManager(requireContext(), "GreenFlowApp/1.0")

        requestLocationPermission()
        observeViewModel()
        viewModel.loadTrashBins((requireActivity() as MainActivity).userRole ?: "customer")
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
                mapView.controller.setCenter(currentLocation)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.trashBins.observe(viewLifecycleOwner) { bins ->
            markers.forEach { mapView.overlays.remove(it) }
            markers.clear()
            bins?.filterNotNull()?.forEach { bin ->
                val point = GeoPoint(bin.lat, bin.lng)
                val marker = Marker(mapView).apply {
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
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    }

    private fun drawRoute(points: List<GeoPoint>) {
        val route = Polyline().apply {
            setPoints(points)
            color = 0xFF00FF00.toInt()
            width = 10f
        }
        mapView.overlays.add(route)
        mapView.controller.animateTo(points.last())
        mapView.invalidate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }
}
