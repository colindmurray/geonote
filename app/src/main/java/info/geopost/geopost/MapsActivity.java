package info.geopost.geopost;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity {

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";
    private static final int MAX_POST_SEARCH_DISTANCE = 100;
    private static final int MAX_POST_SEARCH_RESULTS = 50;
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;
    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mMarker;
    private ArrayList<Marker> mMarkers;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private FloatingActionButton mPostButton;
    private LatLng mCurrentLocation;
    private LatLng mLastLocation;
    private HashMap<String, Marker> mMapMarkers = new HashMap<>();;
    private String mSelectedPostObjectId;
    // Fields for the map radius in feet
    private float mRadius = DEFAULT_SEARCH_DISTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Parse.initialize(this, "AeKErBzCZNVuE31Tn2Cu5dezE6zRtmgmmgAndHGG", "eQuaeK38iRN9mi2YJyQ8z1HHopWoMk9XKuOsSJLf");
        ParseObject.registerSubclass(GeoPostObj.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mMarkers = new ArrayList<>();
        setUpMapIfNeeded();
//        ParseUser user = new ParseUser();
        String username = "colin";
        String password = "lovesie";
//        user.setUsername("poop");
//        user.setPassword("poop");
//        user.setEmail("getposthq@gmail.com");
//
//        user.put("phone", "512-666-1234");
//        user.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null){
//                    //Sign up was good.  Can use app.
//                }
//                else{
//                    Log.e(TAG, "Error in sign up is: " + e.toString());
//                }
//            }
//        });

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                Toast.makeText(MapsActivity.this,
                        "Logged in as " + user.getUsername() + ".", Toast.LENGTH_LONG).show();
            }
        });


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

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        doMapQuery();
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
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
    }



    private void doMapQuery() {
        // 1
        LatLng myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }
        // 2
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
                Set<String> toKeep = new HashSet<>();
                // 2
                for (GeoPostObj post : objects) {
                    // 3
                    toKeep.add(post.getObjectId());
                    // 4
                    Marker oldMarker = mMapMarkers.get(post.getObjectId());
                    // 5
                    MarkerOptions markerOpts =
                            new MarkerOptions().position(new LatLng(post.getLocation().getLatitude(), post
                                    .getLocation().getLongitude()));
                    // 6
                    if (post.getLocation().distanceInKilometersTo(myPoint) > mRadius * METERS_PER_FEET
                            / METERS_PER_KILOMETER) {
                        // Set up an out-of-range marker
                    }
                    else {
                        // Set up an in-range marker
                    }
                    // 7
                    Marker marker = mMap.addMarker(markerOpts);
                    mMapMarkers.put(post.getObjectId(), marker);
                    // 8
                    if (post.getObjectId().equals(mSelectedPostObjectId)) {
                        marker.showInfoWindow();
                        mSelectedPostObjectId = null;
                    }
                }
                // 9
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
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("Location", "Lat: " + mCurrentLocation.latitude  +"Lon: " + mCurrentLocation.longitude);
//            LatLng loc2 = new LatLng(location.getLatitude()+0.005, location.getLongitude()+0.005);
//            Log.d("Location", "Lat: " + loc2.latitude  +"Lon: " + loc2.longitude);
//            LatLng loc3 = new LatLng(location.getLatitude()-0.005, location.getLongitude());
//            Log.d("Location", "Lat: " + loc2.latitude  +"Lon: " + loc2.longitude);
//            mMarkers.add(mMap.addMarker(new MarkerOptions().position(mCurrentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
//            mMarkers.add(mMap.addMarker(new MarkerOptions().position(loc2).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
//            mMarkers.add(mMap.addMarker(new MarkerOptions().position(loc3).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));

            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 16.0f));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
        }
    };

}
