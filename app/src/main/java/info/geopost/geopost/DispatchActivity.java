package info.geopost.geopost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import net.danlew.android.joda.JodaTimeAndroid;


public class DispatchActivity extends ActionBarActivity {
    public static final String TAG = "Parse Dispatch";
    private static boolean parse_init = false;

    public DispatchActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        if (!parse_init) {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
            parse_init = true;
        }
        if (ParseUser.getCurrentUser() != null){
            String user = ParseUser.getCurrentUser().getUsername();
            Log.e(TAG, "Current user: " + user);
            startActivityForResult(new Intent(this, MainActivity.class), 0);
        }
        else{
            ParseObject.registerSubclass(GeoPostObj.class);
            ParseLoginBuilder builder = new ParseLoginBuilder(DispatchActivity.this);
            startActivityForResult(builder.build(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.e(TAG, String.format("Request code is: %d and result code is: %d", requestCode, resultCode));
        if (resultCode == RESULT_OK){
            Log.e(TAG, "Starting MainActivity.  -  " + resultCode);
            startActivityForResult(new Intent(this, MainActivity.class), 0);
            finish();
        }
        else if (resultCode == RESULT_CANCELED){
            Log.e(TAG, "Result: RESULT_CANCELED - Closing activity.  -  " + resultCode);
            finish();
        }
        else{
            ParseObject.registerSubclass(GeoPostObj.class);
            ParseLoginBuilder builder = new ParseLoginBuilder(DispatchActivity.this);
            startActivityForResult(builder.build(), 0);
        }
    }

}
