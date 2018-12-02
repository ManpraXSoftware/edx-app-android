package org.edx.mobile.tta.ui.base.mvvm;

import android.content.Context;

import org.edx.mobile.tta.data.DataManager;
import org.edx.mobile.tta.ui.base.TaBaseActivity;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

/**
 * Created by Arjun on 2018/3/11.
 */

public class BaseViewModel {
    protected TaBaseActivity mActivity;
    protected TaBaseFragment mFragment;
    //protected DataManager mDataManager = DataManager.getInstance();

    public BaseViewModel(BaseVMActivity activity) {
        mActivity = activity;
    }

    public BaseViewModel(Context context, TaBaseFragment fragment) {
        mActivity = (TaBaseActivity) context;
        mFragment = fragment;
    }
}
