
// app/src/main/java/com/example/utvonaltervezo/domain/model/Leg.java
package com.example.utvonaltervezo.domain.model;

import org.json.JSONException;
import org.json.JSONObject;

// Domain réteg
public class Leg {
    private Duration duration;

    public Leg(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    static Leg fromJson(JSONObject json) throws JSONException {
        Duration duration = Duration.fromJson(json.getJSONObject("duration"));
        return new Leg(duration);
    }
}
