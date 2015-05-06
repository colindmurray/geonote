package info.geopost.geopost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class CommentActivity extends ActionBarActivity {

    public static GeoPostObj mGeoPostObj;
    private TextView username;
    private TextView body;
    private final String TAG = "CommentActivity";
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mListView;
    private ParseGeoPoint mGeoPoint;
    private VoteCommentHolder mVoteCommentStatus = new VoteCommentHolder();

    private ButtonFlat mPostButton;
    private EditText mReplyText;
    private ParseObject mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        LatLng location = intent.getParcelableExtra(MainActivity.INTENT_EXTRA_LOCATION);
        mGeoPoint = new ParseGeoPoint(location.latitude, location.longitude);

        mPostButton = (ButtonFlat) findViewById(R.id.comment_button);
        mPostButton.setOnClickListener(mPostButtonClickListener);
        mReplyText = (EditText) findViewById(R.id.replyEditText);
        ArrayList<Card> cards = new ArrayList<>();
        mCardArrayAdapter = new CardArrayAdapter(this, cards);
        mListView = (CardListView) findViewById(R.id.commentList);
        if (mListView!=null){
            mListView.setAdapter(mCardArrayAdapter);
        }
        //Get user data to keep track of votes;
        ParseObject userDataPointer = ParseUser.getCurrentUser().getParseObject("UserData");
        userDataPointer.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject userData, ParseException e) {
                mUserData = userData;
                CommentCardHeader card = new CommentCardHeader(getApplicationContext(), mGeoPostObj, mUserData, mVoteCommentStatus);
                mCardArrayAdapter.add(card);
                getComments();
            }
        });

    }

    public CommentCardReply getReplyCard(GeoCommentObj comment) {
        return  new CommentCardReply(this, mGeoPostObj, comment);
    }

    public void getComments() {
        ParseQuery<GeoCommentObj> query = ParseQuery.getQuery(getString(R.string.parse_object_comment));
        query.whereEqualTo("GeoPostObjPointer", mGeoPostObj);
        query.findInBackground(new FindCallback<GeoCommentObj>() {
            public void done(List<GeoCommentObj> commentList, ParseException e) {
                mVoteCommentStatus.setCommentNumber(commentList.size());
                if (e == null) {
                    Log.d(TAG, "Retrieved " + commentList.size() + " comments");
                    for(GeoCommentObj comment : commentList) {
                        mCardArrayAdapter.add(getReplyCard(comment));
                    }
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private Button.OnClickListener mPostButtonClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            GeoCommentObj comment = new GeoCommentObj();
            comment.setText(mReplyText.getText().toString());
            comment.setUser(ParseUser.getCurrentUser());
            comment.setUsername(ParseUser.getCurrentUser().getUsername());
            comment.setVotes(1);
            comment.setGeoPostObjPointer(mGeoPostObj);
            comment.setGeoPostObjId(mGeoPostObj.getObjectId());
            comment.setLocation(mGeoPoint);
            ParseACL acl = new ParseACL();
            acl.setPublicReadAccess(true);
            acl.setPublicWriteAccess(true);
            comment.setACL(acl);
            mCardArrayAdapter.add(getReplyCard(comment));
            int numComments = mVoteCommentStatus.getCommentNumber() + 1;
            mGeoPostObj.setCommentCount(numComments);
            mVoteCommentStatus.setCommentNumber(numComments);
            comment.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
            mReplyText.setText("");
            mReplyText.clearFocus();
            hideKeyboard();
        }
    };

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(VoteCommentHolder.VOTE_STATUS, mVoteCommentStatus.getVoteStatus());
        resultIntent.putExtra(VoteCommentHolder.COMMENT_NUM, mVoteCommentStatus.getCommentNumber());
        resultIntent.putExtra("GeoPostId", mGeoPostObj.getObjectId());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
