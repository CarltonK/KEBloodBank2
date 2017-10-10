package com.oduolgeorgina.kebloodbank;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Edward on 27/02/2017.
 */

@SuppressWarnings("ALL")
interface InterfaceRequest {
   @GET("products/")
   Call<JSONResponse> getJSON();

}
