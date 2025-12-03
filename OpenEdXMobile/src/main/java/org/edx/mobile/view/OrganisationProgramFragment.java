package org.edx.mobile.view;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.ApiLmsService;
import org.edx.mobile.authentication.ApiNewLmsClient;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.EnrollInCourseTask;
import org.edx.mobile.databinding.FragmentNewProgramScreenBinding;
import org.edx.mobile.discovery.model.CourseRuns;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.discovery.model.EnrollResponse;
import org.edx.mobile.discovery.model.ResponseCourseModel;
import org.edx.mobile.discovery.model.ResponseEnrollmentModel;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.MyCourseTask;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.DiscoveryCourseAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import retrofit2.Call;
import retrofit2.Response;

public class OrganisationProgramFragment extends BaseFragment implements
        OnRecyclerItemClickListener<Object>,
        OnNavigateListener {

    public static final String TAG = OrganisationProgramFragment.class.getCanonicalName();

    private static final String ARG_PROGRAM_UUID = "arg_program_uuid";
    private static final String ARG_PROGRAM_NAME = "arg_program_name";
    private static final String ARG_ORGANISATION_NAME = "arg_organisation_name";

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    CourseApi courseApi;

    @Inject
    LoginAPI loginAPI;

    @Inject
    IEdxEnvironment environment;

    private FragmentNewProgramScreenBinding binding;
    private DiscoveryCourseAdapter discoveryCourseAdapter;
    private final Logger logger = new Logger(getClass().getSimpleName());

    private final List<EnrolledCoursesResponse> enrolledCoursesResponses = new ArrayList<>();
    private ResponseCourseModel responseCourseModel;

    private String programUuid;
    private String programName;
    private String organisationName;

    private boolean isEnroll;
    private boolean isEnrollCheck;

    public static OrganisationProgramFragment newInstance(@NonNull String programUuid,
                                                          @NonNull String programName,
                                                          @Nullable String organisationName) {
        OrganisationProgramFragment fragment = new OrganisationProgramFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROGRAM_UUID, programUuid);
        args.putString(ARG_PROGRAM_NAME, programName);
        args.putString(ARG_ORGANISATION_NAME, organisationName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            programUuid = getArguments().getString(ARG_PROGRAM_UUID, "");
            programName = getArguments().getString(ARG_PROGRAM_NAME, "");
            organisationName = getArguments().getString(ARG_ORGANISATION_NAME, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_program_screen, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupStaticUi();
        setupRecycler();
        setupActions();
        showLoadingState();
        ensureTokenAndLoadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.shimmerLayoutOrganisation.startShimmer();
            binding.shimmerLayoutCourse.startShimmer();
            binding.shimmerLayoutProgramName.startShimmer();
            binding.shimmerLayoutCourseButton.startShimmer();
        }
    }

    @Override
    public void onPause() {
        if (binding != null) {
            binding.shimmerLayoutOrganisation.stopShimmer();
            binding.shimmerLayoutCourse.stopShimmer();
            binding.shimmerLayoutProgramName.stopShimmer();
            binding.shimmerLayoutCourseButton.stopShimmer();
        }
        super.onPause();
    }

    private void setupStaticUi() {
        if (binding == null) {
            return;
        }

        binding.selectAProgram.setVisibility(View.GONE);
        binding.rvProgram.setVisibility(View.GONE);
        binding.optionSpinnerPrograms.setVisibility(View.GONE);
        binding.tagName.setVisibility(View.GONE);
        binding.programLanguage.setVisibility(View.GONE);
        binding.linerProgramName.setVisibility(View.VISIBLE);
        binding.ivCheck.setVisibility(View.GONE);
        binding.programNameInCard.setText(getDisplayProgramName());
        binding.organisations.setText(organisationName);
        binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
        binding.shimmerLayoutOrganisation.stopShimmer();
        binding.enrollInProgram.setVisibility(View.GONE);
        binding.unenrollFromProgram.setVisibility(View.GONE);
    }

    private void setupRecycler() {
        if (binding == null || getContext() == null) {
            return;
        }
        discoveryCourseAdapter = new DiscoveryCourseAdapter(requireContext(), this, this);
        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(getContext());
        binding.rvCourses.setLayoutManager(coursesLayoutManager);
        binding.rvCourses.setAdapter(discoveryCourseAdapter);
    }

    private void setupActions() {
        if (binding == null) {
            return;
        }
        binding.enrollInProgram.setOnClickListener(v -> {
            binding.enrollInProgram.setEnabled(false);
            binding.iconProgress.setVisibility(View.VISIBLE);
            EnrollAndUnenrollData.DataCreation data = buildEnrollPayload("enroll");
            try {
                enrollInProgram(data);
            } catch (Exception e) {
                binding.enrollInProgram.setEnabled(true);
                binding.iconProgress.setVisibility(View.GONE);
                logger.error(e);
            }
        });

        binding.unenrollFromProgram.setOnClickListener(v -> {
            binding.unenrollFromProgram.setEnabled(false);
            binding.iconProgress.setVisibility(View.VISIBLE);
            EnrollAndUnenrollData.DataCreation data = buildEnrollPayload("unenroll");
            showConfirmationDialog(getString(R.string.unroll_app_dialog_message), R.string.alert,
                    (dialog, which) -> {
                        dialog.dismiss();
                        try {
                            enrollInProgram(data);
                        } catch (Exception e) {
                            binding.unenrollFromProgram.setEnabled(true);
                            binding.iconProgress.setVisibility(View.GONE);
                            logger.error(e);
                        }
                    });
        });
    }

    private EnrollAndUnenrollData.DataCreation buildEnrollPayload(@NonNull String action) {
        EnrollAndUnenrollData.DataCreation dataCreation = new EnrollAndUnenrollData.DataCreation();
        dataCreation.setCourses(discoveryCourseAdapter.getProgramCoursesIds());
        dataCreation.setAction(action);
        dataCreation.setProgram_uuid(programUuid);
        dataCreation.setProgram_name(getDisplayProgramName());
        dataCreation.setUsername(loginPrefs.getUsername());
        return dataCreation;
    }

    private void ensureTokenAndLoadData() {
        try {
            AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
            long millis = System.currentTimeMillis();
            if (responseJwt == null || (millis - responseJwt.creation_time) > responseJwt.expires_in) {
                createToken();
            } else {
                runParallelTasks();
            }
        } catch (Exception ex) {
            logger.error(ex);
            showErrorState();
        }
    }

    private void createToken() {
        if (getContext() == null) {
            return;
        }
        DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                runParallelTasks();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                showErrorState();
            }
        };
        discoveryTask.execute();
    }

    private void runParallelTasks() {
        int totalTasks = 2;
        AtomicInteger completedTasks = new AtomicInteger(0);
        new CheckEnrollResponseTask(completedTasks, totalTasks).execute();
        new GetCourseListTask(completedTasks, totalTasks).execute();
    }

    private class GetCourseListTask extends AsyncTask<Void, Void, ResponseCourseModel> {
        private final AtomicInteger completedTasks;
        private final int totalTasks;

        GetCourseListTask(AtomicInteger completedTasks, int totalTasks) {
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
        }

        @Override
        protected ResponseCourseModel doInBackground(Void... voids) {
            return fetchCourseList();
        }

        @Override
        protected void onPostExecute(ResponseCourseModel result) {
            if (result != null) {
                responseCourseModel = result;
                populateCourseList();
            } else {
                showErrorState();
            }
            taskCompleted();
        }

        private void taskCompleted() {
            if (completedTasks.incrementAndGet() == totalTasks) {
                hideLoadingState();
            }
        }
    }

    private class CheckEnrollResponseTask extends AsyncTask<Void, Void, ResponseEnrollmentModel> {
        private final AtomicInteger completedTasks;
        private final int totalTasks;

        CheckEnrollResponseTask(AtomicInteger completedTasks, int totalTasks) {
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
        }

        @Override
        protected ResponseEnrollmentModel doInBackground(Void... voids) {
            return fetchEnrollmentStatus();
        }

        @Override
        protected void onPostExecute(ResponseEnrollmentModel result) {
            handleEnrollmentStatus(result);
            taskCompleted();
        }

        private void taskCompleted() {
            if (completedTasks.incrementAndGet() == totalTasks) {
                hideLoadingState();
            }
        }
    }

    private ResponseCourseModel fetchCourseList() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        String selectedLanguage = "en";
        if (getActivity() != null) {
            final String pref = LocaleManager.getLanguagePref(getActivity());
            if (!TextUtils.isEmpty(pref)) {
                selectedLanguage = pref;
            }
        }
        try {
            Call<ResponseCourseModel> call = courseApi.getCourseResponse(token, programUuid, selectedLanguage);
            Response<ResponseCourseModel> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    private ResponseEnrollmentModel fetchEnrollmentStatus() {
        try {
            ApiNewLmsClient apiNewLmsClient = new ApiNewLmsClient(loginAPI.config);
            ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);
            Call<ResponseEnrollmentModel> call = apiService.unrollCheck(
                    loginPrefs.getAuthorizationHeader(),
                    loginPrefs.getUsername(),
                    programUuid
            );
            Response<ResponseEnrollmentModel> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    private void handleEnrollmentStatus(@Nullable ResponseEnrollmentModel result) {
        if (binding == null) {
            return;
        }
        if (result == null) {
            showUnenrolledState();
            return;
        }
        isEnrollCheck = true;
        if ("enrolled".equalsIgnoreCase(result.getEnrollmentStatus())) {
            isEnroll = true;
            showEnrolledState();
            getMyCourseList();
        } else {
            isEnroll = false;
            showUnenrolledState();
            if (responseCourseModel != null) {
                populateCourseList();
            }
        }
    }

    private void populateCourseList() {
        if (binding == null || responseCourseModel == null) {
            return;
        }
        List<CourseRuns> courseRuns = new ArrayList<>();
        if (responseCourseModel.getData() != null) {
            for (ResponseCourseModel.CourseItem courseItem : responseCourseModel.getData()) {
                CourseRuns runs = new CourseRuns();
                runs.setKey(courseItem.getKey());
                runs.setUuid(courseItem.getKey());
                runs.setTitle(courseItem.getTitle());
                runs.setConverted_course_title(courseItem.getConvertedTitle());
                runs.setLanguage(courseItem.getLanguage());
                courseRuns.add(runs);
            }
        }

        discoveryCourseAdapter.setProgramCoursesLists(courseRuns, isEnroll, null);

        binding.courseCount.setText(courseRuns.size() + " " + getString(R.string.courses_available));
        binding.courseCount.setVisibility(View.VISIBLE);

        binding.shimmerLayoutCourse.setVisibility(View.GONE);
        binding.lnEnrollInfo.setVisibility(courseRuns.isEmpty() ? View.GONE : View.VISIBLE);

        if (courseRuns.isEmpty()) {
            binding.errorMsgTv.setText(getString(R.string.no_course_found));
            binding.errorMsgTv.setVisibility(View.VISIBLE);
            binding.errorMsgTv.sendAccessibilityEvent(
                    AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
        } else {
            binding.errorMsgTv.setVisibility(View.GONE);
        }
    }

    private void getMyCourseList() {
        try {
            String selectedLanguage = "en";
            if (getActivity() != null) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
            MyCourseTask myCourseTask = new MyCourseTask(getContext(), programUuid,
                    loginPrefs.getUsername(), loginPrefs.getAuthorizationHeader(), selectedLanguage) {
                @Override
                public void onSuccess(@NonNull List<EnrolledCoursesResponse> result) {
                    enrolledCoursesResponses.clear();
                    enrolledCoursesResponses.addAll(result);
                    populateEnrolledCourses();
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                }
            };
            myCourseTask.execute();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void populateEnrolledCourses() {
        if (binding == null) {
            return;
        }
        List<CourseRuns> courseRuns = new ArrayList<>();
        for (EnrolledCoursesResponse enrolledCourse : enrolledCoursesResponses) {
            if (enrolledCourse.getCourse() == null) {
                continue;
            }
            CourseRuns runs = new CourseRuns();
            runs.setKey(enrolledCourse.getCourse().getId());
            runs.setUuid(enrolledCourse.getCourse().getId());
            runs.setTitle(enrolledCourse.getCourse().getName());
            runs.setConverted_course_title(enrolledCourse.getCourse().getName());
            runs.setCourse_status(enrolledCourse.getCourse_status());
            runs.setLanguage(enrolledCourse.getCourse().getLanguage());
            courseRuns.add(runs);
        }
        discoveryCourseAdapter.setProgramCoursesLists(courseRuns, true, null);
        binding.shimmerLayoutCourse.setVisibility(View.GONE);
        binding.courseCount.setText(courseRuns.size() + " " + getString(R.string.courses_available));
        binding.courseCount.setVisibility(View.VISIBLE);
    }

    private void enrollInProgram(EnrollAndUnenrollData.DataCreation dataCreation) throws Exception {
        EnrollAndUnenrollData enrollData = new EnrollAndUnenrollData();
        enrollData.setData(dataCreation);
        EnrollInCourseTask enrollInCourseTask = new EnrollInCourseTask(enrollData, getContext()) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onSuccess(EnrollResponse enrollResponse) throws Exception {
                super.onSuccess(enrollResponse);
                binding.iconProgress.setVisibility(View.GONE);
                binding.unenrollFromProgram.setEnabled(true);
                binding.enrollInProgram.setEnabled(true);
                if (enrollResponse.isStatus()) {
                    if ("enroll".equals(dataCreation.getAction())) {
                        isEnroll = true;
                        showEnrolledState();
                        getMyCourseList();
                        sendAnalyticsEnroll(enrollData);
                        enrolledStatus(getString(R.string.program_is_successfully_added_to_dashboard));
                    } else {
                        isEnroll = false;
                        enrolledCoursesResponses.clear();
                        discoveryCourseAdapter.setEnroll(false);
                        showUnenrolledState();
                        if (responseCourseModel != null) {
                            populateCourseList();
                        }
                        sendAnalyticsUnenroll(enrollData);
                        enrolledStatus(getString(R.string.program_is_successfully_removed_to_dashboard));
                    }
                } else {
                    showUnenrolledState();
                }
            }

            @Override
            protected void onFinally() {
                super.onFinally();
                binding.iconProgress.setVisibility(View.GONE);
                binding.unenrollFromProgram.setEnabled(true);
                binding.enrollInProgram.setEnabled(true);
            }
        };
        enrollInCourseTask.execute();
    }

    private void showEnrolledState() {
        if (binding == null) {
            return;
        }
        isEnroll = true;
        binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
        binding.enrollInProgram.setVisibility(View.GONE);
        binding.unenrollFromProgram.setVisibility(View.VISIBLE);
        binding.ivCheck.setVisibility(View.VISIBLE);
    }

    private void showUnenrolledState() {
        if (binding == null) {
            return;
        }
        isEnroll = false;
        binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
        binding.enrollInProgram.setVisibility(View.VISIBLE);
        binding.unenrollFromProgram.setVisibility(View.GONE);
        binding.ivCheck.setVisibility(View.GONE);
    }

    private void showLoadingState() {
        if (binding == null) {
            return;
        }
        binding.iconProgress.setVisibility(View.VISIBLE);
        binding.shimmerLayoutProgram.setVisibility(View.VISIBLE);
        binding.shimmerLayoutProgram.startShimmer();
        binding.shimmerLayoutProgramName.setVisibility(View.VISIBLE);
        binding.shimmerLayoutProgramName.startShimmer();
        binding.shimmerLayoutCourse.setVisibility(View.VISIBLE);
        binding.shimmerLayoutCourse.startShimmer();
        binding.shimmerLayoutCourseButton.setVisibility(View.VISIBLE);
        binding.shimmerLayoutCourseButton.startShimmer();
        binding.shimmerEnrollInProgram.setVisibility(View.VISIBLE);
        binding.lnEnrollInfo.setVisibility(View.VISIBLE);
        binding.rvCourses.setVisibility(View.INVISIBLE);
    }

    private void hideLoadingState() {
        if (binding == null) {
            return;
        }
        binding.iconProgress.setVisibility(View.GONE);
        stopContentShimmers();
        binding.shimmerLayoutProgram.setVisibility(View.GONE);
        binding.shimmerLayoutProgramName.setVisibility(View.GONE);
        binding.shimmerLayoutCourse.setVisibility(View.GONE);
        binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
        binding.shimmerEnrollInProgram.setVisibility(View.GONE);
        binding.rvCourses.setVisibility(View.VISIBLE);
    }

    private void showErrorState() {
        if (binding == null) {
            return;
        }
        binding.iconProgress.setVisibility(View.GONE);
        stopContentShimmers();
        binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
        binding.shimmerLayoutProgram.setVisibility(View.GONE);
        binding.shimmerLayoutProgramName.setVisibility(View.GONE);
        binding.shimmerLayoutCourse.setVisibility(View.GONE);
        binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
        binding.shimmerEnrollInProgram.setVisibility(View.GONE);
        //binding.errorMsgTv.setText(getString(R.string.generic_error_message));
        binding.errorMsgTv.setVisibility(View.VISIBLE);
        binding.rvCourses.setVisibility(View.GONE);
        binding.lnEnrollInfo.setVisibility(View.GONE);
    }

    private void stopContentShimmers() {
        if (binding == null) {
            return;
        }
        binding.shimmerLayoutProgram.stopShimmer();
        binding.shimmerLayoutProgramName.stopShimmer();
        binding.shimmerLayoutCourse.stopShimmer();
        binding.shimmerLayoutCourseButton.stopShimmer();
    }
    private void enrolledStatus(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg)
                .setTitle(R.string.alert)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_ok), (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showConfirmationDialog(String message, int titleResource,
                                        DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(titleResource)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_no), (dialog, which) -> {
                    dialog.dismiss();
                    binding.unenrollFromProgram.setEnabled(true);
                    binding.enrollInProgram.setEnabled(true);
                    binding.iconProgress.setVisibility(View.GONE);
                })
                .setNegativeButton(getString(R.string.label_yes), positiveClickListener);

        AlertDialog alert = builder.create();
        alert.setOnShowListener(d -> {
            Button noButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            if (noButton != null) noButton.setContentDescription(getString(R.string.label_no));
            Button yesButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            if (yesButton != null) yesButton.setContentDescription(getString(R.string.label_yes));
        });
        alert.show();
    }

    private void sendAnalyticsEnroll(EnrollAndUnenrollData enrollData) {
        Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ENROLL_PROGRAM_NAME, enrollData.getData().getProgram_Name());
        values.put(Analytics.Keys.ENROLL_PROGRAM_UID, enrollData.getData().getProgram_uuid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.Enroll_Program, null, null, values);
    }

    private void sendAnalyticsUnenroll(EnrollAndUnenrollData enrollData) {
        Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.UNROLL_PROGRAM_NAME, enrollData.getData().getProgram_Name());
        values.put(Analytics.Keys.UNROLL_PROGRAM_UID, enrollData.getData().getProgram_uuid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.Unroll_Program, null, null, values);
    }

    private String getDisplayProgramName() {
        if (!TextUtils.isEmpty(programName)) {
            return programName;
        }
        return programName;
    }

    @Override
    public void onItemClick(View view, Object item) {
        // No-op for program cards; course clicks handled via navigateToAnotherScreen
    }

    @Override
    public void navigateToAnotherScreen(Object item) {
        if (!(item instanceof CourseRuns) || enrolledCoursesResponses.isEmpty() || getActivity() == null) {
            return;
        }
        CourseRuns courseRuns = (CourseRuns) item;
        for (EnrolledCoursesResponse course : enrolledCoursesResponses) {
            if (course.getCourse() != null && TextUtils.equals(courseRuns.getKey(), course.getCourse().getId())) {
                environment.getRouter().showCourseDashboardTabs(getActivity(), course, false);
                break;
            }
        }
    }
}


