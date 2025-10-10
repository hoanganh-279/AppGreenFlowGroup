package com.example.appgreenflow.ui.route;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouteViewModel extends ViewModel {
    private final MutableLiveData<List<LatLong>> trashBins = new MutableLiveData<>();
    private final MutableLiveData<List<LatLong>> shortestPathPoints = new MutableLiveData<>();

    public RouteViewModel() {
        trashBins.setValue(new ArrayList<>());
        shortestPathPoints.setValue(new ArrayList<>());
    }

    public LiveData<List<LatLong>> getTrashBins() {
        return trashBins;
    }

    // Load thùng rác demo
    public void loadTrashBins() {
        List<LatLong> bins = new ArrayList<>();
        Random random = new Random();
        LatLong center = new LatLong(21.0285, 105.8542);
        for (int i = 0; i < 5; i++) {
            double lat = center.latitude + (random.nextDouble() - 0.5) * 0.01;
            double lon = center.longitude + (random.nextDouble() - 0.5) * 0.01;
            bins.add(new LatLong(lat, lon));
        }
        trashBins.setValue(bins);
    }

    public void addTrashBin(LatLong point) {
        List<LatLong> current = trashBins.getValue();
        if (current != null) {
            current.add(point);
            trashBins.setValue(current);
        }
    }

    public void calculateShortestPath(LatLong startPoint) {
        List<LatLong> path = new ArrayList<>();
        path.add(startPoint);

        List<LatLong> bins = trashBins.getValue();
        if (bins != null && !bins.isEmpty()) {
            LatLong nearest = bins.get(0);
            double minDist = distance(startPoint, nearest);
            for (LatLong bin : bins) {
                double dist = distance(startPoint, bin);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = bin;
                }
            }
            LatLong midPoint = new LatLong(
                    (startPoint.latitude + nearest.latitude) / 2,
                    (startPoint.longitude + nearest.longitude) / 2
            );
            path.add(midPoint);
            path.add(nearest);
        }

        shortestPathPoints.setValue(path);
    }

    private double distance(LatLong p1, LatLong p2) {
        double latDiff = p1.latitude - p2.latitude;
        double lonDiff = p1.longitude - p2.longitude;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }

    public LiveData<List<LatLong>> getShortestPathPoints() {
        return shortestPathPoints;
    }
}