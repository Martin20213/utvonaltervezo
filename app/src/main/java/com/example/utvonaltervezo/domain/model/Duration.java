
// app/src/main/java/com/example/utvonaltervezo/domain/model/Duration.java
package com.example.utvonaltervezo.domain.model;

import org.json.JSONException;
import org.json.JSONObject;

// Domain réteg
public class Duration {
    private String text;
    private long value;

    public Duration(String text, long value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }
    public long getValue() { return value; }

    static Duration fromJson(JSONObject json) throws JSONException {
        String text = json.getString("text");
        long value = json.getLong("value");
        return new Duration(text, value);
    }
}
