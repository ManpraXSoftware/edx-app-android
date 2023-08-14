package org.humana.mobile.tta.ui.programs.pendingUnits.viewModel;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class PendingUnitsWebViewViewModel extends BaseViewModel {

    private static final int TAKE = 10;
    private static final int SKIP = 0;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;
    public ObservableField<String> userName = new ObservableField<>();
    private List<Unit> unitsList;

    public RecyclerView.LayoutManager layoutManager;

    public float rating = 0;

    public PendingUnitsWebViewViewModel(BaseVMActivity activity) {
        super(activity);

        Bundle bundle = mActivity.getIntent().getExtras();
        assert bundle != null;
        userName.set(bundle.getString("username"));
        layoutManager = new LinearLayoutManager(mActivity);
        unitsList = new ArrayList<>();
        changesMade = true;
        take = TAKE;

        skip = SKIP;

        mActivity.showLoading();
    }



    public void getWebResponse() {
        String role;
        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
            role= mDataManager.getLoginPrefs().getRole();
        }else {
            role = "staff";
        }

//        mDataManager.setSpecificSession(role,
//                userName.get(), new OnResponseCallback<SuccessResponse>() {
//                    @Override
//                    public void onSuccess(SuccessResponse response) {
//                        if (response.getSuccess()){
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//
//                    }
//                });
    }

}
