package info.geopost.geopost;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by colin on 4/2/15.
 */
public class GeoPostMarker {
    public GeoPostObj geoPostObj;
    public Marker marker;
    public boolean enabled;
    public GeoPostMarker(GeoPostObj obj, Marker marker, boolean enabled) {
        this.geoPostObj = obj;
        this.marker = marker;
        this.enabled = enabled;
    }
}
