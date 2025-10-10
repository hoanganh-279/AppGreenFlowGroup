package com.example.appgreenflow.ui.route;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.R;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.scalebar.DefaultMapScaleBar;

import java.io.InputStream;
import java.util.List;

public class RouteFragment extends Fragment {

    private RouteViewModel mViewModel;
    private MapView mapView;

    public static RouteFragment newInstance() {
        return new RouteFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
        mViewModel.loadTrashBins();  // Load demo thùng rác
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);

        mapView = rootView.findViewById(R.id.mapView);
        AndroidGraphicFactory.createInstance(getApplicationContext());
        mapView.setDisplayModel(new DisplayModel());

        try {
            InputStream mapStream = getActivity().getAssets().open("map/vietnam.map");
            MapFile mapFile = new MapFile(mapStream);
            TileRendererLayer tileRendererLayer = new TileRendererLayer(mapFile, mapView.getModel().displayModel, true, true);
            tileRendererLayer.setRenderTheme(InternalRenderTheme.OSMARENDER);
            mapView.getLayerManager().getLayers().add(tileRendererLayer);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi load map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        MapViewPosition mapViewPosition = mapView.getModel().mapViewPosition;
        mapViewPosition.setCenter(new LatLong(21.0285, 105.8542));
        mapViewPosition.setZoomLevel((byte) 15);

        mapView.getModel().displayModel.setScaleBarPosition(org.mapsforge.map.model.DisplayPosition.BOTTOM_RIGHT);
        DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(mapView.getModel().displayModel);
        mapScaleBar.setLatLong(21.0285, 105.8542);
        mapView.getLayerManager().getLayers().add(mapScaleBar);

        mViewModel.getTrashBins().observe(getViewLifecycleOwner(), this::updateMap);

        mViewModel.calculateShortestPath(new LatLong(21.0285, 105.8542));

        return rootView;
    }

    private void updateMap(List<LatLong> trashBins) {
        mapView.getLayerManager().getLayers().removeIf(layer -> layer instanceof Marker || layer instanceof org.mapsforge.map.layer.overlay.Polyline);

        //thùng rác
        for (int i = 0; i < trashBins.size(); i++) {
            LatLong bin = trashBins.get(i);
            Marker marker = new Marker(mapView);
            marker.setPosition(bin);
            marker.setTitle("Thùng rác #" + (i + 1));
            marker.setSnippet("Lat: " + bin.latitude + ", Lon: " + bin.longitude);
            mapView.getLayerManager().getLayers().add(marker);
        }

        org.mapsforge.map.layer.overlay.Polyline line = new org.mapsforge.map.layer.overlay.Polyline(mapView.getModel().displayModel);
        line.setPoints(mViewModel.getShortestPathPoints());
        line.setColor(AndroidGraphicFactory.convertToAndroidColor(0xFF00FF00));
        line.setWidth(5f);
        mapView.getLayerManager().getLayers().add(line);

        mapView.getLayerManager().redrawLayers();
        Toast.makeText(getContext(), trashBins.size() + " thùng rác", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AndroidGraphicFactory.clearResourceMemoryCache();
        mapView.destroyAll();
    }
}