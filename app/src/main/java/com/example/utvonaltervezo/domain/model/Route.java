package com.example.utvonaltervezo.domain.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Route {
    @SerializedName("legs")
    private List<Leg> legs;

    @SerializedName("overview_polyline")
    private OverviewPolyline overviewPolyline;

    public List<Leg> getLegs() {
        return legs;
    }

    public OverviewPolyline getOverviewPolyline() {
        return overviewPolyline;
    }
}
