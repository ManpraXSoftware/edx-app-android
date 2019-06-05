package org.tta.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import org.tta.mobile.BuildConfig;
import org.tta.mobile.R;
import org.tta.mobile.base.BaseFragment;
import org.tta.mobile.core.IEdxEnvironment;
import org.tta.mobile.databinding.FragmentAccountBinding;
import org.tta.mobile.module.prefs.LoginPrefs;
import org.tta.mobile.util.Config;

public class AccountFragment extends BaseFragment {
    private static final String TAG = AccountFragment.class.getCanonicalName();
    private FragmentAccountBinding binding;

    @Inject
    private Config config;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private LoginPrefs loginPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);

        if (config.isUserProfilesEnabled()) {
            binding.profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getRouter().showUserProfile(getActivity(), loginPrefs.getUsername());
                }
            });
        } else {
            binding.profileBtn.setVisibility(View.GONE);
        }

        binding.settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showSettings(getActivity());
            }
        });

        binding.feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showFeedbackScreen(getActivity(), getString(R.string.email_subject));
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().performManualLogout(getActivity(),
                        environment.getAnalyticsRegistry(), environment.getNotificationDelegate());
            }
        });

        binding.tvVersionNo.setText(String.format("%s %s %s", getString(R.string.label_version),
                BuildConfig.VERSION_NAME, environment.getConfig().getEnvironmentDisplayName()));

        return binding.getRoot();
    }
}
