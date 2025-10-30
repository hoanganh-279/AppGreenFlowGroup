package com.example.appgreenflow.ui.notifications

class Notification {
    @JvmField
    var id: String? = null
    @JvmField
    var location: String? = null
    @JvmField
    var lat: Double = 0.0
    @JvmField
    var lng: Double = 0.0
    @JvmField
    var percent: Int = 0
    var timestamp: Long = 0
    var status: String = "pending"
}
