package org.humana.mobile.view;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.humana.mobile.base.BaseSingleFragmentActivity;
import org.humana.mobile.model.api.EnrolledCoursesResponse;

public class CertificateActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context, @NonNull EnrolledCoursesResponse courseData) {
        return new Intent(context, CertificateActivity.class)
                .putExtra(CertificateFragment.ENROLLMENT, courseData);
    }

    @Override
    public Fragment getFirstFragment() {
        return new CertificateFragment();
    }
}
