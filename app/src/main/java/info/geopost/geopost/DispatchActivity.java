package info.geopost.geopost;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;


public class DispatchActivity extends ActionBarActivity {
    public static final String TAG = "Parse Dispatch";
    public DispatchActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        if (ParseUser.getCurrentUser() != null){
            String user = ParseUser.getCurrentUser().getUsername();
            Log.e(TAG, "Current user: " + user);
            startActivity(new Intent(this, MapsActivity.class));
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
            startActivity(new Intent(this, MapsActivity.class));
        }
        else if (resultCode == RESULT_CANCELED){
            finish();
        }
        else{
            ParseObject.registerSubclass(GeoPostObj.class);
            ParseLoginBuilder builder = new ParseLoginBuilder(DispatchActivity.this);
            startActivityForResult(builder.build(), 0);
        }
    }

}
