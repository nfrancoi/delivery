package com.nfrancoi.delivery.repository.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.GoogleAuthException;
import com.nfrancoi.delivery.repository.googleapi.GoogleApiGateway;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpOauth2Interceptor implements Interceptor {

    private static final String TAG = HttpOauth2Interceptor.class.toString();


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        String token = null;
        try {
            token = GoogleApiGateway.getInstance().getValidIdToken();
            if(token == null){
                Log.e(TAG, "id_token is null");
            }
        } catch (GoogleAuthException e) {
            Log.e(TAG, e.getMessage());
            throw new RuntimeException(e);
        }

        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(newRequest);
    }
}
