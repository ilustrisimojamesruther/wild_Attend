package com.example.wildattend;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class StudentScheduleLate extends Fragment {

    private static final String ARG_PARAM1 = "className";
    private static final String ARG_PARAM2 = "time";

    private String mParam1;
    private String mParam2;

    @ServerTimestamp
    private Date timestamp;

    private Date classStartTime; // Assuming this is the start time of the class

    public StudentScheduleLate() {
        // Required empty public constructor
    }

    public static StudentScheduleLate newInstance(String param1, String param2) {
        StudentScheduleLate fragment = new StudentScheduleLate();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        View rootView = inflater.inflate(R.layout.fragment_student_schedule_late, container, false);

        ImageButton backButton = rootView.findViewById(R.id.backButtonLate);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        Button submitButton = rootView.findViewById(R.id.submitLateButton);
        EditText inputReason = rootView.findViewById(R.id.inputReason);
        submitButton.setOnClickListener(v -> {
            String reason = inputReason.getText().toString().trim();
            if (reason.isEmpty()) {
                showPopup("The text box is empty. Please fill it up.");
            } else {
                // Update user status here
                Toast.makeText(getContext(), "Attendance updated successfully!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        return rootView;
    }

    private void showPopup(String message) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_late_empty, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        Button closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }
}
