package com.example.appgreenflow.ui.policy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;

public class PolicyFragment extends Fragment {

    private PolicyViewModel mViewModel;
    private TextView tvPolicy;
    private ScrollView scrollView;

    public static PolicyFragment newInstance() {
        return new PolicyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PolicyViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_policy, container, false);

        tvPolicy = rootView.findViewById(R.id.tv_policy);
        scrollView = rootView.findViewById(R.id.scrollView);

        String role = ((MainActivity) requireActivity()).getUserRole();
        String policyText = mViewModel.getPolicyText(role).getValue();  // Truyền role vào ViewModel
        if (policyText != null && tvPolicy != null) {
            tvPolicy.setText(policyText);
        }

        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
        }
        return rootView;
    }
}