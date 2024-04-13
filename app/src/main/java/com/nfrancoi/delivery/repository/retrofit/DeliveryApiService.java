package com.nfrancoi.delivery.repository.retrofit;

import com.nfrancoi.delivery.repository.retrofit.model.DeliveryJson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

public interface DeliveryApiService {

    @PUT("delivery")
    Call<ResponseBody> updateDelivery(@Body DeliveryJson delivery);


}
