package org.edx.mobile.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.model.Message;
import org.edx.mobile.util.AutoResizeWebView;
import org.edx.mobile.view.common.OnAccessibilityCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static Context context;
    private OnRecyclerItemClickListener listener;
    private OnNavigateListener onNavigateListener;
    private List<Message> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private OnAccessibilityCallback accessibilityCallback;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private long nextId = 0;

    public ChatAdapter(Context context, OnRecyclerItemClickListener listener, OnNavigateListener onNavigateListener, RecyclerView recyclerView,OnAccessibilityCallback accessibilityCallback) {
        this.context = context;
        this.listener = listener;
        this.onNavigateListener = onNavigateListener;
        this.recyclerView = recyclerView;
        this.accessibilityCallback=accessibilityCallback;
        setHasStableIds(true);
    }

    public void addMessage(Message message) {
        selectedPosition = RecyclerView.NO_POSITION;
        if (message.isResponse() && messages.size() > 2) {
            Message lastMessage = messages.get(messages.size() - 1);
            if (lastMessage.isSimmerActive()) {
                message.setId(lastMessage.getId());
                messages.set(messages.size() - 1, message);
                notifyItemChanged(messages.size() - 1);
            }
        } else {
            message.setId(nextId++);
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    public void setOnPositionListener() {
        int currentPosition = selectedPosition + 1;
        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < messages.size()) {
            Message message = messages.get(currentPosition);
            try {
                listener.onItemClick(null, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ((Activity) context).runOnUiThread(() -> {
            int previousPosition = selectedPosition;
            selectedPosition = currentPosition;
            notifyItemChanged(previousPosition, "selection_change");
            notifyItemChanged(selectedPosition, "selection_change");
            scrollTextViewToTop(recyclerView, selectedPosition);
           // recyclerView.smoothScrollToPosition(selectedPosition);
        });
    }

    private void scrollToBottom() {
        if (recyclerView != null) {
            scrollTextViewToTop(recyclerView, messages.size() - 1);
          //  recyclerView.post(() -> n(messages.size()recyclerView.smoothScrollToPositio - 1));
        }
    }

    public void scrollTextViewToTop(final RecyclerView recyclerView, final int position) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                View view = layoutManager.findViewByPosition(position);

                if (view != null) {
                    int screenHeight = recyclerView.getHeight();
                    int targetPosition = (int) (screenHeight * 0.2); // 20% from the top (80% of screen height)
                    int viewTop = view.getTop();
                    int offset = viewTop - targetPosition;

                    recyclerView.smoothScrollBy(0, offset);
                } else {
                    // If the view is not currently visible, scroll to its position first
                    recyclerView.scrollToPosition(position);

                    // Then post a delayed action to adjust the scroll position
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollTextViewToTop(recyclerView, position);
                        }
                    });
                }
            }
        });
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0).equals("selection_change")) {
            updateSelectionState(holder, position);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);

        if (message.isSimmerActive()) {
            setupShimmerView(holder);
        } else {
            setupRegularView(holder, message, position);
        }
    }

    private void updateSelectionState(ChatViewHolder holder, int position) {
        Message message = messages.get(position);

        if (selectedPosition == position) {
            holder.lisenOff.setVisibility(View.VISIBLE);
            holder.startAnimation(holder.lisenOff);
            holder.lisenOn.setVisibility(View.GONE);
        } else {
            holder.lisenOff.setVisibility(View.GONE);
            holder.stopAnimation(holder.lisenOff);
            holder.lisenOn.setVisibility(View.VISIBLE);
        }
    }

    private void setupShimmerView(ChatViewHolder holder) {
        holder.shimmerFrameLayout.setVisibility(View.VISIBLE);
        holder.shimmerFrameLayout.startShimmer();
        holder.lisenOn.setVisibility(View.VISIBLE);
        holder.textView.setVisibility(View.GONE);
        holder.lisenOff.setVisibility(View.GONE);
        //holder.webView.setVisibility(View.GONE);
        holder.linearLayout.setBackgroundResource(R.drawable.chatbackground);
       // holder.setLinearLayoutHeight(holder.linearLayout, 250);
    }

    private void setupRegularView(ChatViewHolder holder, Message message, int position) {
        holder.shimmerFrameLayout.stopShimmer();
        holder.shimmerFrameLayout.setVisibility(View.GONE);
        holder.textView.setVisibility(View.GONE);
        holder.stopAnimation(holder.lisenOff);
        holder.lisenOn.setVisibility(View.VISIBLE);
        holder.lisenOff.setVisibility(View.GONE);

        updateSelectionState(holder, position);

        if (position < 2) {
            holder.makeResponseUIForStaticResponse(message);
        } else {
            setupMessageView(holder, message);
        }

        setupClickListeners(holder, message);
    }

    private void setupMessageView(ChatViewHolder holder, Message message) {
        if (message.isUser()) {
            //holder.webView.setVisibility(View.GONE);
            holder.textView.setText(holder.capitalizeFirstLetter(message.getText()));
            holder.textView.setVisibility(View.VISIBLE);
        } else {
            holder.textView.setVisibility(View.VISIBLE);
            setupWebView(holder, message);
        }
    }

    private void setupWebView(ChatViewHolder holder, Message message) {

        holder.textView.setText(Html.fromHtml(message.getText()));
        if(message.isScrollingEnable()) {
            scrollToBottom();
            message.setScrollingEnable(false);
        }
        accessibilityCallback.shiftAccessibilityFocusToFirstItemText(holder.getAdapterPosition(),message);

//        holder.webView.loadDataWithBaseURL(null, message.getText(), "text/html", "UTF-8", null);
//        holder.webView.post(() -> holder.webView.resizeWebView());
//        holder.webView.setVisibility(View.VISIBLE);
//        holder.webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                holder.webView.post(() -> {
//                    holder.webView.setFocusable(true);
//                    holder.webView.setFocusableInTouchMode(true);
//                    boolean focusRequested = holder.webView.requestFocus();
//                    holder.webView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
//                    // Log the focus request result
//                    Log.d("Accessibility", "WebView focus requested: " + focusRequested);
//                });
//                accessibilityCallback.shiftAccessibilityFocusToFirstItemText(holder.getAdapterPosition(),message);
//
//            }
//        });

//        holder.webView.setOnSizeChangedListener((width, height) -> {
//            if (height > 0 && message.isScrollingEnable()) {
//                scrollToBottom();
//                message.setScrollingEnable(false);
//            }
//        });
    }

    private void setupClickListeners(ChatViewHolder holder, Message message) {
        holder.lisenOn.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                int previousPosition = selectedPosition;
                selectedPosition = currentPosition;
                notifyItemChanged(previousPosition, "selection_change");
                notifyItemChanged(selectedPosition, "selection_change");
                listener.onItemClick(view, message);
            }
        });

        holder.lisenOff.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                holder.stopAnimation(holder.lisenOff);
                holder.lisenOff.setVisibility(View.GONE);
                holder.lisenOn.setVisibility(View.VISIBLE);
                onNavigateListener.navigateToAnotherScreen(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getId();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private LinearLayout linearLayout, linearLayoutMessageOutter;
        private ImageButton lisenOn, lisenOff;
        //private AutoResizeWebView webView;
        ShimmerFrameLayout shimmerFrameLayout;
        Animation breathingAnimation;

        //AutoResizeWebView autoResizeWebView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewMessage);
            linearLayout = itemView.findViewById(R.id.linearLayoutViewMessage);
            shimmerFrameLayout = itemView.findViewById(R.id.textViewMessage_shimmer);
            linearLayoutMessageOutter = itemView.findViewById(R.id.linearLayoutMessageOutter);
            lisenOn = itemView.findViewById(R.id.lisen_on);
            lisenOff = itemView.findViewById(R.id.lisen_off);
//            webView = itemView.findViewById(R.id.webViewMessage);
//            webView.setContentDescription("Response WebView");

            breathingAnimation = AnimationUtils.loadAnimation(context, R.anim.breathing_animation);



           // autoResizeWebView= itemView.findViewById(R.id.webViewMessage);
        }

        void makeResponseUIForStaticResponse(Message message) {
            textView.setText(capitalizeFirstLetter(message.getText()));
            textView.setVisibility(View.VISIBLE);
            //webView.setVisibility(View.GONE);
            linearLayout.setBackgroundResource(R.drawable.chatbackground);
            setLinearLayoutHeightToWrapContent(linearLayout);
            setLinearLayoutWidthToWrapContent(linearLayout);
            linearLayoutMessageOutter.setGravity(Gravity.START);
            linearLayoutMessageOutter.setPadding(8, 8, 8, 8); // Align to the left
        }

        void stopAnimation(ImageButton imageButton) {
            imageButton.clearAnimation();
            imageButton.setBackground(null);
        }

        void startAnimation(ImageButton imageButton) {
            imageButton.setBackgroundResource(R.drawable.circle_background_blue);
            imageButton.startAnimation(breathingAnimation);
        }

        public String capitalizeFirstLetter(String input) {
            if (input == null || input.isEmpty()) {
                return input;
            }

            int firstSpaceIndex = input.indexOf(' ');
            if (firstSpaceIndex == -1) {
                return input.substring(0, 1).toUpperCase() + input.substring(1);
            }

            String firstWord = input.substring(0, firstSpaceIndex);
            String capitalizedFirstWord = firstWord.substring(0, 1).toUpperCase() + firstWord.substring(1);
            return capitalizedFirstWord + input.substring(firstSpaceIndex);
        }

        void bind(Message message) {
            linearLayout.setBackgroundResource(message.isUser() ? R.drawable.chatbot_background_active : R.drawable.chatbackground);
            if (message.isUser()) {
                linearLayoutMessageOutter.setGravity(Gravity.END);
                setLinearLayoutHeightToWrapContent(linearLayout);
                setLinearLayoutWidthToWrapContent(linearLayout);
                linearLayoutMessageOutter.setPadding(60, 8, 8, 8); // Align to the right
            } else {
                linearLayoutMessageOutter.setGravity(Gravity.START);
                setLinearLayoutWidthToMatchContent(linearLayout);
                //webView.setVisibility(View.VISIBLE);
                linearLayoutMessageOutter.setPadding(8, 8, 8, 8); // Align to the left
            }
        }

        private void setLinearLayoutHeight(LinearLayout linearLayoutViewMessage, int heightInPixels) {
            ViewGroup.LayoutParams params = linearLayoutViewMessage.getLayoutParams();
            params.height = heightInPixels;
            linearLayoutViewMessage.setLayoutParams(params);
        }

        private void setLinearLayoutHeightToWrapContent(LinearLayout linearLayoutViewMessage) {
            ViewGroup.LayoutParams params = linearLayoutViewMessage.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            linearLayoutViewMessage.setLayoutParams(params);
        }

        private void setLinearLayoutWidthToMatchContent(LinearLayout linearLayoutViewMessage) {
            ViewGroup.LayoutParams params = linearLayoutViewMessage.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            linearLayoutViewMessage.setLayoutParams(params);
        }

        private void setLinearLayoutWidthToWrapContent(LinearLayout linearLayoutViewMessage) {
            ViewGroup.LayoutParams params = linearLayoutViewMessage.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            linearLayoutViewMessage.setLayoutParams(params);
        }
    }
}