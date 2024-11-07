package com.example.wildattend;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FacultyScheduleTimeout extends Fragment {

    private AppCompatButton timeoutButtonFaculty;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String TAG = "FacultyScheduleTimeout";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;
    private String mParam6;

    private TextView facultyNameTextView;
    private TextView idNumberTextView;
    private ImageView profile_image;

    public FacultyScheduleTimeout() {
        // Required empty public constructor
    }

    public static FacultyScheduleTimeout newInstance(String param1, String param2, String param3, String param4, String param5, String param6) {
        FacultyScheduleTimeout fragment = new FacultyScheduleTimeout();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        args.putString(ARG_PARAM5, param5);
        args.putString(ARG_PARAM6, param6);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
            mParam4 = getArguments().getString(ARG_PARAM4);
            mParam5 = getArguments().getString(ARG_PARAM5);
            mParam6 = getArguments().getString(ARG_PARAM6);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faculty_schedule_timeout, container, false);

        // Initialize views
        profile_image = rootView.findViewById(R.id.profile_image_faculty);
        facultyNameTextView = rootView.findViewById(R.id.facultyName);
        idNumberTextView = rootView.findViewById(R.id.idNumber);
        timeoutButtonFaculty = rootView.findViewById(R.id.facultyTimeOutButton);

        // Set class name and time in the TextViews
        TextView classCodeTextView = rootView.findViewById(R.id.classCode2);
        TextView timeDisplayTextView = rootView.findViewById(R.id.timeDisplay);
        TextView classNameTextView = rootView.findViewById(R.id.className);
        TextView roomLocationTextView = rootView.findViewById(R.id.roomLocation);
        View classColorCircle = rootView.findViewById(R.id.classColorCircle2);

        if (mParam1 != null) {
            classCodeTextView.setText(mParam1);
        }
        if (mParam2 != null && mParam3 != null) {
            String timeDisplay = mParam2 + " - " + mParam3;
            timeDisplayTextView.setText(timeDisplay);
        }
        if (mParam4 != null) {
            classNameTextView.setText(mParam4);
        }
        if (mParam5 != null) {
            roomLocationTextView.setText(mParam5);
        }
        if (mParam6 != null) {
            int classColor = Color.parseColor(mParam6);
            classColorCircle.setBackgroundColor(classColor);
            classColorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(mParam6)));
            classColorCircle.setBackground(getResources().getDrawable(R.drawable.circle_background));

        }

        timeoutButtonFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeOut();
            }
        });

        // Fetch and display user information
        fetchUserInformation();

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
                            String idNumber = documentSnapshot.getString("idNum");
                            String imageUrl = documentSnapshot.getString("img");

                            // Update UI with fetched information
                            facultyNameTextView.setText(firstName + " " + lastName);
                            idNumberTextView.setText(idNumber);

                            // Load the image from URL
                            new FacultyLoadImageTask(profile_image).execute(imageUrl);
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

//    private void timeOut() {
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            String userId = currentUser.getUid();
//            String className = mParam1; // Class name
//            Date timestamp = new Date(); // Current timestamp
//
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//            // Fetch the class ID using the class name
//            db.collection("classes")
//                    .whereEqualTo("classCode", className)
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
//                            String classId = classDocument.getId(); // Get classId from document ID
//
//                            // Fetch the last time-in record for the user
//                            db.collection("attendRecord")
//                                    .document(userId + "_" + classId) // Use userId and classId for document ID
//                                    .get()
//                                    .addOnSuccessListener(documentSnapshot -> {
//                                        if (documentSnapshot.exists()) {
//                                            Date lastTimeIn = documentSnapshot.getDate("timeIn");
//                                            if (lastTimeIn != null) {
//                                                long duration = timestamp.getTime() - lastTimeIn.getTime();
//                                                Log.d(TAG, "Duration between time in and time out: " + duration + " ms");
//
//                                                // Check if the duration is less than 1 hour (3600000 ms)
//                                                if (duration < 3600000) {
//                                                    // Show error message
//                                                    Toast.makeText(getContext(), "You need to wait at least 1 hour before timing out.", Toast.LENGTH_LONG).show();
//                                                    Log.d(TAG, "User attempted to time out before 1 hour elapsed.");
//                                                    return; // Exit the method if the duration is less than 1 hour
//                                                } else {
//                                                    Log.d(TAG, "User waited more than 1 hour, showing timeout confirmation popup.");
//                                                    // Show confirmation popup
//                                                    showTimeoutPopup(userId, classId, timestamp);
//                                                }
//                                            } else {
//                                                Log.e(TAG, "Last time in timestamp is null.");
//                                                Toast.makeText(getContext(), "No valid time-in record found.", Toast.LENGTH_SHORT).show();
//                                            }
//                                        } else {
//                                            Log.e(TAG, "No attendance record found for the user.");
//                                            Toast.makeText(getContext(), "No attendance record found.", Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Log.e(TAG, "Error fetching attendance record", e);
//                                    });
//                        } else {
//                            Log.e(TAG, "Class document not found");
//                            Toast.makeText(getContext(), "Class document not found", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e(TAG, "Error fetching class document", e);
//                    });
//        } else {
//            Log.e(TAG, "User not authenticated");
//            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void timeOut() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String className = mParam1;
            Date currentTimestamp = new Date();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("classes")
                    .whereEqualTo("classCode", className)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
                            String classId = classDocument.getId();

                            // Fetch the latest attendance record for the session
                            db.collection("attendRecord")
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("classId", classId)
                                    .orderBy("timeIn", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(attendSnapshot -> {
                                        if (!attendSnapshot.isEmpty()) {
                                            DocumentSnapshot latestAttendDoc = attendSnapshot.getDocuments().get(0);

                                            // Check if `timeOut` is not already set
                                            if (latestAttendDoc.getDate("timeOut") == null) {
                                                // Proceed with the timeout process
                                                long timeDiff = currentTimestamp.getTime() - latestAttendDoc.getDate("timeIn").getTime();
                                                if (timeDiff < 3600000) {
                                                    Toast.makeText(getContext(), "You need to wait at least 1 hour before timing out.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    showTimeoutPopup(userId, classId, currentTimestamp, latestAttendDoc.getId());
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Timeout has already been recorded for this session.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "No attendance record found.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching attendance record", e));
                        } else {
                            Toast.makeText(getContext(), "Class document not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching class document", e));
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimeoutPopup(String userId, String classId, Date timestamp, String attendDocId) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_timeout_confirm, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        AppCompatButton timeOutYes = popupView.findViewById(R.id.timeOutYes);
        AppCompatButton timeOutNo = popupView.findViewById(R.id.timeOutNo);

        timeOutYes.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Update the specific attendance document with the timeOut
            db.collection("attendRecord")
                    .document(attendDocId)
                    .update("timeOut", timestamp)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Successfully timed out!", Toast.LENGTH_SHORT).show();

                        // Mark class as not ongoing
                        db.collection("classes").document(classId)
                                .update("Ongoing", false)
                                .addOnSuccessListener(aVoid1 -> Log.d(TAG, "Class marked as not ongoing"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update class status", e));
//                              Redirect to FacultySchedule after timeout
                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                    fragmentManager.popBackStack(); // Clear the back stack
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.faculty_frame_layout, new FacultySchedule());
                                    fragmentTransaction.commit();

                        popupWindow.dismiss();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating attendance record", e));
        });

        timeOutNo.setOnClickListener(v -> popupWindow.dismiss());
    }


//
//    private void fetchClassIdAndTimeout(String userId, String className, Date timestamp) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Fetch the class ID using the class name
//        db.collection("classes")
//                .whereEqualTo("classCode", className)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
//                        String classId = classDocument.getId(); // Get classId from document ID
//
//                        // Update the class document to set ongoing to false
//                        Map<String, Object> classUpdate = new HashMap<>();
//                        classUpdate.put("Ongoing", false);
//
//                        db.collection("classes")
//                                .document(classId)
//                                .set(classUpdate, SetOptions.merge())
//                                .addOnSuccessListener(aVoid -> {
//                                    // Record the time out
//                                    Map<String, Object> attendanceUpdate = new HashMap<>();
//                                    attendanceUpdate.put("On-Time", timestamp);
//                                    attendanceUpdate.put("classId", classId); // Include classId in the attendance record
//
//                                    db.collection("attendRecord")
//                                            .document(userId + "_" + classId) // Use userId and classId in document ID
//                                            .set(attendanceUpdate, SetOptions.merge())
//                                            .addOnSuccessListener(aVoid2 -> {
//                                                Log.d(TAG, "Time out recorded successfully!");
//                                            })
//                                            .addOnFailureListener(e -> {
//                                                Log.e(TAG, "Error recording time out", e);
//                                            });
//                                })
//                                .addOnFailureListener(e -> {
//                                    Log.e(TAG, "Error updating class document", e);
//                                });
//                    } else {
//                        Log.e(TAG, "Class document not found");
//                        Toast.makeText(getContext(), "Class document not found", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error fetching class document", e);
//                });
//    }

//    private void showTimeoutPopup(String userId, String classId, Date timestamp) {
//        // Inflate the layout for the popup
//        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_timeout_confirm, null);
//
//        // Create a PopupWindow with the inflated layout
//        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//
//        // Set the location of the popup window
//        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
//
//        // Set click listeners for the Yes and No buttons in the popup
//        AppCompatButton timeOutYes = popupView.findViewById(R.id.timeOutYes);
//        AppCompatButton timeOutNo = popupView.findViewById(R.id.timeOutNo);
//
//        timeOutYes.setOnClickListener(v -> {
//            // Perform actions when "Yes" button is clicked
//            // Record the time-out
//            Map<String, Object> attendanceRecord = new HashMap<>();
//            attendanceRecord.put("userId", userId);
//            attendanceRecord.put("classId", classId);
//            attendanceRecord.put("timeOut", timestamp); // Add timeOut to the record
//
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            db.collection("attendRecord")
//                    .document(userId + "_" + classId) // Update the same document
//                    .set(attendanceRecord, SetOptions.merge())
//                    .addOnSuccessListener(aVoid -> {
//                        Log.d(TAG, "Time out recorded successfully!");
//                        Toast.makeText(getContext(), "Successfully timed out!", Toast.LENGTH_SHORT).show();
//
//                        // Update the class document to set ongoing to false
//                        Map<String, Object> classUpdate = new HashMap<>();
//                        classUpdate.put("Ongoing", false);
//
//                        db.collection("classes")
//                                .document(classId)
//                                .set(classUpdate, SetOptions.merge())
//                                .addOnSuccessListener(aVoid1 -> {
//                                    Log.d(TAG, "Class ongoing status updated to false.");
//                                    // Redirect to FacultySchedule after timeout
//                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//                                    fragmentManager.popBackStack(); // Clear the back stack
//                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                                    fragmentTransaction.replace(R.id.faculty_frame_layout, new FacultySchedule());
//                                    fragmentTransaction.commit();
//
//                                    popupWindow.dismiss(); // Dismiss the popup window after navigation
//                                })
//                                .addOnFailureListener(e -> {
//                                    Log.e(TAG, "Error updating ongoing status", e);
//                                });
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e(TAG, "Error recording time out", e);
//                    });
//        });
//
//        timeOutNo.setOnClickListener(v -> {
//            // Dismiss the popup window
//            popupWindow.dismiss();
//        });
//    }

    private static class FacultyLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public FacultyLoadImageTask(ImageView imageView) {
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