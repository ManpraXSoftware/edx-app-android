package org.tta.mobile.core;

import android.os.Environment;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.course.CourseAPI;
import org.tta.mobile.tta.data.remote.api.TaAPI;
import org.tta.mobile.user.UserAPI;

public interface IEdxDataManager {
    Environment getEnvironment();

    LoginAPI getLoginAPI();

    CourseAPI getCourseAPI();

    UserAPI getUserAPI();

    TaAPI getTaAPI();
}
