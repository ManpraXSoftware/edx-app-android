package org.humana.mobile.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.webkit.ValueCallback;
import android.widget.Toast;

import com.evernote.android.state.State;
import com.google.inject.Inject;
import com.livefront.bridge.Bridge;

import org.humana.mobile.base.BaseFragment;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.view.common.PageViewStateCallback;
import org.humana.mobile.view.common.RunnableCourseComponent;

import static android.app.Activity.RESULT_OK;

public abstract class CourseUnitFragment extends BaseFragment implements PageViewStateCallback, RunnableCourseComponent {
    public interface HasComponent {
        CourseComponent getComponent();
        void navigateNextComponent();
        void navigatePreviousComponent();
    }

    @State
    protected CourseComponent unit;
    protected HasComponent hasComponentCallback;

    @Inject
    IEdxEnvironment environment;

    public Uri imageUri;

    private static final int FILECHOOSER_RESULTCODE   = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            final Bundle args = getArguments();
            unit = (CourseComponent) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
            if (unit != null) {
                /*
                The size of `unit` object could be very big in some courses that have lots of
                sections/subsections/units in them. So, we need to ensure that this object isn't
                stored in the fragment's extras otherwise we might encounter
                TransactionTooLargeException.
                 */
                args.putSerializable(Router.EXTRA_COURSE_UNIT, null);
                setArguments(args);
            }
        }
        /*
        To retain the `unit` object during fragment recreation, we're relying on the Bridge library
        which'll write the object to disk and allow us to restore while the fragment is being
        recreated. Consequently, avoiding the TransactionTooLargeException.
        More info:
        - https://medium.com/@mdmasudparvez/android-os-transactiontoolargeexception-on-nougat-solved-3b6e30597345
        - https://github.com/livefront/bridge
        - https://openedx.atlassian.net/browse/LEARNER-6680
        */
        Bridge.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public void onPageShow() {

    }

    @Override
    public void onPageDisappear() {

    }

    @Override
    public CourseComponent getCourseComponent() {
        return unit;
    }

    @Override
    public abstract void run();

    public void setHasComponentCallback(HasComponent callback) {
        hasComponentCallback = callback;
    }



}
