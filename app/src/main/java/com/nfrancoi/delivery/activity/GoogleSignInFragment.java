package com.nfrancoi.delivery.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.repository.googleapi.GoogleApiGateway;

import java.util.concurrent.Executors;


public class GoogleSignInFragment extends Fragment implements
        View.OnClickListener {

    private static final String TAG = GoogleSignInFragment.class.toString();
    private static final int GOOGLE_SIGN_IN_REQUEST = 9001;
    private static final int REQUEST_AUTHORIZATION = 9002;

    private GoogleSignInClient mGoogleSignInClient;

    private SignInButton signInButton;
    private Button signOutButton;
    private Button revokeButton;
    private TextView mAccountNameTextView;

    private ImageView allowedImage, notAllowedImage;


    public static GoogleSignInFragment newInstance() {
        GoogleSignInFragment fragment = new GoogleSignInFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_google_sign_in, container, false);

        mAccountNameTextView = view.findViewById(R.id.fragment_google_sign_in_account_name);

        allowedImage = view.findViewById(R.id.fragment_google_sign_in_account_allowed);
        notAllowedImage = view.findViewById(R.id.fragment_google_sign_in_account_not_allowed);

        // Button listeners
        signInButton = view.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        signOutButton = view.findViewById(R.id.fragment_google_sign_in_sign_out_button);
        signOutButton.setOnClickListener(this);

        revokeButton = view.findViewById(R.id.fragment_google_sign_in_revoke_button);
        revokeButton.setOnClickListener(this);

        //Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(GoogleApiGateway.getInstance().scopes[0], GoogleApiGateway.getInstance().scopes)
                .requestIdToken("64002358911-pm4jfdbd4jfae9ur4fsd9cp8ho1bvqo8.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        SignInButton signInButton = view.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);


        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if (account != null
                && GoogleSignIn.hasPermissions(account, GoogleApiGateway.getInstance().scopes)
        ) {
            updateUI(account);

        } else {
            updateUI(null);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        if (requestCode == REQUEST_AUTHORIZATION) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        try {
            // Signed in successfully, show authenticated U
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            // Signed out, show unauthenticated UI
            Toast.makeText(requireActivity().getApplicationContext(), "Impossible de se connecter aux API Google errCode:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST);
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }


    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(requireActivity(),
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        updateUI(null);

                    }
                });
    }


    @SuppressLint("StringFormatInvalid")
    private void updateUI(@Nullable GoogleSignInAccount account) {

        String accountLabel;
        if (account != null) {
            accountLabel = account.getDisplayName() + "\n" + account.getEmail();

            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            revokeButton.setVisibility(View.VISIBLE);
            notAllowedImage.setVisibility(View.GONE);
            allowedImage.setVisibility(View.GONE);

            Executors.newSingleThreadExecutor().execute(() -> {
                boolean hasAccess = false;
                try {
                    hasAccess = GoogleApiGateway.getInstance().checkAccountDriveAccess();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                }

                if (hasAccess) {
                    requireActivity().runOnUiThread(() -> {
                        notAllowedImage.setVisibility(View.GONE);
                        allowedImage.setVisibility(View.VISIBLE);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        notAllowedImage.setVisibility(View.VISIBLE);
                        allowedImage.setVisibility(View.GONE);
                    });
                }
                ;
            });
            notAllowedImage.setVisibility(View.GONE);
            allowedImage.setVisibility(View.VISIBLE);


        } else {
            accountLabel = "";
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            revokeButton.setVisibility(View.GONE);
            notAllowedImage.setVisibility(View.GONE);
            allowedImage.setVisibility(View.GONE);

        }

        mAccountNameTextView.setText(accountLabel);
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit();
        prefs.putString("sync_account_name_button", accountLabel);
        prefs.commit();

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.fragment_google_sign_in_sign_out_button:
                signOut();
                break;
            case R.id.fragment_google_sign_in_revoke_button:
                revokeAccess();
                break;
        }
    }


}
