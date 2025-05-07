
// app/src/main/java/com/example/utvonaltervezo/data/repository/GoogleMapsRouteRepository.java
package com.example.utvonaltervezo.data.repository;

import com.example.utvonaltervezo.domain.model.RouteResponse;
import com.example.utvonaltervezo.domain.repository.RouteRepository;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Data réteg
public class GoogleMapsRouteRepository implements RouteRepository {
    private ExecutorService networkExecutor;

    public GoogleMapsRouteRepository() {
        this.networkExecutor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void getRoute(String startPoint, String endPoint, String travelMode, RouteCallback callback) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + startPoint + "&destination=" + endPoint + "&mode=" + travelMode + "&key=AIzaSyDk9jTdaImBhQXKhauibuwymie67ojrj5s";
        networkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.connect();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    RouteResponse routeResponse = RouteResponse.fromJson(jsonResponse);
                    callback.onSuccess(routeResponse);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    callback.onError("Hiba történt az útvonal lekérése során.");
                }
            }
        });
    }

    public void shutdown() {
        if (networkExecutor != null) {
            networkExecutor.shutdown();
        }
    }
}
