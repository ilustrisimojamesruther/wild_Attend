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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FacultyScheduleTimeIn extends Fragment {

    private AppCompatButton timeinButton;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String TAG = "FacultyScheduleTimeIn";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;
    private String mParam6;

    private TextView facultyNameTextView;
    private TextView idNumberTextView;
    private ImageView profile_image;

    public FacultyScheduleTimeIn() {
        // Required empty public constructor
    }

    public static FacultyScheduleTimeIn newInstance(String param1, String param2, String param3, String param4, String param5, String param6) {
        FacultyScheduleTimeIn fragment = new FacultyScheduleTimeIn();
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
        View view = inflater.inflate(R.layout.fragment_faculty_schedule_timein, container, false);

        // Set class name and time in the TextViews
        TextView classCodeTextView = view.findViewById(R.id.course_code);
        TextView timeDisplayTextView = view.findViewById(R.id.timeDisplay);
        TextView classDescTextView = view.findViewById(R.id.className);
        TextView roomLocationTextView = view.findViewById(R.id.roomLocation);
        View classColorCircle = view.findViewById(R.id.classColorCircle);

        if (mParam1 != null) {
            classCodeTextView.setText(mParam1);
        }
        if (mParam2 != null && mParam3 != null) {
            String timeDisplay = mParam2 + " - " + mParam3;
            timeDisplayTextView.setText(timeDisplay);
        }
        if (mParam4 != null) {
            classDescTextView.setText(mParam4);
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

        Button timeInButton = view.findViewById(R.id.timeInButton);
        timeInButton.setOnClickListener(v -> timeIn());

        ImageButton backButton = view.findViewById(R.id.backButtonClass);
        backButton.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Initialize views
        profile_image = view.findViewById(R.id.profile_image_faculty);
        facultyNameTextView = view.findViewById(R.id.facultyName);
        idNumberTextView = view.findViewById(R.id.idNumber);

        // Fetch and display user information
        fetchUserInformation();

        return view;
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

    private void timeIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String classCode = mParam1; // Class code
            String startTime = mParam2; // Start time
            String endTime = mParam3; // End time
            String message = "I'm here on time"; // Default message
            Date timestamp = new Date(); // Current timestamp

            // Set up date formats
            SimpleDateFormat fullDateFormat12Hour = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            SimpleDateFormat timeFormat12Hour = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            try {
                // Get today's date
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // Combine today's date with start and end times
                Date startDate = fullDateFormat12Hour.parse(today + " " + startTime);
                Date endDate = fullDateFormat12Hour.parse(today + " " + endTime);

                // Check if endDate is before startDate
                if (endDate.before(startDate)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(endDate);
                    calendar.add(Calendar.DATE, 1); // Move endDate to the next day
                    endDate = calendar.getTime();
                }

                // Current time
                Date currentTime = new Date();
                Date fifteenMinutesEarly = new Date(startDate.getTime() - 15 * 60 * 1000);

                boolean canTimeIn = currentTime.after(fifteenMinutesEarly) && currentTime.before(endDate);

                if (canTimeIn) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    // Fetch the class ID using the class code
                    db.collection("classes")
                            .whereEqualTo("classCode", classCode)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
                                    String classId = classDocument.getId();  // Get class ID

                                    Boolean ongoing = classDocument.getBoolean("Ongoing");
                                    if (ongoing == null || !ongoing) {
                                        Map<String, Object> attendanceRecord = new HashMap<>();
                                        attendanceRecord.put("userId", userId);
                                        attendanceRecord.put("message", message);
                                        attendanceRecord.put("status", "On-Time");
                                        attendanceRecord.put("timeIn", timestamp);
                                        attendanceRecord.put("classId", classId);  // Use class ID

                                        // Set class to ongoing
                                        Map<String, Object> classUpdate = new HashMap<>();
                                        classUpdate.put("Ongoing", true);

                                        db.collection("classes")
                                                .document(classId)
                                                .set(classUpdate, SetOptions.merge())
                                                .addOnSuccessListener(aVoid -> {
                                                    db.collection("attendRecord")
                                                            .document(userId + "_" + classId)  // Use class ID here
                                                            .set(attendanceRecord, SetOptions.merge())
                                                            .addOnSuccessListener(aVoid2 -> {
                                                                Log.d(TAG, "Time in recorded successfully!");
                                                                showPopup();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e(TAG, "Error recording time in", e);
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error updating class document", e);
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Class is already ongoing", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Class document not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching class document", e);
                            });
                } else {
                    String errorMessage = "Time in is not allowed. Current time: " + timeFormat12Hour.format(currentTime);
                    Log.e(TAG, errorMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }

            } catch (ParseException e) {
                Log.e(TAG, "Error parsing time", e);
            }
        } else {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }





    private void timeOut() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String classCode = mParam1; // Class code
            Date timestamp = new Date(); // Get current timestamp

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Fetch the class ID using the class code
            db.collection("classes")
                    .whereEqualTo("classCode", classCode)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot classDocument = queryDocumentSnapshots.getDocuments().get(0);
                            String classId = classDocument.getId();  // Get class ID

                            // Update the class document to set ongoing to false
                            Map<String, Object> classUpdate = new HashMap<>();
                            classUpdate.put("Ongoing", false);

                            db.collection("classes")
                                    .document(classId)
                                    .set(classUpdate, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Map<String, Object> attendanceUpdate = new HashMap<>();
                                        attendanceUpdate.put("timeOut", timestamp);

                                        db.collection("attendRecord")
                                                .document(userId + "_" + classId)  // Use class ID here
                                                .set(attendanceUpdate, SetOptions.merge())
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Log.d(TAG, "Time out recorded successfully!");
                                                    // Handle successful time out, e.g., navigate to another fragment
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error recording time out", e);
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating class document", e);
                                    });
                        } else {
                            Log.e(TAG, "Class document not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching class document", e);
                    });
        } else {
            Log.e(TAG, "User not authenticated");
        }
    }


    private void showPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_timein, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        View rootView = getView();

        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) rootView).addView(overlay);

        overlay.setClickable(true);
        overlay.setFocusable(true);

        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        AppCompatButton continueButton = popupView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToFacultyScheduleTimeout();

                ((ViewGroup) rootView).removeView(overlay);

                popupWindow.dismiss();
            }
        });
    }

    private void navigateToFacultyScheduleTimeout() {
        ((FacultyDashboard) getActivity()).hideBottomNavigation();
        // Create instance of FacultyScheduleTimeout fragment
        FacultyScheduleTimeout facultyScheduleTimeoutFragment = FacultyScheduleTimeout.newInstance(mParam1, mParam2, mParam3, mParam4, mParam5, mParam6);

        // Navigate to the FacultyScheduleTimeout fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.faculty_frame_layout, facultyScheduleTimeoutFragment); // Use the correct container ID
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
            if (imageViewWeakReference != null && bitmap != null) {
                ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}