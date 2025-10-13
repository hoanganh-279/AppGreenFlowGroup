package com.example.appgreenflow.ui.route;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.R;

public class RouteFragment extends Fragment {

    private RouteViewModel mViewModel;
    private ImageView mapImage;
    private ImageView markerRed;
    private String selectedArea;
    private float selectedX, selectedY;
    private int selectedPercent;

    public static RouteFragment newInstance() {
        return new RouteFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RouteViewModel.class);

        if (getArguments() != null) {
            selectedArea = getArguments().getString("area");
            selectedX = getArguments().getFloat("x");
            selectedY = getArguments().getFloat("y");
            selectedPercent = getArguments().getInt("percent");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);
        mapImage = rootView.findViewById(R.id.mapImage);
        markerRed = rootView.findViewById(R.id.markerRed);

        if (selectedArea != null) {
            showRedMarker();
        } else {
            markerRed.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void showRedMarker() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) selectedX;
        params.topMargin = (int) selectedY;
        markerRed.setLayoutParams(params);

        markerRed.setVisibility(View.VISIBLE);
        markerRed.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Thùng rác " + selectedArea + " đầy " + selectedPercent + "%", Toast.LENGTH_SHORT).show();
        });

        Toast.makeText(getContext(), "Hiển thị vị trí thùng rác " + selectedArea, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (markerRed != null) {
            markerRed.setVisibility(View.GONE);
        }
    }
}