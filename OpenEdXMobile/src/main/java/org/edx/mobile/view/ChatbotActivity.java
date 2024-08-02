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
import org.edx.mobile.databinding.ActivityChatbotBinding;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.model.ChatBotRequestBody;
import org.edx.mobile.model.ChatbotModal;
import org.edx.mobile.model.Message;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.ChatAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.common.OnAccessibilityCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotActivity extends BaseFragmentActivity implements SpeechToTextListener, TalkBackListener, OnRecyclerItemClickListener, OnNavigateListener, OnAccessibilityCallback {

    private static final String TAG = "ChatbotActivity";
    private static final int KEYBOARD_VISIBILITY_THRESHOLD = 200;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private ActivityChatbotBinding binding;
    private ChatAdapter adapter;
    private ChatbotModal chatbotModal;
    private SpeechToTextHelper speechToTextHelper;
    private TextToSpeechHelper textToSpeechHelper;
    private Animation breathingAnimation;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    Config config;

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
        adapter = new ChatAdapter(this, this::onItemClick, this::navigateToAnotherScreen, binding.recyclerView, this::shiftAccessibilityFocusToFirstItemText);
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

        chatbotReply(staticTextChatbot1);
        chatbotReply(staticTextChatbot2);
    }

    private void sendMessage() {
        String message = binding.editTextMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            chatProcess(message);
        }
    }

    private void chatProcess(String message) {
        long messageId = System.currentTimeMillis();
        adapter.addMessage(new Message(messageId, message, true, false, false, false));
        binding.editTextMessage.setText("");
        closeKeyboard();

        checkJwtTokenAndFetchData(message);
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkJwtTokenAndFetchData(String queryText) {
        try {
            AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
            long tokenTime = System.currentTimeMillis() - responseJwt.creation_time;
            if (tokenTime > responseJwt.expires_in) {
                createToken(queryText);
            } else {
                fetchDataAIResponse(queryText);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking JWT token", e);
            chatbotReply(getString(R.string.something_went_wrong));
        }
    }

    private void createToken(String queryText) {
        new DiscoveryTask(getApplicationContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                fetchDataAIResponse(queryText);
            }

            @Override
            public void onException(Exception ex) {
                if (ex instanceof HttpStatusException && ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    Log.e(TAG, "Unauthorized access", ex);
                } else {
                    Log.e(TAG, "Error creating token", ex);
                }
                chatbotReply(getString(R.string.something_went_wrong));
            }
        }.execute();
    }

    private void fetchDataAIResponse(String text) {
        chatbotShimmer();
        String sessionId = chatbotModal != null && chatbotModal.sessionid != null ? "sessionid=" + chatbotModal.sessionid : "";

        ApiNewLmsClient apiNewLmsClient = new ApiNewLmsClient(config.getSubodhaAiBaseUrl()+ ApiConstants.chatBotEndPoint);
        ApiLmsService apiService = apiNewLmsClient.getClient().create(ApiLmsService.class);

        String selectedLanguage = LocaleManager.getLanguagePref(getApplicationContext());
        String fullLanguageName = LocaleManager.getFullLanguageName(selectedLanguage);

        ChatBotRequestBody chatBotRequestBody = new ChatBotRequestBody(text, fullLanguageName);

        String tokenJWT = loginPrefs.getAuthorizationHeaderJwt();
        Call<ChatbotModal> call = apiService.openaiChat(tokenJWT, sessionId, chatBotRequestBody);

        call.enqueue(new Callback<ChatbotModal>() {
            @Override
            public void onResponse(Call<ChatbotModal> call, Response<ChatbotModal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatbotModal = response.body();
                    chatbotReply(chatbotModal.data);
                } else {
                    Log.e(TAG, "Error response: " + response.code());
                    chatbotReply(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<ChatbotModal> call, Throwable t) {
                Log.e(TAG, "Request failed", t);
                chatbotReply(getString(R.string.something_went_wrong));
            }
        });
    }

    private void chatbotReply(String textReply) {
        binding.voiceIconButton.clearFocus();
        binding.recyclerView.post(() -> {
            long messageId = System.currentTimeMillis();
            adapter.addMessage(new Message(messageId, textReply, false, false, true, true));
            binding.voiceIconButton.clearFocus();
            binding.editTextMessage.clearFocus();
        });
    }

    private void chatbotShimmer() {
        long messageId = System.currentTimeMillis();
        adapter.addMessage(new Message(messageId, "", false, true, false, true));
    }

    public void onMicButtonClick() {
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
        stopAnimation();
        chatProcess(text);
    }

    @Override
    public void onSpeechError(String error) {
        stopAnimation();
        Log.e(TAG, "Speech recognition error: " + error);
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
            textToSpeechHelper.speakText(plainText);
        }
    }

    @Override
    public void navigateToAnotherScreen(Object item) {
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
    }
}