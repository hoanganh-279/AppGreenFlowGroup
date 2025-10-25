package com.example.appgreenflow.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appgreenflow.Article;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private FirebaseFirestore db;
    private DocumentSnapshot lastVisible;  // For pagination
    private boolean isFirstLoad = true;

    public HomeViewModel() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Article>> getArticles() {
        return articles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadArticles(boolean loadMore) {
        if (isLoading.getValue() == true) return;  // Prevent duplicate loads

        isLoading.setValue(true);
        errorMessage.setValue(null);

        Query query = db.collection("articles").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        if (!isFirstLoad && lastVisible != null && loadMore) {
            query = query.startAfter(lastVisible);  // Pagination
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Article> currentList = articles.getValue();
                    if (currentList == null) currentList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Article article = doc.toObject(Article.class);
                        if (article != null) {
                            currentList.add(article);
                        }
                    }

                    articles.setValue(currentList);
                    if (!querySnapshot.isEmpty()) {
                        lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);  // Next startAfter
                    }

                    isFirstLoad = false;
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Lỗi load bài báo: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    // Reset for refresh
    public void refreshArticles() {
        articles.setValue(new ArrayList<>());
        lastVisible = null;
        isFirstLoad = true;
        loadArticles(false);
    }
}