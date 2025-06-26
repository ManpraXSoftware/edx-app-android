package org.edx.mobile.view;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.app.App;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.EnrollInCourseTask;
import org.edx.mobile.databinding.FragmentNewProgramScreenBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.AuthoringOrganisations;
import org.edx.mobile.discovery.model.CourseRuns;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.discovery.model.EnrollResponse;
import org.edx.mobile.discovery.model.ProgramCoursesList;
import org.edx.mobile.discovery.model.ProgramModel;
import org.edx.mobile.discovery.model.ProgramResponseModel;
import org.edx.mobile.discovery.model.ProgramResultList;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.MyCourseTask;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.programs.MyProgramTags;
import org.edx.mobile.programs.ProgramTask;
import org.edx.mobile.programs.Programs;
import org.edx.mobile.programs.ResumeCourse;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.DiscoveryCourseAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.ProgramModelAdapter;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;

public class ProgramFragment extends BaseFragment implements OnRecyclerItemClickListener,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    public static final String TAG = NewProgramFragment.class.getCanonicalName();
    private static final int MY_COURSE_LOADER_ID = 0x905000;
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    CourseApi courseApi;
    private static String topic_name = "";
    private static String topic_converted_name = "";
    private FragmentNewProgramScreenBinding binding;
    private ProgramModelAdapter programModelAdapter;
    private DiscoveryCourseAdapter discoveryCourseAdapter;
    private final Logger logger = new Logger(getClass().getSimpleName());
    ArrayList<EnrolledCoursesResponse> enrolledCoursesResponses = new ArrayList<>();

    @javax.inject.Inject
    private IEdxEnvironment environment;
    private String authorising_organisation = "";
    private String program_selected_uuid = "";
    private static String program_uuid = "";
    List<ProgramResponseModel.Program> programResultLists = new ArrayList<>();
    private App mApp;

    private List<MyProgramListModel> myProgramListModels = new ArrayList<>();
    private ResumeCourse resumeCourse;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;
    private ArrayAdapter<String> programAdapter;
    private List<String> programsNameLists = new ArrayList<>();

    public static NewProgramFragment newInstance(@Nullable Bundle bundle) {
        final NewProgramFragment fragment = new NewProgramFragment();
        topic_name = bundle.getString(ProgramActivity.PROGRAM);
        topic_converted_name = bundle.getString(ProgramActivity.PROGRAM_CONVERTED);
        program_uuid = bundle.getString(ProgramActivity.PROGRAM_UUID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topic_name = getArguments().getString(ProgramActivity.PROGRAM);
        topic_converted_name = getArguments().getString(ProgramActivity.PROGRAM_CONVERTED);
        program_uuid = getArguments().getString(ProgramActivity.PROGRAM_UUID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_program_screen, container,
                false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        view.announceForAccessibility("Programs Screen");
        mApp = new App();
        if (!program_uuid.isEmpty()) {
            binding.selectAProgram.setVisibility(View.GONE);
            binding.lnEnrollInfo.setVisibility(View.GONE);
            binding.shimmerLayoutProgram.setVisibility(View.GONE);
            binding.rvProgram.setVisibility(View.GONE);
            binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
            binding.ivCheck.setVisibility(View.GONE);
//            binding.courseDatailUnenroll.setVisibility(View.GONE);
//            binding.courseDatailEnroll.setVisibility(View.GONE);
            binding.lnEnrollInfo.setVisibility(View.GONE);
        }
        String sourceString = "";
        if (topic_converted_name != null && !topic_converted_name.isEmpty()) {
            sourceString = "<b>" + topic_converted_name + "</b> ";
        } else {
            sourceString = "<b>" + topic_name + "</b> ";
        }

        binding.tagName.setText(Html.fromHtml(sourceString));
        programModelAdapter = new ProgramModelAdapter(getActivity(), ProgramFragment.this::onItemClick);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvProgram.setLayoutManager(mLayoutManager);
        binding.rvProgram.setAdapter(programModelAdapter);

        //discoveryCourseAdapter = new DiscoveryCourseAdapter(getActivity(), ProgramFragment.this::onItemClick);
        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(getContext());
        binding.rvCourses.setLayoutManager(mLayoutManager1);
        binding.rvCourses.setAdapter(discoveryCourseAdapter);
        binding.enrollInProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.enrollInProgram.setEnabled(false);
                String strings = discoveryCourseAdapter.getProgramCoursesIds();
                EnrollAndUnenrollData.DataCreation dataCreation = new EnrollAndUnenrollData.DataCreation();
                dataCreation.setCourses(strings);
                dataCreation.setAction("enroll");
                dataCreation.setProgram_uuid(program_selected_uuid);
                dataCreation.setUsername(loginPrefs.getUsername());
                try {
                    enrollcourse(dataCreation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        binding.unenrollFromProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.unenrollFromProgram.setEnabled(false);
                String strings = discoveryCourseAdapter.getProgramCoursesIds();
                EnrollAndUnenrollData.DataCreation dataCreation = new EnrollAndUnenrollData.DataCreation();
                dataCreation.setCourses(strings);
                dataCreation.setAction("unenroll");
                dataCreation.setProgram_uuid(program_selected_uuid);
                dataCreation.setUsername(loginPrefs.getUsername());
                try {
                    enrollcourse(dataCreation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initSpinner() {
        if (program_uuid.isEmpty()) {
            binding.optionSpinnerPrograms.setVisibility(View.VISIBLE);
            binding.shimmerLayoutProgram.setVisibility(View.GONE);
        }
        if (programsNameLists != null && programsNameLists.size() > 10) {
            try {
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);

                // Get private mPopup member variable and try cast to ListPopupWindow
                android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(binding.optionSpinnerPrograms);

                // Set popupWindow height to 500px
                popupWindow.setHeight(500);
            } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
                // silently fail...
            }
        }
        if (getActivity() != null) {
            programAdapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.edx_spinner_dropdown_item, programsNameLists) {
                @Override
                public boolean isEnabled(int position) {   //If question is school grade then disable user input
                    return super.isEnabled(position);
                }

                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    return view;
                }
            };
            programAdapter.setDropDownViewResource(R.layout.edx_spinner_dropdown_item);
            binding.optionSpinnerPrograms.setAdapter(programAdapter);
            binding.optionSpinnerPrograms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                    String filterTitle = (String) parent.getItemAtPosition(position);
//                    binding.optionSpinnerPrograms.setSelection(position, true);
//                    binding.optionSpinnerPrograms.setSelected(true);
//                    (programResultList.getConverted_title() != null && !programResultList.getConverted_title().isEmpty()) ||
                    for (ProgramResponseModel.Program programResultList : programResultLists) {
                        if ( (programResultList.getTitle()).equals(filterTitle) || (programResultList.getTitle()).equals(filterTitle)) {
                          /*  if (programResultList.isProgramEnroll()) {
                                binding.unenrollFromProgram.setVisibility(View.VISIBLE);
                                binding.enrollInProgram.setVisibility(View.GONE);
                                binding.ivCheck.setVisibility(View.VISIBLE);
                                if (topic_converted_name != null && !topic_converted_name.isEmpty()) {
                                    if (programResultList.getConverted_title() != null && !programResultList.getConverted_title().isEmpty()) {
                                        binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                                                topic_converted_name + " " + programResultList.getConverted_title() + " " + getString(R.string.program));
                                    } else {
                                        binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                                                topic_converted_name + " " + programResultList.getTitle() + " " + getString(R.string.program));
                                    }

                                } else {
                                    binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                                            topic_name + " " + programResultList.getTitle() + " " + getString(R.string.program));
                                }

                                binding.courseDatailEnroll.setVisibility(View.VISIBLE);
                                binding.courseDatailUnenroll.setVisibility(View.GONE);
                            } else*/ if (true) {
                                binding.enrollInProgram.setVisibility(View.VISIBLE);
                                binding.unenrollFromProgram.setVisibility(View.GONE);
                                binding.ivCheck.setVisibility(View.GONE);
//                                binding.courseDatailEnroll.setVisibility(View.GONE);
//                                binding.courseDatailUnenroll.setVisibility(View.VISIBLE);
                            }
                            binding.lnEnrollInfo.setVisibility(View.VISIBLE);
                            binding.errorMsgTv.setVisibility(View.GONE);
                            program_selected_uuid = programResultList.getUuid();
                            binding.shimmerLayoutProgramName.setVisibility(View.GONE);
                            binding.linerProgramName.setVisibility(View.VISIBLE);
                            if (programResultList.getTitle() != null && !programResultList.getTitle().isEmpty()) {
                                binding.programNameInCard.setText(programResultList.getTitle());
                            } else {
                                binding.programNameInCard.setText(programResultList.getTitle());
                            }

                            if (programResultList.getAuthoringOrganizations() != null && programResultList.getAuthoringOrganizations().size() > 0) {
                                authorising_organisation = "";
                                if (programResultList.getAuthoringOrganizationUuids().size() > 1) {
                                    for (ProgramResponseModel.AuthoringOrganization authoringOrganisations : programResultList.getAuthoringOrganizations()) {
                                        if (authorising_organisation.isEmpty()) {
                                            authorising_organisation = authoringOrganisations.getName();
                                        } else {
                                            authorising_organisation = authoringOrganisations + "," + authoringOrganisations.getName();
                                        }
                                    }
                                } else {
                                    authorising_organisation = programResultList.getAuthoringOrganizations().get(0).getName();
                                }
                            }
                            binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
                            binding.organisations.setText(authorising_organisation);
                            boolean enroll = false;
                            List<CourseRuns> courseRuns = new ArrayList<>();
                            if (enrolledCoursesResponses != null) {
                                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                                    if (enrolledCoursesResponse.getCourse() != null) {
                                        CourseRuns courseRunsObject = new CourseRuns();
                                        courseRunsObject.setKey("12345");
                                        courseRunsObject.setUuid("abcde");
                                        courseRunsObject.setTitle("Introduction to Java");
                                        courseRunsObject.setCourse_status("Active");
                                        courseRunsObject.setConverted_course_title("Java Programming");
                                        courseRuns.add(courseRunsObject);

                                        /*for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
                                            if (programCoursesList.getCourseRuns() != null) {
                                                for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {
                                                    if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                                                        courseRuns.setCourse_status(enrolledCoursesResponse.getCourse_status());
                                                        enroll = true;
                                                    }
                                                }
                                            }
                                        }*/
                                    }
                                }
                            }
                            binding.shimmerLayoutCourseButton.setVisibility(View.GONE);


                            binding.shimmerLayoutCourse.setVisibility(View.GONE);
                            discoveryCourseAdapter.setProgramCoursesLists(courseRuns,/* programResultList.isProgramEnroll()*/ true, resumeCourse);
                            if (courseRuns == null) {
                                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                                binding.lnEnrollInfo.setVisibility(View.GONE);
                                binding.errorMsgTv.setVisibility(View.VISIBLE);
                                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                            } else if (courseRuns.size() == 0) {
                                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                                binding.lnEnrollInfo.setVisibility(View.GONE);
                                binding.errorMsgTv.setVisibility(View.VISIBLE);
                                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                            }

                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        try {
            getMyCourseList();
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(runnable, delay);
                    if (binding.iconProgress.getVisibility() == View.VISIBLE) {
                        binding.enrollInProgram.setEnabled(false);
                        binding.unenrollFromProgram.setEnabled(false);
                        if (getActivity() != null)
                            Toast.makeText(getContext(), getActivity().getString(R.string.data_is_loading), Toast.LENGTH_SHORT).show();
                    } else {
                        binding.enrollInProgram.setEnabled(true);
                        binding.unenrollFromProgram.setEnabled(true);
                        if (getActivity() != null)
                            Toast.makeText(getContext(), getActivity().getString(R.string.data_loaded), Toast.LENGTH_SHORT).show();
                        handler.removeCallbacks(runnable);
                    }
                }
            }, delay);
            getMyPrograms(true);
            //  loadData(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.shimmerLayoutOrganisation.startShimmer();
        binding.shimmerLayoutProgram.startShimmer();
        binding.shimmerLayoutCourse.startShimmer();
        binding.shimmerLayoutProgramName.startShimmer();
        binding.shimmerLayoutCourseButton.startShimmer();
        super.onResume();
    }

    @Override
    public void onPause() {
        binding.shimmerLayoutOrganisation.stopShimmer();
        binding.shimmerLayoutProgram.stopShimmer();
        binding.shimmerLayoutCourse.stopShimmer();
        binding.shimmerLayoutProgramName.stopShimmer();
        binding.shimmerLayoutCourseButton.stopShimmer();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkTokenExpire() throws Exception {
        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
        long millis = System.currentTimeMillis();
        long tokenTime = millis - responseJwt.creation_time;
        if (tokenTime > responseJwt.expires_in) {
            createToken();
        } else {
            getPrograms();
        }
    }


    private void createToken() throws Exception {
        if (this.getActivity() != null) {
            DiscoveryTask discoveryTask = new DiscoveryTask(this.getActivity()) {
                @Override
                public void onSuccess(@NonNull AuthResponseJwt result) {
                    getPrograms();
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    } else {
                        ex.printStackTrace();
                    }
                }
            };
            discoveryTask.execute();
        }
    }

    private void getPrograms() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        Call<ProgramModel> programModelCall = courseApi.getProgramsWithTopicName(token, selectedLanguage, topic_name);

        /*programModelCall.enqueue(new DiscoveryCallback<ProgramModel>() {
            @Override
            protected void onResponse(@NonNull ProgramModel responseBody) {
                if (responseBody != null) {
                    if (responseBody.getProgramResultLists() != null) {
                        if (responseBody.getProgramResultLists().size() > 0) {
                            binding.shimmerLayoutOrganisation.stopShimmer();
                            if (!program_uuid.isEmpty()) {
                                programResultLists.clear();
                                for (ProgramResultList programResultList : responseBody.getProgramResultLists()) {
                                    if (programResultList.getUuid() != null) {
                                        if (programResultList.getUuid().equals(program_uuid)) {
                                            if (programResultList.getConverted_title() != null && !programResultList.getConverted_title().isEmpty()) {
                                                if (!programsNameLists.contains(programResultList.getConverted_title()))
                                                    programsNameLists.add(programResultList.getConverted_title());
                                            } else {
                                                if (!programsNameLists.contains(programResultList.getTitle()))
                                                    programsNameLists.add(programResultList.getTitle());
                                            }

                                            programResultList.setProgramEnroll(true);
                                            programResultLists.add(programResultList);
                                            break;
                                        }
                                    }
                                }
                                Collections.sort(programResultLists, new Comparator<ProgramResultList>() {
                                    @Override
                                    public int compare(ProgramResultList lhs, ProgramResultList rhs) {
                                        return lhs.getTitle().compareTo(rhs.getTitle());
                                    }
                                });
                                //programModelAdapter.setPrograms(programResultLists,
                                 //       programResultLists.get(0).getConverted_title());
                                //Set Course
                                binding.shimmerLayoutProgramName.setVisibility(View.GONE);

                                if (programResultLists.get(0).getConverted_title() != null && !programResultLists.get(0).getConverted_title().isEmpty()) {
                                    binding.programNameInCard.setText(programResultLists.get(0).getConverted_title());
                                } else {
                                    binding.programNameInCard.setText(programResultLists.get(0).getTitle());
                                }
                                binding.linerProgramName.setVisibility(View.VISIBLE);
                                if (programResultLists.get(0).getAuthoring_organizations() != null) {
                                    authorising_organisation = "";
                                    if (programResultLists.get(0).getAuthoring_organizations().size() > 1) {
                                        for (AuthoringOrganisations authoringOrganisations : programResultLists.get(0).getAuthoring_organizations()) {
                                            if (authorising_organisation.isEmpty()) {
                                                authorising_organisation = authoringOrganisations.getName();
                                            } else {
                                                authorising_organisation = authoringOrganisations + "," + authoringOrganisations.getName();
                                            }
                                        }
                                    } else {
                                        authorising_organisation = programResultLists.get(0).getAuthoring_organizations().get(0).getName();
                                    }
                                }
                                binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
                                binding.organisations.setText(authorising_organisation);
                                boolean enroll = false;
                                if (enrolledCoursesResponses != null) {
                                    for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                                        if (enrolledCoursesResponse.getCourse() != null) {
                                            for (ProgramCoursesList programCoursesList : programResultLists.get(0).getCourses()) {
                                                if (programCoursesList.getCourseRuns() != null) {
                                                    for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {
                                                        if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                                                            courseRuns.setCourse_status(enrolledCoursesResponse.getCourse_status());
                                                            enroll = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    List<CourseRuns> courseRuns = new ArrayList<>();
                                    for (ProgramCoursesList programCoursesList : programResultLists.get(0).getCourses()) {
                                        courseRuns.addAll(programCoursesList.getCourseRuns());
                                    }
                                    binding.shimmerLayoutCourse.setVisibility(View.GONE);
                                    discoveryCourseAdapter.setProgramCoursesLists(courseRuns, true, resumeCourse);
                                    if (getContext() != null)
                                        binding.courseCount.setText(String.valueOf(courseRuns.size()) + " " +
                                                getContext().getString(R.string.courses_available));
                                    binding.courseCount.setVisibility(View.VISIBLE);
                                }
                            } else {
                                programResultLists.clear();
                                programResultLists = responseBody.getProgramResultLists();

                                for (ProgramResultList programResultList : programResultLists) {
                                    if (programResultList.getConverted_title() != null && !programResultList.getConverted_title().isEmpty()) {
                                        if (!programsNameLists.contains(programResultList.getConverted_title()))
                                            programsNameLists.add(programResultList.getConverted_title());
                                    } else {
                                        if (!programsNameLists.contains(programResultList.getTitle()))
                                            programsNameLists.add(programResultList.getTitle());
                                    }
                                    boolean programEnroll = false;
                                    if (myProgramListModels != null && myProgramListModels.size() > 0) {
                                        for (MyProgramListModel myProgramListModel : myProgramListModels) {
                                            if (myProgramListModel.getProgramUUid().equals(programResultList.getUuid())) {
                                                programEnroll = true;
                                            }
                                        }
                                        programResultList.setProgramEnroll(programEnroll);
                                    }
                                }
                                Log.d("number_of_programs", String.valueOf(responseBody.getProgramResultLists().size()));
                                Collections.sort(responseBody.getProgramResultLists(), new Comparator<ProgramResultList>() {
                                    @Override
                                    public int compare(ProgramResultList lhs, ProgramResultList rhs) {
                                        return lhs.getTitle().compareTo(rhs.getTitle());
                                    }
                                });
                                Collections.sort(programsNameLists, new Comparator<String>() {
                                    @Override
                                    public int compare(String lhs, String rhs) {
                                        return lhs.compareTo(rhs);
                                    }
                                });
                                initSpinner();
                               /* programModelAdapter.setPrograms(responseBody.getProgramResultLists(),
                                        responseBody.getProgramResultLists().get(0).getTitle());*/
             /*               }
                        }
                    }
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });*/
    }

    private void enrollcourse(EnrollAndUnenrollData.DataCreation dataCreation) throws JSONException {
        EnrollAndUnenrollData enrollAndUnenrollData = new EnrollAndUnenrollData();
        enrollAndUnenrollData.setData(dataCreation);
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        EnrollAndUnenrollData enrollAndUnenrollData1 = new EnrollAndUnenrollData();
        enrollAndUnenrollData1.setData(dataCreation);

        EnrollInCourseTask enrollInCourseTask = new EnrollInCourseTask(enrollAndUnenrollData1, getContext()) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onSuccess(EnrollResponse enrollResponse) throws Exception {
                super.onSuccess(enrollResponse);
                binding.unenrollFromProgram.setEnabled(true);
                binding.unenrollFromProgram.setEnabled(true);
                if (enrollResponse.isStatus()) {
                    getMyCourseList();
                    getMyPrograms(false);
                    if (dataCreation.getAction().equals("enroll")) {
                        programModelAdapter.setProgramEnroll(true, program_selected_uuid);
                        discoveryCourseAdapter.setEnroll(true);
                        binding.enrollInProgram.setVisibility(View.GONE);
                        binding.unenrollFromProgram.setVisibility(View.VISIBLE);
                        binding.ivCheck.setVisibility(View.VISIBLE);
                        if (topic_converted_name != null && !topic_converted_name.isEmpty()) {
//                            binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
//                                    topic_converted_name + " " + binding.programNameInCard.getText().toString() +
//                                    " " + getString(R.string.program));
                        } else {
//                            binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
//                                    topic_name + " " + binding.programNameInCard.getText().toString() +
//                                    " " + getString(R.string.program));
                        }
//                        binding.courseDatailEnroll.setVisibility(View.VISIBLE);
//                        binding.courseDatailUnenroll.setVisibility(View.GONE);
                        enrolledStatus(getString(R.string.program_is_successfully_added_to_dashboard));
                    } else {
                        programModelAdapter.setProgramEnroll(false, program_selected_uuid);
                        discoveryCourseAdapter.setEnroll(false);
                        binding.enrollInProgram.setVisibility(View.VISIBLE);
                        binding.unenrollFromProgram.setVisibility(View.GONE);
                        binding.ivCheck.setVisibility(View.GONE);
                        binding.unenrollFromProgram.setVisibility(View.GONE);
//                        binding.courseDatailEnroll.setVisibility(View.GONE);
//                        binding.courseDatailUnenroll.setVisibility(View.VISIBLE);
                        enrolledStatus(getString(R.string.program_is_successfully_removed_to_dashboard));
                    }
                }
            }
        };
        enrollInCourseTask.execute();
    }

    private void enrolledStatus(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Uncomment the below code to Set the message and title from the strings.xml file
        builder.setMessage(msg).setTitle(R.string.alert);

        //Setting message manually and performing action on button click
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle(R.string.alert);
        alert.show();
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof ProgramResultList) {
           /* ProgramResultList programResultList = (ProgramResultList) item;
            if (programResultList.isProgramEnroll()) {
                binding.unenrollFromProgram.setVisibility(View.VISIBLE);
                binding.enrollInProgram.setVisibility(View.GONE);
                binding.ivCheck.setVisibility(View.VISIBLE);
                binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                        topic_name + " " + programResultList.getTitle() + " " + getString(R.string.program));
                binding.courseDatailEnroll.setVisibility(View.VISIBLE);
                binding.courseDatailUnenroll.setVisibility(View.GONE);
            } else {
                binding.enrollInProgram.setVisibility(View.VISIBLE);
                binding.unenrollFromProgram.setVisibility(View.GONE);
                binding.ivCheck.setVisibility(View.GONE);
                binding.courseDatailEnroll.setVisibility(View.GONE);
                binding.courseDatailUnenroll.setVisibility(View.VISIBLE);
            }
            binding.lnEnrollInfo.setVisibility(View.VISIBLE);
            binding.errorMsgTv.setVisibility(View.GONE);
            program_selected_uuid = programResultList.getUuid();
            binding.shimmerLayoutProgramName.setVisibility(View.GONE);
            binding.linerProgramName.setVisibility(View.VISIBLE);

            if (programResultList.getAuthoring_organizations() != null && programResultList.getAuthoring_organizations().size() > 0) {
                authorising_organisation = "";
                if (programResultList.getAuthoring_organizations().size() > 1) {
                    for (AuthoringOrganisations authoringOrganisations : programResultList.getAuthoring_organizations()) {
                        if (authorising_organisation.isEmpty()) {
                            authorising_organisation = authoringOrganisations.getName();
                        } else {
                            authorising_organisation = authoringOrganisations + "," + authoringOrganisations.getName();
                        }
                    }
                } else {
                    authorising_organisation = programResultList.getAuthoring_organizations().get(0).getName();
                }
            }
            binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
            binding.organisations.setText(authorising_organisation);
            boolean enroll = false;
            if (enrolledCoursesResponses != null) {
                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                    if (enrolledCoursesResponse.getCourse() != null) {
                        for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
                            if (programCoursesList.getCourseRuns() != null) {
                                for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {
                                    if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                                        courseRuns.setCourse_status(enrolledCoursesResponse.getCourse_status());
                                        enroll = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
            List<CourseRuns> courseRuns = new ArrayList<>();
            for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
                courseRuns.addAll(programCoursesList.getCourseRuns());
            }
            binding.shimmerLayoutCourse.setVisibility(View.GONE);
            discoveryCourseAdapter.setProgramCoursesLists(courseRuns, *//*enroll*//* programResultList.isProgramEnroll(), resumeCourse);
            if (courseRuns == null) {
                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                binding.lnEnrollInfo.setVisibility(View.GONE);
                binding.errorMsgTv.setVisibility(View.VISIBLE);
                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
            } else if (courseRuns.size() == 0) {
                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                binding.lnEnrollInfo.setVisibility(View.GONE);
                binding.errorMsgTv.setVisibility(View.VISIBLE);
                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
            }*/
        } else {
            CourseRuns courseRuns = (CourseRuns) item;
            if (enrolledCoursesResponses != null) {
                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                    if (enrolledCoursesResponse.getCourse() != null) {
                        if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                            environment.getRouter().showCourseDashboardTabs(getActivity(), enrolledCoursesResponse,
                                    false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {
        return new CoursesAsyncLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader, AsyncTaskResult<List<EnrolledCoursesResponse>> result) {
        final Exception exception = result.getEx();
        if (exception != null) {
            if (exception instanceof AuthException) {
                loginPrefs.clear();
                getActivity().finish();
            } else if (exception instanceof HttpStatusException) {
                final HttpStatusException httpStatusException = (HttpStatusException) exception;
                switch (httpStatusException.getStatusCode()) {
                    case HttpStatus.UNAUTHORIZED: {
                        environment.getRouter().forceLogout(getContext(),
                                environment.getAnalyticsRegistry(),
                                environment.getNotificationDelegate());
                        break;
                    }
                }
            } else {
                logger.error(exception);
            }

        } else if (result.getResult() != null) {
            enrolledCoursesResponses = new ArrayList<EnrolledCoursesResponse>(result.getResult());
            programModelAdapter.notifyDataSetChanged();
        } else if (result.getResult() == null) {

        }
    }


    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader) {
    }

    protected void loadData(boolean showProgress) {
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, null, this);
    }

    private void getMyCourseList() throws Exception {
        MyCourseTask myCourseTask = new MyCourseTask(getContext(),program_uuid, loginPrefs.getUsername(), loginPrefs.getAuthorizationHeader(),"en") {
            @Override
            public void onSuccess(@NonNull List<EnrolledCoursesResponse> result) {
                if (result != null) {
                    enrolledCoursesResponses = new ArrayList<EnrolledCoursesResponse>(result);
                    Log.e("ResponseCompleteData", enrolledCoursesResponses + "");
                    /*if (!program_uuid.isEmpty()) {
                        //boolean enroll = false;
                        if (programResultLists != null && programResultLists.size() > 0) {
                            for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                                if (enrolledCoursesResponse.getCourse() != null) {
                                    for (ProgramCoursesList programCoursesList : programResultLists.get(0).getCourses()) {
                                        if (programCoursesList.getCourseRuns() != null) {
                                            for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {
                                                if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                                                    courseRuns.setCourse_status(enrolledCoursesResponse.getCourse_status());
                                                    //enroll = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            List<CourseRuns> courseRuns = new ArrayList<>();
                            for (ProgramCoursesList programCoursesList : programResultLists.get(0).getCourses()) {
                                courseRuns.addAll(programCoursesList.getCourseRuns());
                            }
                            binding.shimmerLayoutCourse.setVisibility(View.GONE);
                            discoveryCourseAdapter.setProgramCoursesLists(courseRuns, true, resumeCourse);
                            binding.courseCount.setText(String.valueOf(courseRuns.size()) + " " +
                                    context.getString(R.string.courses_available));
                            binding.courseCount.setVisibility(View.VISIBLE);
                        }
                    }*/
                }
                binding.iconProgress.setVisibility(View.GONE);
//                initSpinner();
                programModelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Exception ex) {
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                } else {

                }
            }
        };
        myCourseTask.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getMyPrograms(boolean check) throws Exception {
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        ProgramTask discoveryTask = new ProgramTask(getContext(), loginPrefs.getUsername(), selectedLanguage) {
            @Override
            public void onSuccess(@NonNull List<Programs> result) throws Exception {
                if (check) {
                    checkTokenExpire();
                }
                Log.e("getMyPrograms>>>>",result+"");
                if (result != null) {
                    String userType = loginPrefs.getUserType();
                    List<MyProgramListModel> newProgramsListforTeacher = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforStudent = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforBoth = new ArrayList<>();
                    /*for (Programs programs : result) {
                        if (programs.getTags() != null) {
                            for (String tag : programs.getTags()) {
                                if (tag.toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(tag);
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforTeacher.add(myProgramListModel);
                                }
                                if (tag.toLowerCase().contains("student")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(tag);
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforStudent.add(myProgramListModel);
                                }
                                if (!tag.toLowerCase().contains("student") && !tag.toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(tag);
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforBoth.add(myProgramListModel);
                                }
                            }
                        }
                    }*/
                    for (Programs programs : result) {
                        if (programs.getTags() != null) {
                            for (MyProgramTags myProgramTags : programs.getTags()) {
                                if (myProgramTags.getTag_title() != null && myProgramTags.getTag_title().toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforTeacher.add(myProgramListModel);
                                }
                                if (myProgramTags.getTag_title() != null && myProgramTags.getTag_title().toLowerCase().contains("student")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforStudent.add(myProgramListModel);
                                }
                                if (myProgramTags.getTag_title() != null && !myProgramTags.getTag_title().toLowerCase().contains("student") && !myProgramTags.getTag_title().toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforBoth.add(myProgramListModel);
                                }
                            }
                        }
                    }
                    if (userType != null) {
                        if (userType.contains("teacher")) {
                            newProgramsListforTeacher.addAll(newProgramsListforBoth);
                            myProgramListModels.addAll(newProgramsListforTeacher);
                        } else {
                            newProgramsListforStudent.addAll(newProgramsListforBoth);
                            myProgramListModels.addAll(newProgramsListforStudent);
                        }
                    }
                    resumeCourse = null;
                    if (myProgramListModels != null && myProgramListModels.size() > 0) {
                        for (MyProgramListModel programListModel : myProgramListModels) {
                            if (programListModel.getResume_program() != null) {
                                if (resumeCourse == null) {
                                    resumeCourse = new ResumeCourse();
                                    resumeCourse.setBlock_id(programListModel.getResume_program().getBlock_id());
                                    resumeCourse.setCourse_id(programListModel.getResume_program().getCourse_id());
                                    resumeCourse.setCourse_name(programListModel.getResume_program().getCourse_name());
                                    resumeCourse.setProgramName(programListModel.getProgramName());
                                    resumeCourse.setTagName(programListModel.getTagName());
                                }
                            }
                        }
                    }
                    discoveryCourseAdapter.setResumeCourse(resumeCourse);
                }
            }

            @Override
            public void onException(Exception ex) {
                try {
                    checkTokenExpire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                } else {

                }
            }
        };
        discoveryTask.execute();
    }

}

