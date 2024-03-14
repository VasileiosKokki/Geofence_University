package gr.hua.dit.android.ergasiait2021042;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GPSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGpsEnabled) {
                // GPS is enabled
                // Perform actions for GPS being enabled
//                Toast.makeText(context, "GPS is enabled", Toast.LENGTH_SHORT).show();
//                int a = 4;
                Intent intent2 = new Intent();
                intent2.setClass(context.getApplicationContext(), LocationService.class);
                context.startService(intent2);
//                Log.d("CUSTOM MESSAGE","GPS is enabled");
            } else {
                // GPS is disabled
                // Perform actions for GPS being disabled
//                Toast.makeText(context, "GPS is disabled", Toast.LENGTH_SHORT).show();
//                int b = 3;
//                Log.d("CUSTOM MESSAGE","GPS is disabled");
                Intent intent3 = new Intent(context.getApplicationContext(), LocationService.class);
                context.stopService(intent3);
            }
        }

    }
}
