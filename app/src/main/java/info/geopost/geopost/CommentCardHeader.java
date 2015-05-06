package info.geopost.geopost;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.rey.material.widget.FloatingActionButton;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;


/**
 * Created by Majisto on 5/4/2015.
 */
public class CommentCardHeader extends Card
{
    private final GeoPostObj mGeoPostObj;
    private int mCurrentVote;
    private final ParseObject mUserData;
    private TextView time;
    private TextView username;
    private TextView body;
    private FloatingActionButton mUpvoteButton;
    private FloatingActionButton mDownvoteButton;
    private VoteButtonLogic mVoteButtonLogic;
    private TextView mVoteRatio;
    private VoteCommentHolder mVoteCommentStatus;

    public CommentCardHeader(Context context, GeoPostObj geoPostObj, ParseObject userData, VoteCommentHolder voteCommentHolder) {
        super(context, R.layout.card_comment_header);
        mVoteCommentStatus = voteCommentHolder;
        mUserData = userData;
        mGeoPostObj = geoPostObj;
        mCurrentVote = GeoPostObj.getVoteStatus(userData, geoPostObj);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        time = (TextView) parent.findViewById(R.id.time);
        username = (TextView) parent.findViewById(R.id.comment_header_username);
        body = (TextView) parent.findViewById(R.id.comment_header_text);
        username.setText(mGeoPostObj.getUsername());
        body.setText(mGeoPostObj.getText());

        Date currentDate = mGeoPostObj.getCreatedAt();
        //JodaTime is amazing!
        LocalDateTime dateTime = new LocalDateTime(currentDate);
        LocalDateTime currentTime = new LocalDateTime();
        int numdays = Days.daysBetween(dateTime, currentTime).getDays();
        int numHours = Hours.hoursBetween(dateTime, currentTime).getHours();
        int numMinutes = Minutes.minutesBetween(dateTime, currentTime).getMinutes();
        if (numdays > 0)
            time.setText((numdays > 1) ? (numdays + " days ago.") : ("1 day ago."));
        else if (numHours > 0)
            time.setText((numHours > 1) ? (numHours + " hours ago.") : ("1 hour ago."));
        else
            time.setText((numMinutes > 1) ? (numMinutes + " minutes ago.") : ("1 minute ago."));

        mVoteRatio = (TextView) parent.findViewById(R.id.voteRatioTextView);
        mVoteRatio.setText("" + mGeoPostObj.getVotes());

        mUpvoteButton = (FloatingActionButton) parent.findViewById(R.id.upvote_button);
        mUpvoteButton.setOnClickListener(mUpvoteClickListener);
        mDownvoteButton = (FloatingActionButton) parent.findViewById(R.id.downvote_button);
        mDownvoteButton.setOnClickListener(mDownvoteClickListener);

        mVoteButtonLogic = new VoteButtonLogic(parent.getContext(), mUpvoteButton, mDownvoteButton);

        mVoteButtonLogic.setModalVoteStatusBackground(mCurrentVote);
    }

    private View.OnClickListener mUpvoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Upvoting post: " + mGeoPostObj.getObjectId());
            if(mCurrentVote != GeoPostObj.UPVOTE) {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.UPVOTE);
                mVoteButtonLogic.updateModalVoteStatusButtonBackground(GeoPostObj.UPVOTE, mCurrentVote);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote = GeoPostObj.UPVOTE;
            } else {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.NOVOTE);
                mVoteButtonLogic.updateModalVoteStatusButtonBackground(GeoPostObj.NOVOTE, mCurrentVote);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote = GeoPostObj.NOVOTE;
            }
        }
    };

    private View.OnClickListener mDownvoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Downvoting post: " + mGeoPostObj.getObjectId());
            if(mCurrentVote != GeoPostObj.DOWNVOTE) {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.DOWNVOTE);
                mVoteButtonLogic.updateModalVoteStatusButtonBackground(GeoPostObj.DOWNVOTE, mCurrentVote);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote = GeoPostObj.DOWNVOTE;
            } else {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.NOVOTE);
                mVoteButtonLogic.updateModalVoteStatusButtonBackground(GeoPostObj.NOVOTE, mCurrentVote);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote = GeoPostObj.NOVOTE;
            }
        }
    };
}
