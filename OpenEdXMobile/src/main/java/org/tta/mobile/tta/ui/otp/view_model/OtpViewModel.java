package org.tta.mobile.tta.ui.otp.view_model;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.google.android.gms.common.api.CommonStatusCodes;

import org.tta.mobile.R;
import org.tta.mobile.authentication.AuthResponse;
import org.tta.mobile.exception.AuthException;
import org.tta.mobile.http.HttpResponseStatusException;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Page;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.SurveyType;
import org.tta.mobile.tta.data.model.authentication.MobileNumberVerificationResponse;
import org.tta.mobile.tta.data.model.authentication.RegisterResponse;
import org.tta.mobile.tta.data.model.authentication.SendOTPResponse;
import org.tta.mobile.tta.data.model.authentication.VerifyOTPForgotedPasswordResponse;
import org.tta.mobile.tta.data.model.authentication.VerifyOTPResponse;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.task.authentication.GenerateOtpTask;
import org.tta.mobile.tta.task.authentication.MobileNumberVerificationTask;
import org.tta.mobile.tta.task.authentication.OTPVerificationForgotedPasswordTask;
import org.tta.mobile.tta.task.authentication.RegisterTask;
import org.tta.mobile.tta.task.authentication.VerifyOtpTask;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.logistration.UserInfoActivity;
import org.tta.mobile.tta.ui.otp.IncomingSms;
import org.tta.mobile.tta.ui.otp.SmsModule;
import org.tta.mobile.tta.ui.otp.SmsResponse;
import org.tta.mobile.tta.ui.otp.SmsUtil;
import org.tta.mobile.tta.ui.reset_password.ResetPasswordActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.images.ErrorUtils;

import de.greenrobot.event.EventBus;

public class OtpViewModel extends BaseViewModel {

    public ObservableField<String> digit1 = new ObservableField<>("");
    public ObservableField<String> digit2 = new ObservableField<>("");
    public ObservableField<String> digit3 = new ObservableField<>("");
    public ObservableField<String> digit4 = new ObservableField<>("");
    public ObservableField<String> digit5 = new ObservableField<>("");
    public ObservableField<String> digit6 = new ObservableField<>("");

    public ObservableBoolean valid1 = new ObservableBoolean();
    public ObservableBoolean valid2 = new ObservableBoolean();
    public ObservableBoolean valid3 = new ObservableBoolean();
    public ObservableBoolean valid4 = new ObservableBoolean();
    public ObservableBoolean valid5 = new ObservableBoolean();
    public ObservableBoolean valid6 = new ObservableBoolean();

    public ObservableBoolean focus1 = new ObservableBoolean();
    public ObservableBoolean focus2 = new ObservableBoolean();
    public ObservableBoolean focus3 = new ObservableBoolean();
    public ObservableBoolean focus4 = new ObservableBoolean();
    public ObservableBoolean focus5 = new ObservableBoolean();
    public ObservableBoolean focus6 = new ObservableBoolean();

    private String otp = "";

    public IncomingSms incomingSms;

    private String number;

    private String password;

    private String otpSource;

    private boolean receiverRegistered = false;

    public TextWatcher watcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit1.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid1.set(true);
                shiftFocusTo(digit2);
            } else {
                valid1.set(false);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit2.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid2.set(true);
                shiftFocusTo(digit3);
            } else {
                valid2.set(false);
//                shiftFocusTo(digit1);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher3 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit3.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid3.set(true);
                shiftFocusTo(digit4);
            } else {
                valid3.set(false);
//                shiftFocusTo(digit2);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher4 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit4.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid4.set(true);
                shiftFocusTo(digit5);
            } else {
                valid4.set(false);
//                shiftFocusTo(digit3);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher5 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit5.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid5.set(true);
                shiftFocusTo(digit6);
            } else {
                valid5.set(false);
//                shiftFocusTo(digit4);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public TextWatcher watcher6 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            digit6.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 1 && numString.matches("[0-9]+")){
                valid6.set(true);
            } else {
                valid6.set(false);
//                shiftFocusTo(digit5);
            }

            otp = digit1.get() + digit2.get() + digit3.get() + digit4.get() + digit5.get() + digit6.get();
        }
    };

    public View.OnKeyListener keyListener2 = (v, keyCode, event) -> {
        if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && (digit2.get() == null || digit2.get().equals(""))) {
            digit1.set("");
            shiftFocusTo(digit1);
        }
        return false;
    };

    public View.OnKeyListener keyListener3 = (v, keyCode, event) -> {
        if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && (digit3.get() == null || digit3.get().equals(""))) {
            digit2.set("");
            shiftFocusTo(digit2);
        }
        return false;
    };

    public View.OnKeyListener keyListener4 = (v, keyCode, event) -> {
        if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && (digit4.get() == null || digit4.get().equals(""))) {
            digit3.set("");
            shiftFocusTo(digit3);
        }
        return false;
    };

    public View.OnKeyListener keyListener5 = (v, keyCode, event) -> {
        if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && (digit5.get() == null || digit5.get().equals(""))) {
            digit4.set("");
            shiftFocusTo(digit4);
        }
        return false;
    };

    public View.OnKeyListener keyListener6 = (v, keyCode, event) -> {
        if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && (digit6.get() == null || digit6.get().equals(""))) {
            digit5.set("");
            shiftFocusTo(digit5);
        }
        return false;
    };

    public OtpViewModel(BaseVMActivity activity, String mobile_number, String password, String otpSource) {
        super(activity);
        this.number = mobile_number;
        this.password = password;
        this.otpSource = otpSource;

        /*incomingSms =new IncomingSms(new IMessageReceiver() {
            @Override
            public void onMessage(String from, String text) {
                OTP_helper helper=new OTP_helper();
                if(helper.isValidSender(from))
                {
                    otp = helper.getOTPFromMesssageBody(text);

                    digit1.set(String.valueOf(otp.charAt(0)));
                    digit2.set(String.valueOf(otp.charAt(1)));
                    digit3.set(String.valueOf(otp.charAt(2)));
                    digit4.set(String.valueOf(otp.charAt(3)));
                    digit5.set(String.valueOf(otp.charAt(4)));
                    digit6.set(String.valueOf(otp.charAt(5)));

                    //Toast.makeText(ctx,text, Toast.LENGTH_LONG).showLoading();
                    unregisterMessageListener();
                    //mFrag.incomingSms = null;
                }
            }
        });
        registerMessageListner();*/

    }

    private void shiftFocusTo(ObservableField<String> digit){

        focus1.set(false);
        focus2.set(false);
        focus3.set(false);
        focus4.set(false);
        focus5.set(false);
        focus6.set(false);

        if (digit.equals(digit1)){
            focus1.set(true);
        } else if (digit.equals(digit2)){
            focus2.set(true);
        } else if (digit.equals(digit3)){
            focus3.set(true);
        } else if (digit.equals(digit4)){
            focus4.set(true);
        } else if (digit.equals(digit5)){
            focus5.set(true);
        } else {
            focus6.set(true);
        }

    }

    public void resendOtp(){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        mActivity.showLoading();

        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_MOBILE_NUMBER, number);

        //adding version for otp handling
        if(mDataManager.getConfig().getSMSKey()!=null || !mDataManager.getConfig().getSMSKey().isEmpty()) {
            parameters.putString("version", "1");
            parameters.putString("sms_key", mDataManager.getConfig().getSMSKey());
        }

        SmsModule.intialiseSMSRetrieverClient(mActivity);
        if (otpSource.equals(Constants.OTP_SOURCE_RESET_PASSWORD)) {
            generateOtpForResetPassword(parameters);
        } else if (otpSource.equals(Constants.OTP_SOURCE_REGISTER)) {
            generateOtpForRegistration(parameters);
        }

    }

    private void generateOtpForResetPassword(Bundle parameters){
        new MobileNumberVerificationTask(mActivity, parameters){
            @Override
            protected void onSuccess(MobileNumberVerificationResponse mobileNumberVerificationResponse) throws Exception {
                super.onSuccess(mobileNumberVerificationResponse);
                mActivity.hideLoading();

                if (mobileNumberVerificationResponse.mobile_number() != null && !mobileNumberVerificationResponse.mobile_number().equals("")){
//                    registerMessageListner();
                    mActivity.showShortSnack(mActivity.getString(R.string.otp_sent_successfully));
                } else {
                    mActivity.showErrorDialog(mActivity.getString(R.string.reset_password_failure),
                            mActivity.getString(R.string.unable_resend_otp));
                }
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters1 = new Bundle();
                parameters1.putString(Constants.KEY_CLASS_NAME, OtpViewModel.class.getName());
                parameters1.putString(Constants.KEY_FUNCTION_NAME, "generateOtpForResetPassword");
                parameters1.putString(Constants.KEY_DATA, "parameters = " + parameters);
                Logger.logCrashlytics(ex, parameters1);
                mActivity.hideLoading();
                mActivity.showErrorDialog(mActivity.getString(R.string.user_not_exist),
                        mActivity.getString(R.string.user_not_exist_message));
            }
        }.execute();
    }

    private void generateOtpForRegistration(Bundle parameters){
        new GenerateOtpTask(mActivity, parameters){
            @Override
            protected void onSuccess(SendOTPResponse sendOTPResponse) throws Exception {
                super.onSuccess(sendOTPResponse);
                mActivity.hideLoading();

                if (sendOTPResponse.mobile_number().equals(number)){
//                    registerMessageListner();
                    mActivity.showShortSnack(mActivity.getString(R.string.otp_sent_successfully));
                } else {
                    mActivity.showErrorDialog(mActivity.getString(R.string.registration_failure),
                            mActivity.getString(R.string.unable_resend_otp));
                }
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters1 = new Bundle();
                parameters1.putString(Constants.KEY_CLASS_NAME, OtpViewModel.class.getName());
                parameters1.putString(Constants.KEY_FUNCTION_NAME, "generateOtpForRegistration");
                parameters1.putString(Constants.KEY_DATA, "parameters = " + parameters);
                Logger.logCrashlytics(ex, parameters1);
                mActivity.hideLoading();
                String errorMsg = "";
                try {
                    if (((HttpResponseStatusException) ex).getStatusCode() == 409) {
                        errorMsg = mActivity.getString(R.string.account_already_exist);
                    } else if (((HttpResponseStatusException) ex).getStatusCode() == 404) {
                        errorMsg = mActivity.getString(R.string.enter_valid_number);
                    } else {
                        errorMsg = mActivity.getString(R.string.server_not_responding);
                    }
                } catch (Exception exp) {
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_CLASS_NAME, OtpViewModel.class.getName());
                    parameters.putString(Constants.KEY_FUNCTION_NAME, "generateOtpForRegistration");
                    parameters.putString(Constants.KEY_DATA, "Mobile number = " + number);
                    Logger.logCrashlytics(exp, parameters);
                    errorMsg = mActivity.getString(R.string.server_not_responding);
                }

                mActivity.showErrorDialog(mActivity.getString(R.string.registration_failure), errorMsg);
            }
        }.execute();
    }

    public void verify(){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        mActivity.showLoading();

        if (otpSource.equals(Constants.OTP_SOURCE_RESET_PASSWORD)) {
            verifyOtpForForgottenPassword();
        } else if (otpSource.equals(Constants.OTP_SOURCE_REGISTER)) {
            verifyOtp();
        }
    }

    private void verifyOtpForForgottenPassword(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_OTP, otp);
        parameters.putString(Constants.KEY_MOBILE_NUMBER, number);

        new OTPVerificationForgotedPasswordTask(mActivity, parameters){
            @Override
            protected void onSuccess(VerifyOTPForgotedPasswordResponse verifyOTPForgotedPasswordResponse) throws Exception {
                super.onSuccess(verifyOTPForgotedPasswordResponse);
                mActivity.hideLoading();

                if(verifyOTPForgotedPasswordResponse.transaction_id() != null && !verifyOTPForgotedPasswordResponse.transaction_id().equals("")) {
                    String user_otp_transation_id = verifyOTPForgotedPasswordResponse.transaction_id();

                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_MOBILE_NUMBER, number);
                    parameters.putString(Constants.KEY_OTP_TRANSACTION_ID, user_otp_transation_id);

                    ActivityUtil.gotoPage(mActivity, ResetPasswordActivity.class, parameters);
                    mActivity.finish();

                }
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters1 = new Bundle();
                parameters1.putString(Constants.KEY_CLASS_NAME, OtpViewModel.class.getName());
                parameters1.putString(Constants.KEY_FUNCTION_NAME, "verifyOtpForForgottenPassword");
                parameters1.putString(Constants.KEY_DATA, "parameters = " + parameters);
                Logger.logCrashlytics(ex, parameters1);
                mActivity.hideLoading();
                mActivity.showErrorDialog(mActivity.getString(R.string.otp_verify_failure),
                        mActivity.getString(R.string.otp_verify_failure_message));
            }
        }.execute();
    }

    private void verifyOtp(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_OTP, otp);
        parameters.putString(Constants.KEY_MOBILE_NUMBER, number);

        new VerifyOtpTask(mActivity, parameters){
            @Override
            protected void onSuccess(VerifyOTPResponse verifyOTPResponse) throws Exception {
                super.onSuccess(verifyOTPResponse);
                register();
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters1 = new Bundle();
                parameters1.putString(Constants.KEY_CLASS_NAME, OtpViewModel.class.getName());
                parameters1.putString(Constants.KEY_FUNCTION_NAME, "verifyOtp");
                parameters1.putString(Constants.KEY_DATA, "parameters = " + parameters);
                Logger.logCrashlytics(ex, parameters1);
                mActivity.hideLoading();
                mActivity.showErrorDialog(mActivity.getString(R.string.otp_verify_failure),
                        mActivity.getString(R.string.otp_verify_failure_message));
            }
        }.execute();
    }

    private void register() {
        new RegisterTask(mActivity, getRegisterMeBundle(),
                mDataManager.getLoginPrefs().getSocialLoginAccessToken()){

            @Override
            protected void onSuccess(RegisterResponse registerResponse) throws Exception {
                super.onSuccess(registerResponse);
                signIn();
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, OtpViewModel.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "register");
                parameters.putString(Constants.KEY_DATA, "mobile number = " + number);
                Logger.logCrashlytics(ex, parameters);
                mActivity.hideLoading();
                mActivity.showErrorDialog(mActivity.getString(R.string.registration_failure),
                        mActivity.getString(R.string.registration_failure_message));
            }
        }.execute();
    }

    private void signIn(){

        mDataManager.login(number, password, new OnResponseCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                mActivity.hideLoading();
                performBackgroundTasks();
                ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mActivity.analytic.addMxAnalytics_db("TA Registration",Action.Registration,
                        Page.RegistrationPage.name(), Source.Mobile, number);
                mActivity.finish();
            }

            @Override
            public void onFailure(Exception e) {
                if (e instanceof AuthException) {
                    mActivity.showErrorDialog(
                            mActivity.getString(R.string.login_error),
                            mActivity.getString(R.string.login_failed));
                } else {
                    mActivity.showErrorDialog(null, ErrorUtils.getErrorMessage(e, mActivity));
                }
            }
        });

        /*new LoginTask(mActivity, number, password){
            @Override
            protected void onSuccess(AuthResponse authResponse) throws Exception {
                super.onSuccess(authResponse);
                mActivity.hideLoading();
                ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mActivity.finish();
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hideLoading();

                if (ex instanceof AuthException) {
                    mActivity.showErrorDialog(
                            mActivity.getString(R.string.login_error),
                            mActivity.getString(R.string.login_failed));
                } else {
                    super.onException(ex);
                    mActivity.showErrorDialog(null, ErrorUtils.getErrorMessage(ex, mActivity));
                }
            }
        }.execute();*/

    }

    private void performBackgroundTasks(){
        mDataManager.setCustomFieldAttributes(null);
        mDataManager.setConnectCookies();
        mDataManager.checkSurvey(mActivity, SurveyType.Login);
        mDataManager.updateFirebaseToken(getActivity());
    }

    private Bundle getRegisterMeBundle()
    {
        Bundle regiParameter=new Bundle();

        // set honor_code and terms_of_service to true
        regiParameter.putString(Constants.KEY_HONOR_CODE, "true");
        regiParameter.putString(Constants.KEY_TERMS_OF_SERVICE, "true");

        //set parameter required by social registration
        final String access_token = mDataManager.getLoginPrefs().getSocialLoginAccessToken();
        final String backstore = mDataManager.getLoginPrefs().getSocialLoginProvider();
        boolean fromSocialNet = !TextUtils.isEmpty(access_token);
        if (fromSocialNet) {
            regiParameter.putString(Constants.KEY_ACCESS_TOKEN, access_token);
            regiParameter.putString(Constants.KEY_PROVIDER, backstore);
            regiParameter.putString(Constants.KEY_CLIENT_ID, mDataManager.getConfig().getOAuthClientId());
        }
        regiParameter.putString(Constants.KEY_NAME, number);
        regiParameter.putString(Constants.KEY_USERNAME, number);
        regiParameter.putString(Constants.KEY_PASSWORD, password );
        regiParameter.putString(Constants.KEY_EMAIL, number + "@theteacherapp.org" );
        regiParameter.putString(Constants.KEY_STATE, "");

        return regiParameter;
    }

    /*private void registerMessageListner()
    {
        if (!receiverRegistered) {
            IntentFilter ifilter = new IntentFilter();
            // Use number higher than 999 if you want to be able to stop processing and not to put
            // auth messages into the inbox.
            ifilter.setPriority(1000);
            ifilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            // Create and hold the receiver. We need to unregister on shutdown
            mActivity.registerReceiver(incomingSms, ifilter );
            receiverRegistered = true;
        }
    }

    public void unregisterMessageListener(){
        if (receiverRegistered) {
            mActivity.unregisterReceiver(incomingSms);
            receiverRegistered = false;
        }
    }*/

    @SuppressWarnings("unused")
    public void onEventMainThread(SmsResponse e) {
        Log.d("_______LOG_______", "sms event received");
        switch(e.getStatus().getStatusCode()) {
            case CommonStatusCodes.SUCCESS:

                otp = SmsUtil.getOtp(e.getData());
                Log.d("_______LOG_______", "otp : " + otp);
                if (otp != null && otp.length() == 6) {
                    digit1.set(String.valueOf(otp.charAt(0)));
                    digit2.set(String.valueOf(otp.charAt(1)));
                    digit3.set(String.valueOf(otp.charAt(2)));
                    digit4.set(String.valueOf(otp.charAt(3)));
                    digit5.set(String.valueOf(otp.charAt(4)));
                    digit6.set(String.valueOf(otp.charAt(5)));
                }

                break;
            case CommonStatusCodes.TIMEOUT:
                mActivity.showLongSnack(mActivity.getString(R.string.otp_fetch_failure));
                break;
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }
}
