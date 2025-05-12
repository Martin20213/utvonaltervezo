package com.example.utvonaltervezo.data.repository;

import com.example.utvonaltervezo.data.api.GoogleMapsApi;
import com.example.utvonaltervezo.domain.model.RouteResponse;
import com.example.utvonaltervezo.domain.repository.RouteRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleMapsRouteRepository implements RouteRepository {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private GoogleMapsApi googleMapsApi;

    public GoogleMapsRouteRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleMapsApi = retrofit.create(GoogleMapsApi.class);
    }

    @Override
    public void getRoute(String startPoint, String endPoint, String travelMode, RouteCallback callback) {
        Call<RouteResponse> call = googleMapsApi.getRoute(startPoint, endPoint, travelMode, "AIzaSyDk9jTdaImBhQXKhauibuwymie67ojrj5s");
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Hiba történt az útvonal lekérése során.");
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                callback.onError("Hiba történt az útvonal lekérése során: " + t.getMessage());
            }
        });
    }
}
