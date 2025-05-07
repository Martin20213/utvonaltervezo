// app/src/main/java/com/example/utvonaltervezo/presentation/ui/MainActivity.java
package com.example.utvonaltervezo.presentation.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.example.utvonaltervezo.data.repository.GoogleMapsRouteRepository;
import com.example.utvonaltervezo.domain.interactor.GetRouteInteractor;
import com.example.utvonaltervezo.domain.usecase.GetRouteUseCase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

// Presentation réteg
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
    private Polyline currentRoute;

    private String timezone = "Europe/Budapest";

    private GetRouteUseCase getRouteUseCase;
    private GoogleMapsRouteRepository routeRepository; // Itt kellene inicializálni a repository-t

    // API kulcsot itt tároljuk.
    private static final String API_KEY = "AIzaSyDk9jTdaImBhQXKhauibuwymie67ojrj5s"; // TODO: Cserélje ki a saját API kulcsára!
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // Handler a fő szálhoz

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

        // Inicializáljuk a szálkezelőt és a repository-t.
        routeRepository = new GoogleMapsRouteRepository(); // Inicializáljuk a GoogleMapsRouteRepository-t
        RoutePresenter presenter = new RoutePresenterImplement(this);
        getRouteUseCase = new GetRouteInteractor(routeRepository, presenter);

        // Gombok eseménykezelői
        planRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startText = startPointEditText.getText().toString().trim();
                String endText = endPointEditText.getText().toString().trim();

                if (TextUtils.isEmpty(startText)) {
                    Toast.makeText(MainActivity.this, "Add meg a kiinduló pontot!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(endText)) {
                    Toast.makeText(MainActivity.this, "Add meg a célpontot!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    getRouteUseCase.execute(startText, endText, travelMode);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Hiba történt az útvonal tervezése során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        bikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "bicycling";
                Toast.makeText(MainActivity.this, "Biciklivel fogunk tervezni!", Toast.LENGTH_SHORT).show();
            }
        });

        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "walking";
                Toast.makeText(MainActivity.this, "Gyalogosan fogunk tervezni!", Toast.LENGTH_SHORT).show();
            }
        });

        carButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "driving";
                Toast.makeText(MainActivity.this, "Autóval fogunk tervezni!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            updateLocationUI();
            getDeviceLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hiba a helymeghatározás során: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        try {
            updateLocationUI();
            getDeviceLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hiba a helymeghatározás során: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

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

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng, DEFAULT_ZOOM));

                            String locationText = lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude();
                            startPointEditText.setText(locationText);
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } else {
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hiba a helymeghatározás során: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

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
            Toast.makeText(this, "Hiba a helymeghatározás során: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void displayRoute(List<LatLng> decodedPath, String durationText, long durationValue) {
        // A térkép kezelése a fő szálon kell történjen
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (currentRoute != null) {
                    currentRoute.remove();
                }
                currentRoute = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));

                TextView durationTextView = findViewById(R.id.durationTextView);
                String hunDurationText = durationText
                        .replace("hours", "óra")
                        .replace("hour", "óra")
                        .replace("mins", "perc")
                        .replace("min", "perc");
                durationTextView.setText("Útvonal időtartama: " + hunDurationText);

                String arrivalTime = calculateArrivalTime(durationValue);
                TextView arrivalTimeTextView = findViewById(R.id.arrivalTimeTextView);
                arrivalTimeTextView.setText("Érkezési idő: " + arrivalTime);
            }
        });
    }

    public void showErrorMessage(String message) {
        // Toast megjelenítése a fő szálon
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateArrivalTime(long durationValue) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone)); // Kifejezetten magyar időzóna
        calendar.add(Calendar.SECOND, (int) durationValue);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone(timezone)); // Formázás is magyar időzónával

        Date arrivalTime = calendar.getTime();
        return sdf.format(arrivalTime);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        routeRepository.shutdown();
    }
}

