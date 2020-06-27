package com.nfrancoi.delivery.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.nfrancoi.delivery.R;

import java.util.Arrays;
import java.util.function.IntPredicate;

public class PermissionActivity extends AppCompatActivity {

    public static final String PERMISSIONS = "PERMISSIONS";
    private static final int PERMISSIONS_INT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_delivery_list);
        String[] permissions = getIntent().getStringArrayExtra(PERMISSIONS);
        if (!this.checkPermissions(permissions)) {
            requestPermissions(permissions, PERMISSIONS_INT);

        } else {
            this.proceedPermissionsGranted();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //  getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true;
    }

    private boolean checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != this.getBaseContext().checkSelfPermission(permission)) {
                Toast.makeText(this.getBaseContext(), "Permission requested: " + permission,
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_INT: {

                boolean pisAllPermOK = Arrays.stream(grantResults).anyMatch(new IntPredicate() {
                    @Override
                    public boolean test(int value) {
                        return value == PackageManager.PERMISSION_DENIED;
                    }
                });
                if (pisAllPermOK) {
                    this.proceedPermissionNotGranted();
                } else {
                    this.proceedPermissionsGranted();
                }

            }
        }

    }

    private void proceedPermissionsGranted() {
        Toast.makeText(this.getBaseContext(), "Permissions granted!",
                Toast.LENGTH_LONG).show();
        this.setResult(RESULT_OK);
        finish();

    }


    private void proceedPermissionNotGranted() {
        Toast.makeText(this.getBaseContext(), "Permissions not granted",
                Toast.LENGTH_LONG).show();
        this.setResult(RESULT_CANCELED);
        finish();
    }

}
