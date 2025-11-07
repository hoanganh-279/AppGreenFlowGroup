package com.example.appgreenflow.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.appgreenflow.R

class ArticleDetailFragment : Fragment() {
    private var tvTitle: TextView? = null
    private var tvContent: TextView? = null
    private var ivImage: ImageView? = null
    private var btnBack: ImageButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_detail, container, false)
        
        tvTitle = view.findViewById(R.id.tvDetailTitle)
        tvContent = view.findViewById(R.id.tvDetailContent)
        ivImage = view.findViewById(R.id.ivDetailImage)
        btnBack = view.findViewById(R.id.btnBack)
        
        setupViews()
        return view
    }

    private fun setupViews() {
        val args = arguments
        if (args != null) {
            tvTitle?.text = args.getString("title", "Không có tiêu đề")
            tvContent?.text = args.getString("content", "Không có nội dung")
            
            val imageUrl = args.getString("imageUrl")
            if (!imageUrl.isNullOrEmpty()) {
                context?.let { ctx ->
                    Glide.with(ctx)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image)
                        .into(ivImage!!)
                }
            }
        }

        btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        fun newInstance(title: String?, desc: String?, content: String?, imageUrl: String?): ArticleDetailFragment {
            val fragment = ArticleDetailFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("desc", desc)
            args.putString("content", content)
            args.putString("imageUrl", imageUrl)
            fragment.arguments = args
            return fragment
        }
    }
}