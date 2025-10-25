package com.example.appgreenflow.ui.policy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;

public class PolicyFragment extends Fragment {

    private PolicyViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PolicyViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_policy, container, false);

        TextView tvPolicy = rootView.findViewById(R.id.tv_policy);
        ScrollView scrollView = rootView.findViewById(R.id.scrollView);

        String role = ((MainActivity) requireActivity()).getUserRole();
        mViewModel.loadPolicy(role);  // Load dynamic policy từ Firestore

        // Observe LiveData để update text khi data load xong
        mViewModel.getPolicyText().observe(getViewLifecycleOwner(), policyText -> {
            if (policyText != null && tvPolicy != null) {
                tvPolicy.setText(policyText);
            } else if (tvPolicy != null) {
                tvPolicy.setText("Đang tải chính sách...");  // Placeholder
            }
        });

        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
        }
        return rootView;
    }
}