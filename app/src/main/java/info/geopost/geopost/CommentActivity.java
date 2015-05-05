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

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
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

    private Button mPostButton;
    private EditText mReplyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        LatLng location = intent.getParcelableExtra(MainActivity.INTENT_EXTRA_LOCATION);
        mGeoPoint = new ParseGeoPoint(location.latitude, location.longitude);

        mPostButton = (Button) findViewById(R.id.comment_button);
        mPostButton.setOnClickListener(mPostButtonClickListener);
        mReplyText = (EditText) findViewById(R.id.replyEditText);
        ArrayList<Card> cards = new ArrayList<>();
        CommentCardHeader card = new CommentCardHeader(this, mGeoPostObj);
        cards.add(card);
        Date currentDate = mGeoPostObj.getCreatedAt();
        //JodaTime is amazing!
        LocalDateTime dateTime = new LocalDateTime(currentDate);
        LocalDateTime currentTime = new LocalDateTime();
        int numdays = Days.daysBetween(dateTime, currentTime).getDays();
        int numHours = Hours.hoursBetween(dateTime, currentTime).getHours();
        int numMinutes = Minutes.minutesBetween(dateTime, currentTime).getMinutes();
        if (numdays > 0){
            Log.e(TAG, "Number of days: " + numdays);
        }
        else if (numHours > 0){
            Log.e(TAG, "Number of hours difference is: " + numHours);
        }
        else{
            Log.e(TAG, "Number of minutes is: " + numMinutes);
        }

        mCardArrayAdapter = new CardArrayAdapter(this, cards);

        mListView = (CardListView) findViewById(R.id.commentList);
        if (mListView!=null){
            mListView.setAdapter(mCardArrayAdapter);
        }

//        for (int i = 0; i < 15; i++) {
//            CommentCardReply card_reply = new CommentCardReply(this, mGeoPostObj);
//            cards.add(card_reply);
//        }
        getComments();
    }

    public CommentCardReply getReplyCard(GeoCommentObj comment) {
        return  new CommentCardReply(this, mGeoPostObj, comment);
    }

    public void getComments() {
        ParseQuery<GeoCommentObj> query = ParseQuery.getQuery(getString(R.string.parse_object_comment));
        query.whereEqualTo("GeoPostObjPointer", mGeoPostObj.getObjectId());
        query.findInBackground(new FindCallback<GeoCommentObj>() {
            public void done(List<GeoCommentObj> commentList, ParseException e) {
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
            comment.setGeoPostObjPointer(mGeoPostObj.getObjectId());
            comment.setLocation(mGeoPoint);
            mCardArrayAdapter.add(getReplyCard(comment));
            comment.saveInBackground();
            mReplyText.setText("");
            mReplyText.clearFocus();
            hideKeyboard();
        }
    };
}
