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
    private Drawable upvote_pressed;
    private Drawable downvote_pressed;
    private Drawable upvote_unpressed;
    private Drawable downvote_unpressed;
//    private int mCurrentVote;
    private int mLastVote;
    private TextView mVoteRatio;
    private CurrentVoteGetterSetter mCurrentVote;

    public GeoPostObj getmGeoPostObj() {
        return mGeoPostObj;
    }

    public GeoCard(Context context, GeoPostObj geoPostObj, ParseObject userData, CurrentVoteGetterSetter currentVote) {
        super(context, R.layout.card_layout);
        mGeoPostObj = geoPostObj;
        mUserData = userData;
        mCurrentVote = currentVote;
        initialize();
    }

    public GeoCard(Context context, GeoPostObj geoPostObj, int innerLayout, ParseObject userData, CurrentVoteGetterSetter currentVote) {
        super(context, innerLayout);
        mGeoPostObj = geoPostObj;
        mUserData = userData;
        mCurrentVote = currentVote;
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
        mLastVote = mCurrentVote.getCurrentVote();
        mCurrentVote.setCurrentVote(GeoPostObj.getVoteStatus(mUserData, mGeoPostObj));

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

        upvote_pressed = parent.getResources().getDrawable(R.drawable.up_vote);
        downvote_pressed = parent.getResources().getDrawable(R.drawable.down_vote);
        upvote_unpressed = parent.getResources().getDrawable(R.drawable.up_vote_unpressed);
        downvote_unpressed = parent.getResources().getDrawable(R.drawable.down_vote_unpressed);

        updateModalVoteStatus(mCurrentVote.getCurrentVote(), mLastVote, true);

    }

    
    private View.OnClickListener mUpvoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Upvoting post: " + mGeoPostObj.getObjectId());
            if(mCurrentVote.getCurrentVote() != GeoPostObj.UPVOTE) {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.UPVOTE);
                updateModalVoteStatus(GeoPostObj.UPVOTE, mCurrentVote.getCurrentVote(), true);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote.setCurrentVote(GeoPostObj.UPVOTE);
            } else {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.NOVOTE);
                updateModalVoteStatus(GeoPostObj.NOVOTE, mCurrentVote.getCurrentVote(), true);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote.setCurrentVote(GeoPostObj.NOVOTE);
            }
        }
    };

    private View.OnClickListener mDownvoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Downvoting post: " + mGeoPostObj.getObjectId());
            if(mCurrentVote.getCurrentVote() != GeoPostObj.DOWNVOTE) {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.DOWNVOTE);
                updateModalVoteStatus(GeoPostObj.DOWNVOTE, mCurrentVote.getCurrentVote(), true);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote.setCurrentVote(GeoPostObj.DOWNVOTE);
            } else {
                GeoPostObj.updateVoteStatus(mUserData, mGeoPostObj, GeoPostObj.NOVOTE);
                updateModalVoteStatus(GeoPostObj.NOVOTE, mCurrentVote.getCurrentVote(), true);
                mVoteRatio.setText("" + mGeoPostObj.getVotes());
                mCurrentVote.setCurrentVote(GeoPostObj.NOVOTE);
            }
        }
    };

    private void updateModalVoteStatus(int voteStatus, int currentVoteStatus, boolean animation) {
        if(voteStatus == currentVoteStatus) {
            return;
        } else if (voteStatus == 1 && currentVoteStatus == -1) {
            mUpvoteButton.setIcon(upvote_pressed, animation);
            mDownvoteButton.setIcon(downvote_unpressed, animation);
        } else if (voteStatus == -1 && currentVoteStatus == 1) {
            mUpvoteButton.setIcon(upvote_unpressed, animation);
            mDownvoteButton.setIcon(downvote_pressed, animation);
        } else if (voteStatus == 0 && currentVoteStatus == 1) {
            mUpvoteButton.setIcon(upvote_unpressed, animation);
        } else if (voteStatus == 0 && currentVoteStatus == -1) {
            mDownvoteButton.setIcon(downvote_unpressed, animation);
        } else if (voteStatus == 1) {
            mUpvoteButton.setIcon(upvote_pressed, animation);
        } else if (voteStatus == -1) {
            mDownvoteButton.setIcon(downvote_pressed, animation);
        } else {
            mUpvoteButton.setIcon(upvote_unpressed, animation);
            mDownvoteButton.setIcon(downvote_unpressed, animation);
        }
    }
}
