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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        scheduleItems = new ArrayList<>();
        adapter = new ClassItemAdapter(requireContext(), scheduleItems, R.layout.list_next_class, false);
        listView.setAdapter(adapter);

        // Set the current date
        TextView dateTextView = rootView.findViewById(R.id.date);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Fetch and display user information
        fetchUserInformation();

        setupListView();
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
                navigateToClassSchedule(selectedItem.getClassCode(),selectedItem.getStartTime(), selectedItem.getEndTime(),  selectedItem.getClassDesc(), selectedItem.getClassRoom());
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

                        Log.d(TAG, "Class Name: " + classDesc);

                        ClassItem item = new ClassItem(classCode, classDesc, formattedStartTime, formattedEndTime, classColor, classRoom);
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

    private void navigateToClassSchedule(String classCode, String startTime, String endTime, String classDesc, String classRoom) {
        // Create instance of StudentScheduleClass fragment and pass class code as argument
        StudentScheduleTimeIn studentScheduleClassFragment = StudentScheduleTimeIn.newInstance(classCode, startTime, endTime, classDesc, classRoom);

        // Navigate to the StudentScheduleClass fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, studentScheduleClassFragment)
                .addToBackStack(null)
                .commit();
    }
}
