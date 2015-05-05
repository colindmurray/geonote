package info.geopost.geopost;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("GeoPostObj")
public class GeoCommentObj extends ParseObject {
    private static final String TAG = GeoCommentObj.class.getSimpleName();

    public static final int DOWNVOTE = -1;
    public static final int UPVOTE = 1;
    public static final int NOVOTE = 0;

    public GeoCommentObj() {
    }

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

    public String getGeoPostPointer() {
        return getString("GeoPostObjPointer");
    }

    public void setGeoPostObjPointer(String value) {
        put("GeoPostObjPointer", value);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public String getUsername() {
        return getString("username");
    }

    public void setUsername(String value) {
        put("username", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<GeoPostObj> getQuery() {
        return ParseQuery.getQuery(GeoPostObj.class);
    }
}