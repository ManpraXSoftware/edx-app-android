package org.tta.mobile.test.feature;

import org.tta.mobile.test.feature.data.Credentials;
import org.tta.mobile.test.feature.interactor.AppInteractor;
import org.junit.Test;

public class RegisterFeatureTest extends FeatureTest {
    @Test
    public void afterRegistering_withFreshCredentials_myCoursesScreenIsDisplayed() {
        new AppInteractor()
                .launchApp()
                .observeLandingScreen()
                .navigateToRegistrationScreen()
                .observeRegistrationScreen()
                .createAccount(Credentials.freshCredentials(environment.getConfig()))
                .observeMyCoursesScreen();
    }
}
