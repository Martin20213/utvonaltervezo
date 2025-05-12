// app/src/main/java/com/example/utvonaltervezo/presentation/ui/MainActivity.java
package com.example.utvonaltervezo.presentation.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;


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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private LatLng selectedStartPoint = null;
    private LatLng selectedEndPoint = null;
    private com.google.android.gms.maps.model.Marker startMarker = null;
    private com.google.android.gms.maps.model.Marker endMarker = null;

    private Geocoder geocoder;

    private boolean isStartPointJustUpdated = false;

    private enum ActivateField {NONE, START, END}
    private ActivateField activateField = ActivateField.NONE;

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
                    showToast("Add meg a kiinduló pontot!");
                    return;
                }
                if (TextUtils.isEmpty(endText)) {
                    showToast("Add meg a célpontot!");
                    activateField = ActivateField.END;
                    return;
                }
                try {
                    getRouteUseCase.execute(startText, endText, travelMode);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Hiba történt az útvonal tervezése során: "+ e.getMessage());
                }
            }
        });

        bikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "bicycling";
                showToast("Biciklivel fogunk tervezni!");
            }
        });

        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "walking";
                showToast("Gyalogosan fogunk tervezni!");
            }
        });

        carButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelMode = "driving";
                showToast("Autóval fogunk tervezni!");
            }
        });
        geocoder = new Geocoder(this, java.util.Locale.getDefault());
        startPointEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateField = ActivateField.START;
                showToast("Válassz pontot a térképen a kiinduló helyhez (hosszan nyomva)!");
            }
        });

        endPointEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateField = ActivateField.END;
                showToast("Válassz pontot a térképen a célhoz (hosszan nyomva)!");
            }
        });
        startPointEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    // Átugrás a célpont mezőre
                    endPointEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        endPointEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Bezárjuk a billentyűzetet
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    // Meghívjuk a planRouteButton onClick-ját, ha már be van állítva
                    planRouteButton.performClick();
                    return true;
                }
                return false;
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
            showToast("Hiba a helymeghatározás során: " + e.getMessage());
        }

        // Kiindulópont és célpont kiválasztása érintéssel
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String addressText = getAddressFromLatLng(latLng);

                if (activateField == ActivateField.START) {
                    selectedStartPoint = latLng;
                    startMarker = updateMarker(startMarker, latLng, "Kiindulópont");
                    startPointEditText.setText(addressText);
                    showToast("Kiindulópont beállítva!");
                    removeCurrentRoute();
                    activateField = ActivateField.NONE;
                } else if (activateField == ActivateField.END) {
                    selectedEndPoint = latLng;
                    if (endMarker != null) endMarker.remove();
                    endMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Célpont")
                            .draggable(true));
                    endPointEditText.setText(addressText);
                    showToast("Célpont beállítva!");
                    removeCurrentRoute();
                    activateField = ActivateField.NONE;
                } else {
                    showToast("Előbb válassz kiindulópontot vagy célpontot!");
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(com.google.android.gms.maps.model.Marker marker) {}

            @Override
            public void onMarkerDrag(com.google.android.gms.maps.model.Marker marker) {}

            @Override
            public void onMarkerDragEnd(com.google.android.gms.maps.model.Marker marker) {
                LatLng newPosition = marker.getPosition();
                String addressText = getAddressFromLatLng(newPosition);

                if (marker.equals(startMarker)) {
                    selectedStartPoint = newPosition;
                    startPointEditText.setText(addressText);
                    showToast("Kiindulópont áthelyezve!");
                } else if (marker.equals(endMarker)) {
                    selectedEndPoint = newPosition;
                    endPointEditText.setText(addressText);
                    showToast("Célpont áthelyezve!");
                }
                removeCurrentRoute();
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                updateStartPointToCurrentLocation();
                return false; // false: a térkép is odaugrik
            }
        });
    }
    //Segédfüggvény (setOnMapLongClickListener)
    private String getAddressFromLatLng(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Város (locality), utca (thoroughfare), házszám (subThoroughfare)
                String city = address.getLocality();
                // Ha a városnév nincs kitöltve, nézzük meg az admin area-t is (néha ott van)
                if (city == null || city.isEmpty()) {
                    city = address.getSubAdminArea();
                }
                String street = address.getThoroughfare();
                String number = address.getSubThoroughfare();

                StringBuilder sb = new StringBuilder();
                if (city != null && !city.isEmpty()) {
                    sb.append(city);
                }
                if (street != null && !street.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(street);
                }
                if (number != null && !number.isEmpty()) {
                    sb.append(" ").append(number);
                }

                // Ha sikerült város+utca(+házszám) összeállítani, azt add vissza
                if (sb.length() > 0) {
                    return sb.toString();
                }

                // Ha nincs, próbáld a teljes címet
                String fullAddress = address.getAddressLine(0);
                if (fullAddress != null && !fullAddress.isEmpty()) {
                    return fullAddress;
                }

                // Ha van hely neve (feature name), azt add vissza
                if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
                    return address.getFeatureName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Ha semmi nincs, akkor a koordináta
        return latLng.latitude + ", " + latLng.longitude;
    }
    //Segédfüggvény (setOnMyLocationButtonClickListener)
    private void updateStartPointToCurrentLocation() {
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

                            // Útvonal törlése
                            removeCurrentRoute();

                            // Kiindulópont frissítése
                            selectedStartPoint = currentLatLng;
                            String addressText = getAddressFromLatLng(currentLatLng);
                            startPointEditText.setText(addressText);

                            // Marker frissítése
                            startMarker = updateMarker(startMarker, currentLatLng, "Kiindulópont");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        isStartPointJustUpdated = true;
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
            showToast("Hiba a helymeghatározás során: " + e.getMessage());
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

                            // Kiindulópont beállítása csak induláskor
                            selectedStartPoint = currentLatLng;
                            String addressText = getAddressFromLatLng(currentLatLng);
                            startPointEditText.setText(addressText);

                            // Marker beállítása
                            startMarker = updateMarker(startMarker, currentLatLng, "Kiindulópont");

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
            showToast("Hiba a helymeghatározás során: " + e.getMessage());
        }
    }

    public void displayRoute(List<LatLng> decodedPath, String durationText, long durationValue) {
        // A térkép kezelése a fő szálon
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (currentRoute != null) {
                    currentRoute.remove();
                }
                currentRoute = mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(0xFFFFA500));

                // Új: Számold ki a határokat a megrajzolt útvonalhoz
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : decodedPath) {
                    builder.include(point);
                }
                LatLngBounds bounds = builder.build();

                // Új: Mozgasd a kamerát a megfelelő határokra
                int padding = 100; // Opcionális padding a jobb megjelenésért
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

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
    private void removeCurrentRoute() {
        if (currentRoute != null) {
            currentRoute.remove();
            currentRoute = null;
        }
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private com.google.android.gms.maps.model.Marker updateMarker(com.google.android.gms.maps.model.Marker oldMarker, LatLng position, String title) {
        if (oldMarker != null) oldMarker.remove();
        return mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .draggable(true));
    }
    private String calculateArrivalTime(long durationValue) {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        Calendar arrival = (Calendar) now.clone();
        arrival.add(Calendar.SECOND, (int) durationValue);

        SimpleDateFormat fullFormat = new SimpleDateFormat("MMM d (EEEE) HH:mm", new java.util.Locale("hu"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new java.util.Locale("hu"));
        fullFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        timeFormat.setTimeZone(TimeZone.getTimeZone(timezone));

        boolean nextDay = arrival.get(Calendar.YEAR) > now.get(Calendar.YEAR)
                || arrival.get(Calendar.DAY_OF_YEAR) > now.get(Calendar.DAY_OF_YEAR);

        if (nextDay) {
            return fullFormat.format(arrival.getTime());
        } else {
            return timeFormat.format(arrival.getTime());
        }
    }
}

