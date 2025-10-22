package com.example.appgreenflow;

public class Article {
    public String title, desc, content, imageUrl;
    public long timestamp = System.currentTimeMillis();  // Default

    public Article() {}  // Default constructor cho Firestore
}