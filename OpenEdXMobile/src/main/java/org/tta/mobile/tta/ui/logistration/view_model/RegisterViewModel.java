package org.tta.mobile.tta.ui.logistration.view_model;

import android.Manifest;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import org.tta.mobile.R;
import org.tta.mobile.http.HttpResponseStatusException;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.model.authentication.SendOTPResponse;
import org.tta.mobile.tta.task.authentication.GenerateOtpTask;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.otp.OtpActivity;
import org.tta.mobile.tta.ui.otp.SmsModule;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.PermissionsUtil;

public class RegisterViewModel extends BaseViewModel {

    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    public ObservableField<String> confirmPassword = new ObservableField<>("");
    public ObservableBoolean cellValid = new ObservableBoolean();
    public ObservableBoolean passValid = new ObservableBoolean();
    public ObservableBoolean confirmPassValid = new ObservableBoolean();
    public ObservableInt passDrawable = new ObservableInt();
    public ObservableInt confirmPassDrawable = new ObservableInt();
    public ObservableBoolean passToggleEnabled = new ObservableBoolean();
    public ObservableBoolean confirmPassToggleEnabled = new ObservableBoolean();

    private boolean passVisible = false;
    private boolean confirmPassVisible = false;

    public TextWatcher cellWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            cellphone.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 10 && numString.matches("[0-9]+")) {
                cellValid.set(true);
            } else {
                cellValid.set(false);
            }
        }
    };

    public TextWatcher passWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            password.set(s.toString());
            setPassDrawable();
            String passString = s.toString();
            if (passString.length() >= 3 && passString.equals(confirmPassword.get())) {
                passValid.set(true);
                confirmPassValid.set(true);
            } else {
                passValid.set(false);
                confirmPassValid.set(false);
            }
        }
    };

    public TextWatcher ConfirmPassWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            confirmPassword.set(s.toString());
            setConfirmPassDrawable();
            String confirmPassString = s.toString();
            if (confirmPassString.length() >= 3 && confirmPassString.equals(password.get())) {
                passValid.set(true);
                confirmPassValid.set(true);
            } else {
                passValid.set(false);
                confirmPassValid.set(false);
            }
        }
    };

    public RegisterViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void showPrivacyPolicy(){
        mDataManager.getEdxEnvironment().getRouter().showAuthenticatedWebviewActivity(
                mActivity, mActivity.getString(R.string.privacy_policy_url),
                mActivity.getString(R.string.privacy_policy)
        );
    }

    public void register() {
        //check here for message read and receive for otp feature
        /*if (PermissionsUtil.checkPermissions(Manifest.permission.READ_SMS, mActivity) &&
                PermissionsUtil.checkPermissions(Manifest.permission.RECEIVE_SMS, mActivity)) {
            generateOTP();
        } else {
            mFragment.askForPermissions(new String[]{Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS},
                    PermissionsUtil.READ_SMS_PERMISSION_REQUEST);
        }*/
        generateOTP();
    }

    public void signIn() {
        BaseVMActivity activity = (BaseVMActivity) mActivity;
        if (activity.getViewModel() instanceof SigninRegisterViewModel) {
            SigninRegisterViewModel viewModel = (SigninRegisterViewModel) activity.getViewModel();
            viewModel.toggleTab();
        }
    }

    private void setPassDrawable() {
        if (password.get().length() > 0) {
            passToggleEnabled.set(true);
            if (passVisible) {
                passDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                passDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            passToggleEnabled.set(false);
        }
    }

    private void setConfirmPassDrawable() {
        if (confirmPassword.get().length() > 0) {
            confirmPassToggleEnabled.set(true);
            if (confirmPassVisible) {
                confirmPassDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                confirmPassDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            confirmPassToggleEnabled.set(false);
        }
    }

    public void generateOTP(){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        mActivity.showLoading();

        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_MOBILE_NUMBER, cellphone.get());

        final String access_token = mDataManager.getLoginPrefs().getSocialLoginAccessToken();
        final String backstore = mDataManager.getLoginPrefs().getSocialLoginProvider();
        boolean fromSocialNet = !TextUtils.isEmpty(access_token);
        if (fromSocialNet) {
            parameters.putString(Constants.KEY_ACCESS_TOKEN, access_token);
            parameters.putString(Constants.KEY_PROVIDER, backstore);
            parameters.putString(Constants.KEY_CLIENT_ID, mDataManager.getConfig().getOAuthClientId());
        }

        //adding version for otp handling
        if(mDataManager.getConfig().getSMSKey()!=null || !mDataManager.getConfig().getSMSKey().isEmpty()) {
            parameters.putString("version", "1");
            parameters.putString("sms_key", mDataManager.getConfig().getSMSKey());
        }

        SmsModule.intialiseSMSRetrieverClient(mActivity);
        new GenerateOtpTask(mActivity, parameters) {
            @Override
            protected void onSuccess(SendOTPResponse sendOTPResponse) throws Exception {
                super.onSuccess(sendOTPResponse);
                mActivity.hideLoading();

                if(sendOTPResponse.mobile_number().equals(cellphone.get())){

                    parameters.putString(Constants.KEY_PASSWORD, password.get());
                    parameters.putString(Constants.KEY_OTP_SOURCE, Constants.OTP_SOURCE_REGISTER);
                    ActivityUtil.gotoPage(mActivity, OtpActivity.class, parameters);

                }
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters1 = new Bundle();
                parameters1.putString(Constants.KEY_CLASS_NAME, RegisterViewModel.class.getName());
                parameters1.putString(Constants.KEY_FUNCTION_NAME, "generateOTP");
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
                    parameters.putString(Constants.KEY_CLASS_NAME, RegisterViewModel.class.getName());
                    parameters.putString(Constants.KEY_FUNCTION_NAME, "generateOTP");
                    parameters.putString(Constants.KEY_DATA, "Mobile number = " + cellphone.get());
                    Logger.logCrashlytics(exp, parameters);
                    errorMsg = mActivity.getString(R.string.server_not_responding);
                }

                mActivity.showErrorDialog(mActivity.getString(R.string.registration_failure), errorMsg);
            }
        }.execute();
    }
}
