package org.edx.mobile.view;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.Chatbot.IntentClassifier.IntentProgramClassifier;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextHelper;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.comparator.TalkBackDetector.MyAccessibilityCallback;
import org.edx.mobile.comparator.TalkBackDetector.MyAccessibilityService;
import org.edx.mobile.Chatbot.TextToSpeechHelper.TextToSpeechHelper;
import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentProgramListBinding;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.ActivityProvider;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.notification.NotificationTask;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.ParticularCourseTask;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.programs.MyProgramTags;
import org.edx.mobile.programs.NotificationModel;
import org.edx.mobile.programs.ProgramTask;
import org.edx.mobile.programs.Programs;
import org.edx.mobile.programs.ResumeCourse;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.MyProgramListAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;
import static org.edx.mobile.view.ProgramActivity.CHATBOTFLAG;
import static org.edx.mobile.view.ProgramActivity.MYPROGRAMFLAG;
import static org.edx.mobile.view.ProgramActivity.PROGRAM;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_CONVERTED;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_UUID;

public class MyProgramListFragment extends OfflineSupportBaseFragment
        implements RefreshListener, OnRecyclerItemClickListener, SpeechToTextListener, MyAccessibilityCallback, ActivityProvider, OnNavigateListener, TalkBackListener  /*,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>>*/ {
    public static final String TAG = MyCoursesListFragment.class.getCanonicalName();
    private FragmentProgramListBinding binding;
    private MyProgramListAdapter myProgramListAdapter;
    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    private SpeechToTextHelper speechToTextHelper;
    IntentProgramClassifier classifier;
    private TextToSpeechHelper textToSpeechHelper;

    List<MyProgramListModel> myProgramListData;
    @Inject
    LoginPrefs loginPrefs;


    @Inject
    LoginAPI loginAPI;
    private MyCoursesListFragment.OnExploreButtonClick onExploreButtonClick;
    @Inject
    protected IEdxEnvironment environment;
    private ResumeCourse resumeCourse;

    private final Logger logger = new Logger(getClass().getSimpleName());
    ArrayList<EnrolledCoursesResponse> enrolledCoursesResponses = new ArrayList<>();
    private static final int MY_COURSE_LOADER_ID = 0x905000;
    private EnrolledCoursesResponse courseData;

    private ClipboardService clipboardService;
    MenuItem menuItem;

    MenuItem menuItemNotification;

    //NotificationModel notificationModelData;

    ActivityProvider activityProvider;

    GestureListener gestureListener ;

    // Create a gesture detector
    GestureDetector gestureDetector;

     View menuItemView ;

    private SoundPool soundPool;
    private int soundId;
    ImageView floatingActionButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public MyProgramListFragment setExploreButtonClick(MyCoursesListFragment.OnExploreButtonClick answerChangeListener) {

        this.onExploreButtonClick = answerChangeListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program_list, container, false);
        clipboardService = ClipboardServiceHolder.getClipboardService(getActivity().getApplicationContext());

        binding.hello.setText(getString(R.string.hello) + " " + loginPrefs.getUsername());


        binding.progressBar.setVisibility(View.VISIBLE);
        binding.exploreCourse.setVisibility(View.GONE);

        speechToTextHelper = new SpeechToTextHelper(getActivity().getApplicationContext(), this, getActivity());
        textToSpeechHelper = new TextToSpeechHelper(getActivity().getApplicationContext(), getActivity(),this::onDoneTalkBackListener);
        binding.btnExploreCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExploreButtonClick.onClick();
            }
        });
        if(getActivity()!=null){
            floatingActionButton = getActivity().findViewById(R.id.chatbot_button);
            if(floatingActionButton!=null) {
                floatingActionButton.setVisibility(View.VISIBLE);
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(getContext()!=null) {
                            Intent intent = new Intent(getContext(), ChatbotActivity.class);
                            startActivity(intent);
                        }

                    }
                });
            }

            ObjectAnimator flipAnim = ObjectAnimator.ofFloat(floatingActionButton, "rotationY", 0f, 360f);
            flipAnim.setDuration(2500);
            flipAnim.setInterpolator(new android.view.animation.DecelerateInterpolator());
            flipAnim.setRepeatCount(0);
            flipAnim.setRepeatMode(ObjectAnimator.RESTART);

            // Start the animation
            flipAnim.start();


            TextView textView = getActivity().findViewById(R.id.chatbot_text);
            textView.setVisibility(View.VISIBLE);
            ObjectAnimator bounceAnim = ObjectAnimator.ofFloat(textView, "translationY", 0, -40, 0);
            bounceAnim.setDuration(2500);
            bounceAnim.setInterpolator(new BounceInterpolator());
            bounceAnim.setRepeatCount(0);
            bounceAnim.setRepeatMode(ObjectAnimator.RESTART);

            // Start the animation
            bounceAnim.start();

        }
        binding.resumeCourseContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (courseData != null) {
                   /* environment.getRouter().showCourseUnitDetail(MyProgramListFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,resumeCourse.getBlock_id(), false);*/
                    //   final CourseComponent component = adapter.getItem(position).component;

                    if (resumeCourse.getBlock_id().contains("sequential")) {

                        environment.getRouter().showCourseContainerOutline(MyProgramListFragment.this,
                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,
                                resumeCourse.getBlock_id(), null, false);
                    } else {

                        environment.getRouter().showCourseUnitDetail(MyProgramListFragment.this,
                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,
                                resumeCourse.getBlock_id(), false);
                    }
                    sendAnalyticsRecentCourseDetail();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_course_info), Toast.LENGTH_LONG).show();
                }
            }
        });
        // Create a gesture listener
      //  gestureListener = new GestureListener();

        // Create a gesture detector
      //  gestureDetector = new GestureDetector(getContext(), gestureListener);
        myProgramListAdapter = new MyProgramListAdapter(getActivity(), MyProgramListFragment.this::onItemClick,this::navigateToAnotherScreen);
        activityProvider=this::getSupportFragmentManager;
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.myProgramList.setLayoutManager(mLayoutManager);
        binding.myProgramList.setAdapter(myProgramListAdapter);


        binding.resumeCourseContinueShimmer.startShimmer();
        binding.resumeCourseContinue.setVisibility(View.GONE);


        if (menuItem != null) {
            menuItem.setVisible(true);
        }


        setHasOptionsMenu(true);
        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getContext(), R.raw.beep_sound_2, 1);
        // Initialize AccessibilityService and TextViews
        // Bind to the accessibility service (check if it's running first)

        boolean enabled = isAccessibilityServiceEnabled(getContext(), MyAccessibilityService.class);

        // Bind to the accessibility service (check if it's running first)


        gestureListener = new GestureListener(binding.hello,null,getContext(),this::navigateToAnotherScreen);

        // Create a gesture detector
        gestureDetector = new GestureDetector(getContext(), gestureListener);


        copyTextDataByLongPress();

        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getContext(), R.raw.beep_sound_2, 1);

        if(checkDialogBox()){
            showDialog();
        }
       // getNotification();

        return binding.getRoot();
    }

    private boolean checkDialogBox() {
        int currentVersion=BuildConfig.VERSION_CODE;
        int storedVersion =loginPrefs.getUserVoiceDialogEnabled();
        if(currentVersion>storedVersion){
            loginPrefs.storeUserVoiceDialogEnabled(BuildConfig.VERSION_CODE);
            return true;
        }
        return false;
    }


    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_item_voice);
        menuItem = item;
        menuItemView= menuItem.getActionView();
        if(myProgramListData==null) {
            item.setVisible(false);
        }
        else {
            setFocusOnMic();
        }
        //MenuItem itemNotification = menu.findItem(R.id.menu_item_notification);
        //menuItemNotification=itemNotification;
//        if(notificationModelData!=null){
//            enableNotification(notificationModelData);
//        }
//        else{
//
//        }

        /*if (menuItemView != null) {
            menuItemView.findViewById(R.id.action_view_icon).setVisibility(View.GONE);
            menuItemView.findViewById(R.id.action_view_icon).setFocusable(true);
            menuItemView.findViewById(R.id.action_view_icon).setFocusableInTouchMode(true);
            menuItemView.findViewById(R.id.action_view_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFocusOnMic();
                }
            });
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //loadData(false);
        try {
            getMyPrograms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(menuItem!=null){

            menuItem.setVisible(true);
            menuItem.getActionView().findViewById(R.id.action_view_icon).setVisibility(View.VISIBLE);
        }
        else {

        }
        if (!loginPrefs.getUserVoicePermissionEnabled()) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                loginPrefs.storeUserVoicePermissionEnabled(true);
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment,new MyProgramListFragment(), MyProgramListFragment.TAG).
                            addToBackStack(MyProgramListFragment.TAG)
                            .commit();
                } else {
                    for (int i = 1; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, new MyProgramListFragment(), MyProgramListFragment.TAG)
                            .commit();
                }
            }
        }
    }

    @Override
    public void onRefresh() {

    }

    private void getMyPrograms() throws Exception {
       // menuItem.setVisible(false);
        //View menuItemView = menuItem.getActionView();
        //menuItemView.findViewById(R.id.action_view_icon).setVisibility(View.GONE);
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        ProgramTask discoveryTask = new ProgramTask(getContext(), loginPrefs.getUsername(),
                selectedLanguage) {
            @Override
            public void onSuccess(@NonNull List<Programs> result) {
                binding.progressBar.setVisibility(View.GONE);
                if (result != null) {
                    String userType = loginPrefs.getUserType();
                    List<MyProgramListModel> myProgramListModels = new ArrayList<>();

                    List<MyProgramListModel> newProgramsListforTeacher = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforStudent = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforBoth = new ArrayList<>();
                  /*  for (Programs programs : result) {
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
                                    myProgramListModel.setConvertedTagName(myProgramTags.getTag_title());
                                    // myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramName(programs.getConverted_program_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforTeacher.add(myProgramListModel);
                                }
                                if (myProgramTags.getTag_title() != null && myProgramTags.getTag_title().toLowerCase().contains("student")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setConvertedTagName(myProgramTags.getTag_title());
                                    // myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramName(programs.getConverted_program_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforStudent.add(myProgramListModel);
                                }
                                if (myProgramTags.getTag_title() != null && !myProgramTags.getTag_title().toLowerCase().contains("student") && !myProgramTags.getTag_title().toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(myProgramTags.getConverted_tag_title());
                                    myProgramListModel.setConvertedTagName(myProgramTags.getTag_title());
                                    // myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramName(programs.getConverted_program_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforBoth.add(myProgramListModel);
                                }
                            }
                        }
                    }
                    if (userType != null) {
                        if (userType.contains("teacher")) {
                            myProgramListModels.addAll(newProgramsListforTeacher);
                            myProgramListModels.addAll(newProgramsListforBoth);
                        } else {
                            myProgramListModels.addAll(newProgramsListforStudent);
                            myProgramListModels.addAll(newProgramsListforBoth);
                        }
                    } else {
                        myProgramListModels.addAll(newProgramsListforBoth);
                    }
                    //myProgramListModels.clear();
                    resumeCourse = null;
                    if (myProgramListModels != null && myProgramListModels.size() > 0) {
                        binding.txtYourEnrolledProgram.setVisibility(View.VISIBLE);
                        for (MyProgramListModel programListModel : myProgramListModels) {
                            if (programListModel.getResume_program() != null) {
                                if (resumeCourse == null) {
                                    resumeCourse = new ResumeCourse();

                                   /* resumeCourse.setBlock_id("block-v1%3AVisionEmpower%2BG3_MAT_NCERT_Ch01%2B2021%2Btype%40sequential%2Bblock%40c5034a5f601e40849d6a7a9868bf8c03");
                                    resumeCourse.setCourse_id("course-v1:VisionEmpower+G3_MAT_NCERT_Ch01+2021");
                                    resumeCourse.setCourse_name(programListModel.getResume_program().getConverted_course_name());
                                    resumeCourse.setProgramName(programListModel.getProgramName());
                                    resumeCourse.setTagName(programListModel.getTagName());*/

                                    resumeCourse.setBlock_id(programListModel.getResume_program().getBlock_id());
                                    resumeCourse.setCourse_id(programListModel.getResume_program().getCourse_id());
                                    resumeCourse.setCourse_name(programListModel.getResume_program().getConverted_course_name());
                                    resumeCourse.setProgramName(programListModel.getProgramName());
                                    resumeCourse.setTagName(programListModel.getTagName());
                                }
                            }
                        }

                        if (resumeCourse != null) {
                            binding.tagName.setText(resumeCourse.getTagName());
                            binding.programName.setText(context.getString(R.string.program_name) + " - " +
                                    resumeCourse.getProgramName());
                            binding.courseName.setText(resumeCourse.getCourse_name());
                            binding.lnResumeCourse.setVisibility(View.VISIBLE);
                            getParticularCourse(resumeCourse.getCourse_id());

                        }
                    }
                    if (myProgramListModels != null) {
                        binding.myProgramList.setVisibility(View.VISIBLE);
                    }
                    myProgramListAdapter.setMyProgramList(myProgramListModels);
                    initializeIntentData(myProgramListModels);
                    if(menuItem!=null) {
                        menuItem.setVisible(true);
                    }
                    if(!checkDialogBox()){
                        setFocusOnMic();
                    }
                    if (myProgramListModels == null) {
                        binding.exploreCourse.setVisibility(View.VISIBLE);
                        binding.myProgramList.setVisibility(View.GONE);
                    } else if (myProgramListModels.size() == 0) {
                        binding.exploreCourse.setVisibility(View.VISIBLE);
                        binding.myProgramList.setVisibility(View.GONE);
                    }
                } else {
                    binding.exploreCourse.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onException(Exception ex) {
                binding.progressBar.setVisibility(View.GONE);
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                } else {

                }
            }
        };
        discoveryTask.execute();
    }

    private void setFocusOnMic(){
       if (menuItem != null) {

            if (menuItemView != null) {
                menuItemView.findViewById(R.id.action_view_icon).setVisibility(View.VISIBLE);
                menuItemView.findViewById(R.id.action_view_icon).setFocusable(true);
                menuItemView.findViewById(R.id.action_view_icon).setFocusableInTouchMode(true);
                menuItemView.findViewById(R.id.action_view_icon).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                menuItemView.findViewById(R.id.action_view_icon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMicButtonClick();
                    }
                });

                menuItemView.findViewById(R.id.action_view_icon).requestFocus();
                 menuItemView.findViewById(R.id.action_view_icon).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);

     //           menuItemView.findViewById(R.id.action_view_icon).requestFocus();
  //              menuItemView.findViewById(R.id.action_view_icon).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {

                Log.e(TAG, "Action view for menu item is null");
            }

        } else {

            Log.e(TAG, "Menu item not found");
        }

    }


    @Override
    protected boolean isShowingFullScreenError() {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (getActivity() != null) {
            onNetworkConnectivityChangeEvent(event);
        }
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof MyProgramListModel) {
            MyProgramListModel myProgramListModel = (MyProgramListModel) item;
            navigateToAnotherScreen(myProgramListModel);
        }
    }
/*
    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {
        return new CoursesAsyncLoader(getActivity());
    }*/

  /*  @Override
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
            if (resumeCourse != null) {
                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                    if (enrolledCoursesResponse.getCourse() != null) {
                        if (enrolledCoursesResponse.getCourse().getId() != null) {
                            if (enrolledCoursesResponse.getCourse().getId().equals(resumeCourse.getCourse_id())) {
                                courseData = enrolledCoursesResponse;
                            }
                        }
                    }
                }
            }
        } else if (result.getResult() == null) {

        }
    }


    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader) {
    }

    protected void loadData(boolean showProgress) {
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, null, this);
    }
*/

    public void getParticularCourse(String courseId) {
        try {
            ParticularCourseTask particularCourseTask = new ParticularCourseTask(getContext(), loginPrefs.getUsername(),
                    courseId) {
                @Override
                public void onSuccess(@NonNull ArrayList<EnrolledCoursesResponse> result) {
                    try {
                        if (result != null) {
                            // Handle successful response
                            ArrayList<EnrolledCoursesResponse> data = result;
                            if (data != null) {
                                enrolledCoursesResponses = data;
                                if (enrolledCoursesResponses != null) {
                                    for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                                        if (enrolledCoursesResponse.getCourse() != null) {
                                            if (enrolledCoursesResponse.getCourse().getId() != null) {
                                                // if (enrolledCoursesResponse.getCourse().getId().equals(resumeCourse.getCourse_id())) {
                                                courseData = enrolledCoursesResponse;
                                                binding.resumeCourseContinueShimmer.stopShimmer();
                                                binding.resumeCourseContinueShimmerLayout.setVisibility(View.GONE);
                                                binding.resumeCourseContinue.setVisibility(View.VISIBLE);
                                                // resumeCourse.setCourse_name(enrolledCoursesResponse.getCourse().getName());
                                                //}
                                            }
                                        }
                                    }
                                }
                                Log.d("enrolledCoursesResponses", enrolledCoursesResponses.get(0).getCourse().getId() + "" + enrolledCoursesResponses.get(0).getCourse().getName());
                            } else {
                                Log.e("enrolledCoursesResponses", "Response body is null");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("enrolledCoursesResponses", "Exception in onResponse", e);
                    }
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    } else {

                    }
                }
            };
            particularCourseTask.execute();


        } catch (Exception e) {
            Log.e("enrolledCoursesResponses", "Exception in getParticularCourse", e);
        }
    }


    void sendAnalyticsCourseDetail(MyProgramListModel myProgramListModel) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.PROGRAM_NAME, myProgramListModel.getProgramName());
        values.put(Analytics.Keys.LINKED_TOPIC_NAME, myProgramListModel.getConvertedTagName());
        values.put(Analytics.Keys.PROGRAM_UUID, myProgramListModel.getProgramUUid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.View_Program, null, "Click", values);
    }

    void sendAnalyticsRecentCourseDetail() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.LINKED_PROGRAM_NAME, resumeCourse.getProgramName());
        values.put(Analytics.Keys.LINKED_TOPIC_NAME, resumeCourse.getTagName());
        values.put(Analytics.Keys.RECENT_COUSRE_UID, resumeCourse.getCourse_id());
        values.put(Analytics.Keys.RECENT_COUSRE_NAME, resumeCourse.getCourse_name());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.RECENT_COURSE, null, "Click", values);
    }
    void copyTextDataByLongPress() {

        setGestureListeners(binding.hello);
        setGestureListeners(binding.recentlyAccessedCourse);
        setGestureListeners(binding.tagName);
        setGestureListeners(binding.programName);
        setGestureListeners(binding.courseName);
        setGestureListeners(binding.txtYourEnrolledProgram);
    }

    private void setGestureListeners(TextView textView) {
        GestureListener gestureListener = new GestureListener( textView,null,getContext(),this::navigateToAnotherScreen);
        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }


    @Override
    public void onSpeechResult(String text) {


        Map<String, String> result = classifier.classifyIntent(text);
        String keyToSpeak = "message";
        String valueToSpeak = result.get(keyToSpeak);
        textToSpeechHelper.speakText(valueToSpeak);

        String keyToIntent = "Intent";
        String valueToIntent = result.get(keyToIntent);

        String keyToAction = "action";
        String valueToAction = result.get(keyToAction);
        if (valueToAction.equals("true")) {
            for (MyProgramListModel myProgramListModel : myProgramListData) {
                String tagName = myProgramListModel.getTagName();

                if (tagName != null && !tagName.isEmpty()) {
                    if (tagName.equals(valueToIntent)) {
                        navigateToAnotherScreen(myProgramListModel);
                        break;
                    }

                }

                String term = myProgramListModel.getProgramName();
                if (term != null && !term.isEmpty()) {
                    if (term.equals(valueToIntent)) {
                        navigateToAnotherScreen(myProgramListModel,true);
                        break;
                    }
                }


            }
        }
    }

    public void onMicButtonClick() {
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        textToSpeechHelper.stop();
        speechToTextHelper.startSpeechRecognition();
    }


    @Override
    public void onSpeechError(String error) {
        //Toast.makeText(getContext(), "Please click on Voice Search button again", Toast.LENGTH_SHORT).show();
    }

    public void initializeIntentData(List<MyProgramListModel> myProgramList) {
        myProgramListData = myProgramList;
        List<String> tags = new ArrayList<>();
        for (MyProgramListModel myProgramListModel : myProgramList) {
            String term = myProgramListModel.getProgramName();
            if (term != null && !term.isEmpty()) {
                tags.add(term);
            }
            String TagName = myProgramListModel.getConvertedTagName();
            if (TagName != null && !TagName.isEmpty()) {
                tags.add(TagName);
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

    @Override
    public void onTextView1LongPressed() {

    }

    @Override
    public void onTextView2LongPressed() {

    }

    @Override
    public FragmentManager getSupportFragmentManager() {
        return super.getActivity().getSupportFragmentManager();
    }

    @Override
    public void navigateToAnotherScreen(Object item) {
         if (item instanceof MyProgramListModel) {

             if(floatingActionButton!=null) {
                 floatingActionButton.setVisibility(View.GONE);
                 getActivity().findViewById(R.id.chatbot_text).setVisibility(View.GONE);
             }
             MyProgramListModel myProgramListModel = (MyProgramListModel) item;
        MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
        MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
        NewProgramFragment newProgramFragment = new NewProgramFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putString(PROGRAM, myProgramListModel.getConvertedTagName());
        bundle1.putString(PROGRAM_CONVERTED, myProgramListModel.getTagName());
        bundle1.putString(PROGRAM_UUID, myProgramListModel.getProgramUUid());
        bundle1.putBoolean(MYPROGRAMFLAG, true);
        newProgramFragment.setArguments(bundle1);
        sendAnalyticsCourseDetail(myProgramListModel);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, newProgramFragment, NewProgramFragment.TAG).addToBackStack(NewProgramFragment.TAG)
                .commit();
    }
    }
    public void navigateToAnotherScreen(Object item,boolean chatBotFlagProgram) {
        if (item instanceof MyProgramListModel) {
            if(floatingActionButton!=null) {
                floatingActionButton.setVisibility(View.GONE);
                getActivity().findViewById(R.id.chatbot_text).setVisibility(View.GONE);
            }
            MyProgramListModel myProgramListModel = (MyProgramListModel) item;
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
            NewProgramFragment newProgramFragment = new NewProgramFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(PROGRAM, myProgramListModel.getConvertedTagName());
            bundle1.putString(PROGRAM_CONVERTED, myProgramListModel.getTagName());
            bundle1.putString(PROGRAM_UUID, myProgramListModel.getProgramUUid());
            bundle1.putBoolean(MYPROGRAMFLAG, true);
            bundle1.putBoolean(CHATBOTFLAG,chatBotFlagProgram);
            newProgramFragment.setArguments(bundle1);
            sendAnalyticsCourseDetail(myProgramListModel);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, newProgramFragment, NewProgramFragment.TAG).addToBackStack(NewProgramFragment.TAG)
                    .commit();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                // Pass the recognized text to your SpeechToTextHelper's onSpeechResult() method

                onSpeechResult(recognizedText);
            }
            if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
                speechToTextHelper.onActivityResult(requestCode, resultCode, data);
            }
        }
        catch (Exception e){

        }
    }

    @Override
    public void onDoneTalkBackListener() {

    }


    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate a layout for the dialog
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_dialog_voice_layout, null);

        builder.setView(dialogView);

        AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().getDecorView().setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // Set onCancelListener to handle dismissal when the user clicks outside
        /*alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setFocusOnMic();
                alert.dismiss();
                // Execute your function when the user clicks outside the dialog
               // onMicButtonClick();
            }
        });*/

        LinearLayout linearLayout=dialogView.findViewById(R.id.text_dialog_layout);

        linearLayout.requestFocus();
        linearLayout.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);

        TextView titleTextButton = dialogView.findViewById(R.id.dialog_close_button);
        titleTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.getWindow().getDecorView().setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                alert.dismiss();

                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        setFocusOnMic();
                    }
                };
                final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                worker.schedule(task, 1, TimeUnit.SECONDS);
            }
        });

        alert.show();
    }



//    public void getNotification() {
//
//
//        try {
//            NotificationTask particularTask = new NotificationTask(getContext(),1) {
//                @Override
//                public void onSuccess(@NonNull NotificationModel result) {
//                    try {
//                        if (result != null) {
//                            // Handle successful response
//                            NotificationModel data = result;
//                            notificationModelData=data;
//                            if (data != null) {
//                                enableNotification(data);
//                                Log.d("NotificationModel",data.toString() );
//                            } else {
//                                Log.e("NotificationModel", "Response body is null");
//                            }
//                        }
//                    } catch (Exception e) {
//                        Log.e("NotificationModel", "Exception in onResponse", e);
//                    }
//                }
//
//                @Override
//                public void onException(Exception ex) {
//                    if (ex instanceof HttpStatusException &&
//                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                    } else {
//
//                    }
//                }
//            };
//            particularTask.execute();
//
//
//        } catch (Exception e) {
//            Log.e("enrolledCoursesResponses", "Exception in getParticularCourse", e);
//        }
//    }

//    void enableNotification(NotificationModel notificationModel){
//        if(menuItemNotification!=null) {
//            menuItemNotification.setActionView(R.layout.custum_notification_menu_icon);
//            View notificationMenuItemView = menuItemNotification.getActionView();
//            if(notificationModel.getUnreadCount()>0){
//                notificationMenuItemView.findViewById(R.id.custom_notification_bell_icon_relativeLayout).requestFocus();
//                notificationMenuItemView.findViewById(R.id.custom_notification_bell_icon_relativeLayout).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
//            }
//
//            RelativeLayout relativeLayoutNotification= notificationMenuItemView.findViewById(R.id.custom_notification_bell_icon_relativeLayout);
//
//            ViewCompat.setAccessibilityDelegate(relativeLayoutNotification, new AccessibilityDelegateCompat() {
//                @Override
//                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
//                    super.onInitializeAccessibilityNodeInfo(host, info);
//                    host.setLongClickable(false);
//                    String notificationButton=getString(R.string.notification_button);
//                    info.setContentDescription(notificationButton+" "+notificationModel.getUnreadCount()+", . , . , , "+"Unread");
//
//                }
//            });
//            notificationMenuItemView.findViewById(R.id.custom_notification_bell_icon_relativeLayout).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    environment.getRouter().showNotificationActivity(getActivity(), notificationModel, loginPrefs.getUsername());
//                }
//            });
//            notificationMenuItemView.findViewById(R.id.bell_icon).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    environment.getRouter().showNotificationActivity(getActivity(), notificationModel, loginPrefs.getUsername());
//                }
//            });
//            notificationMenuItemView.findViewById(R.id.bell_icon).setVisibility(View.VISIBLE);
//            notificationMenuItemView.findViewById(R.id.notification_count).setVisibility(View.VISIBLE);
//            TextView notificationCount= notificationMenuItemView.findViewById(R.id.notification_count);
//            notificationCount.setText(String.valueOf(notificationModel.getUnreadCount()));
//        }
//    }

}

