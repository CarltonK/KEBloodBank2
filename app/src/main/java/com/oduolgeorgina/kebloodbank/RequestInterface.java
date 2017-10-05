package com.oduolgeorgina.kebloodbank;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Edward on 23/01/2017.
 */

@SuppressWarnings("ALL")
interface RequestInterface {

    @POST("app/")
    Call<ServerResponse> operation(@Body ServerRequest request);
    
}
