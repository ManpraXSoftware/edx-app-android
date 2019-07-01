package org.tta.mobile.tta.ui.logistration;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.logistration.view_model.RegisterViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.util.ResourceUtil;

public class RegisterFragment extends TaBaseFragment {
    private static final int RANK = 1;

    private RegisterViewModel viewModel;

    private TextView tvPrivacyPolicy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new RegisterViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_register, viewModel)
                .getRoot();

        tvPrivacyPolicy = view.findViewById(R.id.privacy_policy);

        String ppText = getString(R.string.privacy_policy);
        String ppMessage = ResourceUtil.getFormattedString(getResources(), R.string.privacy_policy_message,
                "privacy_policy", ppText).toString();
        SpannableString ppSpan = new SpannableString(ppMessage);
        ppSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@androidx.annotation.NonNull View widget) {
                viewModel.showPrivacyPolicy();
            }

            @Override
            public void updateDrawState(@androidx.annotation.NonNull TextPaint ds) {
                super.updateDrawState(ds);

                ds.setUnderlineText(true);
            }
        }, ppMessage.indexOf(ppText), ppMessage.indexOf(ppText) + ppText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvPrivacyPolicy.setText(ppSpan);
        tvPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        tvPrivacyPolicy.setHighlightColor(Color.TRANSPARENT);

        return view;
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        viewModel.generateOTP();
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.signup.name()));
    }
}
