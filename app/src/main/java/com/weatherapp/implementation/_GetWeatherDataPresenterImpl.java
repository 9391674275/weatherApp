package com.weatherapp.implementation;

import com.weatherapp.network.APIClient;
import com.weatherapp.network.ApiInterface;
import com.weatherapp.model.response.WeatherInfoResponse;
import com.weatherapp.interfaces.GetWeatherDataPresenter;
import com.weatherapp.interfaces.GetWeatherMainView;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class _GetWeatherDataPresenterImpl implements GetWeatherDataPresenter {
    GetWeatherMainView mainView;

    public _GetWeatherDataPresenterImpl(GetWeatherMainView mainView) {
        this.mainView = mainView;
    }

    ApiInterface apiInterface = APIClient.getClient().create(ApiInterface.class);


    @Override
    public void getWeather(final String city_name, final String app_Id) {

        mainView.showProgress();

        Call<WeatherInfoResponse> call = apiInterface.getWeather(city_name, app_Id);

        call.enqueue(new Callback<WeatherInfoResponse>() {
            @Override
            public void onResponse(Call<WeatherInfoResponse> call, Response<WeatherInfoResponse> response) {

                mainView.hideProgress();
                if (response.isSuccessful()) {
                    if (response == null) {
                        mainView.showProgress();
                    } else {
                        mainView.hideProgress();
                        mainView.getWeatherResponse(response.body());
                    }

                } else {

                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        mainView.failure(jsonObject.getString("errorMessage"), true);

                    } catch (Exception e) {
                        mainView.failure(e.getMessage(), false);
                    }

                }


            }

            @Override
            public void onFailure(Call<WeatherInfoResponse> call, Throwable t) {

                mainView.hideProgress();
                mainView.failure(t.getMessage(), false);
            }
        });

    }
}
