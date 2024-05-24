package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FacultyHome extends Fragment {

    private ListView attendanceLogListView;
    private static final String TAG = "FacultyHome";
    private TextView facultyNameTextView;
    private ImageView profile_image;

    public FacultyHome() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_home, container, false);

        // Initialize the views
        facultyNameTextView = rootView.findViewById(R.id.home_header);
        profile_image = rootView.findViewById(R.id.profile_image);
        attendanceLogListView = rootView.findViewById(R.id.attendanceLogListView);

        // Set the current date
        TextView dateTextView = rootView.findViewById(R.id.date);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Fetch and display user information
        fetchUserInformation();

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
                    .whereEqualTo("email", userEmail) // Assuming the field in Firestore is "email"
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
