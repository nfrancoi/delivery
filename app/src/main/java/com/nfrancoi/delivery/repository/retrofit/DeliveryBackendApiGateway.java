package com.nfrancoi.delivery.repository.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nfrancoi.delivery.repository.retrofit.model.DeliveryJson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeliveryBackendApiGateway {
    static final String TAG = DeliveryBackendApiGateway.class.getSimpleName();
    //DEV
    //static final String BASE_URL = "http://192.168.100.122:8080/";

    //Staging
    static final String BASE_URL = "https://delivery-backend-tst-f7e2cea8747c.herokuapp.com/";

    //PROD
   // static final String BASE_URL = "https://delivery-backend-prd-a2c3c3cf085b.herokuapp.com/";

    private static DeliveryBackendApiGateway deliveryBackendApiGateway;

    public static synchronized DeliveryBackendApiGateway getInstance() throws NoSuchAlgorithmException, KeyManagementException {
        if (deliveryBackendApiGateway == null) {
            deliveryBackendApiGateway = new DeliveryBackendApiGateway();

            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            HttpOauth2Interceptor httpOauth2Interceptor = new HttpOauth2Interceptor();


            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .addInterceptor(httpOauth2Interceptor)
                    .build();

            //manage dates
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            deliveryBackendApiGateway.retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return deliveryBackendApiGateway;
    }


    private Retrofit retrofit;

    public DeliveryJson retrieveDeliveryByNoteId(String noteId) throws IOException {
        DeliveryApiService deliveryApiService = retrofit.create(DeliveryApiService.class);

        Call<DeliveryJson> call = deliveryApiService.retrieveDeliveryByNoteId(noteId);

        Response<DeliveryJson> response = call.execute();
        if (!response.isSuccessful()) {
            throw new IOException(response.toString());
        }
        DeliveryJson deliveryJson = response.body();
        return deliveryJson;
    }


    public void saveDeliveryDetailsToBackendApi(DeliveryJson deliveryJson) throws IOException {
        DeliveryApiService deliveryApiService = retrofit.create(DeliveryApiService.class);

        Call<ResponseBody> call = deliveryApiService.updateDelivery(deliveryJson);

        Response<ResponseBody> response = call.execute();
        if (!response.isSuccessful()) {
            throw new IOException(response.toString());
        }
    }


}
