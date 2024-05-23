package com.example.wildattend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class StudentScheduleClass extends Fragment {

    private AppCompatButton presentButton;
    private static final String ARG_PARAM1 = "className";
    private static final String ARG_PARAM2 = "time";
    private static final String TAG = "StudentScheduleClass";

    private String mParam1;
    private String mParam2;

    private TextView studentNameTextView;
    private TextView idNumberTextView;
    private TextView classNameTextView;
    private TextView timeDisplay;
    private ImageView profile_image;

    public StudentScheduleClass() {
        // Required empty public constructor
    }

    public static StudentScheduleClass newInstance(String className, String time) {
        StudentScheduleClass fragment = new StudentScheduleClass();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, className);
        args.putString(ARG_PARAM2, time);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_class, container, false);
        presentButton = rootView.findViewById(R.id.presentButton);
        presentButton.setOnClickListener(view -> showPopup());

        ImageButton backButton = rootView.findViewById(R.id.backButtonClass);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        profile_image = rootView.findViewById(R.id.profile_image);
        studentNameTextView = rootView.findViewById(R.id.studentName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);
        classNameTextView = rootView.findViewById(R.id.className);
        timeDisplay = rootView.findViewById(R.id.timeDisplay);

        fetchUserInformation();

        if (getArguments() != null) {
            String className = getArguments().getString(ARG_PARAM1);
            String time = getArguments().getString(ARG_PARAM2);
            classNameTextView.setText(className);
            timeDisplay.setText(time);
        }

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

                            studentNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            new LoadImageTask(profile_image).execute(imageUrl);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user document", e));
        } else {
            Log.e(TAG, "User is not authenticated");
        }
    }

    private void showPopup() {
        String className = classNameTextView.getText().toString();
        String time = timeDisplay.getText().toString();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, className);
        args.putString(ARG_PARAM2, time);

        StudentScheduleLate studentScheduleLateFragment = StudentScheduleLate.newInstance(className, time);
        studentScheduleLateFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, studentScheduleLateFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
            if (bitmap != null) {
                ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
