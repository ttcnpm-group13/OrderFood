package com.example.eat.Remote;

import com.example.eat.Model.MyResponse;
import com.example.eat.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAYq0OId4:APA91bGxpW66c_u48rUJQUe2xG58nxnHLn7syS_lbL9POjPILrGM7t0lQ0QuX8x_u0SmfC-iuCm8Sz8eLTuDkGh-ThDsdS-bK5q6dCo06emHsqpbPZCRYcLCkeQu00k_lq8Jnl0kr4Dk"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
