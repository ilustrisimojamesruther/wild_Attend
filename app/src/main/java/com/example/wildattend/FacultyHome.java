package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FacultyHome extends Fragment {

    private TextView upcomingTimeTextView;
    private TextView upcomingCourseTextView;
    private MaterialTextView courseView;

    private ListView attendanceLogListView;
    private static final String TAG = "FacultyHome";
    private TextView facultyNameTextView;
    private ImageView profile_image;

    private FacultyProgressBar facultyProgressBar;

    private TextView percentageText;
    private TextView timeInValue;
    private TextView timeOutValue;

    public FacultyHome() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_home, container, false);

        // Initialize the views
        upcomingTimeTextView = rootView.findViewById(R.id.upcomingValue);
        upcomingCourseTextView = rootView.findViewById(R.id.courseText);
        courseView = rootView.findViewById(R.id.courseView);
        facultyNameTextView = rootView.findViewById(R.id.home_header);
        profile_image = rootView.findViewById(R.id.profile_image_faculty);
        attendanceLogListView = rootView.findViewById(R.id.attendanceLogListView);
        facultyProgressBar = rootView.findViewById(R.id.facultyProgressBar);
        percentageText = rootView.findViewById(R.id.percentageText);
        timeInValue = rootView.findViewById(R.id.timeInValue);
        timeOutValue = rootView.findViewById(R.id.timeOutValue);


        // Fetch and display the attendance progress
        fetchAttendanceProgress();

//        int progress = 80; // Example progress value
//        facultyProgressBar.setProgress(progress);
//        percentageText.setText(progress + "%");

        // Set the current date
        TextView dateTextView = rootView.findViewById(R.id.date);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Fetch and display user information
        fetchUserInformation();

        // Fetch and display the last time-in and time-out
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            fetchLastTimeInAndOut(currentUser.getUid());
        } else {
            Log.e(TAG, "User is not authenticated");
        }


        // Setup the attendance log ListView
        setupListView(inflater);

        return rootView;
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

                            // Update UI with fetched information
                            facultyNameTextView.setText("Good Morning," + " " + firstName + "!");

                            // Load the image from URL
                            new LoadImageTask(profile_image).execute(imageUrl);

                            // Fetch classes
                            fetchUserClasses(currentUser.getUid());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user document", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }


    private void fetchUserClasses(String userId) {
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
                        String formattedStartTime = formatTime(startTime); // Format the start time
                        String endTime = documentSnapshot.getString("endTime");
                        String formattedEndTime = formatTime(endTime); // Format the end time

                        // Update UI with class details
                        upcomingTimeTextView.setText(formattedStartTime + " - " + formattedEndTime);
                        upcomingCourseTextView.setText(classCode);
                        courseView.setText(classCode.substring(0, 3)); // Assuming courseView displays a short code
                    } else {
                        Log.e(TAG, "Class document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching class details", e);
                });
    }

    private String formatTime(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting time", e);
            return time; // Return original time in case of error
        }
    }


    private void setupListView(LayoutInflater inflater) {
        // Sample data for the ListView
        String[] attendanceLogs = {
                "Time In",
                "Start Class",
                "End Class",
                "Rest Break"
        };

        // Create an ArrayAdapter to display the attendance log items
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_attendance_log, R.id.actionText, attendanceLogs);

        // Inflate the header layout and add it to the ListView
        View headerView = inflater.inflate(R.layout.header_list_attendance_log, null);
        attendanceLogListView.addHeaderView(headerView);

        // Set the adapter to the ListView
        attendanceLogListView.setAdapter(adapter);
    }

    private void fetchAttendanceProgress() {
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
                                facultyProgressBar.setProgress(0);
                                percentageText.setText("0%");
                                Log.d(TAG, "No attendance records found");
                                return;
                            }

                            Log.d(TAG, "Attendance records fetched successfully, count: " + queryDocumentSnapshots.size());

                            int totalClasses = queryDocumentSnapshots.size();
                            int attendedClasses = 0;

                            // Calculate attended classes
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                String status = documentSnapshot.getString("status");
                                if ("Timed Out".equals(status)) {
                                    attendedClasses++;
                                }
                            }

                            // Calculate progress percentage
                            if (totalClasses > 0) {
                                int progress = (attendedClasses * 100) / totalClasses;
                                facultyProgressBar.setProgress(progress);
                                percentageText.setText(progress + "%");
                            } else {
                                facultyProgressBar.setProgress(0);
                                percentageText.setText("0%");
                            }
                        }
                    });
        } else {
            Log.e(TAG, "No current user found.");
        }
    }


    private void fetchLastTimeInAndOut(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("attendRecord")
                .whereEqualTo("userId", userId) // Filter by userId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot latestDocument = null;
                    Timestamp latestTimeIn = null;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp timeIn = document.getTimestamp("timeIn");
                        if (timeIn != null && (latestTimeIn == null || timeIn.toDate().after(latestTimeIn.toDate()))) {
                            latestTimeIn = timeIn;
                            latestDocument = document;
                        }
                    }

                    if (latestDocument != null) {
                        // Extract timeIn and timeOut from the latest document
                        Timestamp timeIn = latestDocument.getTimestamp("timeIn");
                        Timestamp timeOut = latestDocument.getTimestamp("timeOut");

                        // Update the UI
                        timeInValue.setText(timeIn != null ? formatTimestamp(timeIn) : "No Time In");
                        timeOutValue.setText(timeOut != null ? formatTimestamp(timeOut) : "No Time Out");
                    } else {
                        // Handle no records found
                        timeInValue.setText("No Time In");
                        timeOutValue.setText("No Time Out");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e(TAG, "Error fetching attendance records: " + e.getMessage(), e);
                    timeInValue.setText("Error");
                    timeOutValue.setText("Error");
                });
    }


    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "N/A"; // Return a fallback value if the timestamp is null
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, MMM d, yyyy", Locale.getDefault());
        return dateFormat.format(timestamp.toDate()); // Convert Timestamp to Date and format it
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
                Log.e(TAG, "Error loading image from URL", e);
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
}
