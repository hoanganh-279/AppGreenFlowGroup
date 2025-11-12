package com.example.appgreenflow.ui.report

data class Report(
    var reportId: String = "",
    var userId: String = "",
    var userName: String = "",
    var location: Location = Location(),
    var nearestBinId: String? = null,
    var type: String = "",
    var description: String = "",
    var images: List<String> = emptyList(),
    var isUrgent: Boolean = false,
    var status: String = "pending", // pending, assigned, processing, done
    var assignedTo: String? = null,
    var assignedAt: Long? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var address: String = "",
    var rating: Int = 0,
    var feedback: String = ""
) {
    data class Location(
        var lat: Double = 0.0,
        var lng: Double = 0.0
    )
}
