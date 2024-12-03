package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentHome extends Fragment {

    private static final String TAG = "StudentHome";

    private ListView listView;

    private TextView studentNameTextView;
    private ImageView profile_image;
    private List<ClassItem> scheduleItems;
    private ClassItemAdapter adapter;

    private TextView classesValue;

    private int totalClasses = 0; // Variable to hold the total number of classes

    private StudentProgressBar studentProgressBar;

    private TextView percentageText;

    public StudentHome() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_home, container, false);
        studentNameTextView = rootView.findViewById(R.id.student_header);
        profile_image = rootView.findViewById(R.id.profile_image_student);
        listView = rootView.findViewById(R.id.list_view_schedule);
        classesValue = rootView.findViewById(R.id.classesValue);
        studentProgressBar = rootView.findViewById(R.id.studentProgressBar);
        percentageText = rootView.findViewById(R.id.percentageText);

        // Fetch and display the attendance progress
        fetchAttendanceProgress();

        scheduleItems = new ArrayList<>();
        adapter = new ClassItemAdapter(requireContext(), scheduleItems, R.layout.list_next_class, false);
        listView.setAdapter(adapter);

//        int progress = 46; // Example progress value
//        studentProgressBar.setProgress(progress);
//        percentageText.setText(progress + "%");

        // Set the current date
        TextView dateTextView = rootView.findViewById(R.id.date);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Fetch and display user information
        fetchUserInformation();

        setupListView();
        fetchOnTimeCount();
        fetchLateCount();
        fetchTotalHours();
        fetchUserClasses();
        return rootView;
    }

    private void fetchUserInformation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", userEmail) // Assuming the field in Firestore is "email"
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String imageUrl = documentSnapshot.getString("img");

                            // Update UI with fetched information
                            studentNameTextView.setText("Hi," + " " + firstName + "!");

                            // Load the image from URL
                            new StudentHome.LoadImageTask(profile_image).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user document", e);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            // Handle the case where the user is not authenticated or has signed out
        }
    }

    private void setupListView() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ClassItem selectedItem = (ClassItem) parent.getItemAtPosition(position);
            if (selectedItem != null) {
                // Navigate to the detailed schedule of the selected class
                navigateToClassSchedule(selectedItem.getClassCode(),selectedItem.getStartTime(), selectedItem.getEndTime(),  selectedItem.getClassDesc(), selectedItem.getClassRoom(), selectedItem.getClassColor());
            }
        });
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

    private void fetchUserClasses() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("userClasses")
                    .whereEqualTo("userID", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        totalClasses = queryDocumentSnapshots.size(); // Update total classes count
                        classesValue.setText(String.valueOf(totalClasses));
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

    private void fetchOnTimeCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("attendRecord")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "On-Time")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int onTimeCount = queryDocumentSnapshots.size(); // Count of "On-Time" records

                        // Update UI with the "On-Time" count
                        TextView onTimeValue = getView().findViewById(R.id.onTimeValue);
                        onTimeValue.setText(String.valueOf(onTimeCount));

                        Log.d(TAG, "On-Time records count: " + onTimeCount);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching On-Time records", e);
                        Toast.makeText(requireContext(), "Failed to fetch On-Time data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            // Handle unauthenticated user case
        }
    }

    private void fetchLateCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("attendRecord")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "Late")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int lateCount = queryDocumentSnapshots.size(); // Count of "Late" records

                        // Update UI with the "Late" count
                        TextView lateValue = getView().findViewById(R.id.lateValue);
                        lateValue.setText(String.valueOf(lateCount));

                        Log.d(TAG, "Late records count: " + lateCount);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching Late records", e);
                        Toast.makeText(requireContext(), "Failed to fetch Late data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            // Handle unauthenticated user case
        }
    }


    private void fetchTotalHours() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("attendRecord")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        long totalMilliseconds = 0; // Total time in milliseconds

                        // Loop through each attendance record
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Extract timeIn and timeOut timestamps
                            Timestamp timeInTimestamp = documentSnapshot.getTimestamp("timeIn");
                            Timestamp timeOutTimestamp = documentSnapshot.getTimestamp("timeOut");

                            // Ensure neither timeIn nor timeOut is null
                            if (timeInTimestamp != null && timeOutTimestamp != null) {
                                Date timeIn = timeInTimestamp.toDate();
                                Date timeOut = timeOutTimestamp.toDate();

                                // Add the difference to totalMilliseconds
                                totalMilliseconds += timeOut.getTime() - timeIn.getTime();
                            } else {
                                Log.w(TAG, "Skipping record with missing timeIn or timeOut: " + documentSnapshot.getId());
                            }
                        }

                        // Convert total milliseconds to decimal hours
                        double totalHours = totalMilliseconds / (1000.0 * 60 * 60);

                        // Update the UI with a single decimal value
                        TextView totalHoursValue = getView().findViewById(R.id.totalHoursValue);
                        totalHoursValue.setText(String.format(Locale.getDefault(), "%.1f", totalHours));

                        Log.d(TAG, "Total hours: " + totalHours);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching total hours", e);
                        Toast.makeText(requireContext(), "Failed to fetch total hours", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            // Handle unauthenticated user case
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
                        String formattedStartTime = formatTime(startTime); // Format the start time
                        String endTime = documentSnapshot.getString("endTime");
                        String formattedEndTime = formatTime(endTime); // Format the start time
                        String classColor = documentSnapshot.getString("classColor");
                        String classRoom = documentSnapshot.getString("classRoom");

                        // Fetch day booleans with null checks using temporary variables
                        Boolean mondayObj = documentSnapshot.getBoolean("Monday");
                        boolean monday = mondayObj != null && mondayObj;

                        Boolean tuesdayObj = documentSnapshot.getBoolean("Tuesday");
                        boolean tuesday = tuesdayObj != null && tuesdayObj;

                        Boolean wednesdayObj = documentSnapshot.getBoolean("Wednesday");
                        boolean wednesday = wednesdayObj != null && wednesdayObj;

                        Boolean thursdayObj = documentSnapshot.getBoolean("Thursday");
                        boolean thursday = thursdayObj != null && thursdayObj;

                        Boolean fridayObj = documentSnapshot.getBoolean("Friday");
                        boolean friday = fridayObj != null && fridayObj;

                        Boolean saturdayObj = documentSnapshot.getBoolean("Saturday");
                        boolean saturday = saturdayObj != null && saturdayObj;

                        Boolean sundayObj = documentSnapshot.getBoolean("Sunday");
                        boolean sunday = sundayObj != null && sundayObj;

                        Log.d(TAG, "Class Name: " + classDesc);

                        ClassItem item = new ClassItem(classCode, classDesc, formattedStartTime, formattedEndTime, classColor, classRoom, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
                        scheduleItems.add(item);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Class document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching class details", e);
                });
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
                                studentProgressBar.setProgress(0);
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
                                if ("On-Time".equals(status)) {
                                    attendedClasses++;
                                }
                            }

                            // Calculate progress percentage
                            if (totalClasses > 0) {
                                int progress = (attendedClasses * 100) / totalClasses;
                                studentProgressBar.setProgress(progress);
                                percentageText.setText(progress + "%");
                            } else {
                                studentProgressBar.setProgress(0);
                                percentageText.setText("0%");
                            }
                        }
                    });
        } else {
            Log.e(TAG, "No current user found.");
        }
    }

    private String formatTime(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a");
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting time", e);
            return time; // Return original time in case of error
        }
    }

    private void navigateToClassSchedule(String classCode, String startTime, String endTime, String classDesc, String classRoom, String classColor) {
        // Create instance of StudentScheduleClass fragment and pass class code as argument
        StudentScheduleTimeIn studentScheduleClassFragment = StudentScheduleTimeIn.newInstance(classCode, startTime, endTime, classDesc, classRoom, classColor);

        // Navigate to the StudentScheduleClass fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, studentScheduleClassFragment)
                .addToBackStack(null)
                .commit();
    }
}
