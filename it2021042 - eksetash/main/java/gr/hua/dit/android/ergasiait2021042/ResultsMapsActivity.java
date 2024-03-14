package gr.hua.dit.android.ergasiait2021042;



import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.hua.dit.android.ergasiait2021042.databinding.ActivityResultsMapsBinding;

public class ResultsMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityResultsMapsBinding binding;

    private String AUTHORITY;

    private Location lastKnownLocation;

    private final static int LOCATION_PERMISSION_REQUEST = 4;

    private static int i = 0;

    private Marker currentMarker;

    private static final double EARTH_RADIUS = 6371000; // Earth's radius in meters

    private BroadcastReceiver localreceiver;

    private GPSBroadcastReceiver globalReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AUTHORITY = this.getPackageName();

        binding = ActivityResultsMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.buttonCancel).setOnClickListener(view -> {
            finish(); // Finish the current activity and go back to the previous one
        });

        Button buttonPauseResume = findViewById(R.id.buttonPauseResume);

        if (i == 0) {
            buttonPauseResume.setText("Pause");  // Change button text
        } else {
            buttonPauseResume.setText("Resume");  // Change button text
        }

        buttonPauseResume.setOnClickListener(view -> {

            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), LocationService.class);
            if (i == 0) {
                intent.setAction("pauseLocationUpdates");
                buttonPauseResume.setText("Resume");  // Change button text
                i = 1;
            } else {
                intent.setAction("resumeLocationUpdates");
                buttonPauseResume.setText("Pause");  // Change button text
                i = 0;
            }
            startService(intent);

        });


        IntentFilter filter = new IntentFilter(LocationService.LOCATION_UPDATE_ACTION);
        localreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle location update in your MainActivity
                Location location = intent.getParcelableExtra("location");
                if (location != null) {
                    // Process the location update
                    lastKnownLocation = location;
                    mMap.clear();
                    changeDisplayedLocation();
                    getPointsAndDrawOnMap(mMap);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(localreceiver, filter);

//        GPSBroadcastReceiver globalReceiver = new GPSBroadcastReceiver();
        // prokalei memory leaks etsi opos mas to deiksate
        this.globalReceiver = new GPSBroadcastReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        ResultsMapsActivity.this.registerReceiver(globalReceiver, filter2);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        getPointsAndDrawOnMap(mMap);



        requestLocationPermissions();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void requestLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_REQUEST);
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsEnabled){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), LocationService.class);
            startService(intent);
            InstantiateLocation();
        }
    }


    private void changeDisplayedLocation(){
        if (lastKnownLocation != null) {
            LatLng currentPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Marker in Current Position"));
            float zoomLevel = 16.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoomLevel));
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
                for (int i=0; i<permissions.length; i++){
                    String permission = permissions[i];
                    if (Objects.equals(permission, Manifest.permission.ACCESS_FINE_LOCATION)){
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            requestLocationPermissions();
                        }
                    }
                }
                break;
        }
    }



    private void getPointsAndDrawOnMap(GoogleMap mMap){

        Integer sessionId = SessionManager.getCurrentSessionId(this);
        ContentResolver resolver = this.getContentResolver();
        Uri uri = Uri.parse("content://" + AUTHORITY + "/points/" + sessionId);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        LatLng alreadyAddedLatLng = null;
        List<LatLng> latLngList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            LatLng closestLatLng = null;
            double minDistance = Double.MAX_VALUE;
            do {
//                Integer sessionId = cursor.getInt(0);
                String type = cursor.getString(1);
                Double longitude = cursor.getDouble(2);
                Double latitude = cursor.getDouble(3);
//                Log.d("Custom Message", "SessionId: " + sessionId + "\n" + "Type: " + type + "\n" + "Longitude: " + longitude + "\n" + "Latitude: " + latitude);

                LatLng latLng = new LatLng(latitude, longitude);
                LatLng myLatLng = null;
                if (lastKnownLocation != null) {
                    myLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                }
                BitmapDescriptor markerIcon1 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                BitmapDescriptor markerIcon2 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
                if (type.equals("Center")) {
                    double distance = 0;

                    if (lastKnownLocation != null) {
//                    my position -> if (lastKnownLocation.getLatitude() && lastKnownLocation.getLongitude())

                        distance = haversineDistanceBetween(myLatLng.latitude, myLatLng.longitude,
                                latLng.latitude, latLng.longitude);
                    }

                    // Update closest point if the current one is closer
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestLatLng = latLng;
                    }
                    latLngList.add(latLng);
                } else if (type.equals("Entry")) {
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Entry Point").icon(markerIcon1));
                } else {
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Exit Point").icon(markerIcon2));
                }
            } while (cursor.moveToNext());

            if (closestLatLng != null) {
                alreadyAddedLatLng = closestLatLng;
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(closestLatLng);
                circleOptions.radius(100);
                circleOptions.strokeColor(Color.BLUE);
                circleOptions.visible(true);
                mMap.addCircle(circleOptions);
                // Do something with closestLatLng
            }
            cursor.close();
        }
        for (LatLng latLng : latLngList) {
            if (latLng != alreadyAddedLatLng) {
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(latLng);
                circleOptions.radius(100);
                circleOptions.strokeColor(Color.RED);
                circleOptions.visible(true);
                mMap.addCircle(circleOptions);
            }
        }
        latLngList = null;


    }

    private double haversineDistanceBetween(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance
        return EARTH_RADIUS * c;
    }

    public void InstantiateLocation(){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LocationService.class);
        intent.setAction("trigger_broadcast_for_last_known_location");
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localreceiver);
        if (globalReceiver != null) {
            unregisterReceiver(globalReceiver);
        }
        super.onDestroy();
    }
}