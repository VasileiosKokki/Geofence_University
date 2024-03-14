package gr.hua.dit.android.ergasiait2021042;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    private GPSBroadcastReceiver globalReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.globalReceiver = new GPSBroadcastReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        MainActivity.this.registerReceiver(globalReceiver, filter2);


        findViewById(R.id.buttonToMaps).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });


        findViewById(R.id.buttonToResultsMaps).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ResultsMapsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.buttonEndSession).setOnClickListener(view -> {
            SessionManager.stopSession();
        });
    }

    @Override
    protected void onDestroy() {
        // SessionManager.stopSession();  it isnt called
        if (globalReceiver != null) {
            unregisterReceiver(globalReceiver);
        }
        super.onDestroy();
    }
}