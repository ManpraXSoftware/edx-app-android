package org.edx.mobile.view;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.inject.Inject;

import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextHelper;
import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.Chatbot.TextToSpeechHelper.TextToSpeechHelper;
import org.edx.mobile.R;
import org.edx.mobile.authentication.ApiLmsService;
import org.edx.mobile.authentication.ApiNewLmsClient;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.comparator.TalkBackDetector.MyAccessibilityService;
import org.edx.mobile.discovery.model.DiscoverySubjectResult;
import org.edx.mobile.discovery.model.ProgramResponseModel;
import org.edx.mobile.discovery.model.ResponseEnrollmentModel;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.model.ChatBotRequestBody;
import org.edx.mobile.model.ChatbotModal;
import org.edx.mobile.model.Message;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.AutoResizeWebView;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.adapters.ChatAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotActivity extends AppCompatActivity implements SpeechToTextListener , TalkBackListener, OnRecyclerItemClickListener , OnNavigateListener {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend, voiceIconButton;
    private LinearLayout voiceIconButtonLayout;
    private SpeechToTextHelper speechToTextHelper;
    private TextToSpeechHelper textToSpeechHelper;
    Animation breathingAnimation;
    private ImageView backArrow;
    ChatAdapter adapter;
    ChatbotModal chatbotModal;
    private SoundPool soundPool;
    private int soundId;
    @Inject
    LoginPrefs loginPrefs;

    private FloatingActionButton fab;
    private boolean isScrollingUp = false;

    String accessToken = "";

    @NonNull
    public Config config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        voiceIconButton = findViewById(R.id.voiceIconButton);
        backArrow = findViewById(R.id.back_arrow);
        fab = findViewById(R.id.fab);
        fab.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        initializeChatBot();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatAdapter(this, ChatbotActivity.this::onItemClick, this::navigateToAnotherScreen, recyclerView);
        recyclerView.setAdapter(adapter);

        voiceIconButtonLayout = findViewById(R.id.voiceIconButtonLayout);

        // Load the animation
        breathingAnimation = AnimationUtils.loadAnimation(this, R.anim.breathing_animation);
        stopAnimation();
        staticText();

        editTextMessage = findViewById(R.id.editTextMessage);
        voiceIconButton = findViewById(R.id.voiceIconButton);
        buttonSend = findViewById(R.id.buttonSend);

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    voiceIconButton.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                    stopAnimation();

                } else {
                    voiceIconButton.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Detect when the keyboard is opened
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff > dpToPx(ChatbotActivity.this, 200)) { // if more than 200 dp, it's probably a keyboard...
                    // Keyboard is shown
                } else {
                    // Keyboard is hidden
                }
            }
        });

        voiceIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMicButtonClick();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    chatProcess(message);
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeechHelper.stop();
                finish();
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0 && !isScrollingUp) {
                    isScrollingUp = true;
                    fab.show();
                } else if (dy > 0 && isScrollingUp) {
                    isScrollingUp = false;
                    fab.hide();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        });


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

    void stopAnimation() {
        voiceIconButtonLayout.clearAnimation();
        voiceIconButtonLayout.setBackground(null);
    }

    void startAnimation() {
        voiceIconButtonLayout.setBackgroundResource(R.drawable.circle_background_blue);
        voiceIconButtonLayout.startAnimation(breathingAnimation);

    }


    private void setMargins(LinearLayout layout, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        layout.setLayoutParams(params);
    }

    private float dpToPx(Context context, int dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        if (textToSpeechHelper != null) {
            textToSpeechHelper.stop();
        }
    }

    void staticText() {
        chatbotReply(getString(R.string.static_text_chatbot1));
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getApplicationContext()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getApplicationContext());
        }
        String fullLanguageName = LocaleManager.getFullLanguageName(selectedLanguage);
        chatbotReply(getString(R.string.static_text_chatbot2, fullLanguageName));
    }

    void chatProcess(String message) {
        long messageId = System.currentTimeMillis();
        adapter.addMessage(new Message(messageId,message, true, false, false, false));
        editTextMessage.setText("");
        closeKeyboard();

        localData();
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void checkJwtTokenAndFetchData(String queryText) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkJwtToken(queryText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkJwtToken(String queryText) throws Exception {

        if (accessToken.isEmpty()) {
            createToken(queryText);
        } else {
            fetchDataAIResponse(queryText);
        }
//        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
//        long millis = System.currentTimeMillis();
//        long tokenTime = millis - responseJwt.creation_time;
//        if (tokenTime > responseJwt.expires_in) {
//            createToken(queryText);
//        } else {
//            fetchDataAIResponse(queryText);
//        }
    }

    private void createToken(String queryText) throws Exception {

        DiscoveryTask discoveryTask = new DiscoveryTask(getApplicationContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                accessToken = result.access_token;
                fetchDataAIResponse(queryText);
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

    void localData() {
        chatbotShimmer();
        chatbotReply("<body style='background-color: transparent;'>" + "<h2>Welcome!</h2>\n" +
                "<p>This is an <b>example</b> message with <i>HTML</i> content.</p>\n" +
                "<ul>\n" +
                "    <li>Item 1</li>\n" +
                "    <li>Item 2</li>\n" +
                "    <li>Item 3</li>\n" +
                "</ul>\n" +
                "<p>A LANGUAGE_MAP is created to map language codes to their full names.\n" +
                "The getFullLanguageName method takes a language code as input and returns the corresponding full name. If the language code is not found in the map, it returns \"Unknown Language\".\n" +
                "The example usage demonstrates how to get the selected language code and convert it to the full language name using the getFullLanguageName method.<P>"
                +
                "<p>Visit <a href=\"https://www.example.com\">this link</a> for more information.</p>\n </body>");
    }

    void chatbotReply(String textReply) {
        voiceIconButton.clearFocus();
        // Simulate bot response
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {

                long messageId = System.currentTimeMillis();

                adapter.addMessage(new Message(messageId, textReply, false, false, true, true));
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                // Clear focus from other views
                voiceIconButton.clearFocus();
                editTextMessage.clearFocus();

                // Ensure recyclerView has children before trying to access them
                if (recyclerView.getChildCount() > 0) {
                    // Use ViewTreeObserver to wait for layout to complete
                    recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // Remove the listener to prevent multiple calls
                            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            // Now you can safely access child views
                            if (recyclerView.getChildCount() > 0) {
                                View firstChild = recyclerView.getChildAt(0);
                                if (firstChild != null) {
                                    firstChild.requestFocus();
                                    firstChild.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                                }
                            }
                        }
                    });
                }
                Log.d("recyclerView recyclerView Pre", "New size: " + adapter.getItemCount());
            }
        }, 1500);
    }
    void chatbotShimmer() {
        long messageId = System.currentTimeMillis();
        adapter.addMessage(new Message(messageId,"", false, true, false, true));
    }

    void fetchDataAIResponse(String text) {
        String sessionId = "";
        chatbotShimmer();
        if (chatbotModal != null) {
            if (chatbotModal.sessionid != null) {
                sessionId = "sessionid=" + chatbotModal.sessionid;
            }
        }
        ApiNewLmsClient apiNewLmsClient = new ApiNewLmsClient(ApiConstants.chatBotBaseUrl);
        ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);

        ChatBotRequestBody chatBotRequestBody = new ChatBotRequestBody(text);

        String tokenJWT = "jwt " + accessToken; //loginPrefs.getCurrentAuthJwt().access_token;
        Call<ChatbotModal> call = apiService.openaiChat(
                tokenJWT,
                sessionId,
                chatBotRequestBody
        );

        call.enqueue(new Callback<ChatbotModal>() {
            @Override
            public void onResponse(Call<ChatbotModal> call, Response<ChatbotModal> response) {
                Log.d("ChatbotModal", " data " + response.body().toString());
                if (response.isSuccessful()) {
                    // Handle successful response
                    chatbotModal = response.body();
                    if (chatbotModal != null) {
                        chatbotReply(chatbotModal.data);
                        Log.d("ChatbotModal", " data " + " " + chatbotModal.data + " " + response.body().toString());
                    } else {
                        Log.e("ChatbotModal", "Response body is null");
                    }
                } else {
                    // Handle error response
                    Log.e("ChatbotModal", "Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatbotModal> call, Throwable t) {
                // Handle failure

                Log.e("ChatbotModal", "Request failed", t);
            }
        });

        // Replace with your actual return value


    }

    void initializeChatBot() {
        speechToTextHelper = new SpeechToTextHelper(getApplicationContext().getApplicationContext(), this, ChatbotActivity.this);
        textToSpeechHelper = new TextToSpeechHelper(getApplicationContext(), ChatbotActivity.this, this::onDoneTalkBackListener);
        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(getApplicationContext(), R.raw.beep_sound_2, 1);
    }

    public void onMicButtonClick() {
        stopAnimation();
        // Check microphone permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startAnimation();
        }

        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        if (speechToTextHelper == null) {
            speechToTextHelper = new SpeechToTextHelper(getApplicationContext(), this, ChatbotActivity.this);
        }
        textToSpeechHelper.stop();
        //speechToTextHelper.stopSpeechRecognition();
        speechToTextHelper.startSpeechRecognition();
    }


    @Override
    public void onSpeechResult(String text) {
        stopAnimation();
        chatProcess(text);
    }

    @Override
    public void onSpeechError(String error) {
        stopAnimation();
    }
    @Override
    public void onDoneTalkBackListener() {
        adapter.setOnPositionListener();
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof Message) {
            Message messageOb = (Message) item;
            Spanned spannedText = Html.fromHtml(messageOb.getText(), Html.FROM_HTML_MODE_LEGACY);
            String plainText = spannedText.toString();
            textToSpeechHelper.speakText(plainText);
            // Get the position of the clicked item
        }
    }


    @Override
    public void navigateToAnotherScreen(Object item) {
        textToSpeechHelper.stop();
    }

}
