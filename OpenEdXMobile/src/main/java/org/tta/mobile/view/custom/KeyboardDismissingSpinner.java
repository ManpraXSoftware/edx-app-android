package org.tta.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSpinner;

import org.tta.mobile.util.SoftKeyboardUtil;

/**
 * Subclass of {@link AppCompatSpinner} that dismisses an open keyboard (if visible) on tap.
 */
public class KeyboardDismissingSpinner extends AppCompatSpinner {

    public KeyboardDismissingSpinner(Context context) {
        super(context);
    }

    public KeyboardDismissingSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardDismissingSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        // Dismiss the keyboard if its being displayed
        SoftKeyboardUtil.hide(this);
        return super.performClick();
    }
}
