package info.geopost.geopost;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("CommentObj")
public class GeoCommentObj extends ParseObject {
    private static final String TAG = GeoCommentObj.class.getSimpleName();

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

    public ParseObject getGeoPostPointer() {
        return getParseObject("GeoPostObjPointer");
    }

    public void setGeoPostObjPointer(ParseObject value) {
        put("GeoPostObjPointer", value);
    }

    public String getGeoPostId() {
        return getString("GeoPostObjId");
    }

    public void setGeoPostObjId(String value) {
        put("GeoPostObjId", value);
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