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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_policy, container, false);

        TextView tvPolicy = rootView.findViewById(R.id.tv_policy);
        ScrollView scrollView = rootView.findViewById(R.id.scrollView);

        String policyText = mViewModel.getPolicyText().getValue();
        if (policyText != null && tvPolicy != null) {
            tvPolicy.setText(policyText);
        }

        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
        }
        return rootView;
    }
}