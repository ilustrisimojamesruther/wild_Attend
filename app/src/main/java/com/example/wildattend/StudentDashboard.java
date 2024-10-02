package com.example.wildattend;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.wildattend.databinding.ActivityStudentDashboardBinding;

public class StudentDashboard extends AppCompatActivity {

    ActivityStudentDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initially replace with the StudentHome fragment
        replaceFragment(new StudentHome());

        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.student_home:
                    replaceFragment(new StudentHome());
                    break;
                case R.id.student_schedule:
                    // Replace with the StudentSchedule fragment
                    replaceFragment(new StudentSchedule());
                    break;
                case R.id.student_profile:
                    // Replace with the StudentProfile fragment
                    replaceFragment(new StudentProfile());
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
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        // Commit the transaction
        fragmentTransaction.commit();
    }
    public void hideBottomNavigation() {
        binding.bottomNavigationView.setVisibility(View.GONE);  // Hides the bottom navbar
    }

    // Method to show the BottomNavigationView
    public void showBottomNavigation() {
        binding.bottomNavigationView.setVisibility(View.VISIBLE);  // Shows the bottom navbar
    }
}
