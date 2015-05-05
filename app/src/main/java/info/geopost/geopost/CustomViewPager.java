package info.geopost.geopost;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.rey.material.widget.Slider;
import com.rey.material.widget.Switch;

/**
 * Created by Majisto on 4/20/2015.
 */
public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomViewPager(Context context) {
        super(context);
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return super.canScroll(v, checkV, dx, x, y) || (checkV && customCanScroll(v));
    }

    protected boolean customCanScroll(View v) {
        return v instanceof Slider || v instanceof Switch;
    }
}
