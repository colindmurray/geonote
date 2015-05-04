package info.geopost.geopost;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFloat;
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

import org.json.JSONException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeoMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, FragmentInteractionInterface {

    private GoogleMap mMap;
    private static final String TAG = GeoMapFragment.class.getSimpleName();

    // Dialog attributes
    private MaterialDialog mMaterialDialog;
    private com.rey.material.widget.FloatingActionButton mDownVoteButton;
    private com.rey.material.widget.FloatingActionButton mUpvoteButton;
    private TextView mModalUserName;
    private TextView mModalVoteRatio;
    private TextView mModalTextBody;

    private GeoPostMarker mSelectedGeoPostMarker;
    private Handler mDelayHandler = new Handler();

    private Runnable mDelayShowDialog;
    // Current Location
    private LatLng mCurrentLocation = new LatLng(0.0, 0.0);

    private HashMap<Marker, GeoPostMarker> mGeoPostMarkers = new HashMap<>();
    private HashMap<String, GeoPostMarker> mGeoPostsIdsToMarkers = new HashMap<>();

    // Fields for the map radius in feet
    private float mRadius = MainActivity.DEFAULT_SEARCH_DISTANCE;

    // Set map to current user location on first location event.
    private MainActivityInteractionInterface mMainActivity;
    private boolean mZoomOnFirstLocationEvent = false;
    private int mCurrentVote = 0;

    private Drawable upvote_pressed;
    private Drawable downvote_pressed;
    private Drawable upvote_unpressed;
    private Drawable downvote_unpressed;
    private int mLastVote;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GeoMapFragment newInstance(LatLng location) {
        GeoMapFragment fragment = new GeoMapFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", location.latitude);
        args.putDouble("lon", location.longitude);
        fragment.setArguments(args);

        return fragment;
    }

    public GeoMapFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Current user is: " + ParseUser.getCurrentUser().getUsername());
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
        Bundle args = getArguments();
        if(args != null) {
            mCurrentLocation = new LatLng(args.getDouble("lat"),
                                          args.getDouble("lon"));
        }

        mMaterialDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.activity_modal, false).cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d(TAG, "Cancelled dialog!");

                    }
                }).showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
//                        setModalVoteStatus(mCurrentVote);

                        updateModalVoteStatus(mCurrentVote, mLastVote, true);
                    }
                }).cancelable(true).build();
        mMaterialDialog.setCanceledOnTouchOutside(true);
        mModalUserName = (TextView) mMaterialDialog.findViewById(R.id.postUserNameTextView);
        mModalTextBody = (TextView) mMaterialDialog.findViewById(R.id.postTextBody);
        mModalVoteRatio = (TextView) mMaterialDialog.findViewById(R.id.voteRatioTextView);
        mUpvoteButton = (com.rey.material.widget.FloatingActionButton) mMaterialDialog.findViewById(R.id.upvote_button);
        mUpvoteButton.setOnClickListener(mUpvoteClickListener);
        mDownVoteButton = (com.rey.material.widget.FloatingActionButton) mMaterialDialog.findViewById(R.id.downvote_button);
        mDownVoteButton.setOnClickListener(mDownvoteClickListener);
        mLastVote = 0;

        upvote_pressed = getResources().getDrawable(R.drawable.up_vote);
        downvote_pressed = getResources().getDrawable(R.drawable.down_vote);
        upvote_unpressed = getResources().getDrawable(R.drawable.up_vote_unpressed);
        downvote_unpressed = getResources().getDrawable(R.drawable.down_vote_unpressed);

        MapView view = (MapView) v.findViewById(R.id.map);
        view.onCreate(savedInstanceState);
        view.onResume();
        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMap = view.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                // Redo parse query in new location if user
//                if(mCurrentLocation != null) {
                      // TODO: make this based on last parse query location
//                    LatLng cameraLoc = mMap.getCameraPosition().target;
//                    if (getDistanceInMeters(mCurrentLocation, cameraLoc) > MainActivity.MAX_POST_SEARCH_DISTANCE) {
//                        Log.d(TAG, "User viewing map location far away from current location.  Performing new parse query.");
//                        mMainActivity.doParseQuery(cameraLoc);
//                    }
//                }
            }
        });

        setUpMap();
        return v;
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        if(mCurrentLocation != null) {
        // Move map camera to saved location if one exists.
//        if(mCurrentLocation.latitude != 0.0 && mCurrentLocation.longitude != 0.0) {
            CameraPosition pos = new CameraPosition(mCurrentLocation, 16.0f, 0f, 0f);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            Log.d(TAG, "setUpMap restoring camera position to:\n" +
                    "curLat: " + mCurrentLocation.latitude +
                    " curLon: " + mCurrentLocation.longitude);
        } else {
            mZoomOnFirstLocationEvent = true;
        }
    }


    private LatLng latLngFromParseGeoPoint(ParseGeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (Map.Entry<String, GeoPostMarker> entry : mGeoPostsIdsToMarkers.entrySet()) {
            if (!markersToKeep.contains(entry.getKey())) {
                mGeoPostMarkers.get(entry.getValue().marker).marker.remove();
                mGeoPostMarkers.remove(entry.getValue().marker);
                mGeoPostsIdsToMarkers.remove(entry.getKey());
            }
        }
    }


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            // Updates current location in this, MainActivity, and other fragments.
            mMainActivity.broadcastNewLocation(new LatLng(location.getLatitude(), location.getLongitude()));

            // Backup if location isn't determined before this event.
            if(mZoomOnFirstLocationEvent) {
                CameraPosition pos = new CameraPosition(mCurrentLocation, 16.0f, 0f, 0f);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));
                Log.d(TAG, "Moving camera position to:\n" +
                        "curLat: " + mCurrentLocation.latitude +
                        " curLon: " + mCurrentLocation.longitude);
                mZoomOnFirstLocationEvent = false;
            }

            Log.d(TAG, "OnLocationChanged event - Lat: " + mCurrentLocation.latitude  +"Lon: " + mCurrentLocation.longitude);

            // disable markers now out of range, enable markers in range.
            recalculateUserMarkerDistances();

            // Perform mapQuery if current vs last location within certain distance interval.
            mMainActivity.doParseQueryIfLargeLocationChangeOrTimeout();
        }
    };

    private void recalculateUserMarkerDistances(){
        Log.d(TAG, "Recalculating user markers.");
        for(GeoPostMarker geoPostMarker : new HashSet<>(mGeoPostMarkers.values())) {
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
            LatLng loc = geoPostMarker.marker.getPosition();
            mGeoPostMarkers.remove(geoPostMarker.marker);
            geoPostMarker.marker.remove();
            geoPostMarker.marker = newEnabledMarker(loc);
            mGeoPostMarkers.put(geoPostMarker.marker, geoPostMarker);
            geoPostMarker.enabled = true;
        }
    }

    private Marker newEnabledMarker(LatLng loc) {
        MarkerOptions markerOpts =
                new MarkerOptions().position(loc)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN));
        return mMap.addMarker(markerOpts);
    }

    private Marker newEnabledMarker(GeoPostObj post) {
        return newEnabledMarker(latLngFromParseGeoPoint(post.getLocation()));
    }

    private void disableMarker(GeoPostMarker geoPostMarker) {
        if(geoPostMarker.enabled) {
            Log.d(TAG, "Disabling marker post_id: " + geoPostMarker.geoPostObj.getObjectId());
            LatLng loc = geoPostMarker.marker.getPosition();
            mGeoPostMarkers.remove(geoPostMarker.marker);
            geoPostMarker.marker.remove();
            geoPostMarker.marker = newDisabledMarker(loc);
            mGeoPostMarkers.put(geoPostMarker.marker, geoPostMarker);
        }
    }

    private Marker newDisabledMarker(LatLng loc) {
        MarkerOptions markerOpts =
                new MarkerOptions().position(loc)
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
        Log.d(TAG, "Updating GeoPost markers");
        Set<String> toKeep = new HashSet<>();
        for (GeoPostObj post : geoPostObjList) {
            toKeep.add(post.getObjectId());
            Log.d(TAG, "Adding GeoPost obj: " + post.getObjectId() + " to posts to keep");
            GeoPostMarker oldMarker = mGeoPostMarkers.get(post.getObjectId());
            LatLng loc = latLngFromParseGeoPoint(post.getLocation());
            if(oldMarker == null) {
                GeoPostMarker newMarker;
                // Determine which markers to place based on proximity to current location
                if(getDistanceInMeters(loc, mCurrentLocation) <= mRadius) {
                    Log.d(TAG, "Placing object: " + post.getObjectId() + " enabled");
                    newMarker = new GeoPostMarker(post, newEnabledMarker(post),  true);
                } else {
                    Log.d(TAG, "Placing object: " + post.getObjectId() + " diabled");
                    newMarker = new GeoPostMarker(post, newDisabledMarker(post),  false);
                }
                mGeoPostMarkers.put(newMarker.marker, newMarker);
                mGeoPostsIdsToMarkers.put(post.getObjectId(), newMarker);
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
        mDelayHandler.removeCallbacks(mDelayShowDialog);
        mMap.setMyLocationEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDelayHandler.removeCallbacks(mDelayShowDialog);
        mMap.setMyLocationEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mSelectedGeoPostMarker = mGeoPostMarkers.get(marker);
        Log.d(TAG, "Clicked geopost: " + mSelectedGeoPostMarker.geoPostObj.getObjectId());
        mLastVote = mCurrentVote;
        mCurrentVote = GeoPostObj.getVoteStatus(mMainActivity.getUserData(), mSelectedGeoPostMarker.geoPostObj);

        mModalUserName.setText(mSelectedGeoPostMarker.geoPostObj.getUser().getUsername());
        mModalTextBody.setText(mSelectedGeoPostMarker.geoPostObj.getText());
        mModalVoteRatio.setText("" + mSelectedGeoPostMarker.geoPostObj.getVotes());

        // Zoom to marker click location
        float zoom = mMap.getCameraPosition().zoom;
        // Give camera time to move to new location.  Doing this while loading the dialog ended up being
        // really slow and choppy
        CameraPosition pos = new CameraPosition(marker.getPosition(), zoom, 0f, 0f);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 400, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                mMaterialDialog.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
//                        Log.d(TAG, "setting vote: " + mCurrentVote);
//                        updateModalVoteStatus(-mCurrentVote, mCurrentVote, true);
                    }
                }, 3000);
            }

            @Override
            public void onCancel() {

            }
        });


        return true;
    }

    private void setModalVoteStatus(int voteStatus) {
        switch(voteStatus) {
            case 1:
                mUpvoteButton.setIcon(upvote_pressed, false); //getResources().getDrawable(R.drawable.up_vote), true);
                mDownVoteButton.setIcon(downvote_unpressed, false); //getResources().getDrawable(R.drawable.down_vote_unpressed), true);
                break;
            case -1:
                mUpvoteButton.setIcon(upvote_unpressed, false); //getResources().getDrawable(R.drawable.up_vote_unpressed), true);
                mDownVoteButton.setIcon(downvote_pressed, false); //getResources().getDrawable(R.drawable.down_vote), true);
                break;
            case 0:
                mUpvoteButton.setIcon(upvote_unpressed, false); //getResources().getDrawable(R.drawable.up_vote_unpressed), true);
                mDownVoteButton.setIcon(downvote_unpressed, false); //getResources().getDrawable(R.drawable.down_vote_unpressed), true);
                break;
            default:
                mUpvoteButton.setIcon(upvote_unpressed, false); //getResources().getDrawable(R.drawable.up_vote_unpressed), true);
                mDownVoteButton.setIcon(downvote_unpressed, false); //getResources().getDrawable(R.drawable.down_vote_unpressed), true );
                break;
        }
    }
    private void updateModalVoteStatus(int voteStatus, int currentVoteStatus, boolean animation) {
        if(voteStatus == currentVoteStatus) {
            return;
        } else if (voteStatus == 1 && currentVoteStatus == -1) {
            mUpvoteButton.setIcon(upvote_pressed, animation);
            mDownVoteButton.setIcon(downvote_unpressed, animation);
        } else if (voteStatus == -1 && currentVoteStatus == 1) {
            mUpvoteButton.setIcon(upvote_unpressed, animation);
            mDownVoteButton.setIcon(downvote_pressed, animation);
        } else if (voteStatus == 0 && currentVoteStatus == 1) {
            mUpvoteButton.setIcon(upvote_unpressed, animation);
        } else if (voteStatus == 0 && currentVoteStatus == -1) {
            mDownVoteButton.setIcon(downvote_unpressed, animation);
        } else if (voteStatus == 1) {
            mUpvoteButton.setIcon(upvote_pressed, animation);
        } else if (voteStatus == -1) {
            mDownVoteButton.setIcon(downvote_pressed, animation);
        } else {
            mUpvoteButton.setIcon(upvote_unpressed, animation);
            mDownVoteButton.setIcon(downvote_unpressed, animation);
        }
    }

    private View.OnClickListener mUpvoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Upvoting post: " + mSelectedGeoPostMarker.geoPostObj.getObjectId());
            if(mCurrentVote != GeoPostObj.UPVOTE) {
                GeoPostObj.updateVoteStatus(mMainActivity.getUserData(), mSelectedGeoPostMarker.geoPostObj, GeoPostObj.UPVOTE);
                updateModalVoteStatus(GeoPostObj.UPVOTE, mCurrentVote, true);
                mModalVoteRatio.setText("" + mSelectedGeoPostMarker.geoPostObj.getVotes());
                mCurrentVote = GeoPostObj.UPVOTE;
            } else {
                GeoPostObj.updateVoteStatus(mMainActivity.getUserData(), mSelectedGeoPostMarker.geoPostObj, GeoPostObj.NOVOTE);
                updateModalVoteStatus(GeoPostObj.NOVOTE, mCurrentVote, true);
                mModalVoteRatio.setText("" + mSelectedGeoPostMarker.geoPostObj.getVotes());
                mCurrentVote = GeoPostObj.NOVOTE;
            }
        }
    };

    private View.OnClickListener mDownvoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Downvoting post: " + mSelectedGeoPostMarker.geoPostObj.getObjectId());
            if(mCurrentVote != GeoPostObj.DOWNVOTE) {
                GeoPostObj.updateVoteStatus(mMainActivity.getUserData(), mSelectedGeoPostMarker.geoPostObj, GeoPostObj.DOWNVOTE);
                updateModalVoteStatus(GeoPostObj.DOWNVOTE, mCurrentVote, true);
                mModalVoteRatio.setText("" + mSelectedGeoPostMarker.geoPostObj.getVotes());
                mCurrentVote = GeoPostObj.DOWNVOTE;
            } else {
                GeoPostObj.updateVoteStatus(mMainActivity.getUserData(), mSelectedGeoPostMarker.geoPostObj, GeoPostObj.NOVOTE);
                updateModalVoteStatus(GeoPostObj.NOVOTE, mCurrentVote, true);
                mModalVoteRatio.setText("" + mSelectedGeoPostMarker.geoPostObj.getVotes());
                mCurrentVote = GeoPostObj.NOVOTE;
            }
        }
    };


}
