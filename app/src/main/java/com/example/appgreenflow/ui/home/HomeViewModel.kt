package com.example.appgreenflow.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appgreenflow.ui.home.Article
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception
import java.util.ArrayList

class HomeViewModel : ViewModel() {
    private val articles: MutableLiveData<MutableList<Article?>?> =
        MutableLiveData<kotlin.collections.MutableList<Article?>?>(java.util.ArrayList<Article?>())
    private val isLoading: MutableLiveData<kotlin.Boolean?> =
        MutableLiveData<kotlin.Boolean?>(false)
    private val errorMessage: MutableLiveData<kotlin.String?> = MutableLiveData<kotlin.String?>()
    private val db: FirebaseFirestore
    private var lastVisible: DocumentSnapshot? = null
    private var isFirstLoad = true

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun getArticles(): LiveData<MutableList<Article?>?> {
        return articles
    }

    fun getIsLoading(): LiveData<kotlin.Boolean?> {
        return isLoading
    }

    fun getErrorMessage(): LiveData<kotlin.String?> {
        return errorMessage
    }

    fun loadArticles(loadMore: kotlin.Boolean) {
        if (isLoading.getValue() == true) return

        isLoading.setValue(true)
        errorMessage.setValue(null)

        var query = db.collection("articles")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
        if (!isFirstLoad && lastVisible != null && loadMore) {
            query = query.startAfter(lastVisible) // Pagination
        }

        query.get()
            .addOnSuccessListener(OnSuccessListener { querySnapshot: QuerySnapshot? ->
                var currentList: MutableList<Article?>? = articles.getValue()
                if (currentList == null) currentList = ArrayList<Article?>()

                for (doc in querySnapshot!!.getDocuments()) {
                    val article: Article? = doc.toObject<Article?>(Article::class.java)
                    if (article != null) {
                        currentList.add(article)
                    }
                }

                articles.setValue(currentList)
                if (!querySnapshot.isEmpty()) {
                    lastVisible = querySnapshot.getDocuments()
                        .get(querySnapshot.size() - 1) // Next startAfter
                }

                isFirstLoad = false
                isLoading.setValue(false)
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                errorMessage.setValue("Lỗi load bài báo: " + e!!.message)
                isLoading.setValue(false)
            })
    }

    fun refreshArticles() {
        articles.setValue(java.util.ArrayList<Article?>())
        lastVisible = null
        isFirstLoad = true
        loadArticles(false)
    }
}