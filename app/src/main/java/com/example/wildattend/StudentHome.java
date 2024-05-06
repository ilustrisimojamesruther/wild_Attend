// StudentHome.java

package com.example.wildattend;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StudentHome extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    public StudentHome() {
        // Required empty public constructor
    }

    public static StudentHome newInstance(String param1, String param2) {
        StudentHome fragment = new StudentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_home, container, false);
        listView = view.findViewById(R.id.list_view_schedule);
        setupListView();
        return view;
    }

    public void setupListView() {
        // Sample data for schedule items
        String[] scheduleItems = {"ABC123", "DEF456", "GHI789", "JKL012", "MNO123"};

        // Create an ArrayAdapter to display the schedule items
        adapter = new ArrayAdapter<>(requireContext(), R.layout.list_class_schedule, R.id.course_code, scheduleItems);

        // Set the adapter to the ListView
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Replace with the fragment you want to navigate to
            Fragment fragment = new StudentScheduleClass();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
