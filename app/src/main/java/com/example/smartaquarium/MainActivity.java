package com.example.smartaquarium;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private FirebaseAuth auth;


    void Init()
    {
        bottomNav = findViewById(R.id.bottom_navigation);
        auth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        Init();
        setupBottomNavigation();


        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Show login first
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new LoginFragment())
                    .commit();
        } else {
            // Show main app
            loadFragment(new DashboardFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.navigation_dashboard) {
                selected = new DashboardFragment();
            } else if (id == R.id.navigation_aquarium) {
                selected = new AquariumFragment();
            } else if (id == R.id.navigation_analytics) {
                selected = new AnalyticsFragment();
            } else if (id == R.id.navigation_settings) {
                selected = new SettingsFragment();
            }

            if (selected != null) {
                loadFragment(selected);
            }
            return true;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}
