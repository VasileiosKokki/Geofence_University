package gr.hua.dit.android.ergasiait2021042;

import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_1;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_2;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_3;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_4;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.hua.dit.android.ergasiait2021042.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private String AUTHORITY;

    private final static int LOCATION_PERMISSION_REQUEST = 4;

    private List<Circle> circleList = new ArrayList<>();

    private Location lastKnownLocation;


    private Marker currentMarker;

    private GPSBroadcastReceiver globalReceiver;

    private BroadcastReceiver localreceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AUTHORITY = this.getPackageName();


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        IntentFilter filter = new IntentFilter(LocationService.LOCATION_UPDATE_ACTION);
        localreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle location update in your MainActivity
                Location location = intent.getParcelableExtra("location");
                if (location != null) {
                    // Process the location update
                    lastKnownLocation = location;
                    changeDisplayedLocation();
//                    onLocationChange();
                } else{
//                    int a = 4;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(localreceiver, filter);


        findViewById(R.id.buttonCancel).setOnClickListener(view -> {
//            SessionManager.stopSession(this);
            finish(); // Finish the current activity and go back to the previous one
        });


        List<LatLng> alreadyInsertedPoints = new ArrayList<>();
        findViewById(R.id.buttonStart).setOnClickListener(view -> {
            if (SessionManager.isSessionExpired()) {
                SessionManager.startSession(this);
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), LocationService.class);
                intent.setAction("reset");
                startService(intent);
                alreadyInsertedPoints.clear();
            }
                LatLng latLng;
                for (int i = 0; i < circleList.size(); i++) {
                    Circle circle = circleList.get(i);
                    latLng = circle.getCenter();
                    if (!alreadyInsertedPoints.contains(latLng)) {
                        insertPoint(latLng, "Center");
                        alreadyInsertedPoints.add(latLng);
                    }
                }
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), LocationService.class);
                intent.setAction("getCircleCenters");
                startService(intent);


        });

        // GPSBroadcastReceiver globalReceiver = new GPSBroadcastReceiver();
        // prokalei memory leaks etsi opos mas to deiksate
        this.globalReceiver = new GPSBroadcastReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        MapsActivity.this.registerReceiver(globalReceiver, filter2);



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
        mMap.setOnMapLongClickListener(latLng -> {
            boolean itemRemoved = false;
            for (int i = 0; i < circleList.size(); i++) {
                Circle circle = circleList.get(i);
                if (isPointInsideCircle(latLng, circle)) {
                    // Remove the clicked circle from the map and the list
                    circle.remove();
                    circleList.remove(i);
                    i--; // Adjust index after removing an item
                    itemRemoved = true;
                    break;
                }
            }


            if (itemRemoved == true){
                return;
            }

            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.radius(100);
            circleOptions.strokeColor(Color.RED);
            circleOptions.visible(true);
            Circle newCircle = mMap.addCircle(circleOptions);
            circleList.add(newCircle);

        });

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
            // Add a marker in Sydney and move the camera
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



    private boolean isPointInsideCircle(LatLng point, Circle circle) {
        if (circle == null || circle.getCenter() == null) {
            // Handle the case where the circle or its center is null (return false or throw an exception)
            return false;
        }

        float[] distance = new float[1];
        Location.distanceBetween(point.latitude, point.longitude,
                circle.getCenter().latitude, circle.getCenter().longitude, distance);
        return distance[0] < circle.getRadius();
    }

    private void insertPoint(LatLng latLng, String type){
        if (!SessionManager.isSessionExpired()) {
            Integer sessionId = SessionManager.getCurrentSessionId(this);
            Double longitude = latLng.longitude;
            Double latitude = latLng.latitude;

            ContentResolver resolver = this.getContentResolver();
            Uri uri = Uri.parse("content://" + AUTHORITY + "/points/add");
            ContentValues values = new ContentValues();
            values.put(FIELD_1, sessionId);
            values.put(FIELD_2, type);
            values.put(FIELD_3, longitude);
            values.put(FIELD_4, latitude);

            // Insert data using the ContentResolver
            Uri insertedUri = resolver.insert(uri, values);

            if (insertedUri != null) {
                // Data inserted successfully
                Log.d("ContentProvider", "Data inserted successfully at " + insertedUri.toString());
            } else {
                // Insert failed, handle the error
                Log.e("ContentProvider", "Failed to insert data");
            }
        }


    }




    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localreceiver);
        if (globalReceiver != null) {
            unregisterReceiver(globalReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



    public void InstantiateLocation(){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LocationService.class);
        intent.setAction("trigger_broadcast_for_last_known_location");
        startService(intent);
    }

}