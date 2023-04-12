package com.weatherapp.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Sys implements Serializable {
    @SerializedName("type")
    @Expose
    public int type;
    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("country")
    @Expose
    public String country;
    @SerializedName("sunrise")
    @Expose
    public int sunrise;
    @SerializedName("sunset")
    @Expose
    public int sunset;
}
