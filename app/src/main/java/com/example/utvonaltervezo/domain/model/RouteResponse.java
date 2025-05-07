
// app/src/main/java/com/example/utvonaltervezo/domain/model/RouteResponse.java
package com.example.utvonaltervezo.domain.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Domain réteg
public class RouteResponse {
    private List<Route> routes;

    public RouteResponse(List<Route> routes) {
        this.routes = routes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public static RouteResponse fromJson(JSONObject json) throws JSONException {
        List<Route> routeList = new ArrayList<>();
        JSONArray routesJson = json.getJSONArray("routes");
        for (int i = 0; i < routesJson.length(); i++) {
            routeList.add(Route.fromJson(routesJson.getJSONObject(i)));
        }
        return new RouteResponse(routeList);
    }
}
