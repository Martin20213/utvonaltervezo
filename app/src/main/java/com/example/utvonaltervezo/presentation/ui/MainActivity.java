package com.example.utvonaltervezo.presentation.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.utvonaltervezo.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText startPointEditText, endPointEditText;
    private Button planRouteButton, bikeButton, walkButton, carButton;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng defaultLocation = new LatLng(47.4979, 19.0402); // Budapest
    private static final float DEFAULT_ZOOM = 15f;

    private String travelMode = "driving";

    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";

    private Polyline currentRoute;  // A változó, amely tárolja az aktuális útvonalat (com.google.android.gms.maps.model.Polyline típusú)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elemek
        startPointEditText = findViewById(R.id.startPointEditText);
        endPointEditText = findViewById(R.id.endPointEditText);
        planRouteButton = findViewById(R.id.planRouteButton);
        bikeButton = findViewById(R.id.bikeButton);
        walkButton = findViewById(R.id.walkButton);
        carButton = findViewById(R.id.carButton);

        // Helymeghatározási kliens inicializálása
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Térkép fragment inicializálása
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Helymeghatározás engedély kérése
        getLocationPermission();

        // Útvonal tervezés gomb eseménykezelője
        planRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startText = startPointEditText.getText().toString().trim();
                String endText = endPointEditText.getText().toString().trim();

                // Hibakezelés
                if (TextUtils.isEmpty(startText)) {
                    Toast.makeText(MainActivity.this, "Add meg a kiinduló pontot!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(endText)) {
                    Toast.makeText(MainActivity.this, "Add meg a célpontot!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Útvonal tervezés
                planRoute(startText, endText, travelMode);
            }
        });
        // Bicikli gomb eseménykezelője
        bikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "bicycling";
                Toast.makeText(MainActivity.this, "Biciklivel fogunk tervezni!", Toast.LENGTH_SHORT).show();
            }
        });
        // Gyalogos gomb eseménykezelője
        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "walking";
                Toast.makeText(MainActivity.this, "Gyalogosan fogunk tervezni!", Toast.LENGTH_SHORT).show();
            }
        });
        // Bicikli gomb eseménykezelője
        carButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "driving";
                Toast.makeText(MainActivity.this, "Autóval fogunk tervezni!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Amikor a térkép betöltődött és kész a használatra
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();
        getDeviceLocation();
    }

    /**
     * Helymeghatározási engedély kérése
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Engedélykérés eredményének kezelése
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
        getDeviceLocation();
    }

    /**
     * Eszköz helyének lekérése
     */
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastKnownLocation = task.getResult();
                            LatLng currentLatLng = new LatLng(
                                    lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude());

                            // Térkép pozicionálása az aktuális helyre
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng, DEFAULT_ZOOM));

                            // Aktuális helyet is beállíthatjuk kezdőpontként (opcionális)
                            String locationText = lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude();
                            startPointEditText.setText(locationText);
                        } else {
                            // Ha nem sikerült lekérni a helyet, alapértelmezett helyre ugrunk
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } else {
                // Ha nincs engedély, alapértelmezett helyre ugrunk
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Térkép UI frissítése az engedélyeknek megfelelően
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void planRoute(String startPoint, String endPoint, String travelMode) {
        String url = DIRECTIONS_API_URL + "origin=" + startPoint + "&destination=" + endPoint + "&mode=" + travelMode + "&key=AIzaSyDk9jTdaImBhQXKhauibuwymie67ojrj5s";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.connect();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Útvonal rajzolása a térképen
                    drawRouteOnMap(jsonResponse);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void drawRouteOnMap(JSONObject jsonResponse) throws JSONException {
        // A routes tömb elemének kiválasztása
        JSONArray routes = jsonResponse.getJSONArray("routes");
        if (routes.length() == 0) {
            Log.e("RouteError", "Nincs útvonal a válaszban.");
            return;
        }

        JSONObject route = routes.getJSONObject(0);
        JSONArray legs = route.getJSONArray("legs");
        if (legs.length() == 0) {
            Log.e("LegError", "Nincs láb (útszakasz) az útvonalban.");
            return;
        }

        JSONObject leg = legs.getJSONObject(0);

        // Időtartam és távolság kinyerése
        String durationText = leg.has("duration") ? leg.getJSONObject("duration").getString("text") : "Nincs információ az időtartamról";

        // Az útvonal vonalainak dekódolása
        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
        String encodedPolyline = overviewPolyline.getString("points");
        List<LatLng> decodedPath = decodePolyline(encodedPolyline);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Ha létezik előző útvonal, eltávolítjuk
                if (currentRoute != null) {
                    currentRoute.remove();
                }

                // Új útvonal hozzáadása
                currentRoute = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));

                // Útvonal időtartamának megjelenítése a TextView-ban
                TextView durationTextView = findViewById(R.id.durationTextView);
                String hunDurationText = durationText
                        .replace("hours", "óra")
                        .replace("hour", "óra")
                        .replace("mins", "perc")
                        .replace("min", "perc");
                durationTextView.setText("Útvonal időtartama: " + hunDurationText);

                // Érkezési idő kiszámítása
                String arrivalTime = calculateArrivalTime(durationText);

                // Érkezési idő megjelenítése a TextView-ban
                TextView arrivalTimeTextView = findViewById(R.id.arrivalTimeTextView);
                arrivalTimeTextView.setText("Érkezési idő: " + arrivalTime);
            }
        });
    }

    // Érkezési idő számítása
    private String calculateArrivalTime(String durationText) {
        // Jelenlegi idő lekérése
        long currentTimeMillis = System.currentTimeMillis();
        long travelDurationMillis = parseDuration(durationText); // Átalakítjuk az időtartamot milliszekundumra
        long arrivalTimeMillis = currentTimeMillis + travelDurationMillis;

        // Formázott érkezési idő
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        sdf.setTimeZone(java.util.TimeZone.getDefault()); // A helyi időzóna beállítása
        java.util.Date resultDate = new java.util.Date(arrivalTimeMillis);
        return sdf.format(resultDate);
    }





    // Segédfüggvény az időtartam parsing-jához
    private long parseDuration(String durationText) {
        String[] parts = durationText.split(" ");
        int hours = 0;
        int minutes = 0;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("hour") || parts[i].equals("hrs") || parts[i].equals("hr")) {
                hours = Integer.parseInt(parts[i - 1]);
            } else if (parts[i].equals("minute") || parts[i].equals("mins") || parts[i].equals("min")) {
                minutes = Integer.parseInt(parts[i - 1]);
            }
        }

        // Átváltás milliszekundumra
        return (hours * 3600 + minutes * 60) * 1000;
    }




    // Dekódoló függvény a polyline adatokhoz
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
}
