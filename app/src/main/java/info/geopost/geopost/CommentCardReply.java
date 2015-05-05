package info.geopost.geopost;

import android.content.Context;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Majisto on 5/4/2015.
 */
public class CommentCardReply extends Card
{
    private final GeoPostObj mGeoPostObj;

    public CommentCardReply(Context context, GeoPostObj mGeoPostObj) {
        super(context, R.layout.card_comment_reply);
        this.mGeoPostObj = mGeoPostObj;
    }
}
