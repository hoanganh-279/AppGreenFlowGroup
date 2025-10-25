package com.example.appgreenflow.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgreenflow.Article;
import com.example.appgreenflow.R;
import com.example.appgreenflow.ui.ArticleDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView rvArticles;
    private ArticleAdapter adapter;
    private HomeViewModel viewModel;
    private ProgressBar progressBar;
    private boolean isLoading = false;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        if (view == null) return null;

        rvArticles = view.findViewById(R.id.rvArticles);
        progressBar = view.findViewById(R.id.progressBar);

        // Fallback nếu XML chưa sync (hiếm)
        if (progressBar == null) {
            progressBar = new ProgressBar(getContext());
            ((ViewGroup) rvArticles.getParent()).addView(progressBar, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            progressBar.setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupViewModel();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ArticleAdapter(new ArrayList<>(), article -> {
            if (getContext() != null) {
                Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
                intent.putExtra("title", article.title);
                intent.putExtra("desc", article.desc);
                intent.putExtra("content", article.content);
                startActivity(intent);
            }
        });
        if (rvArticles != null) {
            rvArticles.setAdapter(adapter);
            rvArticles.setLayoutManager(new LinearLayoutManager(getContext()));

            rvArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && !isLoading) {
                        int totalItemCount = layoutManager.getItemCount();
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        if (totalItemCount <= (lastVisibleItem + 5)) {
                            viewModel.loadArticles(true);
                        }
                    }
                }
            });
        }
    }

    private void setupViewModel() {
        if (getActivity() != null) {
            viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
            viewModel.getArticles().observe(getViewLifecycleOwner(), this::updateArticles);
            viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showLoading);
            viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);

            // Initial load
            viewModel.loadArticles(false);
        }
    }

    private void updateArticles(List<Article> articlesList) {
        if (adapter != null) {
            adapter.updateData(articlesList);
        }
        isLoading = false;
    }

    private void showLoading(Boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        isLoading = loading;
    }

    private void showError(String message) {
        if (message != null && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rvArticles != null) {
            rvArticles.clearOnScrollListeners();
        }
        rvArticles = null;
        adapter = null;
        progressBar = null;
    }
}