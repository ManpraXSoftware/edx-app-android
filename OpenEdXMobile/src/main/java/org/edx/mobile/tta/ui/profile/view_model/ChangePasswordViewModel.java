package org.edx.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import org.edx.mobile.R;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.model.profile.ChangePasswordResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class ChangePasswordViewModel extends BaseViewModel {

    public ObservableField<String> oldPass = new ObservableField<>("");
    public ObservableField<String> newPass = new ObservableField<>("");
    public ObservableBoolean oldValid = new ObservableBoolean();
    public ObservableBoolean newValid = new ObservableBoolean();
    public ObservableInt oldPassDrawable = new ObservableInt();
    public ObservableInt newPassDrawable = new ObservableInt();
    public ObservableBoolean oldPassToggleEnabled = new ObservableBoolean();
    public ObservableBoolean newPassToggleEnabled = new ObservableBoolean();

    private boolean oldPassVisible = false;
    private boolean newPassVisible = false;

    public TextWatcher oldWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null){
                oldPass.set(s.toString());
                if (oldPass.get().length() >= 3){
                    oldValid.set(true);
                } else {
                    oldValid.set(false);
                }
            } else {
                oldPass.set("");
                oldValid.set(false);
            }
            setOldPassDrawable();
        }
    };

    public TextWatcher newWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null){
                newPass.set(s.toString());
                if (newPass.get().length() >= 3){
                    newValid.set(true);
                } else {
                    newValid.set(false);
                }
            } else {
                newPass.set("");
                newValid.set(false);
            }
            setNewPassDrawable();
        }
    };

    public ChangePasswordViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void save(){
        mActivity.showLoading();
        mDataManager.changePassword(oldPass.get(), newPass.get(), new OnResponseCallback<ChangePasswordResponse>() {
            @Override
            public void onSuccess(ChangePasswordResponse data) {
                mActivity.hideLoading();
                mActivity.showLongSnack("Password changed successfully");
                mActivity.onBackPressed();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                if (e instanceof AuthException){
                    mActivity.showLongSnack(e.getLocalizedMessage());
//                    mDataManager.logout();
                } else {
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            }
        });
    }

    private void setOldPassDrawable() {
        if (oldPass.get().length() > 0) {
            oldPassToggleEnabled.set(true);
            if (oldPassVisible) {
                oldPassDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                oldPassDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            oldPassToggleEnabled.set(false);
        }
    }

    private void setNewPassDrawable() {
        if (newPass.get().length() > 0) {
            newPassToggleEnabled.set(true);
            if (newPassVisible) {
                newPassDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                newPassDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            newPassToggleEnabled.set(false);
        }
    }
}
