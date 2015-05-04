package info.geopost.geopost;

import android.os.UserHandle;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@ParseClassName("GeoPostObj")
public class GeoPostObj extends ParseObject {
    private static final String TAG = GeoPostObj.class.getSimpleName();

    public static final int DOWNVOTE = -1;
    public static final int UPVOTE = 1;
    public static final int NOVOTE = 0;

    public GeoPostObj() {}

    public String getText() {
        return getString("text");
    }

    public void setText(String value) {
        put("text", value);
    }

    public int getVotes() {
        return getInt("votes");
    }

    public void setVotes(int value) {
        put("votes", value);
    }

    public String getTitle(){
        return getString("title");
    }

    public void setTitle(String value){
        put("title", value);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public String getUsername() {return getString("username");}

    public void setUsername(String value) {put ("username", value);}

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<GeoPostObj> getQuery() {
        return ParseQuery.getQuery(GeoPostObj.class);
    }


    public static int getVoteStatus(ParseObject userData, GeoPostObj post) {
        JSONObject userHistory = userData.getJSONObject("UserHistory");
        int userVoteStatus = 0;
        if(userHistory == null) {
            return userVoteStatus;
        }

        try {
            userVoteStatus = (int) userHistory.get(post.getObjectId());
        } catch (JSONException e) {
            Log.d(TAG, "Failed to get user post status for geoPostId: " + post.getObjectId());
            e.printStackTrace();
        }
        return userVoteStatus;
    }
    public static void setVoteStatus(ParseObject userData, GeoPostObj post, int vote)  {
        if(vote > UPVOTE || vote < DOWNVOTE) {
            throw new RuntimeException("Invalid user vote status: " + vote);
        }
        JSONObject userHistory = userData.getJSONObject("UserHistory");
        if(userHistory == null) {
            userHistory = new JSONObject();
            userData.put("UserHistory", userHistory);
        }
        // Save user post history to local data store.
        try {
            userHistory.put(post.getObjectId(), vote);
        } catch (JSONException e) {
            Log.d(TAG, "Failed to set user post status for geoPostId: " + post.getObjectId());
            e.printStackTrace();
        }
        userData.pinInBackground();
    }
    public static void updateVoteStatus(ParseObject userData, GeoPostObj post, int vote) {
        int currentVoteStatus = getVoteStatus(userData, post);
        if(currentVoteStatus == vote) {
            return;
        } else if (currentVoteStatus == -vote) {
            post.setVotes(post.getVotes() + (vote * 2));
        } else if (vote == 0) {
            post.setVotes(post.getVotes() + -currentVoteStatus);
        } else {
            post.setVotes(post.getVotes() + vote);
        }
        post.saveInBackground();
        setVoteStatus(userData, post, vote);
    }
}