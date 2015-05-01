package info.geopost.geopost;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by colin on 4/29/15.
 */
public interface MainActivityInteractionInterface {
    public List<GeoPostObj> getGeopostObjects();
    public void doParseQuery();
    public LatLng getCurrentLocation();
}
