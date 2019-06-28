package org.tta.mobile.tta.ui.reset_password.view_model;

import android.Manifest;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import org.tta.mobile.R;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.model.authentication.MobileNumberVerificationResponse;
import org.tta.mobile.tta.task.authentication.MobileNumberVerificationTask;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.otp.OtpActivity;
import org.tta.mobile.tta.ui.otp.SmsModule;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.PermissionsUtil;

import static org.tta.mobile.tta.Constants.KEY_MOBILE_NUMBER;

public class EnterNumberViewModel extends BaseViewModel {

    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableBoolean cellValid = new ObservableBoolean();

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
            if (numString.length() == 10 && numString.matches("[0-9]+")){
                cellValid.set(true);
            } else {
                cellValid.set(false);
            }
        }
    };

    public EnterNumberViewModel(BaseVMActivity activity) {
        super(activity);
    }

    public void verify(){
        //check here for message read and receive for otp feature
        /*if (PermissionsUtil.checkPermissions(Manifest.permission.READ_SMS, mActivity) &&
                PermissionsUtil.checkPermissions(Manifest.permission.RECEIVE_SMS, mActivity)) {
            generateOTP();
        } else {
            mActivity.askForPermissions(new String[]{Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS},
                    PermissionsUtil.READ_SMS_PERMISSION_REQUEST);
        }*/
        generateOTP();
    }

    public void generateOTP(){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        mActivity.showLoading();

        Bundle parameters = new Bundle();
        parameters.putString(KEY_MOBILE_NUMBER, cellphone.get());

        //adding version for otp handling
        if(mDataManager.getConfig().getSMSKey()!=null || !mDataManager.getConfig().getSMSKey().isEmpty()) {
            parameters.putString("version", "1");
            parameters.putString("sms_key", mDataManager.getConfig().getSMSKey());
        }

        SmsModule.intialiseSMSRetrieverClient(mActivity);
        new MobileNumberVerificationTask(mActivity, parameters){
            @Override
            protected void onSuccess(MobileNumberVerificationResponse mobileNumberVerificationResponse) throws Exception {
                super.onSuccess(mobileNumberVerificationResponse);
                mActivity.hideLoading();

                if (mobileNumberVerificationResponse.mobile_number() != null && !mobileNumberVerificationResponse.mobile_number().equals("")){
                    parameters.putString(Constants.KEY_OTP_SOURCE, Constants.OTP_SOURCE_RESET_PASSWORD);
                    ActivityUtil.gotoPage(mActivity, OtpActivity.class, parameters);
                }
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hideLoading();
                mActivity.showErrorDialog("User not exist", "User with this mobile number doesn't exist.");
            }
        }.execute();
    }
}
