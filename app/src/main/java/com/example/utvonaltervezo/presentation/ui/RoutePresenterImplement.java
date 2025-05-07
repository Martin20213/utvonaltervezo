// app/src/main/java/com/example/utvonaltervezo/presentation/ui/RoutePresenterImpl.java
package com.example.utvonaltervezo.presentation.ui;

import com.example.utvonaltervezo.domain.model.RouteResponse;
import com.example.utvonaltervezo.domain.model.Route;
import com.example.utvonaltervezo.domain.model.Leg;
import com.example.utvonaltervezo.domain.model.Duration; // Import Duration
import com.example.utvonaltervezo.domain.model.OverviewPolyline; //Import OverviewPolyline
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

// Presentation réteg
public class RoutePresenterImplement implements RoutePresenter {
    private final MainActivity view;

    public RoutePresenterImplement(MainActivity view) {
        this.view = view;
    }

    @Override
    public void presentRoute(RouteResponse routeResponse) {
        if (routeResponse == null || routeResponse.getRoutes().isEmpty()) {
            view.showErrorMessage("Nem található útvonal.");
            return;
        }

        Route route = routeResponse.getRoutes().get(0);
        if (route.getLegs().isEmpty()) {
            view.showErrorMessage("Nem található útvonal.");
            return;
        }
        Leg leg = route.getLegs().get(0);
        Duration duration = leg.getDuration(); // Get Duration object
        String durationText = duration.getText();
        long durationValue = duration.getValue();
        OverviewPolyline overviewPolyline = route.getOverviewPolyline(); // Get OverviewPolyline
        String encodedPolyline = overviewPolyline.getPoints();
        List<LatLng> decodedPath = decodePolyline(encodedPolyline);

        view.displayRoute(decodedPath, durationText, durationValue);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            polyline.add(new LatLng(lat * 1E-5, lng * 1E-5));
        }
        return polyline;
    }

    private String calculateArrivalTime(long durationValue) {
        long currentTimeMillis = System.currentTimeMillis();
        long arrivalTimeMillis = currentTimeMillis + durationValue * 1000;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        Date resultDate = new Date(arrivalTimeMillis);
        return sdf.format(resultDate);
    }

    @Override
    public void presentError(String errorMessage) {
        view.showErrorMessage(errorMessage);
    }
}
