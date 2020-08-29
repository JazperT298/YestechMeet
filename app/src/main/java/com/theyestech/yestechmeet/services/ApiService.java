package com.theyestech.yestechmeet.services;


import com.theyestech.yestechmeet.notifications.MyResponse;
import com.theyestech.yestechmeet.notifications.Sender;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA-SAcS08:APA91bHlebGjNkmtlgi2NHvCsZ6xzSIWRVsQPqlyi-SPBpW_DCPPvbBoe57Rj8h2RHM-SxGZfwgMot277kegBG5VBt96QJaU5AHgoPa_zWZ0RTAp6Hc7geoOk9jpOFxvcjTBn2xKp7Pr"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String remoteBody
    );
}
