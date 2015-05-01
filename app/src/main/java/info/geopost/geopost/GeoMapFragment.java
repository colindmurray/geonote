package info.geopost.geopost;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeoMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, FragmentInteractionInterface {

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";

    private static final String PREF_CURRENT_LAT = "mCurrentLat";
    private static final String PREF_CURRENT_LON = "mCurrentLon";
    private static final String PREF_LAST_LAT = "mLastLat";
    private static final String PREF_LAST_LON = "mLastLon";

    // Conversion from kilometers to meters
    private static final float DEFAULT_SEARCH_DISTANCE = 1000.0f;

    private GoogleMap mMap;
    private static final String TAG = GeoMapFragment.class.getSimpleName();
    private FloatingActionButton mPostButton;
    private LatLng mCurrentLocation = new LatLng(0.0, 0.0);
    private LatLng mLastLocation = new LatLng(0.0, 0.0);
    //    private HashMap<String, Marker> mMapMarkers = new HashMap<>();
    private HashMap<Marker, GeoPostMarker> mGeoPostMarkers = new HashMap<>();

    // Fields for the map radius in feet
    private float mRadius = DEFAULT_SEARCH_DISTANCE;

    private CharSequence mTitle;

    // Access basic application info
    private SharedPreferences mPrefs;

    // Set map to current user location on first location event.
    private boolean zoomToUserLocation = true;
    private com.rey.material.widget.FloatingActionButton mUpvoteButton;
    private MainActivityInteractionInterface mMainActivity;
    private TextView mModalUserName;
    private TextView mModalTextBody;

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
//        setupParse();
        Log.e(TAG, "Current user is: " + ParseUser.getCurrentUser().getUsername());
//        setUpMapIfNeeded();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mMainActivity = (MainActivityInteractionInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
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
//                doMapQuery();
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
        setUpMap();
        return v;
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
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


    private LatLng latLngFromParseGeoPoint(ParseGeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (GeoPostMarker geoPostMarker : new HashSet<>(mGeoPostMarkers.values())) {
            String geoPostId = geoPostMarker.geoPostObj.getObjectId();
            if (!markersToKeep.contains(geoPostId)) {
                mGeoPostMarkers.get(geoPostId).marker.remove();
                mGeoPostMarkers.remove(geoPostId);
            }
        }
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            // Updates current location in this, MainActivity, and other fragments.
            mMainActivity.broadcastNewLocation(new LatLng(location.getLatitude(), location.getLongitude()));

            Log.d(TAG, "OnLocationChanged event - Lat: " + mCurrentLocation.latitude  +"Lon: " + mCurrentLocation.longitude);

            // disable markers now out of range, enable markers in range.
            recalculateUserMarkerDistances();
            // Perform mapQuery if current vs last location within certain distance interval.

            mMainActivity.doParseQueryIfLargeLocationChangeOrTimeout();
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

    @Override
    public void updateGeopostObjects(List<GeoPostObj> geoPostObjList) {
        Log.d(TAG, "HERE!!!!");
        Set<String> toKeep = new HashSet<>();
        for (GeoPostObj post : geoPostObjList) {

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
                mGeoPostMarkers.put(newMarker.marker, newMarker);
            }
        }

        // We call the cleanUpMarkers() method and pass in the toKeep variable to remove any unwanted markers from the map.
        cleanUpMarkers(toKeep);
    }

    @Override
    public void setCurrentLocation(LatLng currentLocation) {
        mCurrentLocation = currentLocation;
    }

    @Override
    public void onStop() {
        super.onStop();
        mMap.setMyLocationEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMap.setMyLocationEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        GeoPostMarker postMarker = mGeoPostMarkers.get(marker);
        if(mGeoPostMarkers.containsKey(marker)) {
            Log.d(TAG, "GeoPostMarker Text: " + mGeoPostMarkers.get(marker).geoPostObj.getText());
        }

        //        Testing modal
        boolean wrapInScrollView = true;
        MaterialDialog m = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.activity_modal, wrapInScrollView)
                .show();
        mModalUserName = (TextView) m.findViewById(R.id.postUserNameTextView);
        mModalUserName.setText(postMarker.geoPostObj.getUser().getUsername());
        mModalTextBody = (TextView) m.findViewById(R.id.postTextBody);
        mModalTextBody.setText(postMarker.geoPostObj.getText());
        mUpvoteButton = (com.rey.material.widget.FloatingActionButton) m.findViewById(R.id.upvote_button);
        mUpvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                mUpvoteButton.setBackgroundColor(getResources().getColor(R.color.green));
            }
        });
        return true;
    }

}
