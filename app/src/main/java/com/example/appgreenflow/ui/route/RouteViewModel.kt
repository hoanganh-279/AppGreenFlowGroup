package com.example.appgreenflow.ui.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appgreenflow.ui.notifications.TrashBin
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.osmdroid.util.GeoPoint

class RouteViewModel : ViewModel() {
    val trashBins: MutableLiveData<MutableList<TrashBin>> = MutableLiveData(mutableListOf())
    val shortestPathPoints: MutableLiveData<MutableList<GeoPoint>> = MutableLiveData(mutableListOf())  // Updated to GeoPoint for consistency with OSMDroid

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getTrashBins(): LiveData<MutableList<TrashBin>> = trashBins

    fun loadTrashBins(role: String) {
        val threshold = if (role == "employee") 50 else 70
        db.collection("trash_bins")
            .whereGreaterThan("percent", threshold)
            .orderBy("percent", Query.Direction.DESCENDING)
            .limit(20)  // Limit + pagination later
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) return@addSnapshotListener
                val bins: MutableList<TrashBin> = mutableListOf()
                snapshot?.documents?.forEach { doc ->
                    val bin = doc.toObject(TrashBin::class.java)
                    bin?.let { bins.add(it) }
                }
                trashBins.value = bins
            }
    }
}