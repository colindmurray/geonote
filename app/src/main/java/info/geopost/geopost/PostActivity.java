package info.geopost.geopost;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class PostActivity extends ActionBarActivity {

    private ButtonFlat mPostButton;
    private ParseGeoPoint mGeoPoint;
    private EditText mPostEditText;
    private EditText mTitleEditText;
    private TextView mCharacterCountTextView;
    private static final int MAX_CHARACTER_COUNT = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PostFragment())
//                    .commit();
//        }
        Intent intent = getIntent();
        LatLng location = intent.getParcelableExtra(MapsActivity.INTENT_EXTRA_LOCATION);
        mGeoPoint = new ParseGeoPoint(location.latitude, location.longitude);

        mCharacterCountTextView = (TextView) findViewById(R.id.character_count_textview);
        mPostButton = (ButtonFlat) findViewById(R.id.post_button);
        mPostEditText = (EditText) findViewById(R.id.post_edittext);
        mTitleEditText = (EditText) findViewById(R.id.title_editText);

        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
//                updatePostButtonState();
                updateCharacterCountTextViewText();
            }
        });

        mPostEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePostButtonState();
                updateCharacterCountTextViewText();
            }
        });

        mPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                post();
            }
        });
        updatePostButtonState();
        updateCharacterCountTextViewText();

    }

    private String getPostEditTextText () {
        return mPostEditText.getText().toString().trim();
    }

    private String getTitleEditTextText () {
        return mTitleEditText.getText().toString().trim();
    }

    private void updatePostButtonState() {
        int length = getTitleEditTextText().length();
        boolean enabled = length > 0 && length < MAX_CHARACTER_COUNT;
        mPostButton.setEnabled(enabled);
    }

    private void updateCharacterCountTextViewText() {
        String characterCountString = String.format("%d/%d", mPostEditText.length(), MAX_CHARACTER_COUNT);
        mCharacterCountTextView.setText(characterCountString);
    }

    private void post () {
        // 1
        GeoPostObj post = new GeoPostObj();
        post.setLocation(mGeoPoint);
        String title = getTitleEditTextText();
        String text = getPostEditTextText();
        final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
        dialog.setMessage(getString(R.string.progress_post));
        dialog.show();
        post.setText(text);
        post.setTitle(title);
        post.setUser(ParseUser.getCurrentUser());

        // 2
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        post.setACL(acl);

        // 3
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PostFragment extends Fragment {

        public PostFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_post, container, false);
            return rootView;
        }
    }

}
