package info.geopost.geopost;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.rey.material.widget.FloatingActionButton;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Majisto on 4/24/2015.
 */
public class GeoCard extends Card
{
    private final ParseObject mUserData;
    protected TextView mTitle;
    protected TextView mUsername;
    protected GeoPostObj mGeoPostObj;
    private FloatingActionButton mUpvoteButton;
    private FloatingActionButton mDownvoteButton;
    private TextView mVoteRatio;
    private int mCurrentVote;
    private VoteButtonLogic mVoteButtonLogic;

    public GeoPostObj getmGeoPostObj() {
        return mGeoPostObj;
    }

    public GeoCard(Context context, GeoPostObj geoPostObj, ParseObject userData) {
        super(context, R.layout.card_layout);
        mGeoPostObj = geoPostObj;
        mUserData = userData;
        mCurrentVote = GeoPostObj.getVoteStatus(userData, geoPostObj);
        initialize();
    }

    public GeoCard(Context context, GeoPostObj geoPostObj, int innerLayout, ParseObject userData) {
        super(context, innerLayout);
        mGeoPostObj = geoPostObj;
        mUserData = userData;
        mCurrentVote = GeoPostObj.getVoteStatus(userData, geoPostObj);
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
        mVoteRatio = (TextView) parent.findViewById(R.id.voteRatioTextView);
        mVoteRatio.setText("" + mGeoPostObj.getVotes());
        mUsername.setText(mGeoPostObj.getUsername());
        mTitle.setText(mGeoPostObj.getText());
        mUpvoteButton = (FloatingActionButton) parent.findViewById(R.id.card_upvote_button);
        mUpvoteButton.setOnClickListener(mUpvoteClickListener);
        mDownvoteButton = (FloatingActionButton) parent.findViewById(R.id.card_downvote_button);
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
