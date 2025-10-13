package com.example.appgreenflow.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;
import com.example.appgreenflow.ui.route.RouteFragment;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel mViewModel;

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);  // Sửa tên layout nếu cần
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CardView cardA = view.findViewById(R.id.cardTrashA);
        CardView cardB = view.findViewById(R.id.cardTrashB);
        CardView cardC = view.findViewById(R.id.cardTrashC);
        CardView cardD = view.findViewById(R.id.cardTrashD);
        CardView cardE = view.findViewById(R.id.cardTrashE);
        CardView cardF = view.findViewById(R.id.cardTrashF);

        cardA.setOnClickListener(v -> navigateToTrashLocation("A", 200f, 150f, 80));  // Pixel x,y thay lat/lon
        cardB.setOnClickListener(v -> navigateToTrashLocation("B", 350f, 120f, 90));
        cardC.setOnClickListener(v -> navigateToTrashLocation("C", 150f, 200f, 75));
        cardD.setOnClickListener(v -> navigateToTrashLocation("D", 450f, 180f, 95));
        cardE.setOnClickListener(v -> navigateToTrashLocation("E", 250f, 250f, 85));
        cardF.setOnClickListener(v -> navigateToTrashLocation("F", 400f, 220f, 70));
    }

    private void navigateToTrashLocation(String area, float x, float y, int percent) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.loadRouteFragment(area, x, y, percent);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không thể chuyển sang bản đồ", Toast.LENGTH_SHORT).show();
        }
    }
}