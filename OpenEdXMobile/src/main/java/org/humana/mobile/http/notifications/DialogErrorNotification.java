package org.humana.mobile.http.notifications;

import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.joanzapata.iconify.Icon;

import org.humana.mobile.base.BaseFragment;
import org.humana.mobile.view.dialog.AlertDialogFragment;

/**
 * A modal dialog notification error message.
 */
public class DialogErrorNotification extends ErrorNotification {
    /**
     * Reference of the {@link Fragment} we will be displaying this error on.
     */
    @NonNull
    private final BaseFragment baseFragment;

    /**
     * Construct a new instance of the notification.
     *
     * @param baseFragment Reference of the {@link Fragment} to use for
     *                     displaying the {@link DialogFragment}.
     */
    public DialogErrorNotification(@NonNull final BaseFragment baseFragment) {
        this.baseFragment = baseFragment;
    }

    /**
     * Show the error notification as a modal dialog, according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon. This is currently ignored here.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    @Override
    public void showError(@StringRes final int errorResId,
                          @Nullable final Icon icon,
                          @StringRes final int actionTextResId,
                          @Nullable final View.OnClickListener actionListener) {
        if (baseFragment.isResumed()) {
            AlertDialogFragment.newInstance(0, errorResId,
                    actionListener == null ? null :
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    actionListener.onClick(((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE));
                                }
                            }
            ).show(baseFragment.getChildFragmentManager(), null);
        }
    }
}
