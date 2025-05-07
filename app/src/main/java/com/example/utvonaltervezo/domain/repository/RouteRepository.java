
// app/src/main/java/com/example/utvonaltervezo/domain/repository/RouteRepository.java
package com.example.utvonaltervezo.domain.repository;

import com.example.utvonaltervezo.domain.model.RouteResponse;

// Domain réteg
public interface RouteRepository {
    interface RouteCallback {
        void onSuccess(RouteResponse routeResponse);
        void onError(String errorMessage);
    }
    void getRoute(String startPoint, String endPoint, String travelMode, RouteCallback callback);
}

