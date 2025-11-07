package com.example.appgreenflow.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {
    private val articles: MutableLiveData<MutableList<Article?>?> =
        MutableLiveData(ArrayList<Article?>())
    private val isLoading: MutableLiveData<Boolean?> =
        MutableLiveData(false)
    private val errorMessage: MutableLiveData<String?> = MutableLiveData()
    private val db: FirebaseFirestore
    private var lastVisible: DocumentSnapshot? = null
    private var isFirstLoad = true

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getArticles(): LiveData<MutableList<Article?>?> {
        return articles
    }

    fun getIsLoading(): LiveData<Boolean?> {
        return isLoading
    }

    fun getErrorMessage(): LiveData<String?> {
        return errorMessage
    }

    fun loadArticles(loadMore: Boolean) {
        if (isLoading.getValue() == true) return

        isLoading.value = true
        errorMessage.value = null

        var query = db.collection("articles")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
        if (!isFirstLoad && lastVisible != null && loadMore) {
            query = query.startAfter(lastVisible)
        }

        query.get()
            .addOnSuccessListener { querySnapshot ->
                var currentList: MutableList<Article?>? = articles.value
                if (currentList == null) currentList = ArrayList()

                for (doc in querySnapshot.documents) {
                    val article = doc.toObject(Article::class.java)
                    if (article != null) {
                        currentList.add(article)
                    }
                }

                articles.value = currentList
                if (!querySnapshot.isEmpty) {
                    lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                }

                isFirstLoad = false
                isLoading.value = false
            }
            .addOnFailureListener { e ->
                errorMessage.value = "Lỗi load bài báo: ${e.message}"
                isLoading.value = false
            }
    }

    fun refreshArticles() {
        articles.value = ArrayList()
        lastVisible = null
        isFirstLoad = true
        loadArticles(false)
    }
}