package projet.fst.ma.mapapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class GoogleMapActivity extends AppCompatActivity {

    private MapView mapp;
    private RequestQueue requesttt_Queue;
    private String show__Urlll = "http://10.0.2.2/map_project/getPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("prefs", MODE_PRIVATE));

        setContentView(R.layout.activity_google_map);

        mapp = findViewById(R.id.mappp);
        mapp.setTileSource(TileSourceFactory.MAPNIK);
        mapp.setBuiltInZoomControls(true);
        mapp.setMultiTouchControls(true);

        mapp.getController().setZoom(15.0);
        mapp.getController().setCenter(new GeoPoint(31.6295, -7.9811));

        requesttt_Queue = Volley.newRequestQueue(getApplicationContext());

        loadPositions();
    }

    private void loadPositions() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                show__Urlll,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray positions = response.getJSONArray("positions");

                            for (int i = 0; i < positions.length(); i++) {
                                JSONObject position = positions.getJSONObject(i);
                                double lat = position.getDouble("latitude");
                                double lng = position.getDouble("longitude");

                                Marker marker = new Marker(mapp);
                                marker.setPosition(new GeoPoint(lat, lng));
                                marker.setTitle("Marker " + (i + 1));

                                Drawable drawable = ContextCompat.getDrawable(GoogleMapActivity.this, android.R.drawable.ic_menu_mylocation);
                                if (drawable != null) {
                                    Bitmap bitmap;
                                    if (drawable instanceof BitmapDrawable) {
                                        bitmap = ((BitmapDrawable) drawable).getBitmap();
                                    } else {
                                        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(bitmap);
                                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                        drawable.draw(canvas);
                                    }
                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
                                    marker.setIcon(new BitmapDrawable(getResources(), scaledBitmap));
                                }

                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                mapp.getOverlays().add(marker);
                            }
                            mapp.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        requesttt_Queue.add(jsonObjectRequest);
    }
}
