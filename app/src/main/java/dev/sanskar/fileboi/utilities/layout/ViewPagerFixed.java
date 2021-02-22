package dev.sanskar.fileboi.utilities.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerFixed extends ViewPager {

    public ViewPagerFixed(@NonNull Context context) {
        super(context);
    }

    public ViewPagerFixed(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        /*
            ViewPager has a bug which causes Runtime errors in few cases when using gestures for a view (PhotoView here) within itself
            refs :
             - https://github.com/Baseflow/PhotoView
             - https://github.com/Baseflow/PhotoView/issues/31#issuecomment-19803926
        */

        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
