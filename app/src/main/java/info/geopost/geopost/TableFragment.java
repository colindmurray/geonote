package info.geopost.geopost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class  TableFragment extends Fragment implements FragmentInteractionInterface {

    private final String TAG = getTag();
    private LatLng mCurrentLocation;
    private MainActivityInteractionInterface mMainActivity;
    ArrayList<Card> mCardsArray = new ArrayList<>();
    HashSet<String> mPostsSet = new HashSet<>();
    CardArrayAdapter mCardArrayAdapter;
    CardListView mListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TableFragment.
     */
    public static TableFragment newInstance(LatLng location) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", location.latitude);
        args.putDouble("lon", location.longitude);
        fragment.setArguments(args);
        return fragment;
    }

    public TableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_table, container, false);
        mListView = (CardListView) view.findViewById(R.id.myList);
        mCardArrayAdapter = new CardArrayAdapter(getActivity().getApplicationContext(), mCardsArray);
        mListView.setAdapter(mCardArrayAdapter);
        Bundle args = getArguments();
        if(args != null) {
            mCurrentLocation = new LatLng(args.getDouble("lat"), args.getDouble("lon"));
        }
        return view;

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
    public void updateGeopostObjects(List<GeoPostObj> geoPostObjList) {
        Log.e(TAG, "In updateGeoPost");

        for (GeoPostObj post : geoPostObjList) {
            if(!mPostsSet.contains(post.getObjectId())) {
                mPostsSet.add(post.getObjectId());
                GeoCard card = new GeoCard(getActivity(), post, mMainActivity.getUserData());
                card.setOnClickListener(new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        GeoCard geoCard = (GeoCard) card;
                        CommentActivity.geoPostObj = geoCard.getmGeoPostObj();
                        startActivity(intent);
                    }
                });
                mCardArrayAdapter.add(card);
            }
        }
    }

    @Override
    public void setCurrentLocation(LatLng currentLocation) {
        mCurrentLocation = currentLocation;
    }

}
