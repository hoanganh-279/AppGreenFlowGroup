package com.example.appgreenflow.ui.route;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appgreenflow.ui.notifications.TrashBin;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

public class RouteViewModel extends ViewModel {
    private final MutableLiveData<List<TrashBin>> trashBins = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LatLong>> shortestPathPoints = new MutableLiveData<>(new ArrayList<>());
    private FirebaseFirestore db;

    public RouteViewModel() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<TrashBin>> getTrashBins() {
        return trashBins;
    }

    public void loadTrashBins(String role) {
        int threshold = "employee".equals(role) ? 50 : 70;
        db.collection("trash_bins")
                .whereGreaterThan("percent", threshold)
                .orderBy("percent", Query.Direction.DESCENDING)
                .limit(20)  // Limit + pagination sau
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    List<TrashBin> bins = new ArrayList<>();
                    if (snapshot != null) {
                        for (var doc : snapshot) {
                            TrashBin bin = doc.toObject(TrashBin.class);
                            if (bin != null) bins.add(bin);
                        }
                    }
                    trashBins.setValue(bins);
                });
    }

    public LiveData<List<LatLong>> getShortestPathPoints() {
        return shortestPathPoints;
    }
}