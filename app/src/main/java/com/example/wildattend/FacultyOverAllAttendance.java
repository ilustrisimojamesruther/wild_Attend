package com.example.wildattend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FacultyOverAllAttendance extends Fragment {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> attendanceItems;

    public FacultyOverAllAttendance() {
        // Required empty public constructor
    }

    public static FacultyOverAllAttendance newInstance() {
        return new FacultyOverAllAttendance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_over_all_attendance, container, false);
        listView = rootView.findViewById(R.id.list_view_attendance);
        rootView.findViewById(R.id.backButtonChangePassword).setOnClickListener(v -> requireActivity().onBackPressed()); // Handle back button click
        setupListView();
        return rootView;
    }

    private void setupListView() {
        attendanceItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, attendanceItems);
        listView.setAdapter(adapter);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("attendRecord")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(requireContext(), "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            return;
                        }

                        attendanceItems.clear();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String className = documentSnapshot.getString("className");
                            Timestamp timeInTimestamp = documentSnapshot.getTimestamp("timeIn");
                            String timeIn = formatTimestamp(timeInTimestamp);
                            Timestamp timeOutTimestamp = documentSnapshot.getTimestamp("timeOut");
                            String timeOut = formatTimestamp(timeOutTimestamp);
                            String message = documentSnapshot.getString("message");
                            String status = documentSnapshot.getString("status");

                            String attendanceEntry = "Class: " + className + "\n"
                                    + "Time In: " + timeIn + "\n"
                                    + "Time Out: " + timeOut + "\n"
                                    + "Message: " + message + "\n"
                                    + "Status: " + status;

                            attendanceItems.add(attendanceEntry);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a");
            return sdf.format(timestamp.toDate());
        } else {
            return "";
        }
    }
}
