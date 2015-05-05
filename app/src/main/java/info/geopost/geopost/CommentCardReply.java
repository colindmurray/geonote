package info.geopost.geopost;

import android.content.Context;

import com.parse.ParseObject;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Majisto on 5/4/2015.
 */
public class CommentCardReply extends Card
{
    private final GeoPostObj mGeoPostObj;
    private final GeoCommentObj mComment;

    public CommentCardReply(Context context, GeoPostObj geoPostObj, GeoCommentObj comment) {
        super(context, R.layout.card_comment_reply);
        mGeoPostObj = geoPostObj;
        mComment = comment;
    }
}
