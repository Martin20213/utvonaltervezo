package com.example.utvonaltervezo.data.api;

import com.example.utvonaltervezo.domain.model.RouteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleMapsApi {
    @GET("directions/json")
    Call<RouteResponse> getRoute(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );
}
