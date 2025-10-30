package com.example.appgreenflow.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R
import com.example.appgreenflow.ui.ArticleDetailActivity

class HomeFragment : androidx.fragment.app.Fragment() {
    private var rvArticles: RecyclerView? = null
    private var adapter: ArticleAdapter? = null
    private var viewModel: HomeViewModel? = null
    private var progressBar: android.widget.ProgressBar? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): android.view.View? {
        val view: android.view.View? = inflater.inflate(R.layout.fragment_home, container, false)
        if (view == null) return null

        rvArticles = view.findViewById<RecyclerView?>(R.id.rvArticles)
        progressBar = view.findViewById<android.widget.ProgressBar?>(R.id.progressBar)

        // Fallback nếu XML chưa sync (hiếm)
        if (progressBar == null) {
            progressBar = android.widget.ProgressBar(getContext())
            (rvArticles.getParent() as android.view.ViewGroup).addView(
                progressBar, android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            progressBar!!.setVisibility(android.view.View.GONE)
        }

        setupRecyclerView()
        setupViewModel()

        return view
    }

    private fun setupRecyclerView() {
        adapter = ArticleAdapter(
            java.util.ArrayList<Article?>(),
            ArticleAdapter.OnArticleClickListener { article: Article? ->
                if (getContext() != null) {
                    val intent: Intent =
                        Intent(requireContext(), ArticleDetailActivity::class.java)
                    intent.putExtra("title", article!!.title)
                    intent.putExtra("desc", article.desc)
                    intent.putExtra("content", article.content)
                    startActivity(intent)
                }
            })
        if (rvArticles != null) {
            rvArticles.setAdapter(adapter)
            rvArticles.setLayoutManager(LinearLayoutManager(getContext()))

            rvArticles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: kotlin.Int,
                    dy: kotlin.Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager: LinearLayoutManager? =
                        recyclerView.getLayoutManager() as LinearLayoutManager?
                    if (layoutManager != null && !isLoading) {
                        val totalItemCount: kotlin.Int = layoutManager.getItemCount()
                        val lastVisibleItem: kotlin.Int =
                            layoutManager.findLastVisibleItemPosition()
                        if (totalItemCount <= (lastVisibleItem + 5)) {
                            viewModel!!.loadArticles(true)
                        }
                    }
                }
            })
        }
    }

    private fun setupViewModel() {
        if (getActivity() != null) {
            viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
            viewModel!!.getArticles().observe(
                getViewLifecycleOwner(),
                { articlesList: kotlin.collections.MutableList<Article?> ->
                    this.updateArticles(
                        articlesList
                    )
                })
            viewModel!!.getIsLoading().observe(
                getViewLifecycleOwner(),
                { loading: kotlin.Boolean -> this.showLoading(loading) })
            viewModel!!.getErrorMessage().observe(
                getViewLifecycleOwner(),
                { message: kotlin.String? -> this.showError(message) })

            // Initial load
            viewModel!!.loadArticles(false)
        }
    }

    private fun updateArticles(articlesList: kotlin.collections.MutableList<Article?>) {
        if (adapter != null) {
            adapter!!.updateData(articlesList)
        }
        isLoading = false
    }

    private fun showLoading(loading: kotlin.Boolean) {
        if (progressBar != null) {
            progressBar!!.setVisibility(if (loading) android.view.View.VISIBLE else android.view.View.GONE)
        }
        isLoading = loading
    }

    private fun showError(message: kotlin.String?) {
        if (message != null && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (rvArticles != null) {
            rvArticles.clearOnScrollListeners()
        }
        rvArticles = null
        adapter = null
        progressBar = null
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}