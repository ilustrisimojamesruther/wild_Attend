package com.example.wildattend;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StudentOverAllAttendance extends Fragment {
    private ListView listView;
    private ArrayAdapter<String> adapter;

    // Rename newInstance parameters and their keys
    public static StudentOverAllAttendance newInstance(String param1, String param2) {
        StudentOverAllAttendance fragment = new StudentOverAllAttendance();
        Bundle args = new Bundle();
        args.putString("PARAM_1_KEY", param1); // Changed ARG_PARAM1 to PARAM_1_KEY
        args.putString("PARAM_2_KEY", param2); // Changed ARG_PARAM2 to PARAM_2_KEY
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_overallattendance, container, false);
        listView = rootView.findViewById(R.id.list_view_attendance);
        setupListView();
        return rootView;
    }

    private void setupListView() {
        String[] overallAttendanceItems = {"ABC123", "DEF456", "GHI789", "JKL012", "MNO123"};

        // Changed context to requireContext()
        adapter = new ArrayAdapter<>(requireContext(), R.layout.list_student_attendance, R.id.course_codes, overallAttendanceItems); // Changed R.layout.list_student_attendance to R.layout.list_item

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Replace with the fragment you want to navigate to
            Fragment fragment = new StudentSchedule();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}