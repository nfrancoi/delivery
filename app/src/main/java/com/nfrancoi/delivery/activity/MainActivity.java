package com.nfrancoi.delivery.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int ACTIVITY_RESULT_REQUEST_PERMISSIONS = 100;
    public static final int ACTIVITY_RESULT_NEW_DELIVERY = 101;

    private DeliveryViewModel deliveryViewModel;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        //add this due to exception  java.lang.RuntimeException: android.os.TransactionTooLargeException: data parcel size 1950984 bytes
        //NFR outState.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        if (savedInstanceState == null) {
            //first time the activity is created

            //
            // Request permissions
            //
            Intent myIntent = new Intent(MainActivity.this,
                    PermissionActivity.class);

            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET};

            myIntent.putExtra(PermissionActivity.PERMISSIONS, permissions);
            startActivityForResult(myIntent, ACTIVITY_RESULT_REQUEST_PERMISSIONS);


        }


    }

    //
    // Menu management
    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_delivery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_action_settings:
                SettingsFragment settingsFragment = new SettingsFragment();
                settingsFragment.setRetainInstance(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_layout, settingsFragment, "settingsFragment")
                        .addToBackStack(null)
                        .commit();
                return true;

            default: {
                return super.onOptionsItemSelected(item);
            }

        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTIVITY_RESULT_NEW_DELIVERY:
                if (resultCode == RESULT_OK) {


                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.fragment_new_delivery_empty_not_saved,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case ACTIVITY_RESULT_REQUEST_PERMISSIONS:
                if (resultCode == RESULT_OK) {
                    this.showDeliverySlideFragment();

                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Veuillez autoriser les permissions", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            default:
                Snackbar.make(findViewById(android.R.id.content), "Case not taken into account", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }

    private void showDeliverySlideFragment() {
        DeliverySwipeFragment deliverySwipeFragment = DeliverySwipeFragment.newInstance(Calendar.getInstance());
        deliverySwipeFragment.setRetainInstance(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, deliverySwipeFragment, "findThisFragment")
                .addToBackStack(null)
                .commit();


    }


}
