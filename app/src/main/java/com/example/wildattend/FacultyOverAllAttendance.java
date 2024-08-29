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
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FacultyOverAllAttendance extends Fragment {
    private static final String TAG = "FacultyOverAllAttendance";
    private ImageView profileImageView;
    private TextView facultyNameTextView;
    private TableLayout tableLayout;
    private Button prevPageButton, nextPageButton;
    private SearchView searchView;

    private List<DocumentSnapshot> allRecords = new ArrayList<>();
    private int currentPage = 0;
    private final int pageSize = 5;
    private int totalPages = 0;

    public FacultyOverAllAttendance() {
        // Required empty public constructor
    }

    public static FacultyOverAllAttendance newInstance() {
        return new FacultyOverAllAttendance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_over_all_attendance, container, false);
        tableLayout = rootView.findViewById(R.id.attendanceTable);
        rootView.findViewById(R.id.backButtonChangePassword).setOnClickListener(v -> requireActivity().onBackPressed()); // Handle back button click
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.profilepicture);
        facultyNameTextView = view.findViewById(R.id.facultyname);
        prevPageButton = view.findViewById(R.id.prevPageButton);
        nextPageButton = view.findViewById(R.id.nextPageButton);
        searchView = view.findViewById(R.id.searchView);

        // Set click listeners for pagination buttons
        prevPageButton.setOnClickListener(v -> onPrevPage());
        nextPageButton.setOnClickListener(v -> onNextPage());

        fetchUserInformation();
        setupTable();

        // Set up search functionality
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
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String imageUrl = documentSnapshot.getString("img");

                            facultyNameTextView.setText(firstName + " " + lastName);

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

    private void setupTable() {
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

                            tableLayout.removeAllViews(); // Clear existing rows

                            // Add header row
                            TableRow headerRow = new TableRow(requireContext());
                            headerRow.addView(createTextView("Course"));
                            headerRow.addView(createTextView("Day"));
                            headerRow.addView(createTextView("Date"));
                            headerRow.addView(createTextView("Status"));
                            tableLayout.addView(headerRow);

                            // Initialize pagination data
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
        }

        // Calculate start and end indices for pagination
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, allRecords.size());

        // Add data rows
        for (int i = start; i < end; i++) {
            DocumentSnapshot documentSnapshot = allRecords.get(i);
            String className = documentSnapshot.getString("className");
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

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecords(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecords(newText);
                return false;
            }
        });
    }

    private void filterRecords(String query) {
        List<DocumentSnapshot> filteredRecords = new ArrayList<>();

        for (DocumentSnapshot document : allRecords) {
            String className = document.getString("className");
            String status = document.getString("status");
            Timestamp timeInTimestamp = document.getTimestamp("timeIn");

            if (className != null && status != null && timeInTimestamp != null) {
                String day = getDayFromTimestamp(timeInTimestamp);
                String date = formatTimestamp(timeInTimestamp);

                // Check if any field contains the query string
                if (containsIgnoreCase(className, query) || containsIgnoreCase(day, query)
                        || containsIgnoreCase(date, query) || containsIgnoreCase(status, query)) {
                    filteredRecords.add(document);
                }
            }
        }

        displayFilteredResults(filteredRecords);
    }

    private void displayFilteredResults(List<DocumentSnapshot> filteredRecords) {
        // Clear previous rows except header
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        // Add filtered rows
        for (DocumentSnapshot documentSnapshot : filteredRecords) {
            String className = documentSnapshot.getString("className");
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
    }

    private boolean containsIgnoreCase(String str, String search) {
        return str != null && search != null && str.toLowerCase().contains(search.toLowerCase());
    }

    private String getDayFromTimestamp(Timestamp timestamp) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        return dayFormat.format(timestamp.toDate());
    }

    private String formatTimestamp(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(timestamp.toDate());
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }
}
