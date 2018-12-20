package org.edx.mobile.tta.ui.reset_password.view_model;

import android.databinding.ObservableField;

import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.otp.OtpActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

public class EnterNumberViewModel extends BaseViewModel {

    public ObservableField<String> cellphone = new ObservableField<>("");

    public EnterNumberViewModel(BaseVMActivity activity) {
        super(activity);
    }

    public void verify(){
        ActivityUtil.gotoPage(mActivity, OtpActivity.class);
    }
}
