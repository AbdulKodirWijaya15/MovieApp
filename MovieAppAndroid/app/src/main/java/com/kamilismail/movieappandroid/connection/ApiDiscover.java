package com.kamilismail.movieappandroid.connection;

import com.kamilismail.movieappandroid.DTO.DiscoverDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiDiscover {

    //serverName
    String BASE_URL = "http://192.168.0.16:8080/";

    @GET("discover/")
    Call<DiscoverDTO> getDiscovery(@Header("Cookie") String cookie);
}
