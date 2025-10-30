package com.example.appgreenflow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.appgreenflow.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;

public class MapFragment extends Fragment {
    private MapView mapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File tileCache = new File(Environment.getExternalStorageDirectory(), "osmdroid");
        Configuration.getInstance().setOsmdroidTileCache(tileCache);
        Configuration.getInstance().load(getContext(), getActivity().getPreferences(Context.MODE_PRIVATE));

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setUseDataConnection(false); // Tắt kết nối internet
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(21.0278, 105.8342);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(startPoint);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(getContext(), "Cần cấp quyền để dùng bản đồ offline!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}