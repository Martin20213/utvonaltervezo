
// app/src/main/java/com/example/utvonaltervezo/domain/model/OverviewPolyline.java
package com.example.utvonaltervezo.domain.model;

import org.json.JSONException;
import org.json.JSONObject;

// Domain réteg
public class OverviewPolyline {
    private String points;

    public OverviewPolyline(String points) {
        this.points = points;
    }

    public String getPoints() {
        return points;
    }

    static OverviewPolyline fromJson(JSONObject json) throws JSONException {
        String points = json.getString("points");
        return new OverviewPolyline(points);
    }
}