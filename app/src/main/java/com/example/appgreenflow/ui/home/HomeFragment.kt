package com.example.appgreenflow.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R

class HomeFragment : Fragment() {
    private var rvArticles: RecyclerView? = null
    private var adapter: ArticleAdapter? = null
    private var viewModel: HomeViewModel? = null
    private var progressBar: ProgressBar? = null
    private var btnAddSampleData: Button? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rvArticles = view.findViewById(R.id.rvArticles)
        progressBar = view.findViewById(R.id.progressBar)
        btnAddSampleData = view.findViewById(R.id.btnAddSampleData)

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
        setupSampleDataButton()

        return view
    }

    private fun setupRecyclerView() {
        val recyclerView = rvArticles ?: return
        adapter = ArticleAdapter(
            mutableListOf<Article>(),
            object : ArticleAdapter.OnArticleClickListener {
                override fun onArticleClick(article: Article) {
                    // Chuyển đến ArticleDetailFragment
                    val detailFragment = ArticleDetailFragment.newInstance(
                        article.title,
                        article.desc,
                        article.content,
                        article.imageUrl
                    )
                    
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_home, detailFragment)
                        .addToBackStack(null)
                        .commit()
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

    private fun setupSampleDataButton() {
        btnAddSampleData?.setOnClickListener {
            SampleDataHelper.addSampleArticles()
            Toast.makeText(context, "Đang thêm dữ liệu mẫu...", Toast.LENGTH_SHORT).show()
            
            // Refresh sau 2 giây
            view?.postDelayed({
                viewModel?.refreshArticles()
            }, 2000)
        }
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