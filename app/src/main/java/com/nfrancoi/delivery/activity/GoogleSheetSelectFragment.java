package com.nfrancoi.delivery.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.googleapi.GoogleApiGateway;

import java.util.concurrent.Executors;


public class GoogleSheetSelectFragment extends Fragment implements
        View.OnClickListener {

    private static final String TAG = GoogleSheetSelectFragment.class.toString();
    private static final int GOOGLE_SIGN_IN_REQUEST = 9001;


    private TextView googleSheetNameTextView;
    private Button saveButton;


    private ImageView allowedImage, notAllowedImage;


    public static GoogleSheetSelectFragment newInstance() {
        GoogleSheetSelectFragment fragment = new GoogleSheetSelectFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_google_sheet_select, container, false);


        saveButton = view.findViewById(R.id.fragment_google_sheet_select_save_button);
        saveButton.setOnClickListener(this);

        googleSheetNameTextView = view.findViewById(R.id.fragment_google_sheet_select_name);

        allowedImage = view.findViewById(R.id.fragment_google_sheet_select_allowed);
        notAllowedImage = view.findViewById(R.id.fragment_google_sheet_select_not_allowed);


        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        String googleSheetName = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("sync_spreadsheet_button", "");
        googleSheetNameTextView.setText(googleSheetName);

        updateUI();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_google_sheet_select_save_button:
                updateUI();
                break;
        }
    }

    private void updateUI() {

        notAllowedImage.setVisibility(View.GONE);
        allowedImage.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            String spreadSheetId = GoogleApiGateway.getInstance().getSpreadSheetIdByName("" + googleSheetNameTextView.getText());

            requireActivity().runOnUiThread(() -> {
                if (spreadSheetId == null) {
                    notAllowedImage.setVisibility(View.VISIBLE);
                    allowedImage.setVisibility(View.GONE);
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit();
                    prefs.putString("sync_spreadsheet_button", googleSheetNameTextView.getText() + "");
                    prefs.putString("sync_spreadsheet_id", "");
                    prefs.apply();

                } else {
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit();
                    prefs.putString("sync_spreadsheet_button", googleSheetNameTextView.getText() + "");
                    prefs.putString("sync_spreadsheet_id", spreadSheetId);
                    prefs.apply();

                    notAllowedImage.setVisibility(View.GONE);
                    allowedImage.setVisibility(View.VISIBLE);


                }

            });
        });
    }
}