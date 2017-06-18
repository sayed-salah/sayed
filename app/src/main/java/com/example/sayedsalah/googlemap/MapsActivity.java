package com.example.sayedsalah.googlemap;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    EditText from, to;
    Button search, swap;
    TextView details;
    String source = null, dest = null;
    ArrayList<Route_details> routes_array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        routes_array = new ArrayList<Route_details>();
        from = (EditText) findViewById(R.id.fromplace);
        to = (EditText) findViewById(R.id.toplace);
        search = (Button) findViewById(R.id.find_path);
        // swap = (Button) findViewById(R.id.swap);
        details = (TextView) findViewById(R.id.distance);

        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getroutedetails();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getroutedetails();
            }
        });


    }


    private void getroutedetails() {
        if (from.getText() == null) {
            Toast.makeText(MapsActivity.this, "Check Your Inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        source = from.getText().toString();

        if (to.getText() == null) {
            Toast.makeText(MapsActivity.this, "Check Your Inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        dest = to.getText().toString();

        try {
            source = URLEncoder.encode(source, "utf-8");
            dest = URLEncoder.encode(dest, "utf-8");
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            if (source == null || source.equals("")) {
                Toast.makeText(MapsActivity.this, "Source Not Detected", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dest == null || dest.equals("")) {
                Toast.makeText(MapsActivity.this, "Destination Not Detected", Toast.LENGTH_SHORT).show();
                return;
            }
            new getroute(source, dest).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Can't Access Your Location", Toast.LENGTH_SHORT).show();

        }
        else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }




    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            Location mylocation=mMap.getMyLocation() ;

            if(dest==null || dest.equals(""))
                dest=to.getText().toString();
            if(dest==null || dest.equals("")) {
                try {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mylocation.getLatitude(), mylocation.getLongitude()), 13));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(mylocation.getLatitude(), mylocation.getLongitude())).title((""))).showInfoWindow();
                    source = mylocation.getLatitude() + "," + mylocation.getLongitude();
                }catch (Exception e)
                {
                    try {
                        Toast.makeText(MapsActivity.this, "Check Your Network And GPS", Toast.LENGTH_SHORT).show();
                    }catch (Exception ee)
                    {}
                }
            }
            else{
                source = mylocation.getLatitude() + "," + mylocation.getLongitude();
                new getroute(source, dest).execute();
            }return true;
        }
    });

    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            if(source==null || source.equals(""))
                source=from.getText().toString();
            if(source==null || source.equals(""))
                Toast.makeText(MapsActivity.this, "Source Can't Detected", Toast.LENGTH_SHORT).show();
            else{
                dest = latLng.latitude + "," + latLng.longitude;
                new getroute(source, dest).execute();
            }

        }
    });


}
    class getroute extends AsyncTask<Void, Void, Void> {

        private String s=null ,d =null;

        public getroute(String s, String d) {
            this.s = s;
            this.d = d;
        }

        @Override
        public void onPreExecute() {

            routes_array.clear();
        }

        @Override
        public Void doInBackground(Void... voids) {
            try {
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                StringBuffer stringBuffer = new StringBuffer();
                String line, json;
                HttpsURLConnection httpsURLConnection = null;



                URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + s + "&destination=" + d + "&key=AIzaSyC2N9QgsQTr-zc4S6LqC7TCjCXxVIarxyk");
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.connect();

                try {

                    inputStream = httpsURLConnection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while ((line = bufferedReader.readLine()) != null)
                        stringBuffer.append(line + "\n");
                    json = stringBuffer.toString();

                    JSONObject jsonData = new JSONObject(json);
                    JSONArray jsonRoutes = jsonData.getJSONArray("routes");
                    for (int i = 0; i < jsonRoutes.length(); i++) {
                        JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                        Route_details route = new Route_details();

                        JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                        JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                        JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                        route.distance = new Distance(jsonDistance.getString("text"));
                        route.duration = new com.example.sayedsalah.googlemap.Duration(jsonDuration.getString("text"));
                        route.endAddress = jsonLeg.getString("end_address");
                        route.startAddress = jsonLeg.getString("start_address");
                        route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                        route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                        route.points = decodePolyLine(overview_polylineJson.getString("points"));
                        routes_array.add(route);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }





    @Override
    protected void onPostExecute(Void aVoid) {
        mMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions();
        try {

            polylineOptions.addAll(routes_array.get(0).points);
            polylineOptions
                    .width(10)
                    .color(Color.GREEN);
            mMap.addPolyline(polylineOptions);
            from.setText("");
            to.setText("");
            details.setText(routes_array.get(0).duration.text + "," + "" + routes_array.get(0).distance.text);
            mMap.addMarker(new MarkerOptions().position(routes_array.get(0).endLocation).title("" + (routes_array.get(0).endAddress)))
                   ;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes_array.get(0).endLocation, 13));
            mMap.addMarker(new MarkerOptions().position(routes_array.get(0).startLocation).title(("" + routes_array.get(0).startAddress))).showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes_array.get(0).startLocation, 13));
        } catch (Exception e) {
            try {
                Toast.makeText(MapsActivity.this, "No Data Retrievable Check Your Inputs Or Netwrok Connection", Toast.LENGTH_SHORT).show();
            }catch (Exception ee)
            {}
            Log.d("OnpostExecute ", "");
        }
    }
}
    List<LatLng> decodePolyLine(final String poly) {

        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }
        return decoded;

    }

}
