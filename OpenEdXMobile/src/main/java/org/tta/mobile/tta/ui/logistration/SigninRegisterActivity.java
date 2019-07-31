package org.tta.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.logistration.view_model.SigninRegisterViewModel;
import org.tta.mobile.view.common.PageViewStateCallback;

/**
 * Created by Arjun on 2018/6/20.
 */

public class SigninRegisterActivity extends BaseVMActivity {

    private ViewPager viewPager;

    private SigninRegisterViewModel viewModel;

    public static boolean isAlreadyOpened;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SigninRegisterViewModel(this);
        binding(R.layout.t_activity_signin_register, viewModel);

        viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAlreadyOpened = true;
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, SigninRegisterActivity.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "onResume");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlreadyOpened = false;
    }
}
