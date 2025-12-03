package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ActionMenuView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.Chatbot.IntentClassifier.IntentSubjectClassifier;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextHelper;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.comparator.TalkBackDetector.TalkBackDetector;
import org.edx.mobile.Chatbot.TextToSpeechHelper.TextToSpeechHelper;
import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentExploreCourseBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.DiscoverySubject;
import org.edx.mobile.discovery.model.DiscoverySubjectResult;
import org.edx.mobile.discovery.model.OrganisationList;
import org.edx.mobile.discovery.model.OrganisationModel;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.model.TagTermResult;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.NewSubjectAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.OrganisationAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

import static android.app.Activity.RESULT_OK;
import static org.edx.mobile.view.ProgramActivity.ORGANISATION_SCREEN_FLAG;
import static org.edx.mobile.view.ProgramActivity.PROGRAM;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_CONVERTED;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_UUID;
import static org.edx.mobile.view.ProgramActivity.TAGSCREENFLAG;
import static org.edx.mobile.view.TagsFragmentActivity.COLOR_CODE;
import static org.edx.mobile.view.TagsFragmentActivity.SUBJECT;
import static org.edx.mobile.view.TagsFragmentActivity.UID;
import static org.edx.mobile.view.TagsFragmentActivity.chatBotFlag;

public class ExploreFragment extends BaseFragment implements OnRecyclerItemClickListener, SpeechToTextListener, OnNavigateListener, TalkBackListener {
    public static final String TAG = ExploreFragment.class.getCanonicalName();
    private SpeechToTextHelper speechToTextHelper;
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    private LoginAPI loginAPI;
    @Inject
    CourseApi courseApi;
    private SoundPool soundPool;
    private int soundId;
    private FragmentExploreCourseBinding binding;
    private NewSubjectAdapter newSubjectAdapter;
    private OrganisationAdapter organisationAdapter;

    IntentSubjectClassifier classifier;


    private TextToSpeechHelper textToSpeechHelper;

    @Inject
    protected IEdxEnvironment environment;

    DiscoverySubject responseBodyDiscoverySubject=null;
    private ClipboardService clipboardService ;

    MenuItem menuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore_course, container,
                false);

        clipboardService = ClipboardServiceHolder.getClipboardService(getActivity().getApplicationContext());

        newSubjectAdapter = new NewSubjectAdapter(getActivity(), ExploreFragment.this::onItemClick,this::navigateToAnotherScreen);


        textToSpeechHelper = new TextToSpeechHelper(getActivity().getApplicationContext(),getActivity(),this::onDoneTalkBackListener);

        TalkBackDetector talkBackDetector = new TalkBackDetector(getActivity().getApplicationContext());

        if (talkBackDetector.isTalkBackEnabled()) {
           System.out.println("talkback enable ");
        } else {
            System.out.println("talkback disable ");
        }
        /*if(!loginPrefs.getUserVoiceDialogEnabled()){
            //showDialog();
            loginPrefs.storeUserVoiceDialogEnabled(true);
        }*/
        if(menuItem!=null){
            menuItem.setVisible(true);
        }
        copyTextDataByLongPress();
        setHasOptionsMenu(true);
        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getContext(), R.raw.beep_sound_2, 1);

        return binding.getRoot();
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.menu_item_voice);
        menuItem=item;

        if (item != null) {
            View menuItemView = item.getActionView();

            if (menuItemView != null) {
                menuItemView.findViewById(R.id.action_view_icon).requestFocus();
                menuItemView.findViewById(R.id.action_view_icon).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {

                Log.e(TAG, "Action view for menu item is null");
            }
            if (responseBodyDiscoverySubject == null) {
                item.setVisible(false);
            }
        } else {

            Log.e(TAG, "Menu item not found");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_item_voice) {
            onMicButtonClick();
            return true;
        } else {
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
       // tagsFragment = new TagsFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        binding.shimmerLayout.startShimmer();
        super.onResume();
        binding.progressBar.setVisibility(View.VISIBLE);
        try {
            getExploreSubjects();
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
    private void getExploreSubjects() throws Exception {
        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
        long millis = System.currentTimeMillis();
        long tokenTime = millis - responseJwt.creation_time;
        if (tokenTime > responseJwt.expires_in) {
            createToken();
        } else {
            getSubjects();
            getOrganisations();
        }
    }

    private void createToken() throws Exception {
        DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                getSubjects();
                getOrganisations();
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

    private void getSubjects() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (getActivity()!=null){
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }

        Call<DiscoverySubject> discoveryCourse = courseApi.getDiscoverySubjects(token,selectedLanguage);
        discoveryCourse.enqueue(new DiscoveryCallback<DiscoverySubject>() {
            @Override
            protected void onResponse(@NonNull DiscoverySubject responseBody) {
                binding.progressBar.setVisibility(View.GONE);
                if (responseBody != null) {
                    Log.d("DiscoveryApi", String.valueOf(responseBody.getCount()));
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                    binding.rvExploreCourse.setLayoutManager(mLayoutManager);
                    binding.rvExploreCourse.setAdapter(newSubjectAdapter);
                    List<DiscoverySubjectResult> discoverySubjects = new ArrayList<>();

                /*    discoverySubjects.addAll(responseBody.getResults());
                    discoverySubjects.addAll(responseBody.getResults());
                    discoverySubjects.addAll(responseBody.getResults());
                    discoverySubjects.addAll(responseBody.getResults());
                    discoverySubjects.addAll(responseBody.getResults());
                    discoverySubjects.addAll(responseBody.getResults());
                    discoverySubjects.addAll(responseBody.getResults());*/
                    newSubjectAdapter.setSubjects(responseBody.getResults());
                    binding.shimmerLayout.setVisibility(View.GONE);

                    responseBodyDiscoverySubject=responseBody;
                    initializeIntentData();
                    if(menuItem!=null) {
                        menuItem.setVisible(true);
                    }
                   setFocusOnMic();
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });
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
            } else {

                Log.e(TAG, "Action view for menu item is null");
            }
            if (responseBodyDiscoverySubject == null) {
                menuItem.setVisible(false);
            }
        } else {

            Log.e(TAG, "Menu item not found");
        }

    }

    private void getOrganisations() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (getActivity()!=null){
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }

        Call<OrganisationList> discoveryOrganisation = courseApi.getOrganisations(token,selectedLanguage);
        discoveryOrganisation.enqueue(new DiscoveryCallback<OrganisationList>() {
            @Override
            protected void onResponse(@NonNull OrganisationList responseBody) {
                if (responseBody != null) {
                    organisationAdapter = new OrganisationAdapter(getActivity(), ExploreFragment.this::onItemClick);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                    binding.rvOrganisation.setLayoutManager(mLayoutManager);
                    binding.rvOrganisation.setAdapter(organisationAdapter);
                    organisationAdapter.setOrganisation(responseBody.getResults());
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof DiscoverySubjectResult) {

            DiscoverySubjectResult discoverySubjectResult = (DiscoverySubjectResult) item;
            sendAnalyticsCourseDetail(discoverySubjectResult);
            navigateToAnotherScreen(discoverySubjectResult);
           /* environment.getRouter().showTagsActivity(getActivity(), discoverySubjectResult.getName(),
                    discoverySubjectResult.getCardColorName());*/

        }
        else if(item instanceof OrganisationModel){
            OrganisationModel organisationModel= (OrganisationModel) item;
            openOrganisationDetails(organisationModel);
        }
    }
    void sendAnalyticsCourseDetail(DiscoverySubjectResult discoverySubjectResult){
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.SUBJECT_NAME,discoverySubjectResult.getName());
        values.put(Analytics.Keys.SUBJECT_UID,discoverySubjectResult.getUuid());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.SUBJECT_SELECTED,null,null,values);
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

        if(valueToAction.equals("true")) {
            for (DiscoverySubjectResult discoverySubjectResult : responseBodyDiscoverySubject.getResults()) {
                if (discoverySubjectResult.getName().equals(valueToIntent)) {
                    navigateToAnotherScreen(discoverySubjectResult,true);
                    break;
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

        List<String> subjects = new ArrayList<>();
        for (int index = 0; index < responseBodyDiscoverySubject.getResults().size(); index++) {
            DiscoverySubjectResult discoverySubjectResult = responseBodyDiscoverySubject.getResults().get(index);
            String discoverySubjectResultName = discoverySubjectResult.getName();
            if (!discoverySubjectResultName.isEmpty()) {
                subjects.add(discoverySubjectResultName);

            }
        }

        String selectedLanguage = "en";
        if (getActivity() != null) {
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }
        classifier = new IntentSubjectClassifier(subjects,selectedLanguage);
    }




    void copyTextDataByLongPress(){
        setGestureListeners(binding.exploreSubject);
    }

    private void setGestureListeners(TextView textView) {
        GestureListener gestureListener = new GestureListener( textView,null,getContext(),this::navigateToAnotherScreen);
        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }


    @Override
    public void navigateToAnotherScreen(Object item) {
      if (item instanceof DiscoverySubjectResult) {
          navigateToAnotherScreen(item,false);
           /* DiscoverySubjectResult discoverySubjectResult = (DiscoverySubjectResult) item;
            sendAnalyticsCourseDetail(discoverySubjectResult);
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
            TagsFragment tagsFragment = new TagsFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(SUBJECT, discoverySubjectResult.getName());
            bundle1.putString(COLOR_CODE, String.valueOf(discoverySubjectResult.getCardColorName()));
            bundle1.putString(UID, String.valueOf(discoverySubjectResult.getUuid()));
            bundle1.putBoolean(chatBotFlag,false );
            tagsFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, tagsFragment, TagsFragment.TAG).addToBackStack(TagsFragment.TAG)
                    .commit();*/
        }
    }
    public void navigateToAnotherScreen(Object item,Boolean chatBotFlagValue) {
        if (item instanceof DiscoverySubjectResult) {


            DiscoverySubjectResult discoverySubjectResult = (DiscoverySubjectResult) item;
            sendAnalyticsCourseDetail(discoverySubjectResult);
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
          //  MainBottomDashboardFragment.backIcon().setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            TagsFragment tagsFragment = new TagsFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(SUBJECT, discoverySubjectResult.getName());
            bundle1.putString(COLOR_CODE, String.valueOf(discoverySubjectResult.getCardColorName()));
            bundle1.putString(UID, String.valueOf(discoverySubjectResult.getUuid()));
            bundle1.putBoolean(chatBotFlag,chatBotFlagValue );
            tagsFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, tagsFragment, TagsFragment.TAG).addToBackStack(TagsFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onDoneTalkBackListener() {

    }

    private void openOrganisationDetails(@NonNull OrganisationModel organisationModel) {
        if (getActivity() == null) {
            return;
        }
        String displayName = !TextUtils.isEmpty(organisationModel.getName())
                ? organisationModel.getName()
                : organisationModel.getKey();
        if (MainBottomDashboardFragment.suodhaIcon() != null) {
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
        }
        if (MainBottomDashboardFragment.backIcon() != null) {
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
        }
        OrganisationDetailsFragment fragment = OrganisationDetailsFragment.newInstance(
                organisationModel.getUuid(), displayName
        );
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, fragment, OrganisationDetailsFragment.TAG)
                .addToBackStack(OrganisationDetailsFragment.TAG)
                .commit();
    }
}
