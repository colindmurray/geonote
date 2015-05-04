package info.geopost.geopost;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Majisto on 4/24/2015.
 */
public class GeoCard extends Card
{
    protected TextView mTitle;
    protected TextView mUsername;
    protected GeoPostObj mGeoPostObj;

    public GeoPostObj getmGeoPostObj() {
        return mGeoPostObj;
    }

    public GeoCard(Context context, GeoPostObj mGeoPostObj) {
        super(context, R.layout.card_layout);
        this.mGeoPostObj = mGeoPostObj;
        initialize();
    }

    public GeoCard(Context context, GeoPostObj mGeoPostObj, int innerLayout) {
        super(context, innerLayout);
        this.mGeoPostObj = mGeoPostObj;
        initialize();
    }

    private void initialize(){
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mTitle = (TextView) parent.findViewById(R.id.textView);
        mUsername = (TextView) parent.findViewById(R.id.usernameCard);

        mUsername.setText(mGeoPostObj.getUsername());
        mTitle.setText(mGeoPostObj.getText());
    }
}
