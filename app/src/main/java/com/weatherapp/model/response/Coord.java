package com.weatherapp.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Coord implements Serializable {
    @SerializedName("lon")
    @Expose
    public double lon;
    @SerializedName("lat")
    @Expose
    public double lat;
}
