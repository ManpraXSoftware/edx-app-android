package org.edx.mobile.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.Chatbot.IntentClassifier.IntentProgramClassifier;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextHelper;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.Chatbot.TextToSpeechHelper.TextToSpeechHelper;
import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.app.App;
import org.edx.mobile.authentication.ApiLmsService;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.authentication.ApiNewLmsClient;
import org.edx.mobile.course.EnrollInCourseTask;
import org.edx.mobile.databinding.FragmentNewProgramScreenBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.CourseRuns;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.discovery.model.EnrollResponse;
import org.edx.mobile.discovery.model.ProgramCoursesList;
import org.edx.mobile.discovery.model.ProgramResponseModel;
import org.edx.mobile.discovery.model.ProgramResultList;
import org.edx.mobile.discovery.model.ResponseCourseModel;
import org.edx.mobile.discovery.model.ResponseEnrollmentModel;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.MyCourseTask;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.programs.ResumeCourse;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.DiscoveryCourseAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.ProgramModelAdapter;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewProgramFragment extends BaseFragment  implements OnRecyclerItemClickListener, SpeechToTextListener, OnNavigateListener, TalkBackListener {

    public static final String TAG = NewProgramFragment.class.getCanonicalName();
    private static final int MY_COURSE_LOADER_ID = 0x905000;


    private SpeechToTextHelper speechToTextHelper;

    IntentProgramClassifier classifier;

    //private FloatingActionButton floatingActionButton;

    private TextToSpeechHelper textToSpeechHelper;
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    CourseApi courseApi;

    @Inject
    LoginAPI loginAPI;
    private static String topic_name = "";
    private static String topic_converted_name = "";
    private FragmentNewProgramScreenBinding binding;
    private ProgramModelAdapter programModelAdapter;
    private DiscoveryCourseAdapter discoveryCourseAdapter;
    private final Logger logger = new Logger(getClass().getSimpleName());
    ArrayList<EnrolledCoursesResponse> enrolledCoursesResponses = new ArrayList<>();

    //private List<ResponseCourseModel.CourseItem> myProgramListModels = new ArrayList<>();
    ResponseCourseModel responseCourseModel=new ResponseCourseModel();

    @javax.inject.Inject
    private IEdxEnvironment environment;
    private String authorising_organisation = "";
    private String program_selected_uuid = "";
    private String program_selected_name = "";
    private static String program_uuid = "";

    private static boolean tag_screen_flag  = false;

    private static boolean organisation_screen_flag  = false;
    private static boolean is_my_program_flag=false;

    private static boolean chatBotFlagProgram=false;

    private  int selected_position=0;
    List<ProgramResponseModel.Program> programResultLists = new ArrayList<>();
    private App mApp;

    private List<MyProgramListModel> myProgramListModels = new ArrayList<>();
    private ResumeCourse resumeCourse;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;
    private ArrayAdapter<String> programAdapter;
    private List<String> programsNameLists = new ArrayList<>();

    ClipboardService clipboardService;

    private SoundPool soundPool;
    private int soundId;

    MenuItem menuItem;

    public static NewProgramFragment newInstance(@Nullable Bundle bundle) {
        final NewProgramFragment fragment = new NewProgramFragment();
        topic_name = bundle.getString(ProgramActivity.PROGRAM);
        topic_converted_name = bundle.getString(ProgramActivity.PROGRAM_CONVERTED);
        program_uuid = bundle.getString(ProgramActivity.PROGRAM_UUID);
        tag_screen_flag=bundle.getBoolean(ProgramActivity.TAGSCREENFLAG);
        organisation_screen_flag=bundle.getBoolean(ProgramActivity.ORGANISATION_SCREEN_FLAG);
        is_my_program_flag=bundle.getBoolean(ProgramActivity.MYPROGRAMFLAG);
        chatBotFlagProgram=bundle.getBoolean(ProgramActivity.CHATBOTFLAG,false);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topic_name = getArguments().getString(ProgramActivity.PROGRAM);
        topic_converted_name = getArguments().getString(ProgramActivity.PROGRAM_CONVERTED);
        program_uuid = getArguments().getString(ProgramActivity.PROGRAM_UUID);
        is_my_program_flag=getArguments().getBoolean(ProgramActivity.MYPROGRAMFLAG);
        tag_screen_flag= getArguments().getBoolean(ProgramActivity.TAGSCREENFLAG);
        organisation_screen_flag=getArguments().getBoolean(ProgramActivity.ORGANISATION_SCREEN_FLAG);
        chatBotFlagProgram=getArguments().getBoolean(ProgramActivity.CHATBOTFLAG,false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_program_screen, container,
                false);
        speechToTextHelper = new SpeechToTextHelper(getActivity().getApplicationContext(), this,getActivity());
        textToSpeechHelper = new TextToSpeechHelper(getActivity().getApplicationContext(),getActivity(),this::onDoneTalkBackListener);
        clipboardService = ClipboardServiceHolder.getClipboardService(getActivity().getApplicationContext());
        copyTextDataByLongPress();
        setHasOptionsMenu(true);
        if(menuItem!=null&&program_uuid.isEmpty()){
            menuItem.setVisible(true);
        }

        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getContext(), R.raw.beep_sound_2, 1);
        return binding.getRoot();
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.menu_item_voice);
        menuItem=item;

        if(programResultLists==null||tag_screen_flag==false &&is_my_program_flag==true) {
            item.setVisible(false);
        }
       /* else{
            if(tag_screen_flag &&!is_my_program_flag) {
                setFocusOnMic();
            }
        }*/
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_voice:

                onMicButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item); // Let the activity handle other items
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // view.announceForAccessibility("Programs Screen");
        mApp = new App();

        if (!program_uuid.isEmpty()) {
            binding.selectAProgram.setVisibility(View.GONE);
            binding.shimmerLayoutProgram.setVisibility(View.GONE);
            binding.rvProgram.setVisibility(View.GONE);
            binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
            binding.ivCheck.setVisibility(View.VISIBLE);
            binding.courseDatailUnenroll.setVisibility(View.GONE);
            binding.courseDatailEnroll.setVisibility(View.GONE);
            binding.lnEnrollInfo.setVisibility(View.GONE);
        }
        String sourceString = "";
        if (topic_converted_name != null && !topic_converted_name.isEmpty()) {
            sourceString = "<b>" + topic_converted_name + "</b> ";
        } else {
            sourceString = "<b>" + topic_name + "</b> ";
        }

        binding.tagName.setText(Html.fromHtml(sourceString));
        binding.tagName.setFocusable(true);


        programModelAdapter = new ProgramModelAdapter(getActivity(), NewProgramFragment.this::onItemClick);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        binding.rvProgram.setLayoutManager(mLayoutManager);
        binding.rvProgram.setAdapter(programModelAdapter);

        discoveryCourseAdapter = new DiscoveryCourseAdapter(getActivity(), NewProgramFragment.this::onItemClick,this::navigateToAnotherScreen);
        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(getContext());
        binding.rvCourses.setLayoutManager(mLayoutManager1);
        binding.rvCourses.setAdapter(discoveryCourseAdapter);
        binding.enrollInProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.enrollInProgram.setEnabled(false);
                binding.iconProgress.setVisibility(View.VISIBLE);
                String strings = discoveryCourseAdapter.getProgramCoursesIds();
                EnrollAndUnenrollData.DataCreation dataCreation = new EnrollAndUnenrollData.DataCreation();
                dataCreation.setCourses(strings);
                dataCreation.setAction("enroll");
                dataCreation.setProgram_uuid(program_selected_uuid);
                dataCreation.setProgram_name(program_selected_name);
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
                binding.iconProgress.setVisibility(View.VISIBLE);
                String strings = discoveryCourseAdapter.getProgramCoursesIds();
                EnrollAndUnenrollData.DataCreation dataCreation = new EnrollAndUnenrollData.DataCreation();
                dataCreation.setCourses(strings);

                dataCreation.setAction("unenroll");
                dataCreation.setProgram_name(program_selected_name);
                dataCreation.setUsername(loginPrefs.getUsername());
                if(!tag_screen_flag){
                    dataCreation.setProgram_uuid(program_uuid);
                    enrolledStatus2(getString(R.string.unroll_app_dialog_message),dataCreation);
                }
                else {
                    dataCreation.setProgram_uuid(program_selected_uuid);
                    try {
                        enrollcourse(dataCreation);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getMyPrograms(true);
            }
        }
        catch (Exception e){

        }

    }

    private void initSpinner() {
        discoveryCourseAdapter.clearProgramCoursesLists();
        if (program_uuid.isEmpty()) {
            binding.optionSpinnerPrograms.setVisibility(View.VISIBLE);
            binding.shimmerLayoutProgram.setVisibility(View.GONE);
        }
        else{
            if(!tag_screen_flag&is_my_program_flag) {
                getMyCourseList();
            }
            else {

                isEnrollCheck=false;
                binding.lnEnrollInfo.setVisibility(View.VISIBLE);
                binding.shimmerLayoutCourseButton.setVisibility(View.VISIBLE);
                binding.shimmerEnrollInProgram.setVisibility(View.VISIBLE);
                binding.enrollInProgram.setVisibility(View.GONE);
                binding.unenrollFromProgram.setVisibility(View.GONE);
                binding.courseDatailUnenroll.setVisibility(View.GONE);
                binding.courseDatailEnroll.setVisibility(View.GONE);
                flag=false;
                runParallelTasks();
            }
        }

        if (programsNameLists != null && programsNameLists.size() > 10) {
            try {
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);

                // Get private mPopup member variable and try cast to ListPopupWindow
                Object popupWindow = popup.get(binding.optionSpinnerPrograms);
                if (popupWindow instanceof android.widget.ListPopupWindow) {
                    ((android.widget.ListPopupWindow) popupWindow).setHeight(500);
                }
            } catch (Exception e) {
                // Handle exceptions more gracefully, e.g., log or print an error message
                e.printStackTrace();
            }
        }

        if (getActivity() != null) {
            programAdapter = new ArrayAdapter<>(getActivity(), R.layout.edx_spinner_dropdown_item, programsNameLists);
            programAdapter.setDropDownViewResource(R.layout.edx_spinner_dropdown_item);
            binding.optionSpinnerPrograms.setAdapter(programAdapter);

            binding.optionSpinnerPrograms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                    filterTitle = (String) parent.getItemAtPosition(position);

                    selected_position = position;




                    try {
                        if (!programResultLists.isEmpty() && position < programResultLists.size()) {
                            for (ProgramResponseModel.Program programResultList : programResultLists) {
                                if ((programResultList.getTitle()).equals(filterTitle)
                                        || (programResultList.getTitle()).equals(filterTitle)) {
                                    program_uuid=programResultList.getUuid();
                                    discoveryCourseAdapter.clearProgramCoursesLists();
                                    program_selected_uuid=programResultList.getUuid();
                                    program_selected_name=programResultList.getTitle();
                                    hideViewsForTagScreen();
                                    showShimmerLayoutForTagScreen();
                                    isEnrollCheck=false;
                                    binding.lnEnrollInfo.setVisibility(View.VISIBLE);
                                    binding.shimmerLayoutCourseButton.setVisibility(View.VISIBLE);
                                    binding.shimmerEnrollInProgram.setVisibility(View.VISIBLE);
                                    binding.enrollInProgram.setVisibility(View.GONE);
                                    binding.unenrollFromProgram.setVisibility(View.GONE);

                                    binding.courseDatailUnenroll.setVisibility(View.GONE);
                                    binding.courseDatailEnroll.setVisibility(View.GONE);
                                    flag=false;

                                    runParallelTasks();

                                }
                            }

                           // getCourseList();
                            //checkEnrollResponse();
                        } else {
                            // Handle the case when the position is out of bounds
                            // You may show a toast, log an error, or handle it as appropriate
                            // For now, just print an error message
                            System.err.println("Invalid position or empty programResultLists");
                        }
                    } catch (Exception e) {
                        // Handle exceptions more gracefully, e.g., log or print an error message
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Handle nothing selected event if needed
                }
            });
        }
    }


    String filterTitle;
    void onProgramSelection(){

        try {
            for (ProgramResponseModel.Program programResultList : programResultLists) {

                if ((programResultList.getTitle()).equals(filterTitle)
                        || (programResultList.getTitle()).equals(filterTitle) || !tag_screen_flag & !is_my_program_flag & programResultList.getUuid().equals(program_uuid)) {

                    sendAnalyticsFilter(programResultList);

                    if (true) {
                        binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                                topic_name + " " + programResultList.getTitle() + " "
                                + getString(R.string.program));
                    }

                    binding.courseDatailEnroll.setVisibility(View.VISIBLE);
                    binding.courseDatailUnenroll.setVisibility(View.GONE);
                    if (programResultList.getTitle() != null
                            && !programResultList.getTitle().isEmpty()) {
                        binding.programNameInCard.setText(programResultList.getTitle());
                    } else {
                        binding.programNameInCard.setText(programResultList.getTitle());
                    }
                } else {
                /*binding.enrollInProgram.setVisibility(View.VISIBLE);
                binding.unenrollFromProgram.setVisibility(View.GONE);
                binding.ivCheck.setVisibility(View.GONE);
                binding.courseDatailEnroll.setVisibility(View.GONE);
                binding.courseDatailUnenroll.setVisibility(View.VISIBLE);*/
                }
                binding.lnEnrollInfo.setVisibility(View.VISIBLE);
                binding.errorMsgTv.setVisibility(View.GONE);
                //program_selected_uuid = programResultList.getUuid();
                //program_selected_name = programResultList.getTitle();
                binding.shimmerLayoutProgramName.setVisibility(View.GONE);
                binding.linerProgramName.setVisibility(View.VISIBLE);


                if (programResultList.getAuthoringOrganizations() != null
                        && programResultList.getAuthoringOrganizations().size() > 0) {
                    authorising_organisation = "";
                    if (programResultList.getAuthoringOrganizations().size() > 1) {
                        for (ProgramResponseModel.AuthoringOrganization authoringOrganisations : programResultList
                                .getAuthoringOrganizations()) {
                            if (authorising_organisation.isEmpty()) {
                                authorising_organisation = authoringOrganisations.getName();
                            } else {
                                authorising_organisation = authoringOrganisations + ","
                                        + authoringOrganisations.getName();
                            }
                        }
                    } else {
                        authorising_organisation = programResultList.getAuthoringOrganizations().get(0)
                                .getName();
                    }
                }
                binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
                binding.organisations.setText(authorising_organisation);

                binding.enrollInProgram.setVisibility(View.GONE);
                if (isEnroll) {
                    enrollCourseSetAtUI();
                } else {
                    responseDataUiSet();
                }
            }
        }
        catch (Exception e){

        }
    }
    Boolean flag=false;

    void responseDataUiSet(){
        List<CourseRuns> courseRuns = new ArrayList<>();
        courseRuns.clear();
        if (responseCourseModel != null) {
            try {
                for (ResponseCourseModel.CourseItem courseItem : responseCourseModel.getData()) {

                    CourseRuns courseRunsObject = new CourseRuns();



                    // Set values using setter methods
                    courseRunsObject.setKey(courseItem.getKey());
                    courseRunsObject.setUuid(courseItem.getKey());
                    courseRunsObject.setTitle(courseItem.getTitle());
                    courseRunsObject.setCourse_status(null);
                    courseRunsObject.setConverted_course_title(courseItem.getConvertedTitle());
                    courseRuns.add(courseRunsObject);
                            /*for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
                                if (programCoursesList.getCourseRuns() != null) {
                                    for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {

                                        if (courseRuns.getKey()
                                                .equals(enrolledCoursesResponse.getCourse().getId())) {
                                            courseRuns.setCourse_status(
                                                    enrolledCoursesResponse.getCourse_status());
                                            enroll = true;
                                        }
                                    }
                                }
                            }
                        }*/

                }
            }
            catch (Exception e){

            }
            discoveryCourseAdapter.setProgramCoursesLists(courseRuns,flag
                    /*programResultList.isProgramEnroll()*/, resumeCourse);
            if(chatBotCourseFlag){

                 chatBotTalkCourse(courseRuns);
            }
            else{

            }
            if(!flag) {
                binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
            }
                /*
                for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
                    courseRuns.addAll(programCoursesList.getCourseRuns());
                }*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(requireContext()!=null)
                if(requireContext().isUiContext()){
                            binding.courseCount.setText(String.valueOf(courseRuns.size()) + " " +
                                   getString(R.string.courses_available));
                            binding.courseCount.setVisibility(View.VISIBLE);
                }
            }

            binding.shimmerLayoutCourse.setVisibility(View.GONE);


            if (courseRuns == null) {
                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                binding.lnEnrollInfo.setVisibility(View.GONE);
                binding.errorMsgTv.setVisibility(View.VISIBLE);
                binding.errorMsgTv.sendAccessibilityEvent(
                        AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
            } else if (courseRuns.size() == 0) {
                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                binding.lnEnrollInfo.setVisibility(View.GONE);
                binding.errorMsgTv.setVisibility(View.VISIBLE);
                binding.errorMsgTv.sendAccessibilityEvent(
                        AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
            }else{
                if(!flag&&isEnrollCheck){

                    binding.ivCheck.setVisibility(View.GONE);
                    binding.courseDatailEnroll.setVisibility(View.GONE);
                    binding.courseDatailUnenroll.setVisibility(View.VISIBLE);
                }
//                if(!tag_screen_flag) {
//                    binding.lnEnrollInfo.setVisibility(View.VISIBLE);
//                    binding.enrollInProgram.setVisibility(View.GONE);
//                    binding.unenrollFromProgram.setVisibility(View.VISIBLE);
//                    binding.ivCheck.setVisibility(View.GONE);
//                    binding.courseDatailEnroll.setVisibility(View.GONE);
//                    binding.courseDatailUnenroll.setVisibility(View.GONE);
//                }
            }
        }
    }

    void enrollCourseSetAtUI(){

        List<CourseRuns> courseRuns = new ArrayList<>();
        courseRuns.clear();
        if (enrolledCoursesResponses != null) {
            for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                if (enrolledCoursesResponse.getCourse() != null) {
                    CourseRuns courseRunsObject = new CourseRuns();

                    // Set values using setter methods
                    courseRunsObject.setKey(enrolledCoursesResponse.getCourse().getId());
                    courseRunsObject.setUuid(enrolledCoursesResponse.getCourse().getId());
                    courseRunsObject.setTitle(enrolledCoursesResponse.getCourse().getName());
                    courseRunsObject.setCourse_status(enrolledCoursesResponse.getCourse_status());
                    courseRunsObject.setConverted_course_title(enrolledCoursesResponse.getCourse().getName());
                    courseRuns.add(courseRunsObject);

                }
            }
            discoveryCourseAdapter.setProgramCoursesLists(courseRuns,true
                    /*programResultList.isProgramEnroll()*/, resumeCourse);
            binding.shimmerLayoutCourseButton.setVisibility(View.GONE);

            binding.courseCount.setText(String.valueOf(courseRuns.size()) + " " +
                    requireContext().getString(R.string.courses_available));
            binding.courseCount.setVisibility(View.VISIBLE);

            binding.shimmerLayoutCourse.setVisibility(View.GONE);
            if(is_my_program_flag && !tag_screen_flag&&chatBotFlagProgram) {
                chatBotTalkCourseFormDashboard(courseRuns);
                chatBotFlagProgram=false;
            }
            if (courseRuns == null) {
                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                binding.lnEnrollInfo.setVisibility(View.GONE);
                binding.errorMsgTv.setVisibility(View.VISIBLE);
                binding.errorMsgTv.sendAccessibilityEvent(
                        AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
            } else if (courseRuns.size() == 0) {
                binding.errorMsgTv.setText(getString(R.string.no_course_found));
                binding.lnEnrollInfo.setVisibility(View.GONE);
                binding.errorMsgTv.setVisibility(View.VISIBLE);
                binding.errorMsgTv.sendAccessibilityEvent(
                        AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
            }else{
                //if(!tag_screen_flag) {
                binding.lnEnrollInfo.setVisibility(View.VISIBLE);
                binding.enrollInProgram.setVisibility(View.GONE);
                binding.unenrollFromProgram.setVisibility(View.VISIBLE);
                binding.ivCheck.setVisibility(View.GONE);
                binding.courseDatailEnroll.setVisibility(View.GONE);
                binding.courseDatailUnenroll.setVisibility(View.GONE);
                //  }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        try {
            discoveryCourseAdapter.clearProgramCoursesLists();
            showShimmerLayoutForTagScreen();
            binding.lnEnrollInfo.setVisibility(View.VISIBLE);
            binding.shimmerLayoutCourseButton.setVisibility(View.VISIBLE);
            binding.shimmerEnrollInProgram.setVisibility(View.VISIBLE);
            binding.enrollInProgram.setVisibility(View.GONE);
            binding.unenrollFromProgram.setVisibility(View.GONE);
            binding.courseDatailUnenroll.setVisibility(View.GONE);
            binding.courseDatailEnroll.setVisibility(View.GONE);


            if(!program_uuid.isEmpty()) {

                runParallelTasks();
            }
            //if(!program_uuid.isEmpty())
            //  getMyCourseList();
            // loadData(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        binding.shimmerLayoutOrganisation.startShimmer();
        binding.shimmerLayoutProgram.startShimmer();
        binding.shimmerLayoutCourse.startShimmer();
        binding.shimmerLayoutProgramName.startShimmer();
        binding.shimmerLayoutCourseButton.startShimmer();*/
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
        if(organisation_screen_flag) {
            Call<ProgramResponseModel> programResponseModel = courseApi.getProgramResponseWithOrganisationName(token, selectedLanguage, topic_name);
            programResponseModel.enqueue(new DiscoveryCallback<ProgramResponseModel>() {
                @Override
                protected void onResponse(@NonNull ProgramResponseModel responseBody) {
                    if (responseBody != null && responseBody.getPrograms() != null) {
                        programResultLists.clear();
                        programResultLists = responseBody.getPrograms();
                        initializeIntentData();

                        if (program_uuid.isEmpty()) {
                        /*if (floatingActionButton != null) {
                            floatingActionButton.show();
                        }*/
                        }
                        if (!program_uuid.isEmpty()) {
                            updateSingleProgram(programResultLists);
                        } else {
                            updateProgramList(programResultLists);
                        }
                    }
                }

                @Override
                protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                    super.onFailure(responseError, error);
                }

            });

        }
        else {
            Call<ProgramResponseModel> programResponseModel = courseApi.getProgramResponseWithTopicName(token, selectedLanguage, topic_name);
            programResponseModel.enqueue(new DiscoveryCallback<ProgramResponseModel>() {
                @Override
                protected void onResponse(@NonNull ProgramResponseModel responseBody) {
                    if (responseBody != null && responseBody.getPrograms() != null) {
                        programResultLists.clear();
                        programResultLists = responseBody.getPrograms();
                        initializeIntentData();
                       // setFocusOnMic();
                        if(chatBotFlagProgram&&tag_screen_flag){
                            chatBotTalkProgram();
                        }
                        if (program_uuid.isEmpty()) {
                        /*if (floatingActionButton != null) {
                            floatingActionButton.show();
                        }*/
                        }
                        if (!program_uuid.isEmpty()) {
                            updateSingleProgram(programResultLists);
                        } else {
                            updateProgramList(programResultLists);
                        }
                    }
                }

                @Override
                protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                    super.onFailure(responseError, error);
                }

            });
        }
    }


    private void updateSingleProgram(List<ProgramResponseModel.Program> programResultLists) {

        for (ProgramResponseModel.Program programResultList : programResultLists) {
            if (programResultList.getUuid() != null && programResultList.getUuid().equals(program_uuid)) {
                program_selected_name=programResultList.getTitle();
                program_selected_uuid=programResultList.getUuid();

                updateProgramList(Collections.singletonList(programResultList));
                break;
            }
        }
    }

    private void updateProgramList(List<ProgramResponseModel.Program> programResultLists) {
        if (!programResultLists.isEmpty()) {
            binding.shimmerLayoutOrganisation.stopShimmer();
            handleProgramLists(programResultLists);
            if(program_uuid.isEmpty()) {
                initSpinner();
                if(menuItem!=null) {
                    menuItem.setVisible(true);
                }
            }
            programModelAdapter.setPrograms(programResultLists,
                    programResultLists.get(0).getTitle());
        }
    }

    private void handleProgramLists(List<ProgramResponseModel.Program> programResultLists) {
        if (!program_uuid.isEmpty()) {
            handleSingleProgram(programResultLists);
        } else {
            handleMultiplePrograms(programResultLists);
        }
    }

    private void handleSingleProgram(List<ProgramResponseModel.Program> programResultLists) {
        ProgramResponseModel.Program singleProgram = programResultLists.get(0);
        if (singleProgram.getTitle() != null && !singleProgram.getTitle().isEmpty()) {
            if (!programsNameLists.contains(singleProgram.getTitle())) {
                programsNameLists.add(singleProgram.getTitle());
            }
        } else {
            if (!programsNameLists.contains(singleProgram.getTitle())) {
                programsNameLists.add(singleProgram.getTitle());
            }
        }
        //singleProgram.setProgramEnroll(true);
        showProgramDetails(singleProgram);
    }

    private void handleMultiplePrograms(List<ProgramResponseModel.Program> programResultLists) {
        programsNameLists.clear();
        for (ProgramResponseModel.Program programResultList : programResultLists) {
            if (programResultList.getTitle() != null
                    && !programResultList.getTitle().isEmpty()) {
                if (!programsNameLists.contains(programResultList.getTitle())) {
                    programsNameLists.add(programResultList.getTitle());
                }
            } else {
                if (!programsNameLists.contains(programResultList.getTitle())) {
                    programsNameLists.add(programResultList.getTitle());
                }
            }
            boolean programEnroll = isProgramEnrolled(programResultList);
            //programResultList.setProgramEnroll(programEnroll);
        }
        sortProgramLists(programResultLists);
        showProgramDetails(programResultLists.get(0));
        /*if (program_uuid.isEmpty()) {
            try {
                if (programResultLists.get(0).isProgramEnroll()) {
                    if (isGetMyCourseListRun) {
                        isEnroll = true;
                    }
                }
            } catch (Exception e) {
                // Handle exception if needed
            }
        }*/
    }

    private boolean isProgramEnrolled(ProgramResponseModel.Program programResultList) {
        if (myProgramListModels != null && myProgramListModels.size() > 0) {
            for (MyProgramListModel myProgramListModel : myProgramListModels) {
                if (myProgramListModel.getProgramUUid().equals(programResultList.getUuid())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sortProgramLists(List<ProgramResponseModel.Program> programResultLists) {
        Collections.sort(programResultLists, new Comparator<ProgramResponseModel.Program>() {
            @Override
            public int compare(ProgramResponseModel.Program lhs, ProgramResponseModel.Program rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        Collections.sort(programsNameLists, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
    }

    private void showProgramDetails(ProgramResponseModel.Program programResultList) {
        binding.shimmerLayoutProgramName.setVisibility(View.GONE);
        binding.linerProgramName.setVisibility(View.VISIBLE);
        String programName = getProgramName(programResultList);
        binding.programNameInCard.setText(programName);

        List<ProgramResponseModel.AuthoringOrganization> authoringOrganizations = programResultList.getAuthoringOrganizations();
        handleAuthoringOrganizations(authoringOrganizations);

        //handleEnrolledCourses(programResultList);
    }

    private String getProgramName(ProgramResponseModel.Program programResultList) {
        return (programResultList.getTitle() != null && !programResultList.getTitle().isEmpty()) ?
                programResultList.getTitle() : programResultList.getTitle();
    }

    private void handleAuthoringOrganizations(List<ProgramResponseModel.AuthoringOrganization> authoringOrganizations) {
        if (authoringOrganizations != null && authoringOrganizations.size() > 0) {
            authorising_organisation = buildAuthorizingOrganizationString(authoringOrganizations);
        }
        binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
        binding.organisations.setText(authorising_organisation);
    }

    private String buildAuthorizingOrganizationString(List<ProgramResponseModel.AuthoringOrganization> authoringOrganizations) {
        StringBuilder authorizingOrganizationString = new StringBuilder();
        if (authoringOrganizations.size() > 1) {
            for (ProgramResponseModel.AuthoringOrganization authoringOrganisation : authoringOrganizations) {
                if (authorizingOrganizationString.length() == 0) {
                    authorizingOrganizationString.append(authoringOrganisation.getName());
                } else {
                    authorizingOrganizationString.append(",").append(authoringOrganisation.getName());
                }
            }
        } else {
            authorizingOrganizationString.append(authoringOrganizations.get(0).getName());
        }
        return authorizingOrganizationString.toString();
    }
    private List<CourseRuns> getCourseRuns(ProgramResultList programResultList) {
        List<CourseRuns> courseRuns = new ArrayList<>();

        for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
            if (programCoursesList.getCourseRuns() != null)


            {
                courseRuns.addAll(programCoursesList.getCourseRuns());
            }
        }

        return courseRuns;
    }


    private void handleEnrolledCourses(ProgramResultList programResultList) {
        // enroll = updateCourseRunsStatus(programResultList);
        List<CourseRuns> courseRuns = getCourseRuns(programResultList);
        binding.shimmerLayoutCourse.setVisibility(View.GONE);
        discoveryCourseAdapter.setProgramCoursesLists(courseRuns, true, resumeCourse);
        if (getContext() != null) {
            binding.courseCount.setText(String.valueOf(courseRuns.size()) + " " +
                    getContext().getString(R.string.courses_available));
        }
        binding.courseCount.setVisibility(View.VISIBLE);
        if (!tag_screen_flag) {
            binding.lnEnrollInfo.setVisibility(View.VISIBLE);
            binding.enrollInProgram.setVisibility(View.GONE);
            binding.unenrollFromProgram.setVisibility(View.VISIBLE);
            binding.ivCheck.setVisibility(View.GONE);
            binding.courseDatailEnroll.setVisibility(View.GONE);
            binding.courseDatailUnenroll.setVisibility(View.GONE);
        }
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
                binding.enrollInProgram.setEnabled(true);

                if (enrollResponse.isStatus()) {
                    if (dataCreation.getAction().equals("enroll")) {

                        isEnroll=true;
                        getMyCourseList();
                        programModelAdapter.setProgramEnroll(true, program_selected_uuid);
                        discoveryCourseAdapter.setEnroll(true);
                        binding.enrollInProgram.setVisibility(View.GONE);
                        binding.unenrollFromProgram.setVisibility(View.VISIBLE);
                        binding.ivCheck.setVisibility(View.VISIBLE);
                        if (topic_converted_name != null && !topic_converted_name.isEmpty()) {
                            binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                                    topic_converted_name + " " + binding.programNameInCard.getText().toString() +
                                    " " + getString(R.string.program));

                           //
                            // sendAnalyticsEnroll(enrollAndUnenrollData);
                        } else {

                            binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
                                    topic_name + " " + binding.programNameInCard.getText().toString() +
                                    " " + getString(R.string.program));
                           sendAnalyticsEnroll(enrollAndUnenrollData);
                        }
                        binding.courseDatailEnroll.setVisibility(View.VISIBLE);
                        binding.courseDatailUnenroll.setVisibility(View.GONE);
                        enrolledStatus(getString(R.string.program_is_successfully_added_to_dashboard));

                    } else {isEnroll=false;
                        programModelAdapter.setProgramEnroll(false, program_selected_uuid);
                        discoveryCourseAdapter.setEnroll(false);
                        binding.enrollInProgram.setVisibility(View.VISIBLE);
                        binding.unenrollFromProgram.setVisibility(View.GONE);
                        binding.ivCheck.setVisibility(View.GONE);
                        binding.unenrollFromProgram.setVisibility(View.GONE);
                        binding.courseDatailEnroll.setVisibility(View.GONE);
                        binding.courseDatailUnenroll.setVisibility(View.VISIBLE);
                        sendAnalyticsUnroll(enrollAndUnenrollData);
                        if(tag_screen_flag) {
                            enrolledStatus(getString(R.string.program_is_successfully_removed_to_dashboard));
                        }
                        else {
                            if(is_my_program_flag) {
                                Intent intent
                                        = new Intent(getActivity(), MainBottomDashboardFragment.class);
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        }

                    }
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

    private boolean updateCourseRunsStatus(ProgramResultList programResultList) {
        boolean enroll = false;
        if (enrolledCoursesResponses != null) {
            for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                if (enrolledCoursesResponse.getCourse() != null) {
                    for (ProgramCoursesList programCoursesList : programResultList.getCourses()) {
                        if (programCoursesList.getCourseRuns() != null) {
                            for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {
                                //if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                                    courseRuns.setCourse_status(enrolledCoursesResponse.getCourse_status());
                                    enroll = true;
                                //}
                            }
                        }
                    }
                }
            }
        }
        return enroll;
    }


    private void enrolledStatus(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Uncomment the below code to Set the message and title from the strings.xml
        // file
        builder.setMessage(msg).setTitle(R.string.status);

        // Setting message manually and performing action on button click
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Creating dialog box
        AlertDialog alert = builder.create();
        // Setting the title manually
        alert.setTitle(R.string.status);
        alert.show();
    }


    private void showConfirmationDialog(String message, int titleResource, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the message and title from resources
        builder.setMessage(message)
                .setTitle(titleResource)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_no),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        binding.unenrollFromProgram.setEnabled(true);
                        binding.enrollInProgram.setEnabled(true);
                        binding.iconProgress.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton(getString(R.string.label_yes),positiveClickListener) ;

        // Creating dialog box
        AlertDialog alert = builder.create();
        // Setting the title from resources
        alert.show();
    }


    // To show the dialog with "Yes" on the left and "No" on the right.
    private void enrolledStatus2(String msg, EnrollAndUnenrollData.DataCreation dataCreation) {
        showConfirmationDialog(msg, R.string.status, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                try {
                    enrollcourse(dataCreation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof ProgramResultList) {

            /*
             * ProgramResultList programResultList = (ProgramResultList) item;
             * if (programResultList.isProgramEnroll()) {
             * binding.unenrollFromProgram.setVisibility(View.VISIBLE);
             * binding.enrollInProgram.setVisibility(View.GONE);
             * binding.ivCheck.setVisibility(View.VISIBLE);
             * binding.courseDatailEnroll.setText(getString(R.string.enrolled_in) + " " +
             * topic_name + " " + programResultList.getTitle() + " " +
             * getString(R.string.program));
             * binding.courseDatailEnroll.setVisibility(View.VISIBLE);
             * binding.courseDatailUnenroll.setVisibility(View.GONE);
             * } else {
             * binding.enrollInProgram.setVisibility(View.VISIBLE);
             * binding.unenrollFromProgram.setVisibility(View.GONE);
             * binding.ivCheck.setVisibility(View.GONE);
             * binding.courseDatailEnroll.setVisibility(View.GONE);
             * binding.courseDatailUnenroll.setVisibility(View.VISIBLE);
             * }
             * binding.lnEnrollInfo.setVisibility(View.VISIBLE);
             * binding.errorMsgTv.setVisibility(View.GONE);
             * program_selected_uuid = programResultList.getUuid();
             * binding.shimmerLayoutProgramName.setVisibility(View.GONE);
             * binding.linerProgramName.setVisibility(View.VISIBLE);
             *
             * if (programResultList.getAuthoring_organizations() != null &&
             * programResultList.getAuthoring_organizations().size() > 0) {
             * authorising_organisation = "";
             * if (programResultList.getAuthoring_organizations().size() > 1) {
             * for (AuthoringOrganisations authoringOrganisations :
             * programResultList.getAuthoring_organizations()) {
             * if (authorising_organisation.isEmpty()) {
             * authorising_organisation = authoringOrganisations.getName();
             * } else {
             * authorising_organisation = authoringOrganisations + "," +
             * authoringOrganisations.getName();
             * }
             * }
             * } else {
             * authorising_organisation =
             * programResultList.getAuthoring_organizations().get(0).getName();
             * }
             * }
             * binding.shimmerLayoutOrganisation.setVisibility(View.GONE);
             * binding.organisations.setText(authorising_organisation);
             * boolean enroll = false;
             * if (enrolledCoursesResponses != null) {
             * for (EnrolledCoursesResponse enrolledCoursesResponse :
             * enrolledCoursesResponses) {
             * if (enrolledCoursesResponse.getCourse() != null) {
             * for (ProgramCoursesList programCoursesList : programResultList.getCourses())
             * {
             * if (programCoursesList.getCourseRuns() != null) {
             * for (CourseRuns courseRuns : programCoursesList.getCourseRuns()) {
             * if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId()))
             * {
             * courseRuns.setCourse_status(enrolledCoursesResponse.getCourse_status());
             * enroll = true;
             * }
             * }
             * }
             * }
             * }
             * }
             * }
             * binding.shimmerLayoutCourseButton.setVisibility(View.GONE);
             * List<CourseRuns> courseRuns = new ArrayList<>();
             * for (ProgramCoursesList programCoursesList : programResultList.getCourses())
             * {
             * courseRuns.addAll(programCoursesList.getCourseRuns());
             * }
             * binding.shimmerLayoutCourse.setVisibility(View.GONE);
             * discoveryCourseAdapter.setProgramCoursesLists(courseRuns,
             *//* enroll *//*
             * programResultList.isProgramEnroll(), resumeCourse);
             * if (courseRuns == null) {
             * binding.errorMsgTv.setText(getString(R.string.no_course_found));
             * binding.lnEnrollInfo.setVisibility(View.GONE);
             * binding.errorMsgTv.setVisibility(View.VISIBLE);
             * binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.
             * WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
             * } else if (courseRuns.size() == 0) {
             * binding.errorMsgTv.setText(getString(R.string.no_course_found));
             * binding.lnEnrollInfo.setVisibility(View.GONE);
             * binding.errorMsgTv.setVisibility(View.VISIBLE);
             * binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.
             * WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
             * }
             */
        } else {
            CourseRuns courseRuns = (CourseRuns) item;
            navigateToAnotherScreen(courseRuns);
        }
    }

    boolean chatBotCourseFlag=false;
    void changeDropdown(ProgramResponseModel.Program program){

// Suppose you want to select a program with a specific title
        String desiredProgramTitle = program.getTitle();

// Find the position of the desired program in the programsNameLists
        int desiredProgramPosition = programsNameLists.indexOf(desiredProgramTitle);
// Check if the desired program is found in the list
        if (desiredProgramPosition != -1) {
            // Set the selected item in the Spinner
            binding.optionSpinnerPrograms.setSelection(desiredProgramPosition);

            // Optionally, you can trigger the item selection logic programmatically
            // (similar to what happens when a user manually selects an item)
            binding.optionSpinnerPrograms.setSelection(desiredProgramPosition, true);
            chatBotCourseFlag=true;
            // Now, the onItemSelected callback will be triggered
            // with the selected program set to desiredProgramTitle.
        } else {
            // Handle the case when the desired program is not found in the list
            // You may show a toast, log an error, or handle it as appropriate
            System.err.println("Desired program not found in the list");
        }

    }
/*
    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {
        return new CoursesAsyncLoader(getActivity());
    }*/
/*
    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader,
            AsyncTaskResult<List<EnrolledCoursesResponse>> result) {
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
    }*/

    /*protected void loadData(boolean showProgress) {
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, null, this);
    }*/

    private boolean isEnroll = false;
    private boolean isEnrollCheck = false;
    private boolean isGetMyCourseListRun = true;

    private void getMyCourseList() {

        isEnroll=true;
        try {
            String selectedLanguage = "en";
            if (getActivity() != null) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }

            if (tag_screen_flag) {
               // hideViewsForTagScreen();
                //showShimmerLayoutForTagScreen();
            }

            MyCourseTask myCourseTask = new MyCourseTask(getContext(), program_uuid,
                    loginPrefs.getUsername(), loginPrefs.getAuthorizationHeader(),selectedLanguage) {
                @Override
                public void onSuccess(@NonNull List<EnrolledCoursesResponse> result) {
                    try {
                        if (tag_screen_flag) {
                            showViewsForTagScreen();
                            hideShimmerLayoutForTagScreen();
                        }
                        enrolledCoursesResponses = new ArrayList<>(result);
                        onProgramSelection();
                        binding.iconProgress.setVisibility(View.GONE);
                        programModelAdapter.notifyItemChanged(selected_position);
                    }
                    catch (Exception e){
                        Log.d("Error",e.toString());
                    }
                }

                @Override
                public void onException(Exception ex) {
                    handleException(ex);
                }
            };

            myCourseTask.execute();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void runParallelTasks() {
        int totalTasks = 2; // Total number of parallel tasks
        AtomicInteger completedTasks = new AtomicInteger(0);

        GetCourseListTask getCourseListTask = new GetCourseListTask(completedTasks, totalTasks);
        getCourseListTask.execute();

        CheckEnrollResponseTask checkEnrollResponseTask = new CheckEnrollResponseTask(completedTasks, totalTasks);
        checkEnrollResponseTask.execute();
    }

    @Override
    public void navigateToAnotherScreen(Object item) {
        if (item instanceof CourseRuns) {
            CourseRuns courseRuns = (CourseRuns) item;
            String program_Uid = "";
            if (tag_screen_flag) {
                program_Uid = program_selected_uuid;
            } else {
                program_Uid = program_uuid;
            }


            if (enrolledCoursesResponses != null) {
                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                    if (enrolledCoursesResponse.getCourse() != null) {
                        if (courseRuns.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                            sendAnalyticsCourseView(enrolledCoursesResponse, program_Uid);
                            environment.getRouter().showCourseDashboardTabs(getActivity(), enrolledCoursesResponse,
                                    false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDoneTalkBackListener() {
          //  setFocusOnMic();
    }


    private class GetCourseListTask extends AsyncTask<Void, Void, ResponseCourseModel> {

        private AtomicInteger completedTasks;
        private int totalTasks;

        GetCourseListTask(AtomicInteger completedTasks, int totalTasks) {
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
        }

        @Override
        protected ResponseCourseModel doInBackground(Void... voids) {
            return getCourseList();
        }

        @Override
        protected void onPostExecute(ResponseCourseModel responseBody) {
            // Handle the result of getCourseList() here
            if (responseBody != null) {
                    if (tag_screen_flag) {
                        showViewsForTagScreen();
                        hideShimmerLayoutForTagScreen();
                    }
                    if(!isEnrollCheck) {
                        flag = true;
                    }
                    responseCourseModel = responseBody;
                    onProgramSelection();
                    binding.iconProgress.setVisibility(View.GONE);
                    programModelAdapter.notifyItemChanged(selected_position);


                taskCompleted();
            }
        }

        private void taskCompleted() {
            int completed = completedTasks.incrementAndGet();
            if (completed == totalTasks) {
                Log.d("taskCompleted ", "Complete both task");
                // All tasks completed, perform any additional actions here
                // For example, stop shimmer effects or update UI
            }
        }
    }

    private class CheckEnrollResponseTask extends AsyncTask<Void, Void, ResponseEnrollmentModel> {

        private AtomicInteger completedTasks;
        private int totalTasks;

        CheckEnrollResponseTask(AtomicInteger completedTasks, int totalTasks) {
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
        }

        @Override
        protected ResponseEnrollmentModel doInBackground(Void... voids) {
            return checkEnrollResponse();
        }
        @Override
        protected void onPostExecute(ResponseEnrollmentModel result) {
            if (result != null) {

                if (result.getEnrollmentStatus().equals("enrolled")) {
                    getMyCourseList();
                    isEnroll = true;
                }
                taskCompleted();
            }
        }

        private void taskCompleted() {
            int completed = completedTasks.incrementAndGet();
            if (completed == totalTasks) {
                Log.d("CheckEnrollResponseTask taskCompleted", "Completed both tasks");
                // All tasks completed, perform any additional actions here
                // For example, stop shimmer effects or update UI
            }
        }
    }

    private ResponseCourseModel getCourseList() {
        isEnroll = false;
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

        try {
            Call<ResponseCourseModel> programResponseModel = courseApi.getCourseResponse(token, program_uuid, selectedLanguage);
            Response<ResponseCourseModel> response = programResponseModel.execute();

            if (response.isSuccessful()) {
                ResponseCourseModel responseBody = response.body();
                return responseBody;
            } else {
                if(getActivity()!=null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if(getActivity().getApplicationContext().isUiContext()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to get course list", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ResponseEnrollmentModel checkEnrollResponse() {
        final ResponseEnrollmentModel[] responseEnrollmentModel = {null};
        ApiNewLmsClient apiNewLmsClient=new ApiNewLmsClient(loginAPI.config.getApiHostURL());
        ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);

        Call<ResponseEnrollmentModel> call = apiService.unrollCheck(
                loginPrefs.getAuthorizationHeader(),
                loginPrefs.getUsername(),
                program_uuid
        );

        call.enqueue(new Callback<ResponseEnrollmentModel>() {
            @Override
            public void onResponse(Call<ResponseEnrollmentModel> call, Response<ResponseEnrollmentModel> response) {
                if (response.isSuccessful()) {
                    // Handle successful response

                    isEnrollCheck=true;
                    ResponseEnrollmentModel data = response.body();
                    if (data != null) {
                        // Log the response body or relevant information
                        responseEnrollmentModel[0]=data;
                        if (data.getEnrollmentStatus().equals("enrolled")) {
                            getMyCourseList();
                            isEnroll = true;
                            binding.enrollInProgram.setVisibility(View.GONE);
                            binding.unenrollFromProgram.setVisibility(View.VISIBLE);

                        }
                        else{
                            if(flag) {
                                flag = false;
                                responseDataUiSet();
                                binding.enrollInProgram.setVisibility(View.VISIBLE);
                                binding.unenrollFromProgram.setVisibility(View.GONE);
                            }
                        }
                        Log.d("ResponseEnrollmentModel",responseEnrollmentModel[0].getEnrollmentStatus()+""+responseEnrollmentModel[0].isStatus());
                    } else {
                        Log.e("ResponseEnrollmentModel", "Response body is null");
                    }
                } else {
                    // Handle error response
                    Log.e("ResponseEnrollmentModel", "Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseEnrollmentModel> call, Throwable t) {
                // Handle failure

                Log.e("ResponseEnrollmentModel", "Request failed", t);
            }
        });

        Log.d("NewProgramFragment", "checkEnrollResponse executed "+responseEnrollmentModel[0]);
        return responseEnrollmentModel[0]; // Replace with your actual return value
    }

    private void addDelay() {
        // Create a handler
        Handler handler = new Handler();

        // Add a delay of 30 seconds (30000 milliseconds)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Code to be executed after the delay
                // This will be executed after 30 seconds
                // For example, you can put your logic here
                // ...
            }
        }, 30000);
    }



    private void hideViewsForTagScreen() {
        binding.rvCourses.setVisibility(View.GONE);
        binding.courseCount.setVisibility(View.GONE);
        binding.programNameInCard.setVisibility(View.GONE);
        binding.linerProgramName.setVisibility(View.GONE);
        binding.courseStatus.setVisibility(View.GONE);
        binding.errorMsgTv.setVisibility(View.GONE);
    }

    private void showViewsForTagScreen() {
        binding.rvCourses.setVisibility(View.VISIBLE);
        binding.programNameInCard.setVisibility(View.VISIBLE);
        binding.linerProgramName.setVisibility(View.VISIBLE);
        binding.courseStatus.setVisibility(View.VISIBLE);
        binding.errorMsgTv.setVisibility(View.VISIBLE);
    }

    private void showShimmerLayoutForTagScreen() {
        binding.shimmerLayoutCourse.setVisibility(View.VISIBLE);
    }

    private void hideShimmerLayoutForTagScreen() {
        binding.shimmerLayoutCourse.setVisibility(View.GONE);
    }

    private void handleException(Exception ex) {
        if (ex instanceof HttpStatusException && ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
            // Handle unauthorized exception
        } else {
            // Handle other exceptions
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getMyPrograms(boolean check) throws Exception {
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }

        checkTokenExpire();
       /* ProgramTask discoveryTask = new ProgramTask(getContext(), loginPrefs.getUsername(), selectedLanguage) {
            @Override
            public void onSuccess(@NonNull List<Programs> result) throws Exception {
                if (check) {
                    checkTokenExpire();
                }
                Log.e("getMyPrograms>>>>", result + "");
                if (result != null) {
                    String userType = loginPrefs.getUserType();
                    List<MyProgramListModel> newProgramsListforTeacher = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforStudent = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforBoth = new ArrayList<>();
                    /*
                     * for (Programs programs : result) {
                     * if (programs.getTags() != null) {
                     * for (String tag : programs.getTags()) {
                     * if (tag.toLowerCase().contains("teacher")) {
                     * MyProgramListModel myProgramListModel = new MyProgramListModel();
                     * myProgramListModel.setTagName(tag);
                     * myProgramListModel.setProgramName(programs.getProgram_title());
                     * myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                     * myProgramListModel.setResume_program(programs.getResumePrograms());
                     * newProgramsListforTeacher.add(myProgramListModel);
                     * }
                     * if (tag.toLowerCase().contains("student")) {
                     * MyProgramListModel myProgramListModel = new MyProgramListModel();
                     * myProgramListModel.setTagName(tag);
                     * myProgramListModel.setProgramName(programs.getProgram_title());
                     * myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                     * myProgramListModel.setResume_program(programs.getResumePrograms());
                     * newProgramsListforStudent.add(myProgramListModel);
                     * }
                     * if (!tag.toLowerCase().contains("student") &&
                     * !tag.toLowerCase().contains("teacher")) {
                     * MyProgramListModel myProgramListModel = new MyProgramListModel();
                     * myProgramListModel.setTagName(tag);
                     * myProgramListModel.setProgramName(programs.getProgram_title());
                     * myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                     * myProgramListModel.setResume_program(programs.getResumePrograms());
                     * newProgramsListforBoth.add(myProgramListModel);
                     * }
                     * }
                     * }
                     * }
                     *//*
                    for (Programs programs : result) {
                        if (programs.getTags() != null) {
                            for (MyProgramTags myProgramTags : programs.getTags()) {
                                if (myProgramTags.getTag_title() != null
                                        && myProgramTags.getTag_title().toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforTeacher.add(myProgramListModel);
                                }
                                if (myProgramTags.getTag_title() != null
                                        && myProgramTags.getTag_title().toLowerCase().contains("student")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforStudent.add(myProgramListModel);
                                }
                                if (myProgramTags.getTag_title() != null
                                        && !myProgramTags.getTag_title().toLowerCase().contains("student")
                                        && !myProgramTags.getTag_title().toLowerCase().contains("teacher")) {
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
        discoveryTask.execute();*/
    }

    void sendAnalyticsFilter(ProgramResponseModel.Program programResponseModel) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.PROGRAM_NAME, programResponseModel.getTitle());
        values.put(Analytics.Keys.PROGRAM_UUID, programResponseModel.getUuid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.SELECT_PROGRAM, null, null, values);
    }

    void sendAnalyticsEnroll(EnrollAndUnenrollData enrollAndUnenrollData) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ENROLL_PROGRAM_NAME, enrollAndUnenrollData.getData().getProgram_Name());
        values.put(Analytics.Keys.ENROLL_PROGRAM_UID, enrollAndUnenrollData.getData().getProgram_uuid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.Enroll_Program, null, null, values);
    }

    void sendAnalyticsUnroll(EnrollAndUnenrollData enrollAndUnenrollData) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.UNROLL_PROGRAM_NAME, enrollAndUnenrollData.getData().getProgram_Name());
        values.put(Analytics.Keys.UNROLL_PROGRAM_UID, enrollAndUnenrollData.getData().getProgram_uuid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.Unroll_Program, null, null, values);
    }

    void sendAnalyticsCourseView(EnrolledCoursesResponse enrolledCoursesResponse,String program_Uid) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.COUSRE_NAME, enrolledCoursesResponse.getCourse().getName());
        values.put(Analytics.Keys.COURSE_UID, enrolledCoursesResponse.getCourse().getId());
        values.put(Analytics.Keys.LINKED_PROGRAM_UUID,program_Uid);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.VIEW_COURSE, null, null, values);
    }

    @Override
    public void onSpeechResult(String text) {

        Map<String,String> result = classifier.classifyIntent(text);

        String keyToSpeak = "message";
        String valueToSpeak = result.get(keyToSpeak);
        textToSpeechHelper.speakText(valueToSpeak);

        String keyToIntent = "Intent";
        String valueToIntent = result.get(keyToIntent);

        String keyToAction = "action";
        String valueToAction = result.get(keyToAction);
        if(valueToAction.equals("true"))
            for (ProgramResponseModel.Program program : programResultLists) {
                if(program.getTitle().equals(valueToIntent))
                {
                    changeDropdown(program);
                    break;
                }
            }
    }

    @Override
    public void onSpeechError(String error) {

    }

    public void onMicButtonClick() {
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        textToSpeechHelper.stop();
        speechToTextHelper.startSpeechRecognition();

    }
    private void setFocusOnMic(){

        if(!tag_screen_flag && !program_uuid.isEmpty() ){
            binding.tagName.setFocusable(true);
            binding.tagName.setFocusableInTouchMode(true);
            binding.tagName.requestFocus();
            binding.tagName.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
        }
       else if (menuItem != null) {
            View menuItemView = menuItem.getActionView();

            if (menuItemView != null) {

                // Move UI operations to the main thread using runOnUiThread
                menuItemView.findViewById(R.id.action_view_icon).setVisibility(View.VISIBLE);
                menuItemView.findViewById(R.id.action_view_icon).setFocusable(true);
                menuItemView.findViewById(R.id.action_view_icon).setFocusableInTouchMode(true);
                menuItemView.findViewById(R.id.action_view_icon).requestFocus();
                menuItemView.findViewById(R.id.action_view_icon).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                menuItemView.findViewById(R.id.action_view_icon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMicButtonClick();
                    }
                });


            } else {

                Log.e(TAG, "Action view for menu item is null");
            }
            if (programResultLists == null||programResultLists.isEmpty()) {
                menuItem.setVisible(false);
                menuItemView.findViewById(R.id.action_view_icon).setVisibility(View.GONE);
            }
        } else {

            Log.e(TAG, "Menu item not found");
        }

    }
    public void initializeIntentData() {
        if (!programResultLists.isEmpty()) {
            setFocusOnMic();
            List<String> tags = new ArrayList<>();
            for (ProgramResponseModel.Program program : programResultLists) {
                String title = program.getTitle();
                if (title != null && !title.isEmpty()) {
                    tags.add(title);
                }
            }
            String selectedLanguage = "en";
            if (getActivity() != null) {
                if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                    selectedLanguage = LocaleManager.getLanguagePref(getActivity());
                }
            }
            classifier = new IntentProgramClassifier(tags, selectedLanguage,getContext());
        }
    }
    void copyTextDataByLongPress(){

        setGestureListeners(binding.organisations);


        /*binding.organisations.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy =  binding.organisations.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        setGestureListeners(binding.programNameInCard);
        /*binding.programNameInCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.programNameInCard.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        setGestureListeners(binding.tagName);
        /*binding.tagName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.tagName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        setGestureListeners(binding.selectAProgram);
        /*binding.selectAProgram.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.selectAProgram.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        binding.optionSpinnerPrograms.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int selectedItemPosition = binding.optionSpinnerPrograms.getSelectedItemPosition();
                // Get the selected item text
                String selectedItem = (String) binding.optionSpinnerPrograms.getItemAtPosition(selectedItemPosition);
                clipboardService.copyText(selectedItem);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        setGestureListeners(binding.courseCount);
        /*binding.courseCount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.courseCount.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        setGestureListeners(binding.courseCount);
        /*binding.courseCount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.courseCount.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        setGestureListeners(binding.errorMsgTv);
        /*binding.errorMsgTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.errorMsgTv.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
       // setGestureListeners(binding.enrollInProgram);
        /*binding.enrollInProgram.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.enrollInProgram.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        //setGestureListeners(binding.unenrollFromProgram);
        /*binding.unenrollFromProgram.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.unenrollFromProgram.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
    }

    private void setGestureListeners(TextView textView) {
        GestureListener gestureListener = new GestureListener( textView,null,getContext(),this::navigateToAnotherScreen);
        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void setGestureListeners2(TextView textView) {
        GestureListener gestureListener = new GestureListener( textView,null,getContext(),this::navigateToAnotherScreen);
        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public void chatBotTalkCourse(List<CourseRuns> courseRuns){

        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
       // textToSpeechHelper.stop();
        chatBotCourseFlag=false;
        String program="";
        if(!is_my_program_flag && tag_screen_flag) {
            program = binding.optionSpinnerPrograms.getSelectedItem().toString();
        }
        else {
            program = binding.tagName.getText().toString();
        }
        String valueToSpeak = checkMessageLanguageCourse(courseRuns.size(),selectedLanguage);
        String baseString = getResources().getString(R.string.user_intent_program_message);

        String formattedString = baseString.replace("%1$s", String.valueOf(program));
        String completeString=formattedString+" "+valueToSpeak;

        textToSpeechHelper.speakText(completeString);

    }

    public void chatBotTalkCourseFormDashboard(List<CourseRuns> courseRuns){

        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        // textToSpeechHelper.stop();
        chatBotCourseFlag=false;

        String valueToSpeak = checkMessageLanguageCourse(courseRuns.size(),selectedLanguage);
        String baseString = getResources().getString(R.string.user_intent_program_message);

        textToSpeechHelper.speakText(valueToSpeak);

    }

    public void chatBotTalkProgram(){
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        textToSpeechHelper.stop();
        String valueToSpeak =checkMessageLanguageProgram(programResultLists.size(),selectedLanguage);
        textToSpeechHelper.speakText(valueToSpeak);
    }

    private String checkMessageLanguageCourse(int messageCode,String selectedLanguage) {

        String baseString = getResources().getString(R.string.user_intent_course_found);

        String formattedString = baseString.replace("%1$s", String.valueOf(messageCode));
        return formattedString;

    }
    private String checkMessageLanguageProgram(int messageCode,String selectedLanguage) {
        String baseString = getResources().getString(R.string.user_intent_program_found);

        String formattedString = baseString.replace("%1$s", String.valueOf(messageCode));
        return formattedString;

    }
}
