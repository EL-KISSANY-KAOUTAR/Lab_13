package projet.fst.ma.mapapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button boutn__Map;
    private double latitudee_e;
    private double longitude_ee;
    private double altitudeee;
    private float accuracyy;
    RequestQueue request__Queuee;
    String insertt__Urlll = "http://10.0.2.2/map_project/createPosition.php";
    LocationManager location__Managerr;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        request__Queuee = Volley.newRequestQueue(getApplicationContext());
        location__Managerr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boutn__Map = findViewById(R.id.btnn__Map);
        boutn__Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GoogleMapActivity.class));
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitudee_e = location.getLatitude();
                longitude_ee = location.getLongitude();
                altitudeee = location.getAltitude();
                accuracyy = location.getAccuracy();

                Log.d(TAG, "Location Changed: Lat=" + latitudee_e + ", Lon=" + longitude_ee);

                String msg = String.format(
                        getResources().getString(R.string.new_location), latitudee_e,
                        longitude_ee, altitudeee, accuracyy);

                addPosition(latitudee_e, longitude_ee);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, "Provider Enabled: " + provider);
            }
            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d(TAG, "Provider Disabled: " + provider);
            }
        };

        // For testing: Use both GPS and Network providers with short intervals
        try {
            if (location__Managerr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location__Managerr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            }
            if (location__Managerr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location__Managerr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting location updates", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission refusée.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(Request.Method.POST,
                insertt__Urlll, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Server Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley Error: " + error.toString());
                if (error.networkResponse != null) {
                    Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));

                String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                params.put("imei", androidId);

                Log.d(TAG, "Sending Params: " + params.toString());
                return params;
            }
        };
        request__Queuee.add(request);
    }
}
