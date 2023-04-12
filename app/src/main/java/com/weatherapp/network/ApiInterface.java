package com.weatherapp.network;

import com.weatherapp.model.response.WeatherInfoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @GET("weather?")
    Call<WeatherInfoResponse> getWeather(@Query("q") String city_name, @Query("appid") String appid);
}
