package com.example.appgreenflow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.appgreenflow.ui.home.HomeFragment;
import com.example.appgreenflow.ui.notifications.NotificationsFragment;
import com.example.appgreenflow.ui.policy.PolicyFragment;
import com.example.appgreenflow.ui.route.RouteFragment;
import com.example.appgreenflow.ui.settings.SettingsFragment;
import com.example.appgreenflow.ui.support.SupportFragment;  // Thêm import
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ImageButton drawerToggleBtn;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String userRole = "customer";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("GreenFlow");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerToggleBtn = findViewById(R.id.drawerToggleBtn);
        navigationView = findViewById(R.id.navigationView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        drawerToggleBtn.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        setupHeader();
        checkUserRole();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView textUserName = headerView.findViewById(R.id.textUserName);
            TextView emailUser = headerView.findViewById(R.id.emailUser);
            ImageView userImage = headerView.findViewById(R.id.userImage);

            if (user != null) {
                emailUser.setText(user.getEmail() != null ? user.getEmail() : "No email");
                textUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : (user.getEmail() != null ? user.getEmail() : "User"));
                userImage.setImageResource(R.drawable.outline_account);
            }
        }
    }

    private void checkUserRole() {
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userRole = documentSnapshot.getString("role") != null ? documentSnapshot.getString("role") : "customer";
                // Customize menu theo role
                navigationView.getMenu().findItem(R.id.nav_support).setVisible("employee".equals(userRole));
                Toast.makeText(this, "Chào " + userRole + "!", Toast.LENGTH_SHORT).show();

                // Subscribe FCM topic cho employee (sau khi load role)
                if ("employee".equals(userRole)) {
                    FirebaseMessaging.getInstance().subscribeToTopic("employee")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Đã subscribe thông báo nhân viên!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Lỗi subscribe FCM!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi load role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        Class<?> fragmentClass = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            fragmentClass = HomeFragment.class;
        } else if (itemId == R.id.nav_route) {
            fragmentClass = RouteFragment.class;
        } else if (itemId == R.id.nav_notifications) {
            fragmentClass = NotificationsFragment.class;
        } else if (itemId == R.id.nav_support && "employee".equals(userRole)) {
            fragmentClass = SupportFragment.class;
        } else if (itemId == R.id.nav_consumer_policy) {
            fragmentClass = PolicyFragment.class;
        } else if (itemId == R.id.nav_settings) {
            fragmentClass = SettingsFragment.class;
        } else if (itemId == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, Login.class));
            finish();
            return true;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi load fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (fragment != null) {
            loadFragment(fragment);
            navigationView.setCheckedItem(itemId);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_home, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    public void performLogout() {
        mAuth.signOut();
        startActivity(new Intent(this, Login.class));
        finish();
    }

    public void loadRouteFragment(String area, double lat, double lng, int percent) {
        Bundle args = new Bundle();
        args.putString("area", area);
        args.putDouble("lat", lat);
        args.putDouble("lng", lng);
        args.putInt("percent", percent);

        RouteFragment fragment = new RouteFragment();
        fragment.setArguments(args);

        loadFragment(fragment);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        Toast.makeText(this, "Chuyển đến vị trí thùng rác " + area, Toast.LENGTH_SHORT).show();
    }

    // Getter cho role
    public String getUserRole() {
        return userRole;
    }
}