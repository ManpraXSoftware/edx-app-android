package org.tta.mobile.tta.ui.reset_password;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tta.mobile.R;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.reset_password.view_model.EnterNumberViewModel;
import org.tta.mobile.util.PermissionsUtil;

public class EnterNumberActivity extends BaseVMActivity {

    private EnterNumberViewModel viewModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new EnterNumberViewModel(this);
        binding(R.layout.t_activity_enter_number, viewModel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PermissionsUtil.READ_SMS_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    viewModel.generateOTP();
                }
        }
    }
}
