package org.tta.mobile.tta.ui.deep_link.view_model;

import android.os.Bundle;

import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.enums.SourceType;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.tta.mobile.tta.ui.course.CourseDashboardActivity;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.AppUtil;

public class DeepLinkViewModel extends BaseViewModel {

    public DeepLinkViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.onAppStart();
    }

    public void fetchContent(long contentId){

        mDataManager.getContent(contentId, new OnResponseCallback<Content>() {
            @Override
            public void onSuccess(Content data) {
                showContentDashboard(data);
                mActivity.finish();
            }

            @Override
            public void onFailure(Exception e) {
                gotoLandingPage();
            }
        });

    }

    public void fetchContent(String sourceIdentity) {

        mDataManager.getContentFromSourceIdentity(sourceIdentity, new OnResponseCallback<Content>() {
            @Override
            public void onSuccess(Content data) {
                showContentDashboard(data);
                mActivity.finish();
            }

            @Override
            public void onFailure(Exception e) {
                gotoLandingPage();
            }
        });

    }

    private void showContentDashboard(Content content){

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, content);
        parameters.putBoolean(Constants.KEY_IS_PUSH, true);
        if (content.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                content.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);
        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }

    public void gotoLandingPage(){
        if (!LandingActivity.isAlreadyOpened) {
            ActivityUtil.gotoPage(mActivity, LandingActivity.class);
        }
        mActivity.finish();
    }
}
