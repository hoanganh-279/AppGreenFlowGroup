package com.example.appgreenflow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

class MapFragment : Fragment() {
    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Tối ưu: Dùng context.cacheDir thay vì external storage
        val tileCache = File(requireContext().cacheDir, "osmdroid")
        Configuration.getInstance().osmdroidTileCache = tileCache
        Configuration.getInstance().load(
            requireContext(), 
            requireActivity().getPreferences(Context.MODE_PRIVATE)
        )

        // Không cần WRITE_EXTERNAL_STORAGE cho Android 10+
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)

        mapView?.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setUseDataConnection(true) // Bật để load tiles
            setBuiltInZoomControls(true)
            setMultiTouchControls(true)
            
            // Tối ưu: Set zoom limits
            minZoomLevel = 10.0
            maxZoomLevel = 19.0
            
            val startPoint = GeoPoint(21.0278, 105.8342)
            controller.setZoom(12.0)
            controller.setCenter(startPoint)
        }

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            Toast.makeText(
                context,
                "Cần cấp quyền để dùng bản đồ offline!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDetach()
        mapView = null
    }
}