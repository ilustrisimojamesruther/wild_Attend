package com.example.wildattend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class StudentScheduleTimeout extends Fragment {

    private AppCompatButton timeOutButton;
    private static final String ARG_PARAM1 = "className";
    private static final String ARG_PARAM2 = "time";
    private static final String TAG = "StudentScheduleTimeout";

    private String mParam1;
    private String mParam2;

    private TextView classNameTextView;
    private TextView timeDisplay;

    public StudentScheduleTimeout() {
        // Required empty public constructor
    }

    public static StudentScheduleTimeout newInstance(String className, String time) {
        StudentScheduleTimeout fragment = new StudentScheduleTimeout();
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
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_timeout, container, false);
        timeOutButton = rootView.findViewById(R.id.timeOutButton);

        classNameTextView = rootView.findViewById(R.id.className);
        timeDisplay = rootView.findViewById(R.id.timeDisplay);

        if (getArguments() != null) {
            String className = getArguments().getString(ARG_PARAM1);
            String time = getArguments().getString(ARG_PARAM2);
            classNameTextView.setText(className);
            timeDisplay.setText(time);
        }

        // Back Button
        ImageButton backButton = rootView.findViewById(R.id.backButtonClass);
        backButton.setOnClickListener(v -> getActivity().onBackPressed()); // Go back to the previous fragment/activity

        // Time Out Button Click Listener
        timeOutButton.setOnClickListener(v -> timeOut());

        return rootView;
    }

    private void timeOut() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String className = mParam1; // Assuming you have the class name available
            String time = mParam2; // Assuming you have the class time available
            Date timestamp = new Date(); // Get current timestamp

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("attendRecord")
                    .document(userId)
                    .update("timeOut", timestamp)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Time out recorded successfully!");
                        // Display a message or perform any additional action upon successful time-out
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error recording time out", e);
                        // Handle error
                    });
        } else {
            Log.e(TAG, "User not authenticated");
            // Handle authentication error
        }
    }
}
