package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.nfrancoi.delivery.BuildConfig;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.activity.widget.TextPreference;
import com.nfrancoi.delivery.viewmodel.PreferencesViewModel;
import com.nfrancoi.delivery.worker.ResetDataBaseWorker;
import com.nfrancoi.delivery.worker.SyncDataBaseWorker;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.toString();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(R.string.fragment_settings_title);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.main_activity_preferences, rootKey);

        //
        // Company
        //
        EditTextPreference companyNameEditText = findPreference("company_name");
        EditTextPreference addressEditText = findPreference("company_address");
        EditTextPreference phone1EditText = findPreference("company_phone1");
        EditTextPreference phone2EditText = findPreference("company_phone2");
        EditTextPreference emailEditText = findPreference("company_email");
        EditTextPreference vatEditText = findPreference("company_vat");
        EditTextPreference bankEditText = findPreference("company_bank");


        PreferencesViewModel prefViewModel = ViewModelProviders.of(this).get(PreferencesViewModel.class);
        prefViewModel.getCompany().observe(this, company -> {
            if (company != null) {
                companyNameEditText.setText(company.name);
                companyNameEditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.name = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );

                addressEditText.setText(company.address);
                addressEditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.address = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );


                phone1EditText.setText(company.phoneNumber1);
                phone1EditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.phoneNumber1 = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );

                phone2EditText.setText(company.phoneNumber2);
                phone2EditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.phoneNumber2 = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );

                emailEditText.setText(company.email);
                emailEditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.email = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );

                vatEditText.setText(company.vatNumber);
                vatEditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.vatNumber = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );

                bankEditText.setText(company.bankAccount);
                bankEditText.setOnPreferenceChangeListener((preference, newName) -> {
                            company.bankAccount = newName.toString();
                            prefViewModel.update(company);
                            return true;
                        }
                );
            }

        });


        //
        // Products
        //
        Preference employeesCat = findPreference("employee_default");

        employeesCat.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "click on preference employee");
            return true;
        });


        //
        //Google spreadsheet connect
        //
        TextPreference accountButton = findPreference("sync_account_name_button");
        accountButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showGoogleSignInFragment();
                return true;
            }
        });

        TextPreference syncSpreadSheetButton = findPreference("sync_spreadsheet_button");
        syncSpreadSheetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showGoogleSheetSelectionFragment();
                return true;
            }
        });

        TextPreference syncStartButton = findPreference("sync_start_button");
        syncStartButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WorkManager syncWorkManager = WorkManager.getInstance(requireActivity().getApplicationContext());
                syncWorkManager.enqueue(OneTimeWorkRequest.from(SyncDataBaseWorker.class));
                return true;
            }
        });

        TextPreference syncUploadNotesButton = findPreference("sync_upload_notes_button");
        syncUploadNotesButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
               showGoogleSyncNotesFragment();
                return true;
            }
        });

        //
        // About
        //
        TextPreference versionNumber = findPreference("version_number");
        versionNumber.setText(BuildConfig.VERSION_CODE+"");

        TextPreference resetDbButton = findPreference("reset_db");
        resetDbButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WorkManager resetDatabaseWorkManager = WorkManager.getInstance(requireActivity().getApplicationContext());
                resetDatabaseWorkManager.enqueue(OneTimeWorkRequest.from(ResetDataBaseWorker.class));

                return true;
            }
        });


    }

    private void showGoogleSignInFragment() {


        GoogleSignInFragment googleSignInFragment = GoogleSignInFragment.newInstance();
        googleSignInFragment.setRetainInstance(true);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, googleSignInFragment, "googlesignin")
                .addToBackStack(null)
                .commit();

    }

    private void showGoogleSheetSelectionFragment() {


        GoogleSheetSelectFragment fragment = GoogleSheetSelectFragment.newInstance();
        fragment.setRetainInstance(true);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, "sheetselects")
                .addToBackStack(null)
                .commit();

    }

    private void showGoogleSyncNotesFragment() {


        GoogleSyncNotesFragment fragment = GoogleSyncNotesFragment.newInstance();
        fragment.setRetainInstance(true);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, "syncnotes")
                .addToBackStack(null)
                .commit();

    }

}

