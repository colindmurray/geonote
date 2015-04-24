package info.geopost.geopost;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GeoMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GeoMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeoMapFragment extends Fragment {

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";
    private static final int MAX_POST_SEARCH_DISTANCE = 100;
    private static final int MAX_POST_SEARCH_RESULTS = 75;
    private static final Double DISTANCE_BEFORE_PARSE_UPDATE = 0.5;
    private static final String PREF_CURRENT_LAT = "mCurrentLat";
    private static final String PREF_CURRENT_LON = "mCurrentLon";
    private static final String PREF_LAST_LAT = "mLastLat";
    private static final String PREF_LAST_LON = "mLastLon";

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;
    private static final float DEFAULT_SEARCH_DISTANCE = 1000.0f;
    private static final long PARSE_QUERY_TIMEOUT = 30000;

    private GoogleMap mMap;
    private static final String TAG = GeoMapFragment.class.getSimpleName();
    private FloatingActionButton mPostButton;
    private LatLng mCurrentLocation = new LatLng(0.0, 0.0);
    private LatLng mLastLocation = new LatLng(0.0, 0.0);
    //    private HashMap<String, Marker> mMapMarkers = new HashMap<>();
    private HashMap<String, GeoPostMarker> mGeoPostMarkers = new HashMap<>();
    private String mSelectedPostObjectId;
    private long mLastParseQueryTime;
    private LatLng mLastParseQueryLocation;
    // Fields for the map radius in feet
    private float mRadius = DEFAULT_SEARCH_DISTANCE;

    //hold on to the list returned by a Parse Query
    private List<GeoPostObj> geoPostObjList;

    private CharSequence mTitle;

    // Access basic application info
    private SharedPreferences mPrefs;

    // Set map to current user location on first location event.
    private boolean zoomToUserLocation = true;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GeoMapFragment newInstance() {
        GeoMapFragment fragment = new GeoMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GeoMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupParse();
        Log.e(TAG, "Current user is: " + ParseUser.getCurrentUser().getUsername());

//        setUpMapIfNeeded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mPostButton = (FloatingActionButton) v.findViewById(R.id.map_post_button);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 2
                LatLng myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
                // 3
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra(GeoMapFragment.INTENT_EXTRA_LOCATION, myLoc);
                startActivity(intent);
            }
        });
        MapView view = (MapView) v.findViewById(R.id.map);
        view.onCreate(savedInstanceState);
        view.onResume();
        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMap = view.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                // TODO possibly get new markers when moving map?
                doMapQuery();
            }
        });

        mPrefs = this.getActivity().getSharedPreferences("GeoNote_prefs", 0);

        if (savedInstanceState == null) {
            double currentLat = mPrefs.getFloat(PREF_CURRENT_LAT, 0);
            double currentLon = mPrefs.getFloat(PREF_CURRENT_LON, 0);
            double lastLat = mPrefs.getFloat(PREF_LAST_LAT, 0);
            double lastLon = mPrefs.getFloat(PREF_LAST_LON, 0);
            mCurrentLocation = new LatLng(currentLat, currentLon);
            mLastLocation = new LatLng(lastLat, lastLon);
            Log.d(TAG, "onCreate getting saved prefs:\n" +
                    "curLat: " + mCurrentLocation.latitude +
                    " curLon: " + mCurrentLocation.longitude +
                    " lastLat: " + mLastLocation.latitude +
                    " lastLon: " + mLastLocation.longitude);
        }

        return v;
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        // Move map camera to saved location if one exists.
        if(mCurrentLocation.latitude != 0.0 && mCurrentLocation.longitude != 0.0) {
            CameraPosition pos = new CameraPosition(mCurrentLocation, 16.0f, 0f, 0f);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            Log.d(TAG, "setUpMap restoring camera position to:\n" +
                    "curLat: " + mCurrentLocation.latitude +
                    " curLon: " + mCurrentLocation.longitude);
        }
    }

    private void setupParse() {
        ParseObject.registerSubclass(GeoPostObj.class);
    }

    private void doMapQuery() {
        // 1
        LatLng myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }
        // 2
        Log.d(TAG, "doMapQuery called.");
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // 3
        ParseQuery<GeoPostObj> mapQuery = GeoPostObj.getQuery();
        // 4
        mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);
        // 5
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        // 6
        mapQuery.findInBackground(new FindCallback<GeoPostObj>() {
            @Override
            public void done(List<GeoPostObj> objects, ParseException e) {
                // Check for errors

                // No errors, process query results
                // 1
                geoPostObjList = objects;
                mLastParseQueryTime = System.currentTimeMillis();
                mLastParseQueryLocation = mCurrentLocation;
                Set<String> toKeep = new HashSet<>();
                if (objects != null)
                    Log.d(TAG, "doMapQuery finished: " + objects.size() + " GeoPost items retrieved.");
                for (GeoPostObj post : objects) {

                    toKeep.add(post.getObjectId());
                    GeoPostMarker oldMarker = mGeoPostMarkers.get(post.getObjectId());
                    LatLng loc = latLngFromParseGeoPoint(post.getLocation());

                    if(oldMarker == null) {
                        GeoPostMarker newMarker;
                        if(getDistanceInMeters(loc, mCurrentLocation) <= mRadius) {
                            newMarker = new GeoPostMarker(post, newEnabledMarker(post),  true);
                        } else {
                            newMarker = new GeoPostMarker(post, newDisabledMarker(post),  false);
                        }
                        mGeoPostMarkers.put(post.getObjectId(), newMarker);
                    }
                }

                // We call the cleanUpMarkers() method and pass in the toKeep variable to remove any unwanted markers from the map.
                cleanUpMarkers(toKeep);
            }
        });
    }

    private ParseGeoPoint geoPointFromLocation(LatLng loc) {
        return new ParseGeoPoint(loc.latitude, loc.longitude);
    }

    private LatLng latLngFromParseGeoPoint(ParseGeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<>(mGeoPostMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                mGeoPostMarkers.get(objId).marker.remove();
                mGeoPostMarkers.remove(objId);
            }
        }
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            if(mCurrentLocation != null) {
                mLastLocation = mCurrentLocation;
            }

            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            // Set camera location if this is first location event received (map has just been opened)
            if(mMap != null && mCurrentLocation != null && zoomToUserLocation){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 16.0f));
                zoomToUserLocation = false;
            }
            Log.d(TAG, "OnLocationChanged event - Lat: " + mCurrentLocation.latitude  +"Lon: " + mCurrentLocation.longitude);

            // disable markers now out of range, enable markers in range.
            recalculateUserMarkerDistances();
            // Perform mapQuery if current vs last location within certain distance interval.
            if(mLastParseQueryLocation != null) {
                ParseGeoPoint lastLoc = new ParseGeoPoint(mLastParseQueryLocation.latitude, mLastParseQueryLocation.longitude);
                ParseGeoPoint curLoc = new ParseGeoPoint(mCurrentLocation.latitude, mCurrentLocation.longitude);
                if(curLoc.distanceInKilometersTo(lastLoc) > DISTANCE_BEFORE_PARSE_UPDATE || ((System.currentTimeMillis() - mLastParseQueryTime) > PARSE_QUERY_TIMEOUT) ) {
                    doMapQuery();
                }
            }
        }
    };

    private void recalculateUserMarkerDistances(){
        Log.d(TAG, "Recalculating user markers.");
        for(Map.Entry entry : mGeoPostMarkers.entrySet()) {
            GeoPostMarker geoPostMarker = (GeoPostMarker) entry.getValue();
            if(getDistanceInMeters(geoPostMarker.marker.getPosition(), mCurrentLocation) <= mRadius) {
                enableMarker(geoPostMarker);
            } else {
                disableMarker(geoPostMarker);
            }
        }
    }

    private void enableMarker(GeoPostMarker geoPostMarker) {
        if(!geoPostMarker.enabled) {
            Log.d(TAG, "Enabling marker. post_id: " + geoPostMarker.geoPostObj.getObjectId());
            geoPostMarker.marker.remove();
            geoPostMarker.marker = newEnabledMarker(geoPostMarker.geoPostObj);
            geoPostMarker.enabled = true;
        }
    }

    private Marker newEnabledMarker(GeoPostObj post) {
        //TODO: REDO TITLE AND SNIPPET LOGIC.
        LatLng loc = latLngFromParseGeoPoint(post.getLocation());
        MarkerOptions markerOpts =
                new MarkerOptions().position(loc);
        markerOpts =
                markerOpts.title(post.getTitle())
                        .snippet(post.getUser().getUsername())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN));
        return mMap.addMarker(markerOpts);
    }

    private void disableMarker(GeoPostMarker geoPostMarker) {
        if(geoPostMarker.enabled) {
            Log.d(TAG, "Disabling marker post_id: " + geoPostMarker.geoPostObj.getObjectId());
            LatLng loc = geoPostMarker.marker.getPosition();
            geoPostMarker.marker.remove();
            geoPostMarker.marker = newDisabledMarker(loc);
        }
    }

    private Marker newDisabledMarker(LatLng loc) {
        MarkerOptions markerOpts =
                new MarkerOptions().position(loc);
        markerOpts =
                markerOpts.title(getResources().getString(R.string.post_out_of_range))
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED));
        return mMap.addMarker(markerOpts);
    }

    private Marker newDisabledMarker(GeoPostObj post) {
        return newDisabledMarker(latLngFromParseGeoPoint(post.getLocation()));
    }

    private float getDistanceInMeters(LatLng loc1, LatLng loc2) {
        float[] results = new float[1];
        Location.distanceBetween(
                loc1.latitude,
                loc1.longitude,
                loc2.latitude,
                loc2.longitude,
                results);
        return results[0];
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
