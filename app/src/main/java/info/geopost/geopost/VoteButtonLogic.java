package info.geopost.geopost;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.rey.material.widget.FloatingActionButton;

/**
 * Created by colin on 5/4/15.
 */
public class VoteButtonLogic {
    private final FloatingActionButton mUpVoteButton;
    private final FloatingActionButton mDownVoteButton;
    private final Drawable upvote_pressed;
    private final Drawable downvote_pressed;
    private final Drawable upvote_unpressed;
    private final Drawable downvote_unpressed;
    private final int mDownVoteColor;
    private final int mUpVoteColor;
    private final int mNoVoteColor;

    public VoteButtonLogic(Context context, FloatingActionButton upVoteButton, FloatingActionButton downVoteButton) {
        mUpVoteButton = upVoteButton;
        mDownVoteButton = downVoteButton;

        upvote_pressed = context.getResources().getDrawable(R.drawable.up_vote);
        downvote_pressed = context.getResources().getDrawable(R.drawable.down_vote);
        upvote_unpressed = context.getResources().getDrawable(R.drawable.up_vote_unpressed);
        downvote_unpressed = context.getResources().getDrawable(R.drawable.down_vote_unpressed);

        mUpVoteColor = context.getResources().getColor(R.color.upVoteColor);
        mDownVoteColor = context.getResources().getColor(R.color.downVoteColor);
        mNoVoteColor = context.getResources().getColor(R.color.noVoteColor);
    }

    public void updateModalVoteStatusButtonIcon(int voteStatus, int currentVoteStatus, boolean animation) {
        if (voteStatus == currentVoteStatus) {
            return;
        } else if (voteStatus == 1 && currentVoteStatus == -1) {
            mUpVoteButton.setIcon(upvote_pressed, animation);
            mDownVoteButton.setIcon(downvote_unpressed, animation);
        } else if (voteStatus == -1 && currentVoteStatus == 1) {
            mUpVoteButton.setIcon(upvote_unpressed, animation);
            mDownVoteButton.setIcon(downvote_pressed, animation);
        } else if (voteStatus == 0 && currentVoteStatus == 1) {
            mUpVoteButton.setIcon(upvote_unpressed, animation);
        } else if (voteStatus == 0 && currentVoteStatus == -1) {
            mDownVoteButton.setIcon(downvote_unpressed, animation);
        } else if (voteStatus == 1) {
            mUpVoteButton.setIcon(upvote_pressed, animation);
        } else if (voteStatus == -1) {
            mDownVoteButton.setIcon(downvote_pressed, animation);
        } else {
            mUpVoteButton.setIcon(upvote_unpressed, animation);
            mDownVoteButton.setIcon(downvote_unpressed, animation);
        }
    }

    public void updateModalVoteStatusButtonBackground(int voteStatus, int currentVoteStatus) {
        if (voteStatus == currentVoteStatus) {
            return;
        } else if (voteStatus == 1 && currentVoteStatus == -1) {
            mUpVoteButton.setBackgroundColor(mUpVoteColor);
            mDownVoteButton.setBackgroundColor(mNoVoteColor);
        } else if (voteStatus == -1 && currentVoteStatus == 1) {
            mUpVoteButton.setBackgroundColor(mNoVoteColor);
            mDownVoteButton.setBackgroundColor(mDownVoteColor);
        } else if (voteStatus == 0 && currentVoteStatus == 1) {
            mUpVoteButton.setBackgroundColor(mNoVoteColor);
        } else if (voteStatus == 0 && currentVoteStatus == -1) {
            mDownVoteButton.setBackgroundColor(mNoVoteColor);
        } else if (voteStatus == 1) {
            mUpVoteButton.setBackgroundColor(mUpVoteColor);
        } else if (voteStatus == -1) {
            mDownVoteButton.setBackgroundColor(mDownVoteColor);
        } else {
            mUpVoteButton.setBackgroundColor(mNoVoteColor);
            mDownVoteButton.setBackgroundColor(mNoVoteColor);
        }
    }

    public void setModalVoteStatusIcon(int voteStatus) {
        switch(voteStatus) {
            case 1:
                mUpVoteButton.setIcon(upvote_pressed, false); //getResources().getDrawable(R.drawable.up_vote), true);
                mDownVoteButton.setIcon(downvote_unpressed, false); //getResources().getDrawable(R.drawable.down_vote_unpressed), true);
                break;
            case -1:
                mUpVoteButton.setIcon(upvote_unpressed, false); //getResources().getDrawable(R.drawable.up_vote_unpressed), true);
                mDownVoteButton.setIcon(downvote_pressed, false); //getResources().getDrawable(R.drawable.down_vote), true);
                break;
            case 0:
                mUpVoteButton.setIcon(upvote_unpressed, false); //getResources().getDrawable(R.drawable.up_vote_unpressed), true);
                mDownVoteButton.setIcon(downvote_unpressed, false); //getResources().getDrawable(R.drawable.down_vote_unpressed), true);
                break;
            default:
                mUpVoteButton.setIcon(upvote_unpressed, false); //getResources().getDrawable(R.drawable.up_vote_unpressed), true);
                mDownVoteButton.setIcon(downvote_unpressed, false); //getResources().getDrawable(R.drawable.down_vote_unpressed), true );
                break;
        }
    }

    public void setModalVoteStatusBackground(int voteStatus) {
        switch(voteStatus) {
            case 1:
                mUpVoteButton.setBackgroundColor(mUpVoteColor);
                mDownVoteButton.setBackgroundColor(mNoVoteColor);
                break;
            case -1:
                mUpVoteButton.setBackgroundColor(mNoVoteColor);
                mDownVoteButton.setBackgroundColor(mDownVoteColor);
                break;
            case 0:
                mUpVoteButton.setBackgroundColor(mNoVoteColor);
                mDownVoteButton.setBackgroundColor(mNoVoteColor);
                break;
            default:
                mUpVoteButton.setBackgroundColor(mNoVoteColor);
                mDownVoteButton.setBackgroundColor(mNoVoteColor);
                break;
        }
    }

    public FloatingActionButton getmDownVoteButton() {
        return mDownVoteButton;
    }

    public FloatingActionButton getmUpVoteButton() {
        return mUpVoteButton;
    }
}
