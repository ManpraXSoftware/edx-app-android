package org.edx.mobile.view;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentOrganisationDetailsBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.ProgramModel;
import org.edx.mobile.discovery.model.ProgramResultList;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.OrganisationProgramsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class OrganisationDetailsFragment extends BaseFragment implements OnRecyclerItemClickListener<ProgramResultList> {

    public static final String TAG = OrganisationDetailsFragment.class.getCanonicalName();
    private static final String ARG_ORG_UUID = "arg_org_uuid";
    private static final String ARG_ORG_NAME = "arg_org_name";

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    CourseApi courseApi;

    @Inject
    IEdxEnvironment environment;

    private FragmentOrganisationDetailsBinding binding;
    private OrganisationProgramsAdapter adapter;
    private String organisationUuid;
    private String organisationName;

    public static OrganisationDetailsFragment newInstance(@NonNull String organisationUuid,
                                                          @NonNull String organisationName) {
        OrganisationDetailsFragment fragment = new OrganisationDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ORG_UUID, organisationUuid);
        bundle.putString(ARG_ORG_NAME, organisationName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organisationUuid = getArguments().getString(ARG_ORG_UUID);
            organisationName = getArguments().getString(ARG_ORG_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_organisation_details, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUi();
        try {
            ensureTokenAndLoadPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainBottomDashboardFragment.suodhaIcon() != null) {
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
        }
        if (MainBottomDashboardFragment.backIcon() != null) {
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
        }
    }

    private void setupUi() {
        if (binding == null) return;
        binding.organisationTitle.setText(organisationName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.programList.setLayoutManager(layoutManager);
        adapter = new OrganisationProgramsAdapter(requireContext(), this, organisationName);
        binding.programList.setAdapter(adapter);
    }

    private void ensureTokenAndLoadPrograms() throws Exception {
        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
        long millis = System.currentTimeMillis();
        long tokenTime = millis - responseJwt.creation_time;
        if (tokenTime > responseJwt.expires_in) {
            createToken();
        } else {
            fetchOrganisationPrograms();
        }
    }

    private void createToken() throws Exception {
        if (getContext() == null) return;
        DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                fetchOrganisationPrograms();
            }

            @Override
            public void onException(Exception ex) {
                if (!(ex instanceof HttpStatusException && ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED)) {
                    ex.printStackTrace();
                }
                showErrorState();
            }
        };
        discoveryTask.execute();
    }

    private void fetchOrganisationPrograms() {
        if (binding == null) return;
        showLoadingState();
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        String selectedLanguage = "en";
        if (getActivity() != null) {
            String storedLanguage = LocaleManager.getLanguagePref(getActivity());
            if (!TextUtils.isEmpty(storedLanguage)) {
                selectedLanguage = storedLanguage;
            }
        }

        Call<ProgramModel> call = courseApi.getProgramsByOrganisationUuid(token, selectedLanguage, organisationUuid);
        call.enqueue(new DiscoveryCallback<ProgramModel>() {
            @Override
            protected void onResponse(@NonNull ProgramModel responseBody) {
                if (binding == null) {
                    return;
                }
                List<ProgramResultList> programs = responseBody.getProgramResultLists();
                if (programs == null) {
                    programs = new ArrayList<>();
                }
                if (programs.isEmpty()) {
                    showEmptyState();
                } else {
                    showContentState();
                    adapter.setPrograms(programs);
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
                showErrorState();
            }
        });
    }

    private void showLoadingState() {
        if (binding == null) return;
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.programList.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);
    }

    private void showContentState() {
        if (binding == null) return;
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.programList.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        if (binding == null) return;
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.programList.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    private void showErrorState() {
        showEmptyState();
    }

    @Override
    public void onItemClick(View view, ProgramResultList program) {
        openProgramDetails(program);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openProgramDetails(@NonNull ProgramResultList program) {
        if (getActivity() == null) {
            return;
        }
        if (MainBottomDashboardFragment.suodhaIcon() != null) {
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
        }
        if (MainBottomDashboardFragment.backIcon() != null) {
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
        }
        trackProgramSelection(program);

        OrganisationProgramFragment fragment = OrganisationProgramFragment.newInstance(
                program.getUuid(),
                program.getTitle(),
                organisationName
        );
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, fragment, OrganisationProgramFragment.TAG)
                .addToBackStack(OrganisationProgramFragment.TAG)
                .commit();
    }

    private void trackProgramSelection(@NonNull ProgramResultList program) {
        Map<String, String> values = new HashMap<>();
        String displayName = TextUtils.isEmpty(program.getConverted_title())
                ? program.getTitle()
                : program.getConverted_title();
        if (!TextUtils.isEmpty(displayName)) {
            values.put(Analytics.Keys.PROGRAM_NAME, displayName);
        }
        values.put(Analytics.Keys.PROGRAM_UUID, program.getUuid());
        values.put(Analytics.Keys.ORGANISATION_NAME, organisationName);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.View_Program, null, "Click", values);
    }
}

