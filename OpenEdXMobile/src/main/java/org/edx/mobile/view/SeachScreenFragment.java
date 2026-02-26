package org.edx.mobile.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;
import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.authentication.ApiLmsService;
import org.edx.mobile.authentication.ApiNewLmsClient;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentSearchScreenBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.CombinationOfSeachResult;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.model.SearchResult;
import org.edx.mobile.discovery.model.SearchResultModel;
import org.edx.mobile.discovery.model.TranslatedAudioResponse;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.ParticularCourseTask;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.SearchListAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import static android.app.Activity.RESULT_OK;

public class SeachScreenFragment extends BaseFragment implements OnRecyclerItemClickListener, OnNavigateListener {
    public static final String TAG = SeachScreenFragment.class.getCanonicalName();
    private FragmentSearchScreenBinding binding;
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    CourseApi courseApi;
    private SearchListAdapter searchListAdapter;
    private int page = 1;

    private SoundPool soundPool;
    private int soundId;
    @Inject
    LoginAPI loginAPI;
    @Inject
    protected IEdxEnvironment environment;
    ClipboardService clipboardService;
    public static SeachScreenFragment newInstance(@Nullable Bundle bundle) {
        final SeachScreenFragment fragment = new SeachScreenFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void showSoftKeyboard(View view){
        if(view.requestFocus()){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        clipboardService = ClipboardServiceHolder.getClipboardService(getActivity().getApplicationContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_screen, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.announceForAccessibility("Search Screen");
        binding.shimmerLayout.setVisibility(View.GONE);
      /*  if (binding.editSearch.isFocused()) {
            binding.editSearch.setCursorVisible(true);
        }*/
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().onBackPressed();
                Intent intent
                        = new Intent(getActivity(), MainBottomDashboardFragment.class);
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (binding.editSearch.getText().toString().length() > 0) {
                    binding.searchResults.setVisibility(View.VISIBLE);
                    binding.shimmerLayout.setVisibility(View.VISIBLE);
                    binding.searchResult.setVisibility(View.GONE);
                    binding.searchCount.setVisibility(View.GONE);
                    getSearchResult(binding.editSearch.getText().toString());
                } else {
                    Toast.makeText(getActivity(), "Nothing to search", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            return false;
        });
        binding.editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.seachIcon.getWindowToken(), 0); //Do whatever you intend to do when user click on search button in keyboard.
                    if (binding.editSearch.getText().toString().length() > 0) {

                        binding.searchResults.setVisibility(View.VISIBLE);
                        binding.shimmerLayout.setVisibility(View.VISIBLE);
                        binding.searchResult.setVisibility(View.GONE);
                        binding.searchCount.setVisibility(View.GONE);
                        getSearchResult(binding.editSearch.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Nothing to search", Toast.LENGTH_LONG).show();
                    }
                }

                return true;
            }

        });
        binding.seachIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.seachIcon.getWindowToken(), 0);
                if (binding.editSearch.getText().toString().length() > 0) {

                    binding.searchResults.setVisibility(View.VISIBLE);
                    binding.shimmerLayout.setVisibility(View.VISIBLE);
                    binding.searchResult.setVisibility(View.GONE);
                    binding.searchCount.setVisibility(View.GONE);
                    getSearchResult(binding.editSearch.getText().toString());
                } else {
                    Toast.makeText(getActivity(), "Nothing to search", Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.viewMoreResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSearchNextResult(binding.editSearch.getText().toString());
            }
        });

        searchListAdapter = new SearchListAdapter(getActivity(), SeachScreenFragment.this::onItemClick,this::navigateToAnotherScreen);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.searchResult.setLayoutManager(mLayoutManager);
        binding.searchResult.setAdapter(searchListAdapter);
        copyTextDataByLongPress();

        // search with audio feature start
        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getContext(), R.raw.beep_sound_2, 1);

        binding.micIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkMicPermission()) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                    return;
                }
                currentAudioTranscription = null;
                isRecording = false;
                showVoiceSearchDialog();
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
    }

    /**
     * Checks if TalkBack is enabled
     * @return true if TalkBack is enabled, false otherwise
     */
    private boolean isTalkBackEnabled() {
        if (getActivity() == null) return false;
        AccessibilityManager am = (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am != null && am.isEnabled() && am.isTouchExplorationEnabled();
    }

    /**
     * Announces message for TalkBack users only
     * @param message The message to announce
     */
    private void announceForTalkBack(String message) {
        if (isTalkBackEnabled() && getView() != null) {
            // Use Handler to ensure announcement happens after UI updates
            new Handler().postDelayed(() -> {
                if (getView() != null) {
                    getView().announceForAccessibility(message);
                }
            }, 100);
        }
    }

    /**
     * Updates the enabled/disabled state of Search and Stop buttons based on current state
     * @param searchButtonEnabled true if search button should be enabled (has transcription)
     * @param stopButtonEnabled true if stop button should be enabled (is recording)
     */
    private void updateButtonStates(boolean searchButtonEnabled, boolean stopButtonEnabled) {
        if (voiceDialog != null && voiceDialog.isShowing()) {
            Button positiveButton = voiceDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = voiceDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setEnabled(searchButtonEnabled);
            }
            if (negativeButton != null) {
                negativeButton.setEnabled(stopButtonEnabled);
            }
        }
    }

    /**
     * Calculates reading time based on text length.
     * Average reading speed: ~150-200 words per minute (2.5-3 words/second)
     * We use a conservative estimate of ~2.5 words/second for TalkBack
     * This provides a more accurate delay than a fixed time.
     */
    private long calculateReadingTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 2000; // Default 2 seconds for empty text
        }
        
        // Count words (split by spaces)
        String[] words = text.trim().split("\\s+");
        int wordCount = words.length;
        
        // Calculate time: ~2.5 words per second (conservative estimate for TalkBack)
        // Add extra time for punctuation, pauses, and processing
        long estimatedTime = (long) (wordCount / 2.5 * 1000); // Convert to milliseconds
        
        // Add base time for processing and minimum reading time
        estimatedTime += 1000; // Add 1 second base time
        
        // Set reasonable bounds: minimum 2 seconds, maximum 8 seconds
        return Math.max(2000, Math.min(8000, estimatedTime));
    }

    /**
     * Moves focus to the Speak button after a delay for TalkBack users
     * when transcription fails or is not understood.
     * First focuses on descriptionView to let TalkBack read the error message,
     * then moves focus to Speak button after TalkBack finishes reading.
     * Uses dynamic delay calculation based on text length instead of fixed delay.
     */
    private void moveFocusToSpeakButton() {
        if (isTalkBackEnabled() && neutralButton != null && descriptionView != null) {
            // Get the text from descriptionView to calculate reading time
            String errorText = getString(R.string.transcription_not_understood);
            
            // Calculate reading time dynamically based on text length
            long readingTime = calculateReadingTime(errorText);
            
            // First, announce and focus on descriptionView to let TalkBack read the error message
            // Use announceForAccessibility which properly queues the announcement
            if (voiceDialog != null && voiceDialog.isShowing() && descriptionView != null) {
                descriptionView.setFocusable(true);
                descriptionView.setFocusableInTouchMode(true);
                descriptionView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                
                // Announce the error message first (this queues properly)
                descriptionView.announceForAccessibility(errorText);
                
                // Then request focus after a short delay to ensure announcement is queued
                new Handler().postDelayed(() -> {
                    if (voiceDialog != null && voiceDialog.isShowing() && descriptionView != null) {
                        descriptionView.requestFocus();
                        //descriptionView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                        
                        // After calculated reading time, move focus to Speak button
                        new Handler().postDelayed(() -> {
                            if (voiceDialog != null && voiceDialog.isShowing() && neutralButton != null) {
                                neutralButton.setFocusable(true);
                                neutralButton.setFocusableInTouchMode(true);
                                neutralButton.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                                
                                // Remove focus background/highlight to prevent filled grey color
                                neutralButton.setBackground(null);
                                neutralButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                                
                                neutralButton.requestFocus();
                                //neutralButton.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                                
                                updateButtonStates(false, false);
                            }
                        }, readingTime); // Dynamic delay based on text length
                    }
                }, 300); // Short delay to ensure announcement is queued
            }
        }
    }

    private void getSearchResult(String query) {
        announceForTalkBack(getString(R.string.please_wait));
        sendAnalyticsCourseDetail(query);

        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        page = 1;
        Call<SearchResult> search = courseApi.getSearchNextResult(token, selectedLanguage, String.valueOf(page), "100", query.trim());
        final String finalQuery = query.trim();
        final String finalSelectedLanguage = selectedLanguage;
        search.enqueue(new DiscoveryCallback<SearchResult>() {
            @Override
            protected boolean onUnauthorized(Call<SearchResult> call) {
                if (getContext() == null) return false;
                try {
                    DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
                        @Override
                        public void onSuccess(@NonNull AuthResponseJwt result) {
                            // Retry with new token
                            String newToken = loginPrefs.getAuthorizationHeaderJwt();
                            Call<SearchResult> retryCall = courseApi.getSearchNextResult(newToken, finalSelectedLanguage, String.valueOf(page), "100", finalQuery);
                            retryCall(retryCall);
                        }

                        @Override
                        public void onException(Exception ex) {
                            ResponseError error = new ResponseError(null);
                            if (error != null) {
                                error.setMsg("Token refresh failed");
                            }
                            onFailure(error, ex);
                        }
                    };
                    discoveryTask.execute();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onResponse(@NonNull SearchResult responseBody) {
                List<CombinationOfSeachResult> combinationOfSeachResults = new ArrayList<>();
                List<CombinationOfSeachResult> newcombinationOfSeachResults = new ArrayList<>();
                binding.shimmerLayout.setVisibility(View.GONE);
                if (responseBody != null) {
                    if (responseBody.getNext() != null) {
                        page = page + 1;
                        binding.viewMoreResults.setVisibility(View.VISIBLE);
                    } else {
                        binding.viewMoreResults.setVisibility(View.GONE);
                    }
                    for (SearchResultModel searchResultList : responseBody.getResults()) {
                        if (searchResultList.getProgramDetails() != null) {
                            if (searchResultList.getProgramDetails().getTags() != null) {
                                for (SearchResultModel.ProgramDetails.Tag searchTags : searchResultList.getProgramDetails().getTags()) {
                                    if (searchTags.getTags() != null) {
                                        for (String tag : searchTags.getTags()) {
                                            CombinationOfSeachResult combinationOfSeachResult = new CombinationOfSeachResult();
                                            combinationOfSeachResult.setCourseName(searchResultList.getCourseName());
                                            combinationOfSeachResult.setUnitName(searchResultList.getUnitName());
                                            combinationOfSeachResult.setProgramName(searchTags.getProgramName());
                                            combinationOfSeachResult.setProgram_id(searchTags.getProgramId());
                                            combinationOfSeachResult.setTagName(tag);

                                            // Extracting language from the first CourseRun
                                            if (searchResultList.getCourseLang() != null && !searchResultList.getCourseLang().isEmpty()) {
                                                String language = searchResultList.getCourseLang();
                                                combinationOfSeachResult.setLanguage(language);
                                            }
                                            // new field added
                                            combinationOfSeachResult.setCourse_id(searchResultList.getCourseId());
                                            combinationOfSeachResult.setUnit_id(searchResultList.getUnit_id());
                                            combinationOfSeachResult.setIs_enroll(searchResultList.isIs_enroll());

                                            combinationOfSeachResults.add(combinationOfSeachResult);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                if (combinationOfSeachResults != null && combinationOfSeachResults.size() > 0) {
                    String userType = loginPrefs.getUserType();
                    if (userType != null) {
                        if (userType.toLowerCase().equals("teacher")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                } else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        } else if (userType.toLowerCase().equals("student")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("student")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        }
                    } else {
                        newcombinationOfSeachResults.clear();
                        for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                            if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                    !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                newcombinationOfSeachResults.add(combinationOfSeachResult);
                            }
                        }
                    }
                }
                binding.searchResult.setVisibility(View.VISIBLE);
                searchListAdapter.setSearchResult(newcombinationOfSeachResults);
                binding.searchCount.setVisibility(View.VISIBLE);
                String searchQuery = binding.editSearch.getText().toString().trim();
                String countText = String.valueOf(searchListAdapter.getItemCount()) + " " + getString(R.string.results_for) + " " + searchQuery;
                binding.searchCount.setText(countText);
                
                // Set focus on search result after announcements
                if (isTalkBackEnabled()) {
                        if (binding.searchResults != null && binding.searchResults.getVisibility() == View.VISIBLE) {
                            binding.searchResults.setFocusable(true);
                            binding.searchResults.setFocusableInTouchMode(true);
                            binding.searchResults.requestFocus();
                            binding.searchResults.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                            //announceForTalkBack(getString(R.string.search_result));
                            announceForTalkBack(countText);
                        }
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });

    }

    private void getSearchNextResult(String query) {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        Call<SearchResult> search = courseApi.getSearchNextResult(token, selectedLanguage, String.valueOf(page), "10", query.trim());
        final String finalQuery = query.trim();
        final String finalSelectedLanguage = selectedLanguage;
        final int currentPage = page;
        search.enqueue(new DiscoveryCallback<SearchResult>() {
            @Override
            protected boolean onUnauthorized(Call<SearchResult> call) {
                if (getContext() == null) return false;
                try {
                    DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
                        @Override
                        public void onSuccess(@NonNull AuthResponseJwt result) {
                            // Retry with new token
                            String newToken = loginPrefs.getAuthorizationHeaderJwt();
                        Call<SearchResult> retryCall = courseApi.getSearchNextResult(newToken, finalSelectedLanguage, String.valueOf(currentPage), "10", finalQuery);
                            retryCall(retryCall);
                        }

                        @Override
                        public void onException(Exception ex) {
                            ResponseError error = new ResponseError(null);
                            if (error != null) {
                                error.setMsg("Token refresh failed");
                            }
                            onFailure(error, ex);
                        }
                    };
                    discoveryTask.execute();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onResponse(@NonNull SearchResult responseBody) {
                List<CombinationOfSeachResult> combinationOfSeachResults = new ArrayList<>();
                List<CombinationOfSeachResult> newcombinationOfSeachResults = new ArrayList<>();
                if (responseBody != null) {
                    if (responseBody.getNext() != null) {
                        page = page + 1;
                        binding.viewMoreResults.setVisibility(View.VISIBLE);
                    } else {
                        binding.viewMoreResults.setVisibility(View.GONE);
                    }

                    for (SearchResultModel searchResultList : responseBody.getResults()) {
                        if (searchResultList.getProgramDetails() != null) {
                            if (searchResultList.getProgramDetails().getTags() != null) {
                                for (SearchResultModel.ProgramDetails.Tag searchTags : searchResultList.getProgramDetails().getTags()) {
                                    if (searchTags.getTags() != null) {
                                        for (String tag : searchTags.getTags()) {
                                            CombinationOfSeachResult combinationOfSeachResult = new CombinationOfSeachResult();
                                            combinationOfSeachResult.setCourseName(searchResultList.getCourseName());
                                            combinationOfSeachResult.setProgramName(searchTags.getProgramName());
                                            combinationOfSeachResult.setProgram_id(searchTags.getProgramId());
                                            combinationOfSeachResult.setTagName(tag);

                                            // Extracting language from the first CourseRun
                                            if (searchResultList.getCourseLang() != null && !searchResultList.getCourseLang().isEmpty()) {
                                                String language = searchResultList.getCourseLang();
                                                combinationOfSeachResult.setLanguage(language);
                                            }

                                            // new field added
                                            combinationOfSeachResult.setCourse_id(searchResultList.getCourseId());
                                            combinationOfSeachResult.setUnit_id(searchResultList.getUnit_id());
                                            combinationOfSeachResult.setIs_enroll(searchResultList.isIs_enroll());

                                            combinationOfSeachResults.add(combinationOfSeachResult);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                if (combinationOfSeachResults != null && combinationOfSeachResults.size() > 0) {
                    String userType = loginPrefs.getUserType();
                    if (userType != null) {
                        if (userType.toLowerCase().equals("teacher")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                } else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        } else if (userType.toLowerCase().equals("student")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("student")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                } else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        }
                    } else {
                        newcombinationOfSeachResults.clear();
                        for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                            if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                    !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                newcombinationOfSeachResults.add(combinationOfSeachResult);
                            }
                        }
                    }
                }
                searchListAdapter.updateSearchResult(newcombinationOfSeachResults);
                binding.searchCount.setText(String.valueOf(searchListAdapter.getItemCount()) +
                        " " + "results for " + binding.editSearch.getText().toString().trim());
                binding.searchCount.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });

    }

    @Override
    public void onResume() {
        //  binding.shimmerLayout.startShimmer();
        super.onResume();
    }

    @Override
    public void onPause() {
        //  binding.shimmerLayout.stopShimmer();
        super.onPause();
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof CombinationOfSeachResult) {
            CombinationOfSeachResult combinationOfSeachResult = (CombinationOfSeachResult) item;
            sendAnalyticsCourseDetail(combinationOfSeachResult);
            if (combinationOfSeachResult.isIs_enroll()) {
                // goto unit page directly if enrolled
                getParticularCourse(combinationOfSeachResult.getCourse_id(), combinationOfSeachResult.getUnit_id());
            }else {
                environment.getRouter().showProgramsActivity(getActivity(), combinationOfSeachResult.getTagName(), /*combinationOfSeachResult.getProgram_id()*/ combinationOfSeachResult.getProgram_id());
            }
//            NewProgramFragment newProgramFragment = new NewProgramFragment();
//            Bundle bundle1 = new Bundle();
//            bundle1.putString(PROGRAM, combinationOfSeachResult.getProgramName());
//            bundle1.putString(PROGRAM_CONVERTED, combinationOfSeachResult.getTagName());
//            bundle1.putString(PROGRAM_UUID, combinationOfSeachResult.getProgram_id());
//            newProgramFragment.setArguments(bundle1);
//            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.main_fragment, newProgramFragment, NewProgramFragment.TAG).addToBackStack(NewProgramFragment.TAG)
//                    .commit();

        }
    }
    void sendAnalyticsCourseDetail(String search){
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.SEARCH_STRING,search);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.COURSES_SEARCH,null,null,values);
    }
    void sendAnalyticsCourseDetail(CombinationOfSeachResult combinationOfSeachResult ){
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.NAME,combinationOfSeachResult.getProgramName());
        values.put(Analytics.Keys.Uid,combinationOfSeachResult.getProgram_id());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.DISCOVERY_COURSES_SEARCH,null,null,values);
    }
    void copyTextDataByLongPress(){
        setGestureListeners(binding.searchResults);
        setGestureListeners(binding.searchCount);
        binding.searchCount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.searchCount.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
    private void setGestureListeners(TextView textView) {
        GestureListener gestureListener = new GestureListener( textView,null,getContext(),this::navigateToAnotherScreen);
        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }
    @Override
    public void navigateToAnotherScreen(Object item) {
        if (item instanceof CombinationOfSeachResult) {
            CombinationOfSeachResult combinationOfSeachResult = (CombinationOfSeachResult) item;
            sendAnalyticsCourseDetail(combinationOfSeachResult);
            if (combinationOfSeachResult.isIs_enroll()) {
                // goto unit page directly if enrolled
                getParticularCourse(combinationOfSeachResult.getCourse_id(), combinationOfSeachResult.getUnit_id());
            }else {
                environment.getRouter().showProgramsActivity(getActivity(), combinationOfSeachResult.getTagName(), /*combinationOfSeachResult.getProgram_id()*/ combinationOfSeachResult.getProgram_id());
            }
        }
    }

    String currentAudioTranscription;
    private void getAudioResponse(File audioFile) {
        /*new Handler().postDelayed(() -> {
            currentAudioTranscription = null;
            if(descriptionView!=null)
               descriptionView.setText(currentAudioTranscription==null || currentAudioTranscription.trim().isEmpty()?getString(R.string.no_transcription_found):currentAudioTranscription);

        }, 100);*/

        //sendAnalyticsCourseDetail(query);
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mp3"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);
        ApiNewLmsClient apiNewLmsClient=new ApiNewLmsClient(loginAPI.config);
        ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);

        Call<TranslatedAudioResponse> call = apiService.uploadAudioFile(token, selectedLanguage, loginPrefs.getUsername(), audioPart);
        final String finalSelectedLanguage = selectedLanguage;
        final File finalAudioFile = audioFile;
        call.enqueue(new DiscoveryCallback<TranslatedAudioResponse>() {
            @Override
            protected boolean onUnauthorized(Call<TranslatedAudioResponse> call) {
                if (getContext() == null) return false;
                try {
                    DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
                        @Override
                        public void onSuccess(@NonNull AuthResponseJwt result) {
                            // Retry with new token
                            String newToken = loginPrefs.getAuthorizationHeaderJwt();
                            RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mp3"), finalAudioFile);
                            MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", finalAudioFile.getName(), requestFile);
                            ApiNewLmsClient apiNewLmsClient = new ApiNewLmsClient(loginAPI.config);
                            ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);
                            Call<TranslatedAudioResponse> retryCall = apiService.uploadAudioFile(newToken, finalSelectedLanguage, loginPrefs.getUsername(), audioPart);
                            retryCall(retryCall);
                        }

                        @Override
                        public void onException(Exception ex) {
                            if(descriptionView != null) {
                                descriptionView.setText(getString(R.string.no_transcription_found));
                            }
                            ResponseError error = new ResponseError(null);
                            if (error != null) {
                                error.setMsg("Token refresh failed");
                            }
                            onFailure(error, ex);
                        }
                    };
                    discoveryTask.execute();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onResponse(@NonNull TranslatedAudioResponse response) {
                // Enable Speak button when recording stops
                neutralButton.setEnabled(true);
                if (response!=null) {
                    if(response.getText()!=null && !response.getText().trim().isEmpty()) {
                        currentAudioTranscription = response.getText().trim();
                        if(descriptionView!=null && currentAudioTranscription != null && !currentAudioTranscription.trim().isEmpty()) {
                           currentAudioTranscription = removeStopAtEnd(currentAudioTranscription);
                            descriptionView.setText(currentAudioTranscription == null || currentAudioTranscription.trim().isEmpty() ? getString(R.string.transcription_not_understood) : currentAudioTranscription);
                            // starting focus and accessible descriptionView
                            descriptionView.clearFocus();
                            descriptionView.setFocusable(true);
                            descriptionView.setFocusableInTouchMode(true);
                            descriptionView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                            descriptionView.requestFocus();
                            // Update button states: Search enabled (has transcription), Stop disabled (not recording)
                            updateButtonStates(true, false);
                        } else {
                            // Response text is empty or null - show error message
                            if(descriptionView!=null) {
                                descriptionView.setText(getString(R.string.transcription_not_understood));
                            }
                            moveFocusToSpeakButton();
                        }
                    } else {
                        // Response is null or text is null/empty - show error message
                        if(descriptionView!=null) {
                            descriptionView.setText(getString(R.string.transcription_not_understood));
                        }
                        moveFocusToSpeakButton();
                    }
                }

            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
                if(descriptionView!=null)
                    descriptionView.setText(getString(R.string.transcription_not_understood));
                // Update button states: both disabled (transcription failed, not recording)
                // Enable Speak button in error scenarios so user can try again
                neutralButton.setEnabled(true);
                moveFocusToSpeakButton();
            }
        });

    }


    private MediaRecorder recorder = null;
    private String outputFilePath = "";
    boolean isRecording = false;
    private static final int REQUEST_RECORD_AUDIO = 1001;
    private void startRecording() {
        outputFilePath = requireActivity().getExternalFilesDir(null).getAbsolutePath() + "/search_audio.mp3";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);  // For MP3
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);    // AAC for MP3 compatibility
        recorder.setOutputFile(outputFilePath);
        isRecording = true;
        try {
            recorder.prepare();
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
      /*  new Handler().postDelayed(() -> {
            if(recorder!=null) {
                stopRecording();
                getAudioResponse(new File(outputFilePath));
            }
            }, 5000);*/

    }

    private boolean checkMicPermission() {
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
                isRecording = false;
                currentAudioTranscription =null;
            }
        }catch (Exception e){}
    }

    private AlertDialog voiceDialog;
    private TextView descriptionView; // Class-level to update after transcription
    Button negativeButton;
    Button neutralButton; // Class-level to access in callbacks
    private void showVoiceSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //builder.setTitle(getString(R.string.search_with_voice));

        // ─── Build custom title bar with close button ───────────────────────────────────
        // 1. Create a horizontal container for the title and close button
        LinearLayout titleContainer = new LinearLayout(requireActivity());
        titleContainer.setOrientation(LinearLayout.HORIZONTAL);
        titleContainer.setGravity(Gravity.CENTER_VERTICAL);
        titleContainer.setPadding(60, 50, 30, 10);

        // 2. Create and add the title text
        TextView titleText = new TextView(requireActivity());
        titleText.setText(getString(R.string.search_with_voice));
        titleText.setTextSize(18);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);   // Bold style
        titleText.setLayoutParams(
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );
        titleContainer.addView(titleText);
        ViewCompat.setImportantForAccessibility(titleText, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // 3. Create and add the close (X) button

        // 1. Convert desired dp size to pixels
        int sizeDp = 18; // desired icon size
        float density = requireActivity().getResources().getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * density + 0.5f);

        // 2. Create LayoutParams with exact width/height
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);

        ImageButton closeButton = new ImageButton(requireActivity());
        closeButton.setImageResource(R.drawable.ic_close);
        closeButton.setBackground(null);
        closeButton.setPadding(24, 24, 24, 24);
        closeButton.setContentDescription(getString(R.string.close)); // Add <string name="close">Close</string>
        closeButton.setLayoutParams(lp);
        titleContainer.addView(closeButton);
        ViewCompat.setImportantForAccessibility(closeButton, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // ────────────────────────────────────────────────────────────────────────────────

        // 4. Create main container and description view
        LinearLayout mainContainer = new LinearLayout(requireActivity());
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        descriptionView = new TextView(requireActivity());  // Now class-level
        descriptionView.setText(isRecording ? getString(R.string.listening) : currentAudioTranscription == null || currentAudioTranscription.isEmpty() ? getString(R.string.select_speak_button_to_speak) : currentAudioTranscription);
        // starting focus and accessible false
        /*descriptionView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        descriptionView.setFocusable(false);*/

        descriptionView.setPadding(80, 25, 80, 25);
        builder.setView(descriptionView);
        ViewCompat.setImportantForAccessibility(descriptionView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // Set buttons with null listeners to prevent auto-dismiss
        builder.setPositiveButton(getString(R.string.search_text), null);
        builder.setNegativeButton(getString(R.string.stop), null);
        builder.setNeutralButton(getString(R.string.speak), null);  // Add <string name="back_text">Back</string> to strings.xml


        // 5. Add titleContainer and descriptionView to mainContainer
        mainContainer.addView(titleContainer);
        mainContainer.addView(descriptionView);

        // 6. Set the custom view on the dialog
        builder.setView(mainContainer);

        voiceDialog = builder.create();  // Now class-level
        
        // Prevent dialog from closing when clicking outside
        voiceDialog.setCanceledOnTouchOutside(false);
        voiceDialog.setCancelable(false);
        
        // Set dismiss listener to announce when dialog closes
        voiceDialog.setOnDismissListener(dialog -> {
            if (isTalkBackEnabled()) {
                // Announce dialog closed
                if (binding != null && binding.getRoot() != null) {
                    binding.getRoot().announceForAccessibility(getString(R.string.search_with_voice_dialogue_closed));
                }
            }
        });
        
        voiceDialog.show();

        if (isTalkBackEnabled()) {
            new Handler().postDelayed(() -> {
                if (voiceDialog != null && voiceDialog.isShowing()) {
                    mainContainer.announceForAccessibility(getString(R.string.search_with_voice_dialogue_opened));
                }
            }, 500);
        }
        // Announce dialog opened and set focus to speak button for TalkBack users
        if (isTalkBackEnabled()) {
            new Handler().postDelayed(() -> {
                if (voiceDialog != null && voiceDialog.isShowing()) {
                    ViewCompat.setImportantForAccessibility(titleText, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                    ViewCompat.setImportantForAccessibility(closeButton, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                    ViewCompat.setImportantForAccessibility(descriptionView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                }
            }, 4000);
        }


        // Close button listener
        closeButton.setOnClickListener(v -> {
            stopRecording();
            voiceDialog.dismiss();
        });

        Button positiveButton = voiceDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        negativeButton = voiceDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        neutralButton = voiceDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        positiveButton.setContentDescription(getString(R.string.search_text));
        negativeButton.setContentDescription(getString(R.string.stop));
        neutralButton.setContentDescription(getString(R.string.speak));

        // Apply color selector for enabled/disabled states (darker grey when disabled)
        positiveButton.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.voice_search_button_selector));
        negativeButton.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.voice_search_button_selector));
        neutralButton.setTextColor(ContextCompat.getColorStateList(requireActivity(), R.color.voice_search_button_selector));

        // Speak button always uses grey color (not state-based) to avoid showing filled color when focused
        //neutralButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.edx_brand_primary_accent));
        // Remove background to prevent focus highlight/filled grey color
        //neutralButton.setBackground(null);
       // neutralButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        // Set initial button states: both disabled (no transcription, not recording)
        updateButtonStates(false, false);

        // "Search" button
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentAudioTranscription ==null || currentAudioTranscription.trim().isEmpty()) {
                    Toast.makeText(requireActivity(), getString(R.string.no_transcription_found), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    binding.editSearch.setText(currentAudioTranscription.trim());
                    binding.searchResults.setVisibility(View.VISIBLE);
                    binding.shimmerLayout.setVisibility(View.VISIBLE);
                    binding.searchResult.setVisibility(View.GONE);
                    binding.searchCount.setVisibility(View.GONE);
                    getSearchResult(currentAudioTranscription.trim());
                    stopRecording();
                    voiceDialog.dismiss();
                }catch (Exception e){}
            }
        });

        //  "speak" button
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (!isRecording) {
                currentAudioTranscription = null;
                descriptionView.setText(getString(R.string.listening));
                    stopRecording();
                    startRecording();
                    // Disable Speak button when recording starts
                    neutralButton.setEnabled(false);
                    // Update button states: Stop enabled (recording), Search disabled (no transcription yet)
                    updateButtonStates(false, true);
                    //negativeButton.setText("Stop");
                /*} else {
                    stopRecording();
                    negativeButton.setText("ReSpeak");
                    Toast.makeText(requireActivity(), "Recording stopped", Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        //  "stop" button
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (!isRecording) {
                if(recorder!=null) {
                    currentAudioTranscription = null;
                    descriptionView.setText(getString(R.string.listening));
                    stopRecording();
                    // Update button states: Stop disabled (stopped recording), Search disabled (waiting for transcription)
                    updateButtonStates(false, false);
                    getAudioResponse(new File(outputFilePath));
                }
                //startRecording();
                //negativeButton.setText("Stop");
            }
        });
        // Get AccessibilityManager to check TalkBack status
        AccessibilityManager am = (AccessibilityManager) requireActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
        boolean isTalkBackEnabled = am.isEnabled() && am.isTouchExplorationEnabled();

        // Conditional logic: Delay for TalkBack users, immediate for normal users
     /*   if (isTalkBackEnabled) {
            // For TalkBack: Detect title announcement and delay recording
            new Handler().postDelayed(() -> {
                if (!isRecording) {
                    startRecording();  // Start after delay
                    descriptionView.setText(getString(R.string.listening));
                    // starting focus and accessible false
                    descriptionView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                    descriptionView.setFocusable(true);
                }
            }, 3000);
        }else {
            // For normal users: Start recording immediately
            if (!isRecording) {
                startRecording();
                descriptionView.setText(getString(R.string.listening));
            }
        }*/
    }

    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    EnrolledCoursesResponse courseData;
    public void getParticularCourse(String courseId, String blockId) {
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
                                if (data != null) {
                                    for (EnrolledCoursesResponse enrolledCoursesResponse : data) {
                                        if (enrolledCoursesResponse.getCourse() != null) {
                                            if (enrolledCoursesResponse.getCourse().getId() != null) {
                                                // if (enrolledCoursesResponse.getCourse().getId().equals(resumeCourse.getCourse_id())) {
                                                courseData = enrolledCoursesResponse;
                                                 // redirection to course/unit page directly
                                                if (courseData != null) {
                                                    LocaleManager.setCourseLanguagePref(getContext(),courseData.getCourse().getLanguage());
                                                    if (blockId.contains("sequential")) {

                                                        environment.getRouter().showCourseContainerOutline(SeachScreenFragment.this,
                                                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,
                                                                blockId, null, false);
                                                    } else {
                                                        environment.getRouter().showCourseUnitDetail(SeachScreenFragment.this,
                                                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,
                                                                blockId, false);
                                                    }
                                                } else {
                                                    Toast.makeText(getActivity(), getString(R.string.no_course_info), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    }
                                }
                                Log.d("enrolledCoursesResponse", courseData.getCourse().getId());
                            } else {
                                Log.e("enrolledCoursesResponse", "Response body is null");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("enrolledCoursesResponse", "Exception in onResponse", e);
                    }
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        Log.e("enrolledCoursesResponse", "HttpStatusException in onResponse UNAUTHORIZED", ex);
                    } else {
                        Log.e("enrolledCoursesResponse", "HttpStatusException in onResponse", ex);
                    }
                }
            };
            particularCourseTask.execute();


        } catch (Exception e) {
            Log.e("enrolledCoursesResponse", "Exception in getParticularCourse", e);
        }
    }

    public static String removeStopAtEnd(String input) {
        // (?i) – case-insensitive flag
        // \\b(stop button|stop)\\b – whole word "stop button" or "stop"
        // \\s*$ – any trailing spaces up to end of string
        return input.replaceAll("(?i)\\b(stop button|stop)\\b\\s*$", "").trim();
    }

}
