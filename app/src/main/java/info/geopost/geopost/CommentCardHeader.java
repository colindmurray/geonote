package info.geopost.geopost;

import android.content.Context;

import it.gmariotti.cardslib.library.internal.Card;


/**
 * Created by Majisto on 5/4/2015.
 */
public class CommentCardHeader extends Card
{
    private final GeoPostObj mGeoPostObj;

    public CommentCardHeader(Context context, GeoPostObj mGeoPostObj) {
        super(context, R.layout.card_comment_header);
        this.mGeoPostObj = mGeoPostObj;
    }
}
