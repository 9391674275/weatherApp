package com.weatherapp.views;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.weatherapp.other.ConnectivityDetector.isConnectingToInternet;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.weatherapp.R;
import com.weatherapp.databinding.ActivityMainBinding;
import com.weatherapp.implementation._GetWeatherDataPresenterImpl;
import com.weatherapp.interfaces.GetWeatherDataPresenter;
import com.weatherapp.interfaces.GetWeatherMainView;
import com.weatherapp.model.response.WeatherInfoResponse;
import com.weatherapp.other.Constants;
import com.weatherapp.other.GPSTracker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GetWeatherMainView {
    ActivityMainBinding activityMainBinding;
    String enteredSearchString = "";
    GetWeatherDataPresenter getWeatherDataPresenter;
    GPSTracker gpsTracker;
    double latitude, longitude;
    String locality;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        checkRunTimePermission();
        getWeatherDataPresenter = new _GetWeatherDataPresenterImpl(this);
        activityMainBinding.etSearchButton.setOnClickListener(view -> {
            if (isConnectingToInternet(MainActivity.this)) {
                if (activityMainBinding.etSearch.getText() != null) {
                    if (!activityMainBinding.etSearch.getText().toString().isEmpty()) {
                        enteredSearchString = activityMainBinding.etSearch.getText().toString().trim();
                        Constants.ENTERED_LOCATION = enteredSearchString;
                        getWeatherDataPresenter.getWeather(enteredSearchString, Constants.API_KEY);
                    } else {
                        Toast.makeText(MainActivity.this, "Please provide City", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please provide valid City", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please turn on your Internet connection", Toast.LENGTH_SHORT).show();
            }

        });


    }

    @Override
    public void showProgress() {
        activityMainBinding.weatherProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void hideProgress() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activityMainBinding.weatherProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void getWeatherResponse(WeatherInfoResponse result) {
        imagLoading(activityMainBinding.icWeatherImage, result.weather.get(0).icon);
        activityMainBinding.mainWeather.setText(result.weather.get(0).main);
        activityMainBinding.descWeather.setText(result.weather.get(0).description);
        if (result.main.temp != 0.0) {
            float fahrenheit = (float) result.main.temp;
            float celsius = ((fahrenheit - 32) * 5) / 9;
            activityMainBinding.icTemp.setText("Temperature is: "+celsius + " \u2103");
            activityMainBinding.icHumidity.setText("Humidity is: "+result.main.humidity+"%");
        }
    }

    @Override
    public void failure(String msg, boolean noData) {
        Toast.makeText(this, "SOMETHING WENT WRONG", Toast.LENGTH_SHORT).show();
    }

    public void checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                gpsTracker = new GPSTracker(MainActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitude = gpsTracker.getLatitude();
                    longitude = gpsTracker.getLongitude();
                }
                getAddress(latitude, longitude);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        10);
            }
        } else {
            gpsTracker = new GPSTracker(MainActivity.this); //GPSTracker is class that is used for retrieve user current location
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gpsTracker = new GPSTracker(MainActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitude = gpsTracker.getLatitude();
                    longitude = gpsTracker.getLongitude();
                }
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Permission Required");
                    dialog.setCancelable(false);
                    dialog.setMessage("You have to Allow permission to access user location");
                    dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",
                                    getPackageName(), null));
                            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(i, 1001);
                        }
                    });
                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                } else {
                    if (Constants.ENTERED_LOCATION != null && !Constants.ENTERED_LOCATION.isEmpty()) {
                        getWeatherDataPresenter = new _GetWeatherDataPresenterImpl(this);
                        getWeatherDataPresenter.getWeather(Constants.ENTERED_LOCATION, Constants.API_KEY);
                    }
                }
                //code for deny
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        switch (requestCode) {
            case 1001:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        gpsTracker = new GPSTracker(MainActivity.this);
                        if (gpsTracker.canGetLocation()) {
                            latitude = gpsTracker.getLatitude();
                            longitude = gpsTracker.getLongitude();
                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            locality = obj.getLocality();
            if (locality != null && !locality.isEmpty()) {
                activityMainBinding.etSearch.setText(locality);
                Constants.ENTERED_LOCATION = locality;
                getWeatherDataPresenter = new _GetWeatherDataPresenterImpl(this);
                getWeatherDataPresenter.getWeather(locality.trim(), Constants.API_KEY);
            }
            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void imagLoading(ImageView img, String code) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.mipmap.ic_launcher_round);
        String url_value = "https://openweathermap.org/img/wn/" + code + "@2x.png";
        Glide.with(MainActivity.this).setDefaultRequestOptions(options).load(url_value).transition(withCrossFade()).into(img);

    }
}
