package com.example.appgreenflow.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R
import com.example.appgreenflow.ui.home.Article  // Assuming Article is in data package; adjust import as needed
import com.example.appgreenflow.ui.ArticleDetailActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var rvArticles: RecyclerView? = null
    private var adapter: ArticleAdapter? = null
    private var viewModel: HomeViewModel? = null
    private var progressBar: ProgressBar? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rvArticles = view.findViewById(R.id.rvArticles)
        progressBar = view.findViewById(R.id.progressBar)

        // Fallback if XML not synced (rare)
        if (progressBar == null) {
            val recyclerView = rvArticles ?: return view
            progressBar = ProgressBar(context)
            (recyclerView.parent as? ViewGroup)?.addView(
                progressBar,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            progressBar?.visibility = View.GONE
        }

        setupRecyclerView()
        setupViewModel()

        return view
    }

    private fun setupRecyclerView() {
        val recyclerView = rvArticles ?: return
        adapter = ArticleAdapter(
            mutableListOf<Article>(),
            object : ArticleAdapter.OnArticleClickListener {  // Explicit object for SAM interface
                override fun onArticleClick(article: Article) {
                    context?.let { ctx ->
                        val intent = Intent(ctx, ArticleDetailActivity::class.java)
                        intent.putExtra("title", article.title)
                        intent.putExtra("desc", article.desc)
                        intent.putExtra("content", article.content)
                        startActivity(intent)
                    }
                }
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (!isLoading) {
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    if (totalItemCount <= lastVisibleItem + 5) {
                        viewModel?.loadArticles(true)
                    }
                }
            }
        })
    }

    private fun setupViewModel() {
        activity ?: return
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel?.getArticles()?.observe(
            viewLifecycleOwner
        ) { articlesList: MutableList<Article?>? ->
            val nonNullArticles = articlesList?.filterNotNull() ?: emptyList()
            updateArticles(nonNullArticles)
        }
        viewModel?.getIsLoading()?.observe(
            viewLifecycleOwner
        ) { loading: Boolean? ->
            showLoading(loading ?: false)
        }
        viewModel?.getErrorMessage()?.observe(
            viewLifecycleOwner
        ) { message: String? ->
            showError(message)
        }

        // Initial load
        viewModel?.loadArticles(false)
    }

    private fun updateArticles(articlesList: List<Article>) {
        adapter?.updateData(articlesList)
        isLoading = false
    }

    private fun showLoading(loading: Boolean) {
        progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        isLoading = loading
    }

    private fun showError(message: String?) {
        if (message != null) {
            context?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvArticles?.clearOnScrollListeners()
        rvArticles = null
        adapter = null
        progressBar = null
    }

    @Suppress("UNUSED")  // Suppress warning if not used elsewhere
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}