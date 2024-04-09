package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.databinding.DataBindingUtil;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginTask;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.ActivitySubodhaLoginBinding;
import org.edx.mobile.deeplink.DeepLink;
import org.edx.mobile.deeplink.DeepLinkManager;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.social.SocialLoginDelegate;
import org.edx.mobile.task.Task;
import org.edx.mobile.user.UserInfo;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.dialog.ResetPasswordDialogFragment;
import org.edx.mobile.view.login.LoginPresenter;

import java.util.HashMap;
import java.util.Map;

public class NewLoginActivity extends PresenterActivity<LoginPresenter, LoginPresenter.LoginViewInterface>
        implements SocialLoginDelegate.MobileLoginCallback {
    private SocialLoginDelegate socialLoginDelegate;
    private ActivitySubodhaLoginBinding activityLoginBinding;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    CourseApi courseApi;
    @Inject
    private UserService userService;
    private Toolbar toolbar;
    ClipboardService clipboardService;

    @NonNull
    public static Intent newIntent(@Nullable DeepLink deepLink) {
        final Intent intent = IntentFactory.newIntentForComponent(NewLoginActivity.class);
        intent.putExtra(Router.EXTRA_DEEP_LINK, deepLink);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @NonNull
    @Override
    protected LoginPresenter createPresenter(@Nullable Bundle savedInstanceState) {
        return new LoginPresenter(
                environment.getConfig(),
                new NetworkUtil.ZeroRatedNetworkInfo(getApplicationContext(), environment.getConfig()));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    protected LoginPresenter.LoginViewInterface createView(@Nullable Bundle savedInstanceState) {
        // finally change the color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_subodha__login);
        clipboardService = ClipboardServiceHolder.getClipboardService(getApplicationContext());
        hideSoftKeypad();
        //copyTextDataByLongPress();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this,
                environment.getConfig(), environment.getLoginPrefs(), SocialLoginDelegate.Feature.SIGN_IN);

      /*  activityLoginBinding.socialAuth.facebookButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK));
        activityLoginBinding.socialAuth.googleButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE));
        activityLoginBinding.socialAuth.microsoftButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_MICROSOFT));*/

        activityLoginBinding.loginButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for Validation˜
                callServerForLogin();
            }
        });
        activityLoginBinding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        activityLoginBinding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling help dialog
                if (NetworkUtil.isConnected(NewLoginActivity.this)) {
                    showResetPasswordDialog();
                } else {
                    showAlertDialog(getString(R.string.reset_no_network_title), getString(R.string.network_not_connected));
                }
            }
        });
        ViewCompat.setAccessibilityDelegate(activityLoginBinding.emailEt, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                host.setLongClickable(false);
                info.setContentDescription("");
            }
        });
        ViewCompat.setAccessibilityDelegate(activityLoginBinding.passwordEt, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                host.setLongClickable(false);
                info.setContentDescription("");
            }
        });
        activityLoginBinding.loginBtnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for Validation˜
                callServerForLogin();
            }
        });

      /*  activityLoginBinding.endUserAgreementTv.setMovementMethod(LinkMovementMethod.getInstance());
        activityLoginBinding.endUserAgreementTv.setText(TextUtils.generateLicenseText(getResources(), R.string.by_signing_in));
*/
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LOGIN_SCREEN);

        // enable login buttons at launch
        tryToSetUIInteraction(true);

        Config config = environment.getConfig();
        setTitle(getString(R.string.login_title));

        /*String envDisplayName = config.getEnvironmentDisplayName();
        if (envDisplayName != null && envDisplayName.length() > 0) {
           activityLoginBinding.versionEnvTv.setVisibility(View.VISIBLE);
            String versionName = BuildConfig.VERSION_NAME;
            String text = String.format("%s %s %s",
                    getString(R.string.label_version), versionName, envDisplayName);
            activityLoginBinding.versionEnvTv.setText(text);
        }*/

        return new LoginPresenter.LoginViewInterface() {
            @Override
            public void disableToolbarNavigation() {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setHomeButtonEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowHomeEnabled(false);
                }
            }

            @Override
            public void setSocialLoginButtons(boolean googleEnabled, boolean facebookEnabled,
                                              boolean microsoftEnabled) {
           /*     if (!facebookEnabled && !googleEnabled && !microsoftEnabled) {
                    activityLoginBinding.panelLoginSocial.setVisibility(View.GONE);
                } else {
                    if (!facebookEnabled) {
                        activityLoginBinding.socialAuth.facebookButton.getRoot().setVisibility(View.GONE);
                    }
                    if (!googleEnabled) {
                        activityLoginBinding.socialAuth.googleButton.getRoot().setVisibility(View.GONE);
                    }
                    if (!microsoftEnabled) {
                        activityLoginBinding.socialAuth.microsoftButton.getRoot().setVisibility(View.GONE);
                    }
                }*/
            }
        };
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        socialLoginDelegate.onActivityDestroyed();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", activityLoginBinding.emailEt.getText().toString().trim());

        socialLoginDelegate.onActivitySaveInstanceState(outState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (activityLoginBinding.emailEt.getText().toString().length() == 0) {
            displayLastEmailId();
        }

        socialLoginDelegate.onActivityStarted();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            activityLoginBinding.emailEt.setText(savedInstanceState.getString("username"));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tryToSetUIInteraction(true);
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ResetPasswordDialogFragment.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    showAlertDialog(getString(R.string.success_dialog_title_help),
                            getString(R.string.success_dialog_message_help));
                }
                break;
            }
        }
    }

    private void displayLastEmailId() {
        activityLoginBinding.emailEt.setText(loginPrefs.getLastAuthenticatedEmail());
    }

    public void callServerForLogin() {
        if (!NetworkUtil.isConnected(this)) {
            showAlertDialog(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
            return;
        }

        final String emailStr = activityLoginBinding.emailEt.getText().toString().trim();
        final String passwordStr = activityLoginBinding.passwordEt.getText().toString().trim();

        if (activityLoginBinding.emailEt != null && emailStr.length() == 0) {
            showAlertDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_email));
            activityLoginBinding.emailEt.requestFocus();
        } else if (activityLoginBinding.passwordEt != null && passwordStr.length() == 0) {
            showAlertDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_password));
            activityLoginBinding.passwordEt.requestFocus();
        } else {
            activityLoginBinding.emailEt.setEnabled(false);
            activityLoginBinding.passwordEt.setEnabled(false);

            activityLoginBinding.forgotPasswordTv.setEnabled(false);
            //activityLoginBinding.endUserAgreementTv.setEnabled(false);

            LoginTask logintask = new LoginTask(this, activityLoginBinding.emailEt.getText().toString().trim(),
                    activityLoginBinding.passwordEt.getText().toString()) {
                @Override
                public void onSuccess(@NonNull AuthResponse result) {
                    /*final String token = loginPrefs.getAuthorizationHeaderJwt();
                    if (token!=null){
                        Log.d("Token_JWT " , token);
                    }

                    Call<DiscoveryCourse> discoveryCourse = courseApi.getDiscoverySubjects(token);
                    discoveryCourse.enqueue(new DiscoveryCallback<DiscoveryCourse>() {
                        @Override
                        protected void onResponse(@NonNull DiscoveryCourse responseBody) {
                            if (responseBody!=null){
                                Log.d("DiscoveryApi",String.valueOf(responseBody.getCount()));
                            }
                        }

                        @Override
                        protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                            super.onFailure(responseError, error);
                        }
                    });*/
                    loadProfileInfo(result.profile.username);
                    onUserLoginSuccess(result.profile);
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        onUserLoginFailure(new LoginException(new LoginErrorMessage(
                                getString(R.string.login_error),
                                getString(R.string.login_failed))), null, null);
                    } else if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST) {
                        onUserLoginFailure(new LoginException(new LoginErrorMessage(
                                getString(R.string.login_error),
                                getString(R.string.login_failed))), null, null);
                    } else {
                        onUserLoginFailure(ex, null, null);
                    }
                }
            };
            tryToSetUIInteraction(false);
            logintask.setProgressDialog(activityLoginBinding.progress.progressIndicator);
            logintask.execute();
        }
    }

    private void loadProfileInfo(String userName) {
        userService.getUserInfo(userName).enqueue(new Callback<UserInfo>() {
            @Override
            protected void onResponse(@NonNull UserInfo responseBody) {
                UserInfo userInfo = responseBody;
                if (userInfo != null) {
                    loginPrefs.storeUserType(userInfo.getUser_type());
                    loginPrefs.storeUserInfoJson(userInfo);
                    sendAnalyticsUserLoginSuccessful();
                }
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                super.onFailure(error);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        socialLoginDelegate.onActivityStopped();
    }

    public String getEmail() {
        return activityLoginBinding.emailEt.getText().toString().trim();
    }

    private void showResetPasswordDialog() {
        ResetPasswordDialogFragment.newInstance(getEmail()).show(getSupportFragmentManager(), null);
    }

    // make sure that on the login activity, all errors show up as a dialog as opposed to a flying snackbar
    @Override
    public void showAlertDialog(@Nullable String header, @NonNull String message) {
        super.showAlertDialog(header, message);
    }


    /**
     * Starts fetching profile of the user after login by Facebook or Google.
     *
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend, Task task) {
        tryToSetUIInteraction(false);
        task.setProgressDialog(activityLoginBinding.progress.progressIndicator);
    }

    @SuppressLint("WrongConstant")
    public void onUserLoginSuccess(ProfileModel profile) {
        setResult(RESULT_OK);
        final DeepLink deepLink = getIntent().getParcelableExtra(Router.EXTRA_DEEP_LINK);
        if (deepLink != null) {
            DeepLinkManager.onDeepLinkReceived(this, deepLink);
            return;
        }
        //   if (!environment.getConfig().isRegistrationEnabled()) {
        //environment.getRouter().showMainDashboard(this);
        if (LocaleManager.getLanguagePref(NewLoginActivity.this).isEmpty()) {
            LocaleManager.setNewLocale(NewLoginActivity.this, "en");
        }
        String firstTime = loginPrefs.getFirstTime();
        if (firstTime != null) {
            if (firstTime.equals("true")) {
                environment.getRouter().showMainDashboard(NewLoginActivity.this);
            } else {
                environment.getRouter().showLanguage(this);
            }
        } else {
            environment.getRouter().showLanguage(this);
        }

        //     }
        finish();
    }

    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        tryToSetUIInteraction(true);
        sendAnalyticsUserLoginUnsuccessful();
        if (ex != null && ex instanceof LoginException) {
            LoginErrorMessage errorMessage = (((LoginException) ex).getLoginErrorMessage());
            showAlertDialog(
                    errorMessage.getMessageLine1(),
                    (errorMessage.getMessageLine2() != null) ?
                            errorMessage.getMessageLine2() : getString(R.string.login_failed));
        } else if (ex != null && ex instanceof HttpStatusException &&
                ((HttpStatusException) ex).getStatusCode() == HttpStatus.UPGRADE_REQUIRED) {
            NewLoginActivity.this.showAlertDialog(null,
                    getString(R.string.app_version_unsupported_login_msg),
                    getString(R.string.label_update),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppStoreUtils.openAppInAppStore(NewLoginActivity.this);
                        }
                    }, getString(android.R.string.cancel), null);
        } else {
            showAlertDialog(getString(R.string.login_error), ErrorUtils.getErrorMessage(ex, NewLoginActivity.this));
            logger.error(ex);
        }
    }

    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        if (enable) {
            unblockTouch();
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.login));
        } else {
            blockTouch();
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.signing_in));
        }


       /* activityLoginBinding.socialAuth.facebookButton.getRoot().setClickable(enable);
        activityLoginBinding.socialAuth.googleButton.getRoot().setClickable(enable);
        activityLoginBinding.socialAuth.microsoftButton.getRoot().setClickable(enable);*/

        activityLoginBinding.emailEt.setEnabled(enable);
        activityLoginBinding.passwordEt.setEnabled(enable);

        activityLoginBinding.forgotPasswordTv.setEnabled(enable);
        //  activityLoginBinding.endUserAgreementTv.setEnabled(enable);

        return true;
    }

    String checkValueAvailable(String checkValue){
        return checkValue != null ? checkValue : "not_available";
    }

    void sendAnalyticsUserLoginSuccessful(){
        UserInfo userInfo = loginPrefs.getCurrentUserInfo();

        Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.USER_TYPE,checkValueAvailable(userInfo.getUser_type()));
        values.put(Analytics.Keys.EDUCATION_BROAD, checkValueAvailable(userInfo.getEducation_board()));
        values.put(Analytics.Keys.SCHOOL_NAME, checkValueAvailable(userInfo.getSchool()));
        values.put(Analytics.Keys.GRADE, checkValueAvailable(userInfo.getGrade()));
        values.put(Analytics.Keys.LOCATION, checkValueAvailable(userInfo.getLocation()));
        values.put(Analytics.Keys.VI_SIGHTED, checkValueAvailable(userInfo.getVi_sighted()));
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.USER_LOGIN_SUCCESSFUL,null,null,values);
    }
    void sendAnalyticsUserLoginUnsuccessful(){
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.USER_LOGIN_UNSUCCESSFUL,null,null,null);
    }

    void copyTextDataByLongPress(){
        activityLoginBinding.appText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.appText.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        activityLoginBinding.enterYourEmailBelowTxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.enterYourEmailBelowTxt.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        activityLoginBinding.enterYourPasswordBelowTxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.enterYourPasswordBelowTxt.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        activityLoginBinding.loginBtnTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.loginBtnTv.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        activityLoginBinding.forgotPasswordTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.forgotPasswordTv.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        activityLoginBinding.createANewAccountTxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.createANewAccountTxt.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        /*activityLoginBinding.gmailTxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = activityLoginBinding.gmailTxt.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
    }
}
