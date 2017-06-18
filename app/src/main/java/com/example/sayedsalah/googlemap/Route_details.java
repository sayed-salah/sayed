package com.example.sayedsalah.googlemap;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Sayed Salah on 6/18/2017.
 */
public class Route_details {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
