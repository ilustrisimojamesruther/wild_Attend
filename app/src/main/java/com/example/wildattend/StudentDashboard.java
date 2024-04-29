package com.example.wildattend;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.wildattend.databinding.ActivityMainBinding;
import com.example.wildattend.databinding.ActivityStudentDashboardBinding;

public class StudentDashboard extends AppCompatActivity {

    ActivityStudentDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new StudentHome());

        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.student_home:
                    replaceFragment(new StudentHome());
                    break;
                case R.id.student_schedule:
                    replaceFragment(new StudentSchedule());
                    break;
                case R.id.student_profile:
                    replaceFragment(new StudentProfile());
                    break;
            }
            return true;
        });


    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}