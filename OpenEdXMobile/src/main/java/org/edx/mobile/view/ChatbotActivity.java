package org.edx.mobile.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;

import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextHelper;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.Chatbot.TextToSpeechHelper.TextToSpeechHelper;
import org.edx.mobile.R;
import org.edx.mobile.authentication.ApiLmsService;
import org.edx.mobile.authentication.ApiNewLmsClient;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.ActivityChatbotBinding;
import org.edx.mobile.discovery.model.ResponseCourseModel;
import org.edx.mobile.discovery.model.ResponseEnrollmentModel;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.interfaces.GridClickListener;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.model.ChatBotRequestBody;
import org.edx.mobile.model.ChatbotModal;
import org.edx.mobile.model.GridItem;
import org.edx.mobile.model.Message;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.ParticularCourseTask;
import org.edx.mobile.util.ChatbotUtils;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.ChatAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.common.OnAccessibilityCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotActivity extends BaseFragmentActivity implements SpeechToTextListener, TalkBackListener, OnRecyclerItemClickListener, OnNavigateListener, OnAccessibilityCallback, GridClickListener {

    private static final String TAG = "ChatbotActivity";
    private static final int KEYBOARD_VISIBILITY_THRESHOLD = 200;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private ActivityChatbotBinding binding;
    private ChatAdapter adapter;
    //private ChatbotModal chatbotModal;
    private  String sessionid;
    private SpeechToTextHelper speechToTextHelper;
    private TextToSpeechHelper textToSpeechHelper;
    private Animation breathingAnimation;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    Config config;

    @Inject
    CourseApi courseApi;

    @Inject
    protected IEdxEnvironment environment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatbotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initChatBot();
        setupListeners();
        displayInitialMessages();
    }

    private void initViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, this::onItemClick, this::navigateToAnotherScreen, binding.recyclerView, this::shiftAccessibilityFocusToFirstItemText,this::onGridClick);
        binding.recyclerView.setAdapter(adapter);

        breathingAnimation = AnimationUtils.loadAnimation(this, R.anim.breathing_animation);
        binding.fab.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        stopAnimation();
    }

    private void initChatBot() {
        speechToTextHelper = new SpeechToTextHelper(getApplicationContext(), this, this);
        textToSpeechHelper = new TextToSpeechHelper(getApplicationContext(), this, this::onDoneTalkBackListener);

    }

    private void setupListeners() {
        binding.editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonVisibility(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.buttonSend.setOnClickListener(v -> sendMessage());
        binding.voiceIconButton.setOnClickListener(v -> onMicButtonClick());
        binding.backArrow.setOnClickListener(v -> finish());

        setupKeyboardVisibilityListener();
        setupRecyclerViewScrollListener();
    }

    private void updateSendButtonVisibility(boolean show) {
        binding.voiceIconButton.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.buttonSend.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            stopAnimation();
        }
    }

    private void setupKeyboardVisibilityListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff > dpToPx(ChatbotActivity.this, KEYBOARD_VISIBILITY_THRESHOLD)) {
                    // Keyboard is shown
                } else {
                    // Keyboard is hidden
                }
            }
        });
    }

    private void setupRecyclerViewScrollListener() {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0 && binding.fab.getVisibility() == View.GONE) {
                    binding.fab.show();
                } else if (dy > 0 && binding.fab.getVisibility() == View.VISIBLE) {
                    binding.fab.hide();
                }
            }
        });

        binding.fab.setOnClickListener(v -> {
            binding.recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            binding.fab.hide();
        });
    }

    private void displayInitialMessages() {
        String selectedLanguage = LocaleManager.getLanguagePref(getApplicationContext());
        String fullLanguageName = LocaleManager.getFullLanguageName(selectedLanguage);

        Context localizedContext = LocaleManager.setLocale(this);
        String staticTextChatbot1 = localizedContext.getString(R.string.static_text_chatbot1);
        String staticTextChatbot2 = localizedContext.getString(R.string.static_text_chatbot2, fullLanguageName);
        long messageId1 = System.currentTimeMillis();
        long messageIdReturn1 = adapter.addMessage(new Message(messageId1, "", false, true, true, false));
        chatbotReplyStatic(staticTextChatbot1,messageIdReturn1);
        long messageId2 = System.currentTimeMillis()+1;
        long messageIdReturn2 = adapter.addMessage(new Message(messageId2, "", false, true, true, false));
        chatbotReplyStatic(staticTextChatbot2,messageIdReturn2);
    }

    private void sendMessage() {
        String message = binding.editTextMessage.getText().toString().trim();
        sendAnalyticsEditTextPress();
        if (!message.isEmpty()) {
            chatProcess(message);
        }
    }

    private void chatProcess(String message) {
        long messageId = System.currentTimeMillis();
        adapter.addMessage(new Message(messageId, message, true, false, false, false));
        binding.editTextMessage.setText("");
        closeKeyboard();
        //checkJwtTokenAndFetchData(message);
         sendAnalyticsChatbotInteraction();
         chatbotShimmer(message);
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkJwtTokenAndFetchData(String queryText,long messageId) {
        try {
            AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
            long tokenTime = System.currentTimeMillis() - responseJwt.creation_time;
            if (tokenTime > responseJwt.expires_in) {
                createToken(queryText,messageId);
            } else {
                fetchDataAIResponse(queryText,messageId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking JWT token", e);
            ChatbotModal chatbotModal=new ChatbotModal();
            chatbotModal.setMessageId(messageId);
            chatbotModal.setData(getString(R.string.something_went_wrong));
            chatbotReply(getString(R.string.something_went_wrong),chatbotModal);
        }
    }

    private void createToken(String queryText,long messageId) {
        new DiscoveryTask(getApplicationContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                fetchDataAIResponse(queryText,messageId);
            }

            @Override
            public void onException(Exception ex) {
                if (ex instanceof HttpStatusException && ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    Log.e(TAG, "Unauthorized access", ex);
                } else {
                    Log.e(TAG, "Error creating token", ex);
                }
                ChatbotModal chatbotModal=new ChatbotModal();
                chatbotModal.setMessageId(messageId);
                chatbotModal.setData(getString(R.string.something_went_wrong));
                chatbotReply(getString(R.string.something_went_wrong),chatbotModal);
            }
        }.execute();
    }

//    private void fetchDataAIResponseStatic(long messageId) {
//        // Add static data to chatbotModal (mock data example)
//        ChatbotModal chatbotModal=new ChatbotModal();
//        chatbotModal.setMessageId(messageId);
//        chatbotModal.setData("Water is an essential component for the survival of all living organisms, including plants. It plays several important roles in plants:\\n\\n1. Water is absorbed by the roots of plants from the soil. It is then transported through the stem and into the leaves.\\n\\n2. Water is a major component of the process of photosynthesis, where plants use sunlight to convert water and carbon dioxide into glucose (a type of sugar) and oxygen. This process is vital for the production of food and energy in plants.\\n\\n3. Water helps plants maintain their shape and structure. It provides turgidity to plant cells, allowing them to remain upright and support the plant's overall structure.\\n\\n4. Water also helps in the transportation of nutrients and minerals within the plant. These nutrients are dissolved in water and transported from the roots to the other parts of the plant through specialized tissues called xylem and phloem.\\n\\nIn summary, water is crucial for the growth, development, and survival of plants. It is involved in various physiological processes, including photosynthesis, nutrient transport, and maintenance of plant structure.\\n\\nCOURSE IDS: \\n\\n1. course-v1:VisionEmpower+VE_TIK_S_G7-11+2021\\n2. course-v1:VisionEmpower+VE_TIK_S_G7-01+2021\\n3. course-v1:VisionEmpower+VE_TIK_S_G4-04+2019 course-v1:VisionEmpower+VE_TIK_M_G9_P1_CH04+2020 course-v1:VE+G5_SCI_001+2020_KA course-v1:VisionEmpower+VE_TIK_M_G9_P1_CH05+2020");
//        chatbotModal.setSessionid("yzftgegm25w9bhmc0penta9orb233mlw");
//
//        List<String> courseIdList = Arrays.asList(
//                "course-v1:VisionEmpower+VE_TIK_S_G7-11+2021",
//                "course-v1:VisionEmpower+VE_TIK_M_G9_P1_CH04+2020",
//                "course-v1:VisionEmpower+VE_TIK_S_G4-04+2019",
//                "course-v1:VE+G5_SCI_001+2020_KA",
//                "course-v1:VisionEmpower+VE_TIK_M_G9_P1_CH05+2020"
//        );
//
//        chatbotModal.setCourseIds(courseIdList);
//
//        List<String> programIdList = Arrays.asList(
//                "13f4e543-f84f-4bd6-8dfc-db4369a6a14c",
//                "d83d4e83-78cc-4e73-9874-30cf408c73b4",
//                "7a920bd0-e869-4c45-8ec8-273f05b3f64b",
//                "f93d9e28-21c5-41d4-bd8f-26dff550b872",
//                "72830650-bc92-4863-924f-d1bcb3d60505",
//                "12611734-2c3d-4bb1-a989-2d8f9aaa1211",
//                "7a920bd0-e869-4c45-8ec8-273f05b3f64b",
//                "f93d9e28-21c5-41d4-bd8f-26dff550b872",
//                "71f5b113-39cd-44f8-b3a0-d8c731673eef"
//        );
//        chatbotModal.setProgramUuids(programIdList);
//
//
//        // Simulate a delay (if needed) to mimic network response time
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Process the static response
//
//                checkCourseIdAvailability(chatbotModal);
//                String cleanedData = ChatbotUtils.removeCourseIdsSection(chatbotModal);
//                System.out.println("Data after removing COURSE IDS section:\n" + cleanedData);
//                chatbotModal.setData(cleanedData);
//                chatbotReply(chatbotModal.data,chatbotModal);
//            }
//        }, 3000);  // Simulating a delay of 1 second
//
//        // Log the mock response
//        Log.d(TAG, "Static chatbot response: " + chatbotModal.data);
//    }


    private void fetchDataAIResponse(String text,long messageId) {
        String tokenJWT = loginPrefs.getAuthorizationHeaderJwt();
        String sessionId = sessionid != null ? "sessionid=" + sessionid : "";

        ApiNewLmsClient apiNewLmsClient = new ApiNewLmsClient(config.getAiBaseUrl()+ ApiConstants.chatBotEndPoint,tokenJWT);
        ApiLmsService apiService = apiNewLmsClient.getChatbotClient().create(ApiLmsService.class);

        String selectedLanguage = LocaleManager.getLanguagePref(getApplicationContext());
        String fullLanguageName = LocaleManager.getFullLanguageName(selectedLanguage);

        ChatBotRequestBody chatBotRequestBody = new ChatBotRequestBody(text, fullLanguageName);
        ChatbotModal chatbotModal=new ChatbotModal();
        chatbotModal.setMessageId(messageId);

        Call<ChatbotModal> call = apiService.openaiChat(tokenJWT, sessionId, chatBotRequestBody);

        call.enqueue(new Callback<ChatbotModal>() {
            @Override
            public void onResponse(Call<ChatbotModal> call, Response<ChatbotModal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sendAnalyticsChatbotResponseSuccessful();
                    ChatbotModal chatbotModal = response.body();
                    chatbotModal.setMessageId(messageId);

                    checkCourseIdAvailability(chatbotModal);

                } else {
                    sendAnalyticsChatbotResponseUnsuccessful();
                    Log.e(TAG, "Error response: " + response.code());
                    chatbotModal.setData(getString(R.string.something_went_wrong));
                    chatbotReply(getString(R.string.something_went_wrong),chatbotModal);
                }
            }

            @Override
            public void onFailure(Call<ChatbotModal> call, Throwable t) {
                sendAnalyticsChatbotResponseUnsuccessful();
                Log.e(TAG, "Request failed", t);
                chatbotModal.setData(getString(R.string.something_went_wrong));
                chatbotReply(getString(R.string.something_went_wrong),chatbotModal);
            }
        });
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private void checkCourseIdAvailability(ChatbotModal chatbotModal) {
        boolean isPresent = chatbotModal.getCourseIds().stream()
                .anyMatch(courseId -> ChatbotUtils.isCourseIdPresentInData(chatbotModal, courseId));

        if (isPresent) {
            System.out.println(" Course Before "+chatbotModal.getCourseIds().size()+" "+chatbotModal.getCourseIds());
            List<String> courseIdList = ChatbotUtils.extractCourseIdsFromData(chatbotModal);
            System.out.println(" Course After "+courseIdList+" "+chatbotModal.getCourseIds());
            chatbotModal.setCourseIds(courseIdList);
            System.out.println(" Course  Afterward "+chatbotModal.getCourseIds().size()+" "+chatbotModal.getCourseIds());
            fetchCoursesParallel(chatbotModal.getProgramUuids().subList(0, 3),chatbotModal);
        }

        System.out.println("Is course ID present: " + isPresent);
        String cleanedData = ChatbotUtils.removeCourseIdsSection(chatbotModal);
        System.out.println("Data after removing COURSE IDS section:\n" + cleanedData);
        chatbotModal.setData(cleanedData);
        chatbotReply(chatbotModal.data,chatbotModal);


    }

    private void fetchCoursesParallel(List<String> programUuids,ChatbotModal chatbotModal) {
        System.out.println(" programUuids "+programUuids.toString());
        compositeDisposable.add(
                Observable.fromIterable(programUuids)
                        .flatMap(this::getCourseListObservable)
                        .timeout(30, TimeUnit.SECONDS)
                        .retry(3)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .toList()
                        .subscribe(
                                responseCourseModelList -> handleSuccess(responseCourseModelList,chatbotModal),
                                this::handleError
                        )
        );
    }

    private Observable<ResponseCourseModel> getCourseListObservable(String programUuid) {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        String selectedLanguage = LocaleManager.getLanguagePref(ChatbotActivity.this).isEmpty() ? "en" : LocaleManager.getLanguagePref(ChatbotActivity.this);

        return Observable.fromCallable(() ->
                        courseApi.getCourseResponse(token, programUuid, selectedLanguage).execute()
                ).map(response -> {
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        ResponseCourseModel responseCourseModel = new ResponseCourseModel();
                        return responseCourseModel;
                    }
                }).subscribeOn(Schedulers.io())
                .onErrorResumeNext(error -> {
                    Log.e("API_ERROR", "Error fetching course list for UUID: " + programUuid, error);
                    return Observable.empty();
                });
    }

    private void handleSuccess(List<ResponseCourseModel> responseCourseModelList,ChatbotModal chatbotModal) {
        System.out.println("Received " + responseCourseModelList.size() + " course lists");

        if (chatbotModal != null) {
            List<ResponseCourseModel> filteredCourseList = filterCoursesByIds(responseCourseModelList, chatbotModal);
            System.out.println("chatbotModal courseId "+chatbotModal.courseIds);
            System.out.println("Filtered course list size: " + filteredCourseList.size());

            // Now you can use the filteredCourseList for further processing
            processFilteredCourseList(filteredCourseList,chatbotModal);
        } else {
            System.out.println("ChatbotModal is null, cannot filter courses");
        }
    }

    private void handleError(Throwable error) {
        Log.e("API_ERROR", "Error in fetching course data", error);
    }


    private void processFilteredCourseList(List<ResponseCourseModel> filteredCourseList, ChatbotModal chatbotModal) {
        List<GridItem> gridItems = new ArrayList<>();

        for (ResponseCourseModel courseModel : filteredCourseList) {
            System.out.println(
                    "CourseModel: " +
                            "\nProgramId: " + courseModel.getId() +
                            "\nTopicTitle: " + courseModel.getTopicTitle() +
                            "\nConvertedTopicTitle: " + courseModel.getConvertedTopicTitle() +
                            "\nProgramTitle: " + courseModel.getProgramTitle() +
                            "\nConvertedProgramTitle: " + courseModel.getConvertedProgramTitle() +
                            "\nCourseConvertedTitle: " + courseModel.getConvertedTopicTitle()
            );

            for (ResponseCourseModel.CourseItem courseItem : courseModel.getData()) {
                if(courseModel.getTopicTitle() != null && !courseModel.getTopicTitle().isEmpty() && !courseModel.getTopicTitle().isBlank()) {
                    gridItems.add(createGridItem(courseModel, courseItem));
                }
            }
        }
        chatbotGridReply(gridItems, chatbotModal);
    }


    private GridItem createGridItem(ResponseCourseModel courseModel, ResponseCourseModel.CourseItem courseItem) {
        GridItem gridItem = new GridItem();
        // Set values from the parent ResponseCourseModel
        gridItem.setProgramId(courseModel.getId());
        gridItem.setTopicTitle(courseModel.getTopicTitle());
        gridItem.setConvertedTopicTitle(courseModel.getConvertedTopicTitle());
        gridItem.setProgramTitle(courseModel.getProgramTitle());
        gridItem.setConvertedProgramTitle(courseModel.getConvertedProgramTitle());

        // Set values from the nested CourseItem
        gridItem.setCousreConvertedTitle(courseItem.getConvertedTitle());
        gridItem.setCourseTitle(courseItem.getTitle());
        gridItem.setKey(courseItem.getKey());
        gridItem.setProgramLanguage(courseItem.getLanguage()); // Note: Using CourseItem's language
        gridItem.setCreated(courseItem.getCreated());

        System.out.println(
                "GridItem Data Created:" +
                        "\nProgramId: " + gridItem.getProgramId() +
                        "\nTopicTitle: " + gridItem.getTopicTitle() +
                        "\nConvertedTopicTitle: " + gridItem.getConvertedTopicTitle() +
                        "\nProgramTitle: " + gridItem.getProgramTitle() +
                        "\nConvertedProgramTitle: " + gridItem.getConvertedProgramTitle() +
                        "\nCourseConvertedTitle: " + gridItem.getCousreConvertedTitle() +
                        "\nCourseTitle: " + gridItem.getCourseTitle() +
                        "\nKey: " + gridItem.getKey() +
                        "\nProgramLanguage: " + gridItem.getProgramLanguage() +
                        "\nCreated: " + gridItem.getCreated()
        );

        return gridItem;
    }

    private void chatbotGridReply(List<GridItem> gridItems,ChatbotModal chatbotModal) {
        binding.voiceIconButton.clearFocus();

        System.out.println("HOOLWLWLLWLWLWLW gridItems chatbotGridReply "+gridItems.size());
        binding.recyclerView.post(() -> {
            adapter.updateMessageWithGrid(chatbotModal.getMessageId(), gridItems);
            //adapter.addMessage(message);
            binding.voiceIconButton.clearFocus();
            binding.editTextMessage.clearFocus();
        });
    }


    private void handleError(Throwable error,long messageId) {
        Log.e("API_ERROR", "Error fetching course lists", error);
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error fetching course lists", Toast.LENGTH_SHORT).show());
    }

    private List<ResponseCourseModel> filterCoursesByIds(List<ResponseCourseModel> responseCourseModelList, ChatbotModal chatbotModal) {
        System.out.println("filterCoursesByIds " + responseCourseModelList.size());
        if (chatbotModal == null || chatbotModal.getCourseIds() == null || responseCourseModelList == null) {
            return new ArrayList<>(); // Return empty list if inputs are null
        }

        Set<String> chatbotCourseIds = new HashSet<>(chatbotModal.getCourseIds());

        return responseCourseModelList.stream()
                .map(responseCourseModel -> {
                    ResponseCourseModel filteredModel = new ResponseCourseModel();

                    // Copy all fields from the original model
                    filteredModel.setId(responseCourseModel.getId());
                    filteredModel.setTopicTitle(responseCourseModel.getTopicTitle());
                    filteredModel.setConvertedTopicTitle(responseCourseModel.getConvertedTopicTitle());
                    filteredModel.setProgramLanguage(responseCourseModel.getProgramLanguage());
                    filteredModel.setProgramTitle(responseCourseModel.getProgramTitle());
                    filteredModel.setConvertedProgramTitle(responseCourseModel.getConvertedProgramTitle());

                    // Filter the course items
                    if (responseCourseModel.getData() != null) {
                        List<ResponseCourseModel.CourseItem> filteredCourseItems = responseCourseModel.getData().stream()
                                .filter(courseItem -> chatbotCourseIds.contains(courseItem.getKey()))
                                .collect(Collectors.toList());
                        filteredModel.setData(filteredCourseItems);
                    } else {
                        filteredModel.setData(new ArrayList<>());
                    }

                    return filteredModel;
                })
                .filter(responseCourseModel -> responseCourseModel.getData() != null && !responseCourseModel.getData().isEmpty())
                .collect(Collectors.toList());
    }



    private void checkEnrollResponse(GridItem gridItem) {
        Log.e("config.getApiHostURL()",config.getApiHostURL());
        ApiNewLmsClient apiNewLmsClient=new ApiNewLmsClient(config.getApiHostURL());
        ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);
        try {


            Call<ResponseEnrollmentModel> call = apiService.unrollCheck(
                    loginPrefs.getAuthorizationHeader(),
                    loginPrefs.getUsername(),
                    gridItem.getProgramId()
            );

            call.enqueue(new Callback<ResponseEnrollmentModel>() {
                @Override
                public void onResponse(Call<ResponseEnrollmentModel> call, Response<ResponseEnrollmentModel> response) {
                    binding.progressLoader.setVisibility(View.GONE);
                    if (response.isSuccessful()) {

                        ResponseEnrollmentModel data = response.body();
                        if (data != null) {
                            // Log the response body or relevant information
                            if (data.getEnrollmentStatus().equals("enrolled")) {
                                getParticularCourse(gridItem);
                                Log.d("ResponseEnrollmentModel", data.getEnrollmentStatus() + " " + data.isStatus());
                            } else {
                                navigateToAnotherScreenProgram(gridItem);
                                Log.e("ResponseEnrollmentModel", "Response body is null");
                            }
                        }
                    } else {
                        // Handle error response
                        binding.progressLoader.setVisibility(View.GONE);
                        Log.e("ResponseEnrollmentModel", "Error response: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseEnrollmentModel> call, Throwable t) {
                    // Handle failure
                    binding.progressLoader.setVisibility(View.GONE);
                    Log.e("ResponseEnrollmentModel", "Request failed", t);
                }
            });
        }
        catch (Exception e){
            binding.progressLoader.setVisibility(View.GONE);
            Log.d("error",e.getMessage());
        }
    }

    public void getParticularCourse(GridItem gridItem) {
        try {
            ParticularCourseTask particularCourseTask = new ParticularCourseTask(getApplicationContext(), loginPrefs.getUsername(),
                    gridItem.getKey()) {
                @Override
                public void onSuccess(@NonNull ArrayList<EnrolledCoursesResponse> result) {
                    binding.progressLoader.setVisibility(View.GONE);
                    try {
                        if (result != null) {
                            // Handle successful response
                            ArrayList<EnrolledCoursesResponse> data = result;
                            if (data != null) {
                                if (data != null) {
                                    for (EnrolledCoursesResponse enrolledCoursesResponse : data) {
                                        if (enrolledCoursesResponse.getCourse() != null) {
                                            if (gridItem.getKey().equals(enrolledCoursesResponse.getCourse().getId())) {
                                                sendAnalyticsCourseView(enrolledCoursesResponse, gridItem.getProgramId());
                                                environment.getRouter().showCourseDashboardTabs(ChatbotActivity.this, enrolledCoursesResponse,
                                                        false);
                                            }
                                        }
                                    }
                                }
                                Log.d("enrolledCoursesResponses", data.get(0).getCourse().getId() + "" + data.get(0).getCourse().getName());
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
                    binding.progressLoader.setVisibility(View.GONE);
                }
            };
            particularCourseTask.execute();


        } catch (Exception e) {
            Log.e("enrolledCoursesResponses", "Exception in getParticularCourse", e);
            binding.progressLoader.setVisibility(View.GONE);
        }
    }

    private void chatbotReply(String textReply,ChatbotModal chatbotModal) {
        binding.voiceIconButton.clearFocus();
        binding.recyclerView.post(() -> {
            String cleanedData = ChatbotUtils.removeCourseIdsSection(chatbotModal);
            adapter.updateMessageWithId(chatbotModal.getMessageId(), cleanedData, null);
            //adapter.addMessage(message);
            binding.voiceIconButton.clearFocus();
            binding.editTextMessage.clearFocus();
        });
    }

    private void chatbotReplyStatic(String textReply,long messageId) {
        binding.voiceIconButton.clearFocus();
        binding.recyclerView.post(() -> {
            Message message= new Message(messageId, textReply, false, false, true, true);
            adapter.updateMessageStatic(messageId, textReply);
            //adapter.addMessage(message);
            binding.voiceIconButton.clearFocus();
            binding.editTextMessage.clearFocus();
        });
    }



    private void chatbotShimmer(String messageText) {
        long messageId = System.currentTimeMillis();
        long messageIdx = adapter.addMessage(new Message(messageId, "", false, true, true, false));
        //fetchDataAIResponseStatic(messageIdx);
        checkJwtTokenAndFetchData(messageText,messageIdx);
    }

    public void onMicButtonClick() {
        sendAnalyticsMicButton();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startAnimation();
            textToSpeechHelper.stop();
            speechToTextHelper.startSpeechRecognition();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    private void startAnimation() {
        binding.voiceIconButtonLayout.setBackgroundResource(R.drawable.circle_background_blue);
        binding.voiceIconButtonLayout.startAnimation(breathingAnimation);
    }

    private void stopAnimation() {
        binding.voiceIconButtonLayout.clearAnimation();
        binding.voiceIconButtonLayout.setBackground(null);
    }

    @Override
    public void onSpeechResult(String text) {
        sendAnalyticsTextToSpeechSuccessful();
        stopAnimation();
        chatProcess(text);
    }

    @Override
    public void onSpeechError(String error) {
        stopAnimation();
        Log.e(TAG, "Speech recognition error: " + error);
        sendAnalyticsTextToSpeechUnsuccessful();
    }

    @Override
    public void onDoneTalkBackListener() {
        adapter.setOnPositionListener();
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof Message) {
            Message messageOb = (Message) item;
            String plainText = Html.fromHtml(messageOb.getText(), Html.FROM_HTML_MODE_LEGACY).toString();
            if( messageOb.getGridItems()!=null) {
                for (GridItem gridItem : messageOb.getGridItems()) {
                    if (gridItem.getCousreConvertedTitle()!=null){
                        plainText+=" "+gridItem.getCousreConvertedTitle();
                    }
                    else if(gridItem.getCourseTitle()!=null){
                        plainText+=" "+gridItem.getCourseTitle();
                    }

                }
            }
            sendAnalyticsVoicePlayButtonPress();
            textToSpeechHelper.speakText(plainText);
        }
    }

    @Override
    public void navigateToAnotherScreen(Object item) {
        sendAnalyticsVoiceStopButtonPress();
        textToSpeechHelper.stop();
    }

    @Override
    public void shiftAccessibilityFocusToFirstItemText(int position, Object item) {
        new Handler().postDelayed(this::shiftAccessibilityFocusToResponseItemText, 1000);
    }

    private void shiftAccessibilityFocusToResponseItemText() {
        binding.recyclerView.post(() -> {
            int firstPosition = adapter.getItemCount() - 1;
            RecyclerView.ViewHolder viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(firstPosition);
            if (viewHolder != null) {
                View itemView = viewHolder.itemView;
                TextView textView = itemView.findViewById(R.id.textViewMessage);
                if (textView != null) {
                    textView.requestFocus();
                    textView.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                }
            }
        });
    }

    private float dpToPx(Context context, int dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeechHelper != null) {
            textToSpeechHelper.shutdown();
        }
        if (speechToTextHelper != null) {
            speechToTextHelper.stopSpeechRecognition();
        }
        compositeDisposable.clear();
    }



    @Override
    public void onGridClick(Object item) {
        if (item instanceof GridItem) {
            GridItem gridItem = (GridItem) item;
            //progress_loader
            binding.progressLoader.setVisibility(View.VISIBLE);
            checkEnrollResponse(gridItem);
        }
    }



    void navigateToCourseScreenByCourse(ArrayList<EnrolledCoursesResponse> enrolledCoursesResponses,String courseId,String program_Uid){
        if (enrolledCoursesResponses != null) {
            for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                if (enrolledCoursesResponse.getCourse() != null) {
                    if (courseId.equals(enrolledCoursesResponse.getCourse().getId())) {

                        environment.getRouter().showCourseDashboardTabs(ChatbotActivity.this, enrolledCoursesResponse,
                                false);
                    }
                }
            }
        }
    }

    public void navigateToAnotherScreenProgram(GridItem gridItem) {
        System.out.println(
                " navigateToAnotherScreenProgram GridItem Data:" +
                        "\nProgramId: " + gridItem.getProgramId() +
                        "\nTopicTitle: " + gridItem.getTopicTitle() +
                        "\nConvertedTopicTitle: " + gridItem.getConvertedTopicTitle() +
                        "\nProgramTitle: " + gridItem.getProgramTitle() +
                        "\nConvertedProgramTitle: " + gridItem.getConvertedProgramTitle() +
                        "\nCourseConvertedTitle: " + gridItem.getCousreConvertedTitle() +
                        "\nCourseTitle: " + gridItem.getCourseTitle() +
                        "\nKey: " + gridItem.getKey() +
                        "\nProgramLanguage: " + gridItem.getProgramLanguage() +
                        "\nCreated: " + gridItem.getCreated()
        );
            environment.getRouter().showProgramsActivity(ChatbotActivity.this, gridItem.getTopicTitle(),gridItem.getProgramId());
    }

    void sendAnalyticsCourseView(EnrolledCoursesResponse enrolledCoursesResponse, String program_Uid) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.COURSE_ID, enrolledCoursesResponse.getCourse().getId());
        values.put(Analytics.Keys.PROGRAM_UID, program_Uid);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.COURSE_VIEW, null, "View", values);
    }

    void sendAnalyticsCourseEnroll(String courseId, String program_Uid) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.COURSE_ID, courseId);
        values.put(Analytics.Keys.PROGRAM_UID, program_Uid);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.COURSE_ENROLL, null, "Enroll", values);
    }

    void sendAnalyticsCourseUnenroll(String courseId, String program_Uid) {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.COURSE_ID, courseId);
        values.put(Analytics.Keys.PROGRAM_UID, program_Uid);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.COURSE_UNENROLL, null, "Unenroll", values);
    }

    void sendAnalyticsMicButton() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Button Press");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.MIC_BUTTON_PRESS, null, "Press", values);
    }



    void sendAnalyticsChatbotInteraction() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Interaction");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.CHATBOT_INTERACTION, null, "Interact", values);
    }

    void sendAnalyticsCourseScreenNavigation() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Navigation");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.COURSE_SCREEN_NAVIGATION, null, "Navigate", values);
    }

    void sendAnalyticsEditTextPress() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Edit Text Press");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.EDIT_TEXT_PRESS, null, "Press", values);
    }

    void sendAnalyticsVoicePlayButtonPress() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Voice Play Button Press");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.VOICE_PLAY_BUTTON_PRESS, null, "Play", values);
    }

    void sendAnalyticsVoiceStopButtonPress() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Voice Stop Button Press");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.VOICE_STOP_BUTTON_PRESS, null, "Stop", values);
    }

    void sendAnalyticsChatbotResponseSuccessful() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Response Successful");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.CHATBOT_RESPONSE_SUCCESSFUL, null, "Success", values);
    }

    void sendAnalyticsChatbotResponseUnsuccessful() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Response Unsuccessful");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.CHATBOT_RESPONSE_UNSUCCESSFUL, null, "Failure", values);
    }

    void sendAnalyticsTextToSpeechSuccessful() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Text to Speech Successful");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.TEXT_TO_SPEECH_SUCCESSFUL, null, "Success", values);
    }

    void sendAnalyticsTextToSpeechUnsuccessful() {
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.ACTION, "Text to Speech Unsuccessful");
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.TEXT_TO_SPEECH_UNSUCCESSFUL, null, "Failure", values);
    }



}