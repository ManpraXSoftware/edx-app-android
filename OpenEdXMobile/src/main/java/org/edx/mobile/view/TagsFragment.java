package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.inject.Inject;

import org.edx.mobile.Chatbot.IntentClassifier.IntentSubjectClassifier;
import org.edx.mobile.Chatbot.IntentClassifier.IntentTopicClassifier;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextHelper;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.Chatbot.TextToSpeechHelper.TextToSpeechHelper;
import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentTagsScreenBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.DiscoverySubjectResult;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.model.TagModel;
import org.edx.mobile.discovery.model.TagTermResult;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.TagsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;

import static android.app.Activity.RESULT_OK;
import static java.lang.String.valueOf;
import static org.edx.mobile.util.links.WebViewLink.Param.PROGRAMS;
import static org.edx.mobile.view.ProgramActivity.CHATBOTFLAG;
import static org.edx.mobile.view.ProgramActivity.PROGRAM;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_CONVERTED;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_UUID;
import static org.edx.mobile.view.ProgramActivity.TAGSCREENFLAG;
import static org.edx.mobile.view.TagsFragmentActivity.COLOR_CODE;
import static org.edx.mobile.view.TagsFragmentActivity.SUBJECT;

public class TagsFragment extends BaseFragment implements OnRecyclerItemClickListener,SpeechToTextListener , OnNavigateListener, TalkBackListener {
    public static final String TAG = TagsFragment.class.getCanonicalName();

    private SpeechToTextHelper speechToTextHelper;

    IntentTopicClassifier classifier;

    //private FloatingActionButton floatingActionButton;

    private TextToSpeechHelper textToSpeechHelper;
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    private LoginAPI loginAPI;
    @Inject
    CourseApi courseApi;
    private FragmentTagsScreenBinding binding;
    private static String subject;
    private static String colorCode;
    private static String uuid;

    private static boolean chatBotFlag=false;
    private TagsAdapter tagsAdapter;
    @Inject
    protected IEdxEnvironment environment;

    List<TagTermResult> tagTermResultList=null;
    private ClipboardService clipboardService ;

    private SoundPool soundPool;
    private int soundId;

    MenuItem menuItem;
    public static TagsFragment newInstance(@Nullable Bundle bundle) {
        final TagsFragment fragment = new TagsFragment();
        subject = bundle.getString(TagsFragmentActivity.SUBJECT);
        colorCode = bundle.getString(TagsFragmentActivity.COLOR_CODE);
        uuid= bundle.getString(TagsFragmentActivity.UID);
        chatBotFlag= bundle.getBoolean(String.valueOf(TagsFragmentActivity.chatBotFlag),false);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subject = getArguments().getString(TagsFragmentActivity.SUBJECT);
        colorCode = getArguments().getString(TagsFragmentActivity.COLOR_CODE);
        chatBotFlag = getArguments().getBoolean(TagsFragmentActivity.chatBotFlag,false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags_screen, container,
                false);
        clipboardService = ClipboardServiceHolder.getClipboardService(getActivity().getApplicationContext());
        speechToTextHelper = new SpeechToTextHelper(getActivity().getApplicationContext(), this,getActivity());
        /*floatingActionButton = getActivity().findViewById(R.id.voice_search);
        floatingActionButton.hide();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMicButtonClick();
            }
        });*/
        if(menuItem!=null){
            menuItem.setVisible(true);
        }
        setHasOptionsMenu(true);
       /* new Handler().postDelayed(() -> {
            if(chatBotFlag) {
                chatBotProgramTalk();
            }
           /* AccessibilityManager accessibilityManager = (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager.isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                event.getText().add("Welcome to the next screen"); // Customize your message
                accessibilityManager.sendAccessibilityEvent(event);
            }*/
    /*    }, 2000);*/
        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getContext(), R.raw.beep_sound_2, 1);
        return binding.getRoot();
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.menu_item_voice);
        menuItem=item;
        if(tagTermResultList==null) {
            item.setVisible(false);
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.announceForAccessibility("Tags Screen");
        binding.subjectName.setText(subject);

        if (loginPrefs.getUserType() != null) {
            if (loginPrefs.getUserType().toLowerCase().equals("teacher")) {
                binding.userType.setText(getContext().getString(R.string.for_teacher));
            } else if (loginPrefs.getUserType().toLowerCase().equals("student")) {
                binding.userType.setText(getContext().getString(R.string.for_student));
            }
        } else {
            binding.userType.setText(getContext().getString(R.string.no_user_type));
        }
        tagsAdapter = new TagsAdapter(getActivity(), TagsFragment.this::onItemClick, Integer.valueOf(colorCode),this::navigateToAnotherScreen);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.rvTags.setLayoutManager(mLayoutManager);
        binding.rvTags.setAdapter(tagsAdapter);
        textToSpeechHelper = new TextToSpeechHelper(getActivity().getApplicationContext(),getActivity(),this::onDoneTalkBackListener);
        if(chatBotFlag) {
            chatBotProgramTalk();
        }
        copyTextDataByLongPress();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        binding.shimmerLayout.startShimmer();
        super.onResume();
        try {
            checkToken();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onPause() {
        binding.shimmerLayout.stopShimmer();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkToken() throws Exception {
        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
        long millis = System.currentTimeMillis();
        long tokenTime = millis - responseJwt.creation_time;
        if (tokenTime > responseJwt.expires_in) {
            createToken();
        } else {
            getTopics();
        }
    }

    private void createToken() throws Exception {
        DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                getTopics();
            }

            @Override
            public void onException(Exception ex) {
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                } else {

                }
            }
        };
        discoveryTask.execute();
    }

    private void getTopics() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        Call<TagModel> discoveryTag = courseApi.getTopicsWithSubjectName(token, selectedLanguage, subject);
        discoveryTag.enqueue(new DiscoveryCallback<TagModel>() {
            @Override
            protected void onResponse(@NonNull TagModel responseBody) {

                binding.shimmerLayout.setVisibility(View.GONE);
                String userType = loginPrefs.getUserType();
                List<TagTermResult> accordingToTeacher = new ArrayList<>();
                List<TagTermResult> accordingToStudent = new ArrayList<>();
                List<TagTermResult> accordingNoUserType = new ArrayList<>();
                if (responseBody != null) {
                    if (responseBody.getTerms() != null) {
                        for (TagTermResult tagTermResult : responseBody.getTerms()) {
                            if (tagTermResult.getTerm().toLowerCase().contains("teacher")) {
                                accordingToTeacher.add(tagTermResult);
                            }
                            if (tagTermResult.getTerm().toLowerCase().contains("student")) {
                                accordingToStudent.add(tagTermResult);
                            }
                            if (!tagTermResult.getTerm().toLowerCase().contains("student") &&
                                    !tagTermResult.getTerm().toLowerCase().contains("teacher")) {
                                accordingNoUserType.add(tagTermResult);
                            }
                        }
                        if (userType != null) {
                            if (userType.contains("teacher")) {
                                accordingToTeacher.addAll(accordingNoUserType);
                                tagTermResultList=accordingToTeacher;
                                //floatingActionButton.show();
                                initializeIntentData();
                                menuItem.setVisible(true);
                               // setFocusOnMic();
                                utterCheck();
                                tagsAdapter.setTags(accordingToTeacher, userType);
                                if (accordingToTeacher == null) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                } else if (accordingToTeacher.size() == 0) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                }
                            } else {
                                accordingToStudent.addAll(accordingNoUserType);
                                tagTermResultList=accordingToStudent;
                                //floatingActionButton.show();
                                initializeIntentData();
                               // setFocusOnMic();
                                menuItem.setVisible(true);
                                utterCheck();
                                tagsAdapter.setTags(accordingToStudent, userType);
                                if (accordingToStudent == null) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                } else if (accordingToStudent.size() == 0) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                }
                            }
                        } else {
                            tagTermResultList=accordingNoUserType;
                            //floatingActionButton.show();

                            initializeIntentData();
                            //setFocusOnMic();
                            menuItem.setVisible(true);
                            utterCheck();
                            tagsAdapter.setTags(accordingNoUserType, userType);
                            if (accordingNoUserType == null) {
                                binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                binding.errorMsgTv.setVisibility(View.VISIBLE);
                                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                            } else if (accordingNoUserType.size() == 0) {
                                binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                binding.errorMsgTv.setVisibility(View.VISIBLE);
                                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                            }
                        }

                    }
                } else {
                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });

    }


    TextToSpeech textToSpeech;

    private void utterCheck(){


    }

    private void setFocusOnMic(){

        if (menuItem != null) {
            View menuItemView = menuItem.getActionView();

            if (menuItemView != null) {

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

                menuItemView.findViewById(R.id.action_view_icon).requestFocus();
                menuItemView.findViewById(R.id.action_view_icon).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);





            }
            else {

                Log.e(TAG, "Action view for menu item is null");
            }
            if (tagTermResultList == null||tagTermResultList.isEmpty()) {
                menuItem.setVisible(false);
                menuItemView.findViewById(R.id.action_view_icon).setVisibility(View.GONE);
            }
        } else {

            Log.e(TAG, "Menu item not found");
        }

    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof TagTermResult) {
            TagTermResult tagTermResult = (TagTermResult) item;
            //environment.getRouter().showProgramsActivity(getActivity(), tagTermResult.getTerm(), "");

          /*  ProgramFragment programFragment = new ProgramFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(PROGRAM, tagTermResult.getTerm());
            bundle1.putString(PROGRAM_UUID, "");
            programFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, programFragment, ProgramFragment.TAG).addToBackStack(ProgramFragment.TAG)
                    .commit();*/
         navigateToAnotherScreen(tagTermResult);
        }
    }


    void sendAnalyticsCourseDetail(TagTermResult tagTermResult){
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.TOPIC_NAME,tagTermResult.getTerm());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.SELECTED_TOPIC,null,null,values);
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
            for(TagTermResult tagTermResult :tagTermResultList){
                String convertedTerm = tagTermResult.getConverted_term();
                if (convertedTerm != null && !convertedTerm.isEmpty()) {
                    if(convertedTerm.equals(valueToIntent))
                    {
                        navigateToAnotherScreen(tagTermResult);
                        break;
                    }

                }
                else{
                    String term = tagTermResult.getTerm();
                    if (term != null && !term.isEmpty()) {
                        if(term.equals(valueToIntent))
                        {
                            navigateToAnotherScreenByChatBot(tagTermResult,true);
                            break;
                        }
                    }
                }

            }
    }

    @Override
    public void onSpeechError(String error) {

    }
    public void onMicButtonClick() {
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        speechToTextHelper = new SpeechToTextHelper(getActivity().getApplicationContext(), this,getActivity());
        textToSpeechHelper.stop();
        speechToTextHelper.startSpeechRecognition();
    }

    public void initializeIntentData(){
        List<String> tags = new ArrayList<>();
        if(chatBotFlag) {
            chatBotTalk();
            chatBotFlag=false;
        }
        if(tagTermResultList!=null||!tagTermResultList.isEmpty()){
            new Handler().postDelayed(() -> {
                setFocusOnMic();

           /* AccessibilityManager accessibilityManager = (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager.isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                event.getText().add("Welcome to the next screen"); // Customize your message
                accessibilityManager.sendAccessibilityEvent(event);
            }*/

        }, 1000);

        }
        for(TagTermResult tagTermResult :tagTermResultList){
            String convertedTerm = tagTermResult.getConverted_term();
            if (convertedTerm != null && !convertedTerm.isEmpty()) {
                tags.add(convertedTerm);
            }
            else{
                String term = tagTermResult.getTerm();
                if (term != null && !term.isEmpty()) {
                    tags.add(term);
                }
            }
        }
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        classifier = new IntentTopicClassifier(tags,selectedLanguage);
    }

    void copyTextDataByLongPress(){
        setGestureListeners(binding.userType);
        setGestureListeners(binding.subjectName);
       /* binding.userType.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy =  binding.userType.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        binding.subjectName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.subjectName.getText().toString();
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

    @Override
    public void navigateToAnotherScreen(Object item) {
        if (item instanceof TagTermResult) {
            TagTermResult tagTermResult = (TagTermResult) item;
            sendAnalyticsCourseDetail(tagTermResult);
            NewProgramFragment newProgramFragment = new NewProgramFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(PROGRAM, tagTermResult.getTerm());
            bundle1.putString(PROGRAM_CONVERTED, tagTermResult.getConverted_term());
            bundle1.putString(PROGRAM_UUID, "");
            bundle1.putBoolean(TAGSCREENFLAG, true);
            newProgramFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, newProgramFragment, NewProgramFragment.TAG).addToBackStack(NewProgramFragment.TAG)
                    .commit();
        }
    }

    public void navigateToAnotherScreenByChatBot(Object item,boolean chatBotFlag) {
        if (item instanceof TagTermResult) {
            TagTermResult tagTermResult = (TagTermResult) item;
            sendAnalyticsCourseDetail(tagTermResult);
            NewProgramFragment newProgramFragment = new NewProgramFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(PROGRAM, tagTermResult.getTerm());
            bundle1.putString(PROGRAM_CONVERTED, tagTermResult.getConverted_term());
            bundle1.putString(PROGRAM_UUID, "");
            bundle1.putBoolean(TAGSCREENFLAG, true);
            bundle1.putBoolean(CHATBOTFLAG,chatBotFlag);
            newProgramFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, newProgramFragment, NewProgramFragment.TAG).addToBackStack(NewProgramFragment.TAG)
                    .commit();
        }
    }

    public void chatBotProgramTalk(){
        textToSpeechHelper.stop();

        if(!subject.isEmpty()) {
            String valueToSpeak = checkMessageSubjectLanguage(subject);
        }
    }

    private String checkMessageSubjectLanguage(String Subject) {
        String baseString = getResources().getString(R.string.user_intent_subject_message);
        String formattedString = baseString.replace("%1$s", String.valueOf(Subject));
        return formattedString;
    }

    public void chatBotTalk(){
        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        textToSpeechHelper.stop();
        String valueToSpeak = checkMessageLanguage(tagTermResultList.size(),selectedLanguage);
        textToSpeechHelper.speakText(valueToSpeak);
    }

    private String checkMessageLanguage(int messageCode,String selectedLanguage) {

            String baseString = getResources().getString(R.string.user_intent_topic_found);

            String formattedString = baseString.replace("%1$s", String.valueOf(messageCode));
            return formattedString;

    }

    @Override
    public void onDoneTalkBackListener() {
        //setFocusOnMic();
    }

}
