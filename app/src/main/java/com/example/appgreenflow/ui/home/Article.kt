package com.example.appgreenflow.ui.home

data class Article(
    var title: String? = null,
    var desc: String? = null,
    var content: String? = null,
    var imageUrl: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)