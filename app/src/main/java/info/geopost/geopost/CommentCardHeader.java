package info.geopost.geopost;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;


/**
 * Created by Majisto on 5/4/2015.
 */
public class CommentCardHeader extends Card
{
    private final GeoPostObj mGeoPostObj;
    private TextView time;

    public CommentCardHeader(Context context, GeoPostObj mGeoPostObj) {
        super(context, R.layout.card_comment_header);
        this.mGeoPostObj = mGeoPostObj;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        time = (TextView) parent.findViewById(R.id.time);
        Date currentDate = mGeoPostObj.getCreatedAt();
        //JodaTime is amazing!
        LocalDateTime dateTime = new LocalDateTime(currentDate);
        LocalDateTime currentTime = new LocalDateTime();
        int numdays = Days.daysBetween(dateTime, currentTime).getDays();
        int numHours = Hours.hoursBetween(dateTime, currentTime).getHours();
        int numMinutes = Minutes.minutesBetween(dateTime, currentTime).getMinutes();
        if (numdays > 0)
            time.setText((numdays > 1) ? (numdays + " days ago.") : ("1 day ago."));
        else if (numHours > 0)
            time.setText((numHours > 1) ? (numHours + " hours ago.") : ("1 hour ago."));
        else
            time.setText((numMinutes > 1) ? (numMinutes + " minutes ago.") : ("1 minute ago."));
    }
}
