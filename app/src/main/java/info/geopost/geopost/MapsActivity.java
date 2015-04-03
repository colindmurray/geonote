package info.geopost.geopost;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends ActionBarActivity
            implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";
    private static final int MAX_POST_SEARCH_DISTANCE = 100;
    private static final int MAX_POST_SEARCH_RESULTS = 50;
    private static final Double DISTANCE_BEFORE_PARSE_UPDATE = 0.5;
    private static final String PREF_CURRENT_LAT = "mCurrentLat";
    private static final String PREF_CURRENT_LON = "mCurrentLon";
    private static final String PREF_LAST_LAT = "mLastLat";
    private static final String PREF_LAST_LON = "mLastLon";
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;
    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;
    private static final long PARSE_QUERY_TIMEOUT = 30000;

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private FloatingActionButton mPostButton;
    private LatLng mCurrentLocation = new LatLng(0.0, 0.0);
    private LatLng mLastLocation = new LatLng(0.0, 0.0);
    private HashMap<String, Marker> mMapMarkers = new HashMap<>();
    private String mSelectedPostObjectId;
    private long mLastParseQueryTime;
    private LatLng mLastParseQueryLocation;
    // Fields for the map radius in feet
    private float mRadius = DEFAULT_SEARCH_DISTANCE;

    //navigation drawer
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    // Access basic application info
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout)findViewById(R.id.drawer_layout));

        setupParse();
        Log.e(TAG, "Current user is: " + ParseUser.getCurrentUser().getUsername());
        mPostButton = (FloatingActionButton) findViewById(R.id.map_post_button);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 2
                LatLng myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
                if (myLoc == null) {
                    Toast.makeText(MapsActivity.this,
                            "Please try again after your location appears on the map.", Toast.LENGTH_LONG).show();
                    return;
                }
                // 3
                Intent intent = new Intent(MapsActivity.this, PostActivity.class);
                intent.putExtra(MapsActivity.INTENT_EXTRA_LOCATION, myLoc);
                startActivity(intent);
            }
        });

        mPrefs = getSharedPreferences("GeoNote_prefs", MODE_PRIVATE);

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

        setUpMapIfNeeded();

    }



    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(PREF_CURRENT_LAT, mCurrentLocation.latitude);
        outState.putDouble(PREF_CURRENT_LON, mCurrentLocation.longitude);
        outState.putDouble(PREF_LAST_LAT, mLastLocation.latitude);
        outState.putDouble(PREF_LAST_LON, mLastLocation.longitude);
        Log.d(TAG, "Saving instance state:\n" +
                "curLat: " + mCurrentLocation.latitude +
                " curLon: " + mCurrentLocation.longitude +
                " lastLat: " + mLastLocation.latitude +
                " lastLon: " + mLastLocation.longitude);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        double currentLat = outState.getDouble(PREF_CURRENT_LAT, 0.0);
        double currentLon = outState.getDouble(PREF_CURRENT_LON, 0.0);
        double lastLat = outState.getDouble(PREF_LAST_LAT, 0.0);
        double lastLon = outState.getDouble(PREF_LAST_LON, 0.0);

        mCurrentLocation = new LatLng(currentLat, currentLon);
        mLastLocation = new LatLng(lastLat, lastLon);
        Log.d(TAG, "Restoring instance state:\n" +
                "curLat: " + mCurrentLocation.latitude +
                " curLon: " + mCurrentLocation.longitude +
                " lastLat: " + mLastLocation.latitude +
                " lastLon: " + mLastLocation.longitude);

    }

    private void setupParse() {
          ParseObject.registerSubclass(GeoPostObj.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        double currentLat = mPrefs.getFloat(PREF_CURRENT_LAT, 0);
        double currentLon = mPrefs.getFloat(PREF_CURRENT_LON, 0);
        double lastLat = mPrefs.getFloat(PREF_LAST_LAT, 0);
        double lastLon = mPrefs.getFloat(PREF_LAST_LON, 0);
        mCurrentLocation = new LatLng(currentLat, currentLon);
        mLastLocation = new LatLng(lastLat, lastLon);
        setUpMapIfNeeded();
        doMapQuery();
        Log.d(TAG, "onResume restoring saved prefs:\n" +
                "curLat: " + mCurrentLocation.latitude +
                " curLon: " + mCurrentLocation.longitude +
                " lastLat: " + mLastLocation.latitude +
                " lastLon: " + mLastLocation.longitude);

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putFloat(PREF_CURRENT_LAT, (float) mCurrentLocation.latitude);
        ed.putFloat(PREF_CURRENT_LON, (float) mCurrentLocation.longitude);
        ed.putFloat(PREF_LAST_LAT, (float) mLastLocation.latitude);
        ed.putFloat(PREF_LAST_LON, (float) mLastLocation.longitude);
        ed.apply();

        Log.d(TAG, "onStop saving prefs:\n" +
                "curLat: " + mCurrentLocation.latitude +
                " curLon: " + mCurrentLocation.longitude +
                " lastLat: " + mLastLocation.latitude +
                " lastLon: " + mLastLocation.longitude);
    }

    protected void setMessage() {

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                public void onCameraChange(CameraPosition position) {
                    doMapQuery();
                }
            });
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                mLastParseQueryTime = System.currentTimeMillis();
                mLastParseQueryLocation = mCurrentLocation;
                Set<String> toKeep = new HashSet<>();
                if (objects != null)
                    Log.d(TAG, "doMapQuery finished: " + objects.size() + " GeoPost items retrieved.");
                for (GeoPostObj post : objects) {
                    // 3
                    toKeep.add(post.getObjectId());
                    /*
                    We want to optimize the marker display logic and avoid adding markers that
                    are currently visible on the map view. The mapMarkers variable contains a hash
                    map of previously saved map markers. We're looking for a marker for the AnywallPost
                    object we're currently looping through. We set up oldMarker to check mapMarkers
                    for an entry corresponding to the current AnywallPost object.
                    */
                    Marker oldMarker = mMapMarkers.get(post.getObjectId());
                    // We then initialize a new MarkerOptions to hold the marker properties starting with the AnywallPost location.
                    MarkerOptions markerOpts =
                            new MarkerOptions().position(new LatLng(post.getLocation().getLatitude(), post
                                    .getLocation().getLongitude()));
                    /*
                    Next, we want to set up additional marker properties based on whether the marker
                    is within the user's search radius preference or not. We also make sure not to add
                    a marker if it already exists and has the desired properties.
                     */
                    if (post.getLocation().distanceInKilometersTo(myPoint) > mRadius * METERS_PER_FEET
                            / METERS_PER_KILOMETER) {
                        // Set up an out-of-range marker
                        if (oldMarker != null) {
                            if (oldMarker.getSnippet() == null) {
                                continue;
                            } else {
                                oldMarker.remove();
                            }
                        }
                        markerOpts =
                                markerOpts.title(getResources().getString(R.string.post_out_of_range))
                                        .icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED));
                    }
                    else {
                        // Set up an in-range marker
                        if (oldMarker != null) {
                            if (oldMarker.getSnippet() != null) {
                                continue;
                            } else {
                                oldMarker.remove();
                            }
                        }
                        markerOpts =
                                markerOpts.title(post.getText())
                                        .snippet(post.getUser().getUsername())
                                        .icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN));
                    }
                    // Next, we add the marker to the map's view and also add it to the mapMarkers hash of currently visible markers.
                    Marker marker = mMap.addMarker(markerOpts);
                    mMapMarkers.put(post.getObjectId(), marker);
                    /*
                    We keep track of the currently selected post's id in the selectedPostObjectId
                    private field. This helps maintain UI consistency whenever queries are updated
                    whilst a marker is selected. If the current AnywallPost object was previously
                    selected, we call the showInfoWindow() marker method to display the post details.
                     */
                    if (post.getObjectId().equals(mSelectedPostObjectId)) {
                        marker.showInfoWindow();
                        mSelectedPostObjectId = null;
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

    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<>(mMapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mMapMarkers.get(objId);
                marker.remove();
                mMapMarkers.get(objId).remove();
                mMapMarkers.remove(objId);
            }
        }
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            if(mCurrentLocation != null) {
                mLastLocation = mCurrentLocation;
            }

            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            // Set camera location if no saved location state exists.
            if(mMap != null && mCurrentLocation != null && mCurrentLocation.latitude == 0.0 && mCurrentLocation.longitude == 0.0){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
            }
            mCurrentLocation = currentLocation;
            Log.d(TAG, "OnLocationChanged event - Lat: " + mCurrentLocation.latitude  +"Lon: " + mCurrentLocation.longitude);
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

    //Called when item selected.
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.e(TAG, "Position is: " + position);
        switch(position){
            case 1:
                logoutParseUser();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    private void logoutParseUser(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MapsActivity.this , DispatchActivity.class));
                    finish();
            } //TODO Add error checking.
            }
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MapsActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }
}
