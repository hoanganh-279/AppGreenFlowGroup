package com.example.appgreenflow.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgreenflow.Article;
import com.example.appgreenflow.ui.home.ArticleAdapter;
import com.example.appgreenflow.ui.ArticleDetailActivity;
import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView rvArticles;
    private ArticleAdapter adapter;
    private List<Article> articlesList = new ArrayList<>();
    private FirebaseFirestore db;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        if (view == null) return null;  // Kiểm tra null

        db = FirebaseFirestore.getInstance();
        rvArticles = view.findViewById(R.id.rvArticles);

        setupRecycler();
        loadArticles();
        return view;
    }

    private void setupRecycler() {
        if (rvArticles != null) {
            adapter = new ArticleAdapter(articlesList, article -> {
                if (getContext() != null) {
                    Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
                    intent.putExtra("title", article.title);
                    intent.putExtra("desc", article.desc);
                    intent.putExtra("content", article.content);
                    startActivity(intent);
                }
            });
            rvArticles.setAdapter(adapter);
            rvArticles.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    private void loadArticles() {
        if (db != null) {
            db.collection("articles")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        articlesList.clear();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Article article = doc.toObject(Article.class);
                            if (article != null) {
                                articlesList.add(article);
                            }
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Lỗi load bài báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}