package com.example.appgreenflow.ui.route;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;
import com.example.appgreenflow.ui.notifications.TrashBin;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;


0import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RouteFragment extends Fragment {
    private MapView map;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String userRole;
    private GeoPoint currentLocation;
    private List<Marker> markers = new ArrayList<>();
    private LocationCallback locationCallback;
    private RouteViewModel viewModel;
    private RoadManager roadManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), getActivity().getPreferences(Context.MODE_PRIVATE));
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        db = FirebaseFirestore.getInstance();
        userRole = ((MainActivity) requireActivity()).getUserRole();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        viewModel = new ViewModelProvider(this).get(RouteViewModel.class);

        map = view.findViewById(R.id.map);
        if (map == null) {
            Toast.makeText(getContext(), "Map init failed! Kiểm tra layout XML.", Toast.LENGTH_SHORT).show();
            return view;
        }
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setUseDataConnection(true);  // Bật online cho routing/geocode
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(21.0285, 105.8542);  // HN default
        mapController.setCenter(startPoint);

        // Init OSM RoadManager (A* routing)
        roadManager = new OSRMRoadManager(getContext());
        roadManager.addRequestListener(new RoadManager.RequestListener() {
            @Override
            public void onRouteBuildStarted() {
                // Show loading (thêm ProgressBar nếu cần)
                Log.d("Route", "Building route...");
            }

            @Override
            public void onRouteBuilt(Road road) {
                if (road.mNodes != null && !road.mNodes.isEmpty()) {
                    drawRoute(road.mNodes);
                    Toast.makeText(getContext(), "Route optimized: " + Math.round(road.mLength) + "m", Toast.LENGTH_SHORT).show();
                } else {
                    // Fallback: Manual polyline
                    List<GeoPoint> fallbackPoints = getAllBinPoints();
                    fallbackPoints.add(0, currentLocation);
                    drawRoute(fallbackPoints);
                    Toast.makeText(getContext(), "Fallback route drawn", Toast.LENGTH_SHORT).show();
                }
            }
        });

        EditText etSearch = view.findViewById(R.id.etSearchLocation);
        if (etSearch != null && "customer".equals(userRole)) {
            etSearch.setVisibility(View.VISIBLE);
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                geocodeAndFindNearest(etSearch.getText().toString());
                return true;
            });
        }

        requestPermissions();
        observeViewModel();
        loadTrashBins();  // Load trực tiếp nếu ViewModel chưa full
        return view;
    }

    private void observeViewModel() {
        viewModel.getTrashBins().observe(getViewLifecycleOwner(), bins -> {
            if (bins != null) {
                clearMarkers();
                for (TrashBin bin : bins) {
                    addMarker(bin);
                }
                map.invalidate();
            }
        });
        viewModel.loadTrashBins(userRole);
    }

    // Geocode with Nominatim using HttpURLConnection (thay Apache)
    private void geocodeAndFindNearest(String query) {
        if (query.isEmpty()) return;
        new AsyncTask<Void, Void, GeoPoint>() {
            @Override
            protected GeoPoint doInBackground(Void... voids) {
                try {
                    String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                    String urlString = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&limit=1";
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "GreenFlowApp/1.0");  // Nominatim yêu cầu User-Agent
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONArray array = new JSONArray(response.toString());
                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);
                            double lat = obj.getDouble("lat");
                            double lon = obj.getDouble("lon");
                            return new GeoPoint(lat, lon);
                        }
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    Log.e("Geocode", "Error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(GeoPoint point) {
                if (point != null) {
                    findNearestBin(point);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy vị trí: " + query, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void requestPermissions() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(requireContext(), perms[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), perms, 1);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (fusedLocationClient == null) return;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    if (map != null) {
                        map.getController().setCenter(currentLocation);
                    }
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy vị trí hiện tại!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        // Periodic update cho employee (mỗi 10s)
        if ("employee".equals(userRole)) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) return;
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        if (map != null) {
                            map.getController().animateTo(currentLocation);
                        }
                    }
                }
            };

            LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(false)
                    .build();
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }

    private void loadTrashBins() {
        if (db == null) return;
        db.collection("trash_bins")
                .whereGreaterThan("percent", 50)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    clearMarkers();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        TrashBin bin = doc.toObject(TrashBin.class);
                        if (bin != null) addMarker(bin);
                    }
                    if (map != null) {
                        map.invalidate();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi load: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addMarker(TrashBin bin) {
        if (map == null) return;
        GeoPoint point = new GeoPoint(bin.lat, bin.lng);
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(bin.location + " (" + bin.percent + "%)");

        // Tích hợp tint động
        int iconRes = bin.percent > 80 ? R.drawable.ic_trash_full : R.drawable.ic_trash_half;
        Drawable icon = ContextCompat.getDrawable(getContext(), iconRes);
        if (icon != null) {
            if (bin.percent > 80) {
                icon.setTint(Color.RED);
            } else {
                icon.setTint(Color.parseColor("#FFA500"));
            }
            marker.setIcon(icon);
        }

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        marker.setOnMarkerClickListener((m, mv) -> {
            if ("employee".equals(userRole)) {
                calculateOptimalRoute(point, getAllBinPoints());
            } else {
                findNearestBin(point);
            }
            return true;
        });
        markers.add(marker);
        map.getOverlays().add(marker);
    }

    private GeoPoint findNearestPoint(GeoPoint from, List<GeoPoint> points) {
        if (points.isEmpty()) return from;
        GeoPoint nearest = points.get(0);
        double minDist = distance(from, nearest);
        for (GeoPoint p : points) {
            double d = distance(from, p);
            if (d < minDist) {
                minDist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private double distance(GeoPoint p1, GeoPoint p2) {
        double R = 6371;  // Earth radius km
        double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;  // Meters
    }

    private void calculateOptimalRoute(GeoPoint dest, List<GeoPoint> allBins) {
        if (currentLocation == null) {
            Toast.makeText(getContext(), "Chưa có vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(currentLocation);
        List<GeoPoint> tempBins = new ArrayList<>(allBins);
        GeoPoint curr = currentLocation;
        for (int i = 0; i < Math.min(5, tempBins.size()); i++) {
            GeoPoint nearest = findNearestPoint(curr, tempBins);
            waypoints.add(nearest);
            tempBins.remove(nearest);
            curr = nearest;
        }
        roadManager.getRoad(waypoints);
    }

    private void findNearestBin(GeoPoint clicked) {
        if (currentLocation == null) {
            Toast.makeText(getContext(), "Chưa có vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        List<GeoPoint> allPoints = getAllBinPoints();
        if (allPoints.isEmpty()) return;
        GeoPoint nearest = findNearestPoint(currentLocation, allPoints);
        if (map != null) {
            map.getController().animateTo(nearest);
        }
        Toast.makeText(getContext(), "Thùng gần nhất: " + nearest.toString(), Toast.LENGTH_SHORT).show();
    }

    private List<GeoPoint> getAllBinPoints() {
        List<GeoPoint> points = new ArrayList<>();
        for (Marker m : markers) {
            points.add(m.getPosition());
        }
        return points;
    }

    private void clearMarkers() {
        if (map != null && markers != null) {
            for (Marker m : markers) {
                map.getOverlays().remove(m);
            }
            markers.clear();
        }
    }

    private void drawRoute(List<GeoPoint> points) {
        if (map == null) return;
        map.getOverlays().clear();
        for (Marker m : markers) {
            map.getOverlays().add(m);
        }
        Polyline route = new Polyline();
        route.setColor(Color.GREEN);
        route.setWidth(10f);
        route.setPoints(points);
        route.setGeodesic(true);
        map.getOverlays().add(route);
        map.getController().animateTo(points.get(points.size() - 1));
        map.invalidate();
        Toast.makeText(getContext(), "Tuyến đường tối ưu: " + points.size() + " điểm", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(getContext(), "Cần GPS để route chính xác!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        map = null;
        fusedLocationClient = null;
        markers.clear();
    }
}