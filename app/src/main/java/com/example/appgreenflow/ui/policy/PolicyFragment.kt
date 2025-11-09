package com.example.appgreenflow.ui.policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R

class PolicyFragment : Fragment() {
    private var mViewModel: PolicyViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[PolicyViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_policy, container, false)

        val tvPolicy: TextView = rootView.findViewById(R.id.tv_policy)
        val scrollView: ScrollView = rootView.findViewById(R.id.scrollView)

        val role = (requireActivity() as MainActivity).userRole.orEmpty()
        mViewModel?.loadPolicy(role)

        mViewModel?.getPolicyText()?.observe(
            viewLifecycleOwner
        ) { policyText: String? ->
            tvPolicy.text = policyText ?: "Đang tải chính sách..."
        }

        scrollView.post { scrollView.fullScroll(View.FOCUS_UP) }
        
        // Thêm chat button
        activity?.let { act ->
            com.example.appgreenflow.ChatHelper.addChatButton(act)
        }
        
        return rootView
    }
}