package org.humana.mobile.interfaces;

import com.google.android.material.snackbar.Snackbar;

/**
 * Provides callbacks to control the visibility of {@link Snackbar Snackbar}.
 */
public interface SnackbarStatusListener {
    /**
     * Hide {@link Snackbar Snackbar} if its being displayed.
     */
    void hideSnackBar();

    /**
     * Set the visibility of {@link Snackbar Snackbar} based on the
     * visibility of {@link org.humana.mobile.http.notifications.FullScreenErrorNotification FullScreenErrorNotification}.
     * <br/>
     * Note: At one time only one type of error i.e. either SnackBar or Full Screen error should be
     * visible on screen.
     *
     * @param fullScreenErrorVisibility Visibility of {@link org.humana.mobile.http.notifications.FullScreenErrorNotification FullScreenErrorNotification}.
     */
    void resetSnackbarVisibility(boolean fullScreenErrorVisibility);
}
