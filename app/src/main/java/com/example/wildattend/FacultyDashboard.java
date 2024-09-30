package com.example.wildattend;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.wildattend.databinding.ActivityFacultyDashboardBinding;

public class FacultyDashboard extends AppCompatActivity {

    ActivityFacultyDashboardBinding binding2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding2 = ActivityFacultyDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding2.getRoot());

        // Initially replace with the StudentHome fragment
        replaceFragment(new FacultyHome());

        binding2.facultyBottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.faculty_home:
                    replaceFragment(new FacultyHome());
                    break;
                case R.id.faculty_schedule:
                    replaceFragment(new FacultySchedule());
                    break;
                case R.id.faculty_profile:
                    replaceFragment(new FacultyProfile());
                    break;
            }
            return true;
        });


    }

    private void replaceFragment(Fragment fragment){
        // Get the FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Begin a FragmentTransaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the current fragment with the new one
        fragmentTransaction.replace(R.id.faculty_frame_layout, fragment);
        // Commit the transaction
        fragmentTransaction.commit();
    }
    // Method to hide the BottomNavigationView
    public void hideBottomNavigation() {
        binding2.facultyBottomNavigationView.setVisibility(View.GONE);  // Hides the bottom navbar
    }

    // Method to show the BottomNavigationView
    public void showBottomNavigation() {
        binding2.facultyBottomNavigationView.setVisibility(View.VISIBLE);  // Shows the bottom navbar
    }
}