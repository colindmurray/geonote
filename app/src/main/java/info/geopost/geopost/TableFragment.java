package info.geopost.geopost;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class  TableFragment extends Fragment implements FragmentInteractionInterface{

    private final String TAG = getTag();
    private LatLng mCurrentLocation;

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
        Bundle args = getArguments();
        if(args != null) {
            mCurrentLocation = new LatLng(args.getDouble("lat"),
                    args.getDouble("lon"));
        }
        return view;

    }

    @Override
    public void updateGeopostObjects(List<GeoPostObj> geoPostObjList) {
        Log.e(TAG, "In updateGeoPost");
        ArrayList<Card> cards = new ArrayList<>();
        for (GeoPostObj post : geoPostObjList) {
            GeoCard card = new GeoCard(getActivity(), post);
            cards.add(card);
        }
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity().getApplicationContext(),cards);
        CardListView listView = (CardListView) getActivity().findViewById(R.id.myList);
        if (listView!=null){
            listView.setAdapter(mCardArrayAdapter);
        }
    }

    @Override
    public void setCurrentLocation(LatLng currentLocation) {
        mCurrentLocation = currentLocation;
    }
}
