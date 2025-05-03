package com.example.utvonaltervezo.presentation.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText startPointEditText, endPointEditText;
    private RadioGroup routeModeRadioGroup;
    private Button planRouteButton;

    // Térkép és helymeghatározás
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng defaultLocation = new LatLng(47.4979, 19.0402); // Budapest
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elemek
        startPointEditText = findViewById(R.id.startPointEditText);
        endPointEditText = findViewById(R.id.endPointEditText);
        routeModeRadioGroup = findViewById(R.id.routeModeRadioGroup);
        planRouteButton = findViewById(R.id.planRouteButton);

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
                Toast.makeText(MainActivity.this, "Útvonal tervezés indítása...", Toast.LENGTH_SHORT).show();
                // Itt jöhet majd a Google Directions API használata
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
}
