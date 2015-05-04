package info.geopost.geopost;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by colin on 4/30/15.
 */
public interface FragmentInteractionInterface {
    // TODO: Update argument type and name
    public void updateGeopostObjects(List<GeoPostObj> geoPostObjList);
    public void setCurrentLocation(LatLng currentLocation);
}