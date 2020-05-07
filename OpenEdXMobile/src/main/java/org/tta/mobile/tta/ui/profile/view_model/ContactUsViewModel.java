package org.tta.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.model.profile.FeedbackResponse;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;

public class ContactUsViewModel extends BaseViewModel {

    public ObservableField<String> message = new ObservableField<>("");
    public ObservableBoolean msgValid = new ObservableBoolean();

    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null){
                message.set(s.toString());
                if (message.get().trim().length() > 0){
                    msgValid.set(true);
                } else {
                    msgValid.set(false);
                }
            } else {
                message.set("");
                msgValid.set(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public ContactUsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void send(){
        mActivity.showLoading();
        mDataManager.submitFeedback(message.get().trim(), new OnResponseCallback<FeedbackResponse>() {
            @Override
            public void onSuccess(FeedbackResponse data) {
                mActivity.hideLoading();
                mActivity.showLongSnack(mActivity.getString(R.string.message_sent_successfully));

                mActivity.analytic.addMxAnalytics_db(null, Action.PostFeedback, Nav.profile.name(),
                        Source.Mobile, null);

                mActivity.onBackPressed();

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });
    }
}
