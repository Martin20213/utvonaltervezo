
// app/src/main/java/com/example/utvonaltervezo/domain/model/Route.java
package com.example.utvonaltervezo.domain.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Domain réteg
public class Route {
    private List<Leg> legs;
    private OverviewPolyline overviewPolyline;

    public Route(List<Leg> legs, OverviewPolyline overviewPolyline) {
        this.legs = legs;
        this.overviewPolyline = overviewPolyline;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public OverviewPolyline getOverviewPolyline() {
        return overviewPolyline;
    }

    static Route fromJson(JSONObject json) throws JSONException {
        List<Leg> legsList = new ArrayList<>();
        JSONArray legsJson = json.getJSONArray("legs");
        for (int i = 0; i < legsJson.length(); i++) {
            legsList.add(Leg.fromJson(legsJson.getJSONObject(i)));
        }
        OverviewPolyline overviewPolyline = OverviewPolyline.fromJson(json.getJSONObject("overview_polyline"));
        return new Route(legsList, overviewPolyline);
    }
}
