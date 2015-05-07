package info.geopost.geopost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rey.material.app.ToolbarManager;
import com.rey.material.widget.SnackBar;
import com.rey.material.widget.TabPageIndicator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements ToolbarManager.OnToolbarGroupChangedListener, MainActivityInteractionInterface,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";
    public static final String OBSCURE_CURRENT_LOCATION = "obscure_location";

    public static final int MAX_POST_SEARCH_DISTANCE = 300;
    public static final int MAX_POST_SEARCH_RESULTS = 75;
    public static final Double DISTANCE_BEFORE_PARSE_UPDATE = 0.5;

    public static final float DEFAULT_SEARCH_DISTANCE = 10000.0f;
    private static final long PARSE_QUERY_TIMEOUT = 30000;
    private float mRadius = DEFAULT_SEARCH_DISTANCE;

    //List populated from parse query
    private List<GeoPostObj> geoPostObjList;

    // Parse query statistics
    private long mLastParseQueryTime;
    private LatLng mLastParseQueryLocation;

    //Get location
    private LatLng mCurrentLocation = new LatLng(0.0, 0.0);

    private FragmentInteractionInterface mMapFragmentListener;
    private FragmentInteractionInterface mTableFragmentListerner;

    private DrawerLayout dl_navigator;
    private FrameLayout fl_drawer;
    private ListView lv_drawer;
    private CustomViewPager vp;
    private TabPageIndicator tpi;

    private PagerAdapter mPagerAdapter;

    private Toolbar mToolbar;
    private ToolbarManager mToolbarManager;
    private SnackBar mSnackBar;
    private Tab[] mItems = new Tab[]{Tab.MAPS, Tab.TABLE};
    private long geopoints = 9001;
    private ParseObject mUserData;

    private FloatingActionButton mPostButton;

    private Boolean mObscureLocation;
    public SharedPreferences mPrefs;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mObscureLocation = ParseUser.getCurrentUser().getBoolean(OBSCURE_CURRENT_LOCATION);

        setupParse();
        fl_drawer = (FrameLayout)findViewById(R.id.main_fl_drawer);
        lv_drawer = (ListView)findViewById(R.id.main_lv_drawer);
        mToolbar = (Toolbar)findViewById(R.id.main_toolbar);
        vp = (CustomViewPager)findViewById(R.id.main_vp);
        tpi = (TabPageIndicator)findViewById(R.id.main_tpi);
        dl_navigator = (DrawerLayout)findViewById(R.id.main_dl);
        mSnackBar = (SnackBar)findViewById(R.id.main_sn);

        mToolbarManager = new ToolbarManager(this, mToolbar, 0, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        mToolbarManager.setNavigationManager(new ToolbarManager.BaseNavigationManager(R.style.NavigationDrawerDrawable, this, mToolbar, dl_navigator) {
            @Override
            public void onNavigationClick() {
                if (mToolbarManager.getCurrentGroup() != 0)
                    mToolbarManager.setCurrentGroup(0);
                else
                    dl_navigator.openDrawer(Gravity.START);
            }

            @Override
            public boolean isBackState() {
                return super.isBackState() || mToolbarManager.getCurrentGroup() != 0;
            }

            @Override
            protected boolean shouldSyncDrawerSlidingProgress() {
                return super.shouldSyncDrawerSlidingProgress() && mToolbarManager.getCurrentGroup() == 0;
            }

        });
        mToolbarManager.registerOnToolbarGroupChangedListener(this);

        String [] navNames = new String[4];
        navNames[0] = ParseUser.getCurrentUser().getUsername();
        navNames[1] = "GeoPoints: " + geopoints;
        if (mObscureLocation){ navNames[2] = "Obscure Location: ON"; }
        else{ navNames[2] = "Obscure Location: OFF"; }
        navNames[3] = "Logout";
        ParseObject userDataPointer = ParseUser.getCurrentUser().getParseObject("UserData");
        if(userDataPointer == null) {
            mUserData = new ParseObject("UserData");
            ParseUser.getCurrentUser().put("UserData", mUserData);
            mUserData.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    ParseUser.getCurrentUser().put("UserData", mUserData);
                    ParseUser.getCurrentUser().saveInBackground();
                }
            });

        } else {
            userDataPointer.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject userData, ParseException e) {
                    mUserData = userData;
                }
            });
        }
        lv_drawer.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, navNames));
        lv_drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "Navigation item at position: " + position +" with id: " + id);
                //Switch on position here.
                switch(position){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        TextView tv3 = (TextView) view;
                        mObscureLocation = !mObscureLocation;
                        if (mObscureLocation){tv3.setText("Obscure Location: ON"); }
                        else{ tv3.setText("Obscure Location: OFF"); }
                        ParseUser.getCurrentUser().put(OBSCURE_CURRENT_LOCATION, mObscureLocation);
                        ParseUser.getCurrentUser().saveInBackground();
                        break;
                    case 3:
                        logoutParseUser();
                        break;
                }
            }
        });


        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
        vp.setAdapter(mPagerAdapter);
        tpi.setViewPager(vp);
        tpi.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mSnackBar.dismiss();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        mPostButton = (FloatingActionButton) findViewById(R.id.post_button);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LatLng myLoc = mCurrentLocation;
                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                intent.putExtra(INTENT_EXTRA_LOCATION, myLoc);
                startActivity(intent);
            }
        });
        // get last location from android device.
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        buildGoogleApiClient();
        if (location != null) {
            setLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            doParseQuery(null);
        } else {
            Log.e(TAG, "Null location!");
        }
//        onConnected(null);

    }

    private void setupParse() {
        ParseObject.registerSubclass(GeoPostObj.class);
        ParseObject.registerSubclass(GeoCommentObj.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_le_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mToolbarManager.onPrepareMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, "In IteamSelected");
        switch (item.getItemId()){
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onToolbarGroupChanged(int i, int i2) {
        mToolbarManager.notifyNavigationStateChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public List<GeoPostObj> getGeopostObjects() {
        return null;
    }

    @Override
    public void doParseQuery(LatLng location) {
        Log.d(TAG, "doMapQuery called.");
        final ParseGeoPoint myPoint;
        if(location != null) {
            myPoint = geoPointFromLocation(location);
        } else {
            myPoint = geoPointFromLocation(mCurrentLocation);
        }
        ParseQuery<GeoPostObj> mapQuery = GeoPostObj.getQuery();
        mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        mapQuery.findInBackground(new FindCallback<GeoPostObj>() {
            @Override
            public void done(List<GeoPostObj> objects, ParseException e) {
                // Check for errors

                // No errors, process query results
                // 1
                geoPostObjList = objects;
                mLastParseQueryTime = System.currentTimeMillis();
                mLastParseQueryLocation = mCurrentLocation;

                if (objects != null)
                    Log.d(TAG, "doMapQuery finished: " + objects.size() + " GeoPost items retrieved.");

                mMapFragmentListener.updateGeopostObjects(geoPostObjList);
                mTableFragmentListerner.updateGeopostObjects(geoPostObjList);
            }
        });
    }

    @Override
    public LatLng getCurrentLocation() {
        return mCurrentLocation;
    }

    @Override
    public void broadcastNewLocation(LatLng newLocation) {
        mCurrentLocation = newLocation;
        mMapFragmentListener.setCurrentLocation(mCurrentLocation);

    }

    @Override
    public void setLocation(LatLng newLocation) {
        mCurrentLocation = newLocation;
    }

    @Override
    public void doParseQueryIfLargeLocationChangeOrTimeout() {
        if(mLastParseQueryLocation != null) {
            ParseGeoPoint lastLoc = new ParseGeoPoint(mLastParseQueryLocation.latitude, mLastParseQueryLocation.longitude);
            ParseGeoPoint curLoc = new ParseGeoPoint(mCurrentLocation.latitude, mCurrentLocation.longitude);
            if(curLoc.distanceInKilometersTo(lastLoc) > DISTANCE_BEFORE_PARSE_UPDATE || ((System.currentTimeMillis() - mLastParseQueryTime) > PARSE_QUERY_TIMEOUT) ) {
                doParseQuery(null);
            }
        } else {
            doParseQuery(null);
        }
    }

    @Override
    public ParseObject getUserData() {
        return mUserData;
    }

    private ParseGeoPoint geoPointFromLocation(LatLng loc) {
        return new ParseGeoPoint(loc.latitude, loc.longitude);
    }

    private LatLng latLngFromParseGeoPoint(ParseGeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "ERROR!!!!");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    public enum Tab {
        MAPS("Maps"),
        TABLE("Table");
        private final String name;

        private Tab(String s) {
            name = s;
        }

        public boolean equalsName(String otherName){
            return (otherName != null) && name.equals(otherName);
        }

        public String toString(){
            return name;
        }

    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        Fragment[] mFragments;
        Tab[] mTabs;

        private final Field sActiveField;

         {
            Field f = null;
            try {
                Class<?> c = Class.forName("android.support.v4.app.FragmentManagerImpl");
                f = c.getDeclaredField("mActive");
                f.setAccessible(true);
            } catch (Exception e) {}

            sActiveField = f;
        }

        public PagerAdapter(FragmentManager fm, Tab[] tabs) {
            super(fm);
            mTabs = tabs;
            mFragments = new Fragment[mTabs.length];


            //dirty way to get reference of cached fragment
            try{
                ArrayList<Fragment> mActive = (ArrayList<Fragment>)sActiveField.get(fm);
                if(mActive != null){
                    for(Fragment fragment : mActive){
                        if(fragment instanceof GeoMapFragment)
                            setFragment(Tab.MAPS, fragment);
                        else if(fragment instanceof TableFragment)
                            setFragment(Tab.TABLE, fragment);
                    }
                }
            }
            catch(Exception e){
                Log.e("MainActivity", "Erorr in setting fragment.");}
        }

        private void setFragment(Tab tab, Fragment f){
            for(int i = 0; i < mTabs.length; i++)
                if(mTabs[i] == tab){
                    mFragments[i] = f;
                    break;
                }
        }

        @Override
        public Fragment getItem(int position) {
            if(mFragments[position] == null){
                switch (mTabs[position]) {
                    case MAPS:
                        mFragments[position] = GeoMapFragment.newInstance(mCurrentLocation);
                        mMapFragmentListener = (FragmentInteractionInterface) mFragments[position];
                        break;
                    case TABLE:
                        mFragments[position] = TableFragment.newInstance(mCurrentLocation);
                        mTableFragmentListerner = (FragmentInteractionInterface) mFragments[position];
                        break;
                }
            }

            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs[position].toString().toUpperCase();
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }

    private void logoutParseUser(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this , DispatchActivity.class));
                    finish();
                } //TODO Add error checking.
            }
        });
    }
}
