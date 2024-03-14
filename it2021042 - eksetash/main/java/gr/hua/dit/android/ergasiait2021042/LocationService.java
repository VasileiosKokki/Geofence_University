package gr.hua.dit.android.ergasiait2021042;


import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_1;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_2;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_3;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_4;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service implements LocationListener {
//
//    public interface LocationCallback {
//        void newLocation(Location location);
//    }


    private LocationManager locationManager;

    private String AUTHORITY;

    public static final String LOCATION_UPDATE_ACTION = DbProvider.AUTHORITY + ".LOCATION_UPDATE";

    private List<LatLng> centerList = new ArrayList<>();

    private Location lastKnownLocation;

    private Boolean firstTimeHere = true;

    private static final double EARTH_RADIUS = 6371000; // Earth's radius in meters

    private Boolean leftPreviousCircle = true;

    private Boolean isInAnyCircle = false;




    class LocalBinder extends Binder {
        LocationService getService(){
            return LocationService.this;
        }
    }

    private IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AUTHORITY = this.getPackageName();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (action.equals("pauseLocationUpdates")) {
                onPause();
            } else if (action.equals("resumeLocationUpdates")) {
                onResume();
            } else if (action.equals("trigger_broadcast_for_last_known_location")) {
                sendGetLastKnownLocationBroadcast();
            } else if (action.equals("reset")) {
                reset();
            } else if (action.equals("getCircleCenters")) {
                if (!SessionManager.isSessionExpired()) {
                    getCircleCenters();
                }
            }
            // Add more actions if needed
        } else {
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, this);
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onDestroy();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Notify the callback about the location change
        Intent intent = new Intent(LOCATION_UPDATE_ACTION);
        lastKnownLocation = location;
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());


        if (firstTimeHere != true) {
            isInAnyCircle = false;
            for (int i = 0; i < centerList.size(); i++) {
                LatLng circleCenter = centerList.get(i);

                // Check if the current location is inside any of the circles
                if (isPointInsideCircle(currentLatLng, circleCenter)) {
                    isInAnyCircle = true;
                    break; // No need to continue checking other circles once inside one
                }
            }

            if (isInAnyCircle) {
                if (leftPreviousCircle) {
                    // Entry point
                    insertPoint(currentLatLng, "Entry");
                    leftPreviousCircle = false;
                }
            } else {
                if (!leftPreviousCircle) {
                    // Exit point
                    insertPoint(currentLatLng, "Exit");
                    leftPreviousCircle = true;
                }
            }

        } else {
            firstTimeHere = false;
        }



    }

    public void onPause(){
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public void onResume(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, this);
            }
        }
    }


    private void sendGetLastKnownLocationBroadcast() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        Intent intent = new Intent(LOCATION_UPDATE_ACTION);
        intent.putExtra("location", lastKnownLocation);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



    private void getCircleCenters(){

        Integer sessionId = SessionManager.getCurrentSessionId(this);
        ContentResolver resolver = this.getContentResolver();
        Uri uri = Uri.parse("content://" + AUTHORITY + "/points/centers/" + sessionId);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            centerList.clear();
            do {
//                Integer sessionId = cursor.getInt(0);
//                String type = cursor.getString(1);
                Double longitude = cursor.getDouble(2);
                Double latitude = cursor.getDouble(3);
//                Log.d("Custom Message", "SessionId: " + sessionId + "\n" + "Type: " + type + "\n" + "Longitude: " + longitude + "\n" + "Latitude: " + latitude);

                LatLng latLng = new LatLng(latitude, longitude);
                // Check if latLng already exists in centerList
                centerList.add(latLng);


            } while (cursor.moveToNext());
            cursor.close();
        }

    }

    private boolean isPointInsideCircle(LatLng point, LatLng circleCenter) {
        double circleRadius = 100;
        if (circleCenter == null) {
            // Handle the case where the circle's center is null (return false or throw an exception)
            return false;
        }

//        float[] distance = new float[1];
//        Location.distanceBetween(point.latitude, point.longitude,
//                circleCenter.latitude, circleCenter.longitude, distance);
        double distance = haversineDistanceBetween(point.latitude, point.longitude,
                circleCenter.latitude, circleCenter.longitude);
        return distance < circleRadius;
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

//            if (insertedUri != null) {
//                // Data inserted successfully
//                Log.d("ContentProvider", "Data inserted successfully at " + insertedUri.toString());
//            } else {
//                // Insert failed, handle the error
//                Log.e("ContentProvider", "Failed to insert data");
//            }
        }


    }


    private void reset(){
        leftPreviousCircle = true;
        isInAnyCircle = false;
    }





}
