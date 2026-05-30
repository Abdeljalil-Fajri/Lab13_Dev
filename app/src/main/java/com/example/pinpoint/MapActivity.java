package com.example.pinpoint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

import android.graphics.drawable.BitmapDrawable;

public class MapActivity extends AppCompatActivity {

    private static final String GET_URL =
            "http://10.0.2.2/map_project/getPosition.php";

    private MapView mapView;
    private TextView tvPinCount;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        tvPinCount = findViewById(R.id.tvPinCount);
        mapView    = findViewById(R.id.mapView);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(33.5731, -7.5898));

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        loadPins();
    }

    private void loadPins() {
        tvPinCount.setText("Loading pins from server...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                GET_URL,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");
                        int count = positions.length();

                        for (int i = 0; i < count; i++) {
                            JSONObject pos = positions.getJSONObject(i);
                            double lat  = pos.getDouble("latitude");
                            double lon  = pos.getDouble("longitude");
                            String date = pos.getString("date");
                            String imei = pos.getString("imei");

                            Marker marker = new Marker(mapView);
                            marker.setPosition(new GeoPoint(lat, lon));
                            marker.setTitle("Pin " + (i + 1));
                            marker.setSnippet("Date: " + date
                                    + "\nDevice: " + imei);
                            marker.setAnchor(Marker.ANCHOR_CENTER,
                                    Marker.ANCHOR_BOTTOM);
                            marker.setIcon(getPinIcon());
                            mapView.getOverlays().add(marker);

                            Toast.makeText(getApplicationContext(),
                                    "Lat: " + lat + " Lon: " + lon,
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (count > 0) {
                            JSONObject first = positions.getJSONObject(0);
                            mapView.getController().animateTo(
                                    new GeoPoint(
                                            first.getDouble("latitude"),
                                            first.getDouble("longitude")));
                        }

                        mapView.invalidate();
                        tvPinCount.setText(count + " pin"
                                + (count != 1 ? "s" : "") + " on map");

                    } catch (JSONException e) {
                        tvPinCount.setText("Error parsing data");
                        Toast.makeText(getApplicationContext(),
                                "Parse error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    tvPinCount.setText("Could not reach server");
                    Toast.makeText(getApplicationContext(),
                            "Network error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private BitmapDrawable getPinIcon() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_pin);
        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}