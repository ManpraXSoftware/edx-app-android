package org.tta.mobile.core;

import android.os.Environment;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.course.CourseAPI;
import org.tta.mobile.tta.data.remote.api.TaAPI;
import org.tta.mobile.user.UserAPI;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EdxDataManager  implements  IEdxDataManager {
    @Inject
    Environment environment;

    @Inject
    LoginAPI loginAPI;

    @Inject
    CourseAPI courseAPI;

    @Inject
    UserAPI userAPI;

    @Inject
    TaAPI taAPI;
    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public LoginAPI getLoginAPI() {
        return loginAPI;
    }

    @Override
    public CourseAPI getCourseAPI() {
        return courseAPI;
    }

    @Override
    public UserAPI getUserAPI() {
        return userAPI;
    }

    @Override
    public TaAPI getTaAPI() {
        return taAPI;
    }
}
