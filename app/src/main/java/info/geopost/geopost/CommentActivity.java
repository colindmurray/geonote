package info.geopost.geopost;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class CommentActivity extends ActionBarActivity {

    public static GeoPostObj geoPostObj;
    private TextView username;
    private TextView body;
    private final String TAG = "CommentActivity";
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ArrayList<Card> cards = new ArrayList<>();
        CommentCardHeader card = new CommentCardHeader(this, geoPostObj);
        cards.add(card);

        mCardArrayAdapter = new CardArrayAdapter(this, cards);

        mListView = (CardListView) findViewById(R.id.commentList);
        if (mListView!=null){
            mListView.setAdapter(mCardArrayAdapter);
        }

//        for (int i = 0; i < 15; i++) {
//            CommentCardReply card_reply = new CommentCardReply(this, geoPostObj);
//            cards.add(card_reply);
//        }
        getComments();
    }

    public CommentCardReply getReplyCard(GeoCommentObj comment) {
        return  new CommentCardReply(this, geoPostObj, comment);
    }

    public void getComments() {
        ParseQuery<GeoCommentObj> query = ParseQuery.getQuery(getString(R.string.parse_object_comment));
        query.whereEqualTo("GeoPostObjPointer", geoPostObj.getObjectId());
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
}
