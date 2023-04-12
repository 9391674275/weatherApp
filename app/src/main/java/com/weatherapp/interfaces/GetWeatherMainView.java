package com.weatherapp.interfaces;

import com.weatherapp.model.response.WeatherInfoResponse;

public interface GetWeatherMainView {

    void showProgress();

    void hideProgress();

    void getWeatherResponse(WeatherInfoResponse result);

    void failure(String msg, boolean noData);
}
