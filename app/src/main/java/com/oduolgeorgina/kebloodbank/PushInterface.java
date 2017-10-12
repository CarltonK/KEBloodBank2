package com.oduolgeorgina.kebloodbank;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Mark Carlton on 11/10/2017.
 */

public interface PushInterface {

    @POST("app/sendMultiplePush.php")
    Call<ServerResponse> operation(@Body ServerRequest request);
}
