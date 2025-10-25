package com.example.appgreenflow.ui.notifications;

public class Notification {
    public String id, location;
    public double lat, lng;
    public int percent;
    public long timestamp;
    public String status = "pending";

    public Notification() {}
}