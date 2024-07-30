package org.edx.mobile.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.model.Message;
import org.edx.mobile.util.AutoResizeWebView;
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
    private int selectedPosition = RecyclerView.NO_POSITION;
    boolean isSimmerActive=false;

    public ChatAdapter(Context context, OnRecyclerItemClickListener listener, OnNavigateListener onNavigateListener, RecyclerView recyclerView) {
        this.context = context;
        this.listener = listener;
        this.onNavigateListener = onNavigateListener;
        this.recyclerView = recyclerView;
    }

    public void addMessage(Message message) {
        if ( message.isResponse() && messages.size() > 2) {
            Message lastMessage = messages.get(messages.size() - 1);
            if (lastMessage.isSimmerActive()) {
                messages.set(messages.size() - 1, message);
                notifyItemChanged(messages.size() - 1);
            }
        } else {
            messages.add(message);
            // Notify the adapter that a new item has been inserted
            notifyItemInserted(messages.size() - 1);
        }
        //scrollToBottom();
    }

    int gab = 1;

    public void setOnPositionListener() {
        int currentPosition = selectedPosition + gab;
        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < messages.size()) {
            Message message = messages.get(currentPosition);
            try {

                listener.onItemClick(null, message);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        // Ensure the remaining code runs on the main thread
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int previousPosition = selectedPosition;
                selectedPosition = currentPosition;
                notifyItemChanged(previousPosition); // Refresh previously selected item
                notifyItemChanged(selectedPosition); // Refresh newly selected item

                recyclerView.smoothScrollToPosition(selectedPosition);
            }
        });

    }





    private void scrollToBottom() {
        if (recyclerView != null) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.smoothScrollToPosition(messages.size() - 1); // Scroll to the last position
                }
            });
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textView.setVisibility(View.GONE);
        holder.webView.setVisibility(View.GONE);
        holder.bind(message);
        if (message.isSimmerActive()) {
            holder.shimmerFrameLayout.setVisibility(View.VISIBLE);
            holder.shimmerFrameLayout.startShimmer();
            holder.lisenOn.setVisibility(View.VISIBLE);
            holder.lisenOff.setVisibility(View.GONE);
            holder.webView.setVisibility(View.GONE);

            holder.linearLayout.setBackgroundResource(R.drawable.chatbackground);
            //holder.setLinearLayoutHeightToWrapContent(holder.linearLayout);
            holder.setLinearLayoutHeight(holder.linearLayout,250);
            //holder.setLinearLayoutWidthToMatchContent(holder.linearLayout);
            //message.setSimmerActive(false);
        } else {
            holder.shimmerFrameLayout.stopShimmer();
            holder.shimmerFrameLayout.setVisibility(View.GONE);
            holder.textView.setVisibility(View.GONE);
            holder.lisenOff.setVisibility(View.GONE);
            holder.stopAnimation(holder.lisenOff);
            holder.lisenOn.setVisibility(View.VISIBLE);
            if (selectedPosition == position) {
                holder.lisenOff.setVisibility(View.VISIBLE);
                holder.startAnimation(holder.lisenOff);
                holder.lisenOn.setVisibility(View.GONE);
            }
            if (position < 2) {
                holder.makeResponseUIForStaticResponse(message);
            } else {
                if (message.isUser()) {
                    holder.webView.setVisibility(View.GONE);
                    holder.textView.setText(holder.capitalizeFirstLetter(message.getText()));
                    holder.textView.setVisibility(View.VISIBLE);
                } else {
                    holder.textView.setVisibility(View.GONE);
                    holder.webView.loadDataWithBaseURL(null, message.getText(), "text/html", "UTF-8", null);
                    holder.webView.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.webView.resizeWebView();
                        }
                    });

                    holder.webView.setVisibility(View.VISIBLE);
                    holder.webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);

//                                    holder.webView.requestFocus();
//                                    holder.webView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);// Delay focus request
                        }
                    });

                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ScheduledExecutorService", "ScheduledExecutorService................");

                            holder.webView.setFocusable(true);
//                          holder.webView.setFocusableInTouchMode(true);
                            holder.webView.requestFocus();
                            holder.webView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                            // Code to be executed after the delay
                        }
                    }, 3, TimeUnit.SECONDS); // Delay in seconds

                    holder.webView.setOnSizeChangedListener(new AutoResizeWebView.OnSizeChangedListener() {
                        @Override
                        public void onSizeChanged(int width, int height) {
                            Log.d("WebView", "New size: " + width + "x" + height);
                            if(height>0&&message.isScrollingEnable()) {
                                scrollToBottom();
                                Log.d("scrollToBottom webView", "New size: " + width + "x" + height);
                                message.setScrollingEnable(false);

//                                holder.webView.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        holder.webView.setWebViewClient(new WebViewClient() {
//                                            @Override
//                                            public void onPageFinished(WebView view, String url) {
//                                                super.onPageFinished(view, url);
//                                                view.requestFocus();
//                                            }
//                                        });
//                                        Log.d("holder.webView webView", "New size: " + width + "x" + height);
//                                        holder.webView.setFocusable(true);
//                                        holder.webView.setFocusableInTouchMode(true);
//                                        holder.webView.requestFocus();
//                                        holder.webView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
//                                    }
//                                }, 2000);

                            }
                        }
                    });
                }
            }

            holder.lisenOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        // Update the selected position and refresh the RecyclerView
                        int previousPosition = selectedPosition;
                        selectedPosition = currentPosition;
                        notifyItemChanged(previousPosition); // Refresh previously selected item
                        notifyItemChanged(selectedPosition);// Refresh newly selected item
                        gab=1;
                        listener.onItemClick(view, message);

                    }
                }
            });

            holder.lisenOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        holder.stopAnimation(holder.lisenOff);
                        holder.lisenOff.setVisibility(View.GONE);
                        holder.lisenOn.setVisibility(View.VISIBLE);
                        onNavigateListener.navigateToAnotherScreen(message);
                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private LinearLayout linearLayout, linearLayoutMessageOutter;
        private ImageButton lisenOn, lisenOff;
        private AutoResizeWebView webView;
        ShimmerFrameLayout shimmerFrameLayout;
        Animation breathingAnimation;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewMessage);
            linearLayout = itemView.findViewById(R.id.linearLayoutViewMessage);
            shimmerFrameLayout = itemView.findViewById(R.id.textViewMessage_shimmer);
            linearLayoutMessageOutter = itemView.findViewById(R.id.linearLayoutMessageOutter);
            lisenOn = itemView.findViewById(R.id.lisen_on);
            lisenOff = itemView.findViewById(R.id.lisen_off);
            webView = itemView.findViewById(R.id.webViewMessage);
            webView.setContentDescription("Response WebView");


//            webView.setOnSizeChangedListener(new AutoResizeWebView.OnSizeChangedListener() {
//                @Override
//                public void onSizeChanged(int width, int height) {
//                    Log.d("WebView", "New size: " + width + "x" + height);
//                }
//            });



            breathingAnimation = AnimationUtils.loadAnimation(context, R.anim.breathing_animation);


        }

        void makeResponseUIForStaticResponse(Message message) {
            textView.setText(capitalizeFirstLetter(message.getText()));
            textView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
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

            // Find the first word
            int firstSpaceIndex = input.indexOf(' ');

            // If there is only one word
            if (firstSpaceIndex == -1) {
                return input.substring(0, 1).toUpperCase() + input.substring(1);
            }

            // Capitalize the first letter of the first word
            String firstWord = input.substring(0, firstSpaceIndex);
            String capitalizedFirstWord = firstWord.substring(0, 1).toUpperCase() + firstWord.substring(1);

            // Return the new string with the capitalized first word
            return capitalizedFirstWord + input.substring(firstSpaceIndex);
        }

        void bind(Message message) {
            //textView.setBackgroundResource(message.isUser() ? R.drawable.profile_language_card_selection : R.drawable.chatbackground);
            linearLayout.setBackgroundResource(message.isUser() ? R.drawable.chatbot_background_active : R.drawable.chatbackground);
            if (message.isUser()) {
                linearLayoutMessageOutter.setGravity(Gravity.END);
                setLinearLayoutHeightToWrapContent(linearLayout);
                setLinearLayoutWidthToWrapContent(linearLayout);
                //setLinearLayoutHeight(linearLayout,100);
                linearLayoutMessageOutter.setPadding(60, 8, 8, 8); // Align to the right
            } else {
                //adjustHeight(webView, linearLayout);
                linearLayoutMessageOutter.setGravity(Gravity.START);
                setLinearLayoutWidthToMatchContent(linearLayout);
                webView.setVisibility(View.VISIBLE);
                // setLinearLayoutHeight(linearLayout, 400);
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
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            linearLayoutViewMessage.setLayoutParams(params);
        }
        private void setLinearLayoutWidthToWrapContent(LinearLayout linearLayoutViewMessage) {
            ViewGroup.LayoutParams params = linearLayoutViewMessage.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            linearLayoutViewMessage.setLayoutParams(params);
        }
    }
}
