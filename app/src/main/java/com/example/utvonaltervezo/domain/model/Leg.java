package com.example.utvonaltervezo.domain.model;

import com.google.gson.annotations.SerializedName;

public class Leg {
    @SerializedName("duration")
    private Duration duration;

    public Duration getDuration() {
        return duration;
    }
}
