package com.example.appgreenflow.ui.policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R

class PolicyFragment : Fragment() {
    private var mViewModel: PolicyViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get<PolicyViewModel>(PolicyViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_policy, container, false)

        val tvPolicy = rootView.findViewById<TextView?>(R.id.tv_policy)
        val scrollView = rootView.findViewById<ScrollView?>(R.id.scrollView)

        val role = (requireActivity() as MainActivity).getUserRole()
        mViewModel!!.loadPolicy(role)

        mViewModel!!.getPolicyText()
            .observe(getViewLifecycleOwner(), Observer { policyText: String? ->
                if (policyText != null && tvPolicy != null) {
                    tvPolicy.setText(policyText)
                } else if (tvPolicy != null) {
                    tvPolicy.setText("Đang tải chính sách...")
                }
            })

        if (scrollView != null) {
            scrollView.post(Runnable { scrollView.fullScroll(View.FOCUS_UP) })
        }
        return rootView
    }
}