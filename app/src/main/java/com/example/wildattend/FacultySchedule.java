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
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FacultySchedule extends Fragment {

    private static final String TAG = "FacultySchedule";

    private TextView facultyNameTextView;
    private TextView idNumberTextView;
    private ListView listView;
    private ImageView profile_image;
    private List<ClassItem> scheduleItems;
    private ClassItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_schedule, container, false);

        facultyNameTextView = rootView.findViewById(R.id.facultyName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);
        listView = rootView.findViewById(R.id.list_view_schedule);
        profile_image = rootView.findViewById(R.id.profile_image_faculty);

        scheduleItems = new ArrayList<>();
        adapter = new ClassItemAdapter(requireContext(), scheduleItems, R.layout.list_class_schedule, true);
        listView.setAdapter(adapter);

        fetchUserInformation();
        setupListView();

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
                            String idNumber = documentSnapshot.getString("idNum");
                            String imageUrl = documentSnapshot.getString("img");

                            facultyNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            new LoadImageTask(profile_image).execute(imageUrl);

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
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = sdf.parse(time);
            sdf.applyPattern("h:mm a");
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }


    private void setupListView() {
        listView.setAdapter(adapter);

        // Set item click listener to navigate to class schedule
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ClassItem selectedItem = (ClassItem) parent.getItemAtPosition(position);
            if (selectedItem != null) {
                // Navigate to the detailed schedule of the selected class
                navigateToClassSchedule(selectedItem.getClassCode(), selectedItem.getStartTime(), selectedItem.getEndTime(), selectedItem.getClassDesc(), selectedItem.getClassRoom());
            }
        });
    }


    private void navigateToClassSchedule(String classCode, String startTime, String endTime, String classDesc, String classRoom) {
        // Create instance of FacultyScheduleTimeIn fragment and pass class code as argument
        FacultyScheduleTimeIn facultyScheduleTimeInFragment = FacultyScheduleTimeIn.newInstance(classCode, startTime, endTime, classDesc, classRoom);

        // Navigate to the FacultyScheduleTimeIn fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.faculty_frame_layout, facultyScheduleTimeInFragment)
                .addToBackStack(null)
                .commit();
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
