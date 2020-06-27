package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.viewmodel.PreferencesViewModel;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.toString();

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

        });


        //
        // Products
        //
        Preference employeesCat = findPreference("employees_modify");

        employeesCat.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "click on preference employee");
            return true;
        });


    }

}

