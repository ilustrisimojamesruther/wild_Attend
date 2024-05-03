package com.example.wildattend;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentSchedule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentSchedule extends Fragment {
    private ListView listView;
    private ArrayAdapter<String> adapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudentSchedule() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentSchedule.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentSchedule newInstance(String param1, String param2) {
        StudentSchedule fragment = new StudentSchedule();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_student_schedule, container, false);
        listView = rootView.findViewById(R.id.list_view_schedule);
        setupListView();
        return rootView;
    }

    private void setupListView() {
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

//    private void setupListView() {
//        adapter = new ArrayAdapter<>(requireContext(), R.layout.list_class_schedule, R.id.course_code, scheduleItems);
//        listView.setAdapter(adapter);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(requireContext(), StudentScheduleClass.class);
//                intent.putExtra("key", scheduleItems[position]);
//                startActivity(intent);
//            }
//        });
//    }
}