package com.example.utvonaltervezo.domain.model;

import com.google.gson.annotations.SerializedName;

public class Duration {
    @SerializedName("text")
    private String text;

    @SerializedName("value")
    private long value;

    public String getText() {
        return text;
    }

    public long getValue() {
        return value;
    }
}
