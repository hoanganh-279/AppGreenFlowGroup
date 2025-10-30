package com.example.appgreenflow.ui.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appgreenflow.ui.notifications.TrashBin
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.mapsforge.core.model.LatLong

class RouteViewModel : ViewModel() {
    private val trashBins: MutableLiveData<MutableList<TrashBin?>?> =
        MutableLiveData<MutableList<TrashBin?>?>(
            ArrayList<TrashBin?>()
        )
    val shortestPathPoints: MutableLiveData<MutableList<LatLong?>?> =
        MutableLiveData<MutableList<LatLong?>?>(
            ArrayList<LatLong?>()
        )
    private val db: FirebaseFirestore

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getTrashBins(): LiveData<MutableList<TrashBin?>?> {
        return trashBins
    }

    fun loadTrashBins(role: String?) {
        val threshold = if ("employee" == role) 50 else 70
        db.collection("trash_bins")
            .whereGreaterThan("percent", threshold)
            .orderBy("percent", Query.Direction.DESCENDING)
            .limit(20) // Limit + pagination sau
            .addSnapshotListener(EventListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) return@addSnapshotListener
                val bins: MutableList<TrashBin?> = ArrayList<TrashBin?>()
                if (snapshot != null) {
                    for (doc in snapshot) {
                        val bin: TrashBin = doc.toObject<TrashBin>(TrashBin::class.java)
                        if (bin != null) bins.add(bin)
                    }
                }
                trashBins.setValue(bins)
            })
    }
}