package com.example.wildattend;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StudentOverAllAttendance extends Fragment {

    private ImageView profileImageView;
    private TextView studentNameTextView;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> attendanceItems;

    public static StudentOverAllAttendance newInstance(String param1, String param2) {
        StudentOverAllAttendance fragment = new StudentOverAllAttendance();
        Bundle args = new Bundle();
        args.putString("PARAM_1_KEY", param1);
        args.putString("PARAM_2_KEY", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_overallattendance, container, false);
        listView = rootView.findViewById(R.id.list_view_attendance);
        rootView.findViewById(R.id.backButtonChangePassword).setOnClickListener(v -> requireActivity().onBackPressed()); // Handle back button click
        setupListView();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImageView = view.findViewById(R.id.profilepicture);
        studentNameTextView = view.findViewById(R.id.studentname);

        fetchUserInformation();
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
                        Log.e("StudentOverAllAttendance", "Error fetching user document", e);
                    });
        } else {
            Log.e("StudentOverAllAttendance", "User is not authenticated");
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
                            Log.e("StudentOverAllAttendance", "Error fetching attendance data", e);
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