package com.example.wildattend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentHome extends Fragment {

    private static final String TAG = "StudentHome";

    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_home, container, false);
        listView = rootView.findViewById(R.id.list_view_schedule);
        setupListView();
        fetchUserClasses();
        return rootView;
    }

    private void setupListView() {
        List<String> scheduleItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, scheduleItems);
        listView.setAdapter(adapter);

        // Set item click listener to navigate to class schedule
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = adapter.getItem(position);
            if (selectedItem != null) {
                // Extract class code from the selected item
                String classCode = selectedItem.split(" - ")[0];

                // Navigate to the detailed schedule of the selected class
                navigateToClassSchedule(classCode);
            }
        });
    }

    private void fetchUserClasses() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("userClasses")
                    .whereEqualTo("userID", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String classID = documentSnapshot.getString("classID");
                            fetchClassDetails(classID);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user classes", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }

    private void fetchClassDetails(String classID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("classes")
                .document(classID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String classCode = documentSnapshot.getString("classCode");
                        String classDesc = documentSnapshot.getString("classDesc");
                        String startTime = documentSnapshot.getString("startTime");
                        String endTime = documentSnapshot.getString("endTime");

                        adapter.add(classCode + " - " + classDesc + " (" + startTime + " - " + endTime + ")");
                    } else {
                        Log.e(TAG, "Class document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching class details", e);
                });
    }

    private void navigateToClassSchedule(String classCode) {
        // Create instance of StudentScheduleClass fragment and pass class code as argument
        StudentScheduleClass studentScheduleClassFragment = StudentScheduleClass.newInstance(classCode, null);

        // Navigate to the StudentScheduleClass fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, studentScheduleClassFragment)
                .addToBackStack(null)
                .commit();
    }
}
