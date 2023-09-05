package org.humana.mobile.login;

import android.view.View;

import org.humana.mobile.R;
import org.humana.mobile.view.LoginActivity;
import org.humana.mobile.view.PresenterActivityTest;
import org.humana.mobile.view.login.LoginPresenter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.android.api.Assertions.assertThat;

public class LoginActivityTest extends PresenterActivityTest<LoginActivity, LoginPresenter, LoginPresenter.LoginViewInterface> {

    @Before
    public void setup() {
        startActivity(LoginActivity.newIntent());
    }

    @Test
    public void testSetSocialLoginButtons_withFacebookEnabled_facebookButtonIsVisible() {
        view.setSocialLoginButtons(false, true);
        View panel_login_social = activity.findViewById(R.id.panel_login_social);
        assertThat(panel_login_social).isVisible();
        View google_button = activity.findViewById(R.id.google_button);
        assertThat(google_button).isNotVisible();
        View facebook_button = activity.findViewById(R.id.facebook_button);
        assertThat(facebook_button).isVisible();
    }

    @Test
    public void testSetSocialLoginButtons_withGoogleEnabled_googleButtonIsVisible() {
        view.setSocialLoginButtons(true, false);
        View panel_login_social = activity.findViewById(R.id.panel_login_social);
        assertThat(panel_login_social).isVisible();
        View google_button = activity.findViewById(R.id.google_button);
        assertThat(google_button).isVisible();
        View facebook_button = activity.findViewById(R.id.facebook_button);
        assertThat(facebook_button).isNotVisible();
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginEnabled_socialLoginButtonsAreVisible() {
        view.setSocialLoginButtons(true, true);
        View panel_login_social = activity.findViewById(R.id.panel_login_social);
        assertThat(panel_login_social).isVisible();
        View google_button = activity.findViewById(R.id.google_button);
        assertThat(google_button).isVisible();
        View facebook_button = activity.findViewById(R.id.facebook_button);
        assertThat(facebook_button).isVisible();
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginNotEnabled_socialLoginButtonsNotVisible() {
        view.setSocialLoginButtons(false, false);
        View panel_login_social = activity.findViewById(R.id.panel_login_social);
        assertThat(panel_login_social).isNotVisible();
    }
}
