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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StudentScheduleLate extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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

        // Find and set OnClickListener for the back button
        ImageButton backButton = rootView.findViewById(R.id.backButtonLate);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click event, for example, pop the fragment from the back stack
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // Find and set OnClickListener for the submit button
        Button submitButton = rootView.findViewById(R.id.submitButton);
        EditText inputReason = rootView.findViewById(R.id.inputReason);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = inputReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    showPopup("The text box is empty. Please fill it up.");
                } else {
                    showConfirmationPopup(reason);
                }
            }
        });

        return rootView;
    }

    private void showPopup(String message) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_late_empty, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        View rootView = getView();

        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) rootView).addView(overlay);

        overlay.setClickable(true);
        overlay.setFocusable(true);

        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        Button closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void showConfirmationPopup(String reason) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_late_confirm, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        View rootView = getView();

        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) rootView).addView(overlay);

        overlay.setClickable(true);
        overlay.setFocusable(true);

        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        Button yesButton = popupView.findViewById(R.id.yesButton);
        Button noButton = popupView.findViewById(R.id.noButton);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Reason submitted: " + reason, Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
}
