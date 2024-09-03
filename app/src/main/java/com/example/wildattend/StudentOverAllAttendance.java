package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StudentOverAllAttendance extends Fragment {
    private static final String TAG = "StudentOverAllAttendance";
    private ImageView profileImageView;
    private TextView studentNameTextView;
    private SearchView searchView;
    private TableLayout tableLayout;
    private Button prevPageButton, nextPageButton;

    private List<DocumentSnapshot> allRecords = new ArrayList<>();
    private List<DocumentSnapshot> displayedRecords = new ArrayList<>();
    private int currentPage = 0;
    private final int pageSize = 5;
    private int totalPages = 0;

    public static StudentOverAllAttendance newInstance(String param1, String param2) {
        StudentOverAllAttendance fragment = new StudentOverAllAttendance();
        Bundle args = new Bundle();
        args.putString("PARAM_1_KEY", param1);
        args.putString("PARAM_2_KEY", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_overallattendance, container, false);
        tableLayout = rootView.findViewById(R.id.student_attendanceTable);
        searchView = rootView.findViewById(R.id.studentsearchView);
        prevPageButton = rootView.findViewById(R.id.prevPageButton);
        nextPageButton = rootView.findViewById(R.id.nextPageButton);

        prevPageButton.setOnClickListener(v -> onPrevPage());
        nextPageButton.setOnClickListener(v -> onNextPage());

        rootView.findViewById(R.id.backButtonChangePassword).setOnClickListener(v -> requireActivity().onBackPressed());
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.profilepicture);
        studentNameTextView = view.findViewById(R.id.studentname);

        fetchUserInformation();
        fetchAttendanceRecords();

        setupSearchView();
    }

    private void fetchUserInformation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String imageUrl = documentSnapshot.getString("img");

                            studentNameTextView.setText(firstName + " " + lastName);

                            new LoadImageTask(profileImageView).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user document", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public LoadImageTask(ImageView imageView) {
            this.imageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[0];
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e("LoadImageTask", "Error loading image from URL", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference != null && bitmap != null) {
                ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void fetchAttendanceRecords() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("attendRecord")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Toast.makeText(requireContext(), "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error fetching attendance data", e);
                                return;
                            }

                            if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                                Toast.makeText(requireContext(), "No data available", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "No attendance records found");
                                return;
                            }

                            Log.d(TAG, "Data fetched successfully, count: " + queryDocumentSnapshots.size());

                            allRecords.clear();
                            allRecords.addAll(queryDocumentSnapshots.getDocuments());
                            totalPages = (int) Math.ceil((double) allRecords.size() / pageSize);

                            updateTable();
                        }
                    });
        } else {
            Log.e(TAG, "No current user found.");
        }
    }

    private void updateTable() {
        if (tableLayout == null) {
            Log.e(TAG, "TableLayout is not initialized.");
            return;
        }

        // Remove all child views from TableLayout except the header
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        } else if (childCount == 1) {
            // No data rows to remove, only the header row exists
            tableLayout.removeAllViews();
        }

        // Add header row
        addHeaderRow();

        // Calculate start and end indices for pagination
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, allRecords.size());

        // Add data rows
        for (int i = start; i < end; i++) {
            DocumentSnapshot documentSnapshot = allRecords.get(i);
            String className = documentSnapshot.getString("classCode"); // Assuming field name is classCode
            String status = documentSnapshot.getString("status");
            Timestamp timeInTimestamp = documentSnapshot.getTimestamp("timeIn");
            String timeIn = formatTimestamp(timeInTimestamp);

            TableRow row = new TableRow(requireContext());
            row.addView(createTextView(className));
            row.addView(createTextView(getDayFromTimestamp(timeInTimestamp)));
            row.addView(createTextView(timeIn));
            row.addView(createTextView(status));

            tableLayout.addView(row);
        }

        // Update button states
        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < totalPages - 1);
    }

    private void addHeaderRow() {
        TableRow headerRow = new TableRow(requireContext());

        headerRow.addView(createTextView("Course"));
        headerRow.addView(createTextView("Day"));
        headerRow.addView(createTextView("Date"));
        headerRow.addView(createTextView("Status"));

        tableLayout.addView(headerRow, 0); // Add header row at index 0
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecords(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecords(newText);
                return true;
            }
        });
    }

    private void filterRecords(String query) {
        displayedRecords.clear();
        for (DocumentSnapshot document : allRecords) {
            String className = document.getString("classCode");
            String status = document.getString("status");
            Timestamp timeInTimestamp = document.getTimestamp("timeIn");
            String day = getDayFromTimestamp(timeInTimestamp);
            String date = formatTimestamp(timeInTimestamp);

            if (className.toLowerCase().contains(query.toLowerCase())
                    || status.toLowerCase().contains(query.toLowerCase())
                    || day.toLowerCase().contains(query.toLowerCase())
                    || date.toLowerCase().contains(query.toLowerCase())) {
                displayedRecords.add(document);
            }
        }

        currentPage = 0;
        totalPages = (int) Math.ceil((double) displayedRecords.size() / pageSize);
        updateTable();
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp.toDate());
    }

    private String getDayFromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        return sdf.format(timestamp.toDate());
    }

    private void onPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTable();
        }
    }

    private void onNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTable();
        }
    }
}
