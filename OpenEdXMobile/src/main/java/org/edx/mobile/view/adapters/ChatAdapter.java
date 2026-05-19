package org.edx.mobile.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.GridClickListener;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.model.GridItem;
import org.edx.mobile.model.Message;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.common.OnAccessibilityCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private OnRecyclerItemClickListener listener;
    private GridClickListener gridClickListener;
    private OnNavigateListener onNavigateListener;
    private List<Message> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private OnAccessibilityCallback accessibilityCallback;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public ChatAdapter(Context context, OnRecyclerItemClickListener listener, OnNavigateListener onNavigateListener, RecyclerView recyclerView, OnAccessibilityCallback accessibilityCallback,GridClickListener gridClickListenerCallback) {
        this.context = context;
        this.listener = listener;
        this.onNavigateListener = onNavigateListener;
        this.recyclerView = recyclerView;
        this.accessibilityCallback = accessibilityCallback;
        this.gridClickListener=gridClickListenerCallback;
        setHasStableIds(true);
    }


    public long addMessage(Message message) {
        // If no shimmer message found or it's a user message, add as new
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
        return message.getId();
    }
    int checkIdx=0;
    public void updateMessageWithId(long messageId, String newText, List<GridItem> gridItems) {
        System.out.println(messageId+" updateMessageWithId gridItems "+checkIdx);
        checkIdx++;
        for (int i = 2; i < messages.size(); i++) {
            if (messages.get(i).getId() == messageId) {
                Message updatedMessage = messages.get(i);
                    updatedMessage.setText(newText);
                    updatedMessage.setGridItems(gridItems);
                    updatedMessage.setResponse(true);
                    updatedMessage.setSimmerActive(false);
                    updatedMessage.setUser(false);
                    updatedMessage.setScrollingEnable(true);
                    notifyItemChanged(i);
                    break;

            }
        }
    }

    public void updateMessageWithGrid(long messageId, List<GridItem> gridItems) {
        System.out.println(messageId+" updateMessageWithGrid gridItems "+messageId+" "+checkIdx);
        checkIdx++;
        for (int i = 2; i < messages.size(); i++) {
            if (messages.get(i).getId() == messageId) {
                System.out.println("messages.get(i).getId() "+messages.get(i).getId() +" match Found gridItems "+messageId+" "+checkIdx);
                Message updatedMessage = messages.get(i);
                updatedMessage.setGridItems(gridItems);
                updatedMessage.setText(updatedMessage.getText());
                updatedMessage.setResponse(true);
                updatedMessage.setSimmerActive(false);
                updatedMessage.setUser(false);
                updatedMessage.setScrollingEnable(true);
                notifyItemChanged(i);
                break;

            }
        }
    }

    public void updateMessageStatic(long messageId, String newText) {
        System.out.println(messageId+" updateMessageWithId gridItems "+checkIdx);
        checkIdx++;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId() == messageId) {
                Message updatedMessage = messages.get(i);
                updatedMessage.setText(newText);
                updatedMessage.setSimmerActive(false);
                updatedMessage.setResponse(true);
                updatedMessage.setUser(false);
                updatedMessage.setScrollingEnable(true);
                notifyItemChanged(i);
                break;

            }
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
        });
    }

    private void scrollToBottom() {
        if (recyclerView != null) {
            scrollTextViewToTop(recyclerView, messages.size() - 1);
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
                    int targetPosition = (int) (screenHeight * 0.2);
                    int viewTop = view.getTop();
                    int offset = viewTop - targetPosition;

                    recyclerView.smoothScrollBy(0, offset);
                } else {
                    recyclerView.scrollToPosition(position);
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
        holder.linearLayout.setBackgroundResource(R.drawable.chatbackground);
    }

    private void setupRegularView(ChatViewHolder holder, Message message, int position) {
        holder.shimmerFrameLayout.stopShimmer();
        holder.shimmerFrameLayout.setVisibility(View.GONE);
        holder.textView.setVisibility(View.GONE);
        holder.gridContainer.setVisibility(View.GONE);
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
            holder.textView.setText(holder.capitalizeFirstLetter(message.getText()));
            holder.textView.setVisibility(View.VISIBLE);
        } else {
            holder.textView.setVisibility(View.VISIBLE);
            setupTextView(holder, message);

        }
    }

    private void setupTextView(ChatViewHolder holder, Message message) {
        holder.textView.setText(Html.fromHtml(message.getText()));
        if(message.isScrollingEnable()) {
            scrollToBottom();
            message.setScrollingEnable(false);
        }
        accessibilityCallback.shiftAccessibilityFocusToFirstItemText(holder.getAdapterPosition(), message);
        if (message.hasGridItems() && message.isResponse()) {
            setupGridItems(holder, message.getGridItems());
        } else {
            holder.gridContainer.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners(ChatViewHolder holder, Message message) {
        holder.lisenOn.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                int previousPosition = selectedPosition;
                selectedPosition = currentPosition;
                notifyItemChanged(previousPosition, "selection_change");
                notifyItemChanged(selectedPosition, "selection_change");
                String anounceText="";
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



    private void setupGridItems(ChatViewHolder holder, List<GridItem> gridItems) {
        LinearLayout gridContainer = holder.gridContainer;
        gridContainer.removeAllViews();

        if (gridItems != null && !gridItems.isEmpty()) {
            gridContainer.setVisibility(View.VISIBLE);
            int columnsCount = 2; // Number of items per row
            int marginInDp = 4;
            int marginInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, marginInDp, context.getResources().getDisplayMetrics());

            for (int i = 0; i < gridItems.size(); i += columnsCount) {
                LinearLayout row = new LinearLayout(context);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                if (i > 0) {
                    rowParams.topMargin = marginInPx;
                }

                row.setLayoutParams(rowParams);
                row.setOrientation(LinearLayout.HORIZONTAL);
                gridContainer.addView(row);

                View leftItem = createGridItemView(gridItems, i, row);
                View rightItem = createGridItemView(gridItems, i + 1, row);

                row.addView(leftItem);
                row.addView(rightItem);

                // Ensure both items have the same height
                leftItem.post(() -> {
                    int maxHeight = Math.max(leftItem.getHeight(), rightItem.getHeight());
                    leftItem.getLayoutParams().height = maxHeight;
                    rightItem.getLayoutParams().height = maxHeight;
                    leftItem.requestLayout();
                    rightItem.requestLayout();
                });
            }
        } else {
            gridContainer.setVisibility(View.GONE);
        }
    }

    private View createGridItemView(List<GridItem> gridItems, int index, ViewGroup parent) {
        View itemView;
        if (index < gridItems.size()) {
            itemView = LayoutInflater.from(context).inflate(R.layout.chatbot_grid_item, parent, false);
            TextView textView = itemView.findViewById(R.id.textView);
            GridItem gridItem = gridItems.get(index);
            System.out.println(" gridItem.getCousreConvertedTitle()="+gridItem.getCousreConvertedTitle()+" getCourseTitle="+gridItem.getCourseTitle());
            if(gridItem.getCousreConvertedTitle()!=null&&!gridItem.getCousreConvertedTitle().isBlank()&&!gridItem.getCousreConvertedTitle().isBlank()) {
                Context localizedContext = LocaleManager.setLocale(context);
                String course = localizedContext.getString(R.string.course);
                textView.setText(course+" : "+gridItem.getCousreConvertedTitle());
            }
            else{
                textView.setText("Course : "+gridItem.getCourseTitle());
            }
            itemView.setOnClickListener(v -> {
                if (gridClickListener != null) {
                    System.out.println(
                            "GridItem Data Click:" +
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
                    gridClickListener.onGridClick(gridItem);
                    // listener.onGridItemClick(gridItem);
                }
            });
        } else {
            itemView = new View(context);
        }

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        itemParams.leftMargin = index % 2 != 0 ? (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()) : 0;
        itemView.setLayoutParams(itemParams);

        return itemView;
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
        private LinearLayout linearLayout, linearLayoutMessageOutter, gridContainer;
        private ImageButton lisenOn, lisenOff;
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
            gridContainer = itemView.findViewById(R.id.gridContainer);

            breathingAnimation = AnimationUtils.loadAnimation(context, R.anim.breathing_animation);
        }

        void makeResponseUIForStaticResponse(Message message) {
            textView.setText(capitalizeFirstLetter(message.getText()));
            textView.setVisibility(View.VISIBLE);
            linearLayout.setBackgroundResource(R.drawable.chatbackground);
            setLinearLayoutHeightToWrapContent(linearLayout);
            setLinearLayoutWidthToWrapContent(linearLayout);
            linearLayoutMessageOutter.setGravity(Gravity.START);
            linearLayoutMessageOutter.setPadding(8, 8, 8, 8);
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

            String capitalizedFirstWord="";
            try {
                capitalizedFirstWord = firstWord.substring(0, 1).toUpperCase() + firstWord.substring(1);
            }
            catch (Exception exception){
                Log.e("String error",exception.getMessage());
            }
            return capitalizedFirstWord + input.substring(firstSpaceIndex);
        }

        void bind(Message message) {
            linearLayout.setBackgroundResource(message.isUser() ? R.drawable.chatbot_background_active : R.drawable.chatbackground);
            if (message.isUser()) {
                linearLayoutMessageOutter.setGravity(Gravity.END);
                setLinearLayoutHeightToWrapContent(linearLayout);
                setLinearLayoutWidthToWrapContent(linearLayout);
                linearLayoutMessageOutter.setPadding(60, 8, 8, 8);
            } else {
                linearLayoutMessageOutter.setGravity(Gravity.START);
                setLinearLayoutWidthToMatchContent(linearLayout);
                linearLayoutMessageOutter.setPadding(8, 8, 8, 8);
            }
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