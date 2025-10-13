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

import com.example.appgreenflow.ui.home.HomeFragment;  // Import HomeFragment
import com.example.appgreenflow.ui.notifications.NotificationsFragment;
import com.example.appgreenflow.ui.policy.PolicyFragment;
import com.example.appgreenflow.ui.route.RouteFragment;
import com.example.appgreenflow.ui.settings.SettingsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ImageButton drawerToggleBtn;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("GreenFlow");  // Title cho Toolbar
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerToggleBtn = findViewById(R.id.drawerToggleBtn);
        navigationView = findViewById(R.id.navigationView);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Set click cho nút menu (null check)
        if (drawerToggleBtn != null) {
            drawerToggleBtn.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView textUserName = headerView.findViewById(R.id.textUserName);
            TextView emailUser = headerView.findViewById(R.id.emailUser);
            ImageView userImage = headerView.findViewById(R.id.userImage);

            if (user != null) {
                if (emailUser != null) {
                    emailUser.setText(user.getEmail() != null ? user.getEmail() : "No email");
                }

                if (textUserName != null) {
                    String displayName = user.getDisplayName();
                    textUserName.setText(displayName != null ? displayName : (user.getEmail() != null ? user.getEmail() : "User"));
                }

                if (userImage != null) {
                    userImage.setImageResource(R.drawable.outline_account);
                }
            }
        }

        // Load Home fragment mặc định nếu chưa có
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_route) {
            fragment = new RouteFragment();
            Toast.makeText(this, "Route Clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_notifications) {
            fragment = new NotificationsFragment();
            Toast.makeText(this, "Notification Clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_consumer_policy) {
            fragment = new PolicyFragment();
            Toast.makeText(this, "Consumer Policy Clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return true;
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
        transaction.addToBackStack(null);  // Tránh chồn fragment
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
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    public void performLogout() {
        mAuth.signOut();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void loadRouteFragment(String area, float x, float y, int percent) {
        Bundle args = new Bundle();
        args.putString("area", area);
        args.putFloat("x", x);
        args.putFloat("y", y);
        args.putInt("percent", percent);

        RouteFragment fragment = new RouteFragment();
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_home, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        Toast.makeText(this, "Chuyển đến vị trí thùng rác " + area, Toast.LENGTH_SHORT).show();
    }
}