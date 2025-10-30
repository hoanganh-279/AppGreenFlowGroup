package com.example.appgreenflow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
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
        val tileCache = File(Environment.getExternalStorageDirectory(), "osmdroid")
        Configuration.getInstance().setOsmdroidTileCache(tileCache)
        Configuration.getInstance()
            .load(getContext(), getActivity()!!.getPreferences(Context.MODE_PRIVATE))

        if (ContextCompat.checkSelfPermission(
                getContext()!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById<MapView>(R.id.mapView)

        mapView!!.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView!!.setUseDataConnection(false) // Tắt kết nối internet
        mapView!!.setBuiltInZoomControls(true)
        mapView!!.setMultiTouchControls(true)

        val startPoint = GeoPoint(21.0278, 105.8342)
        mapView!!.getController().setZoom(12.0)
        mapView!!.getController().setCenter(startPoint)

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(
                getContext(),
                "Cần cấp quyền để dùng bản đồ offline!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }
}