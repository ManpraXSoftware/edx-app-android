package org.tta.mobile.tta.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.tta.mobile.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ToolTipView implements ViewTreeObserver.OnPreDrawListener, View.OnClickListener {
    public interface OnToolTipClickedListener {
        void onToolTipClicked(ToolTipView toolTipView);
    }

    private static final long ANIMATION_DURATION = 300L;
    private static final String TOOL_TIP = "tool_tip";
    private static final String REMOVE_TOOL_TIP = "Remove_tool_tip";
    private static List<WeakReference<ToolTipView>> viewsList = new ArrayList<>();
    private final View anchorView;
    private final int gravity;

    private final PopupWindow popupWindow;
    private final LinearLayout container;
    private final TextView text;
    private final ImageView arrow;

    private float pivotX;
    private float pivotY;

    private int screenSize;
    private Boolean isNormalSize = false;
    private Boolean isSmallSize = false;
    private Boolean isLargeSize = false;


    @Nullable
    private OnToolTipClickedListener listener;

    private ToolTipView(Context context, View anchorView, int gravity, ToolTip toolTip) {
        this.anchorView = anchorView;
        this.gravity = gravity;

        // TODO container should NOT capture all events
        container = new LinearLayout(context);
        container.setOnClickListener(this);

        text = new TextView(context);
//        text.setPadding(toolTip.getLeftPadding(), toolTip.getTopPadding(),
//                toolTip.getRightPadding(), toolTip.getBottomPadding());
        text.setGravity(toolTip.getTextGravity());
        text.setTextColor(toolTip.getTextColor());
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, toolTip.getTextSize());
        text.setTypeface(toolTip.getTypeface(), toolTip.getTypefaceStyle());

        screenSize = context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        final int lines = toolTip.getLines();
        if (lines > 0) {
            text.setLines(lines);
            text.setEllipsize(TextUtils.TruncateAt.END);
        }

        final CharSequence txt = TextUtils.isEmpty(toolTip.getText())
                ? context.getString(toolTip.getTextResourceId()) : toolTip.getText();
        text.setText(txt);

        final int backgroundColor = toolTip.getBackgroundColor();
        final float radius = toolTip.getCornerRadius();
        if (radius > 0.0F) {
            final GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(backgroundColor);
            drawable.setGradientType(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(radius);

            //noinspection
            text.setBackgroundDrawable(drawable);
        } else {
            text.setBackgroundColor(backgroundColor);
        }

        arrow = new ImageView(context);
        arrow.setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY));


//        switch(screenSize) {
//            case Configuration.SCREENLAYOUT_SIZE_LARGE:
//                isLargeSize = true;
//                break;
//            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
//                isNormalSize = true;
//                break;
//            case Configuration.SCREENLAYOUT_SIZE_SMALL:
//                isSmallSize = true;
//                break;
//            default:
//        }

        // TODO supports Gravity.NO_GRAVITY
        switch (gravity) {
            case Gravity.LEFT:
                container.setOrientation(LinearLayout.HORIZONTAL);
                text.setBackgroundColor(ContextCompat.getColor(context, R.color.cyan_light));
                container.addView(text, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                arrow.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_black_24dp));
                container.addView(arrow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                break;
            case Gravity.RIGHT:
                container.setOrientation(LinearLayout.HORIZONTAL);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                text.setBackgroundColor(ContextCompat.getColor(context, R.color.cyan_light));
                arrow.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_cyan_24dp));
                container.addView(arrow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                container.addView(text, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                break;
            case Gravity.TOP:
                container.setOrientation(LinearLayout.VERTICAL);
//                text.setBackgroundColor(ContextCompat.getColor(context, R.color.cyan_light));
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                container.addView(text, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                arrow.setBackground(ContextCompat.getDrawable(context, R.drawable.down_arrow));
                arrow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.arrow_background_tint));
                container.addView(arrow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                break;
//            case Gravity.BOTTOM:
//                container.setOrientation(LinearLayout.VERTICAL);
//                arrow.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_arrow_downward_black_24dp));
//                container.addView(arrow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                container.addView(text, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                break;

            case Gravity.BOTTOM:
                container.setOrientation(LinearLayout.VERTICAL);
//                arrow.setVisibility(View.GONE);
//                text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_subdirectory_arrow_right_black_24dp, 0, 0, 0);
                text.setGravity(Gravity.BOTTOM);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//                text.setBackgroundColor(ContextCompat.getColor(context, R.color.cyan_light));
                arrow.setBackground(ContextCompat.getDrawable(context, R.drawable.up_arrow));
                arrow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.arrow_background_tint));
                LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
//                layoutParams.setMargins(0,20,0,0);
                container.addView(arrow, layoutParams);
                container.addView(text, new LinearLayout.LayoutParams(layoutParams));
                break;
        }

        popupWindow = new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

    }

    /**
     * Sets a listener that will be called when the tool tip view is clicked.
     */
    public void setOnToolTipClickedListener(OnToolTipClickedListener listener) {
        this.listener = listener;
    }

    public static void showToolTip(Context context, String msg, View view, int gravity) {
        view.setTag(view.getId(), TOOL_TIP);
        ToolTip toolTip1 = new ToolTip.Builder()
                .withText(msg)
                .withTypefaceStyle(R.font.hind_semibold)
                .withCornerRadius(8.0f)
                .withBackgroundColor(ContextCompat.getColor(context, R.color.cyan_light))
//                .withPadding(15, 15, 15, 15)
                .withTextColor(ContextCompat.getColor(context, R.color.primary_cyan))
                .build();


        ToolTipView toolTipView1 = new Builder(context)
                .withAnchor(view)
                .withToolTip(toolTip1)
                .withGravity(gravity)
                .build();
        viewsList.add(new WeakReference<>(toolTipView1));
        toolTipView1.setOnToolTipClickedListener(toolTipView -> removeAll());
        toolTipView1.show();

    }

    public static void showToolTipPosition(Context context, String msg, View view, int gravity) {
//        view.setTag(view.getId(), TOOL_TIP);
        ToolTip toolTip1 = new ToolTip.Builder()
                .withText(msg)
                .withBackgroundColor(ContextCompat.getColor(context, R.color.white_list_clicked))
                .withPadding(15, 15, 15, 15)
                .withTextColor(ContextCompat.getColor(context, R.color.primary_cyan))
                .build();

        ToolTipView toolTipView1 = new ToolTipView.Builder(context)
                .withAnchor(view)
                .withToolTip(toolTip1)
                .withGravity(gravity)
                .build();
        toolTipView1.show();
    }


    /**
     * Shows the tool tip.
     */
    @UiThread
    public void show() {
        popupWindow.showAsDropDown(anchorView);
//            views.add(anchorView.getId(), anchorView);
        container.getViewTreeObserver().addOnPreDrawListener(this);
    }

    public static Boolean isToolTipAdded(View view) {
        Object obj = view.getTag(view.getId());
        return obj != null;
    }

    /**
     * Shows the tool tip with the specified delay.
     */
    public void showDelayed(long milliSeconds) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                show();
            }
        }, milliSeconds);
    }

    /**
     * Removes the tool tip view from the view hierarchy.
     *
     * @param v
     */
    @UiThread
    public void remove(View v) {
        v.setTag(v.getId(), null);
        container.setPivotX(pivotX);
        container.setPivotY(pivotY);
        container.animate().setDuration(ANIMATION_DURATION).alpha(0.0F).scaleX(0.0F).scaleY(0.0F)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        popupWindow.dismiss();
//                        toolTipView1.popupWindow.setOutsideTouchable(true);
                        popupWindow.dismiss();
                    }
                });
    }

    @UiThread
    public static void removeAll() {
        for (WeakReference<ToolTipView> view : viewsList) {
            if (view != null && view.get() != null) {
                view.get().remove(view.get().anchorView);
            }
        }
        viewsList.clear();

    }


    @Override
    public boolean onPreDraw() {
        container.getViewTreeObserver().removeOnPreDrawListener(this);

        final Context context = container.getContext();
        if (!(context instanceof Activity)) {
            return false;
        }
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int displayWidth = displayMetrics.widthPixels;
        final int displayHeight = displayMetrics.heightPixels;

        final Rect rect = new Rect();
        anchorView.getWindowVisibleDisplayFrame(rect);
        final int statusBarHeight = rect.top;

        final int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        final int anchorTop = location[1] - statusBarHeight;
        final int anchorLeft = location[0];
        final int anchorWidth = anchorView.getWidth();
        final int anchorHeight = anchorView.getHeight();

        final int textWidth = text.getWidth();
        final int textHeight = text.getHeight();
        final int arrowWidth = arrow.getWidth();
        final int arrowHeight = arrow.getHeight();

            if (gravity == Gravity.TOP || gravity == Gravity.BOTTOM) {
                final int width = Math.max(textWidth, arrowWidth);
                final int height = textHeight + arrowHeight;

                final int leftPadding;
                final int topPadding;

                if (gravity == Gravity.TOP) {
                    topPadding = anchorTop - height;
                } else {
                    // gravity == Gravity.BOTTOM
                    topPadding = anchorTop + anchorHeight;
                }

                final int anchorHorizontalCenter = anchorLeft + anchorWidth / 2;
                final int left = anchorHorizontalCenter - width / 2;
                final int right = left + width;
                leftPadding = Math.max(0, right > displayWidth ? displayWidth - width : left);

                container.setPadding(leftPadding, topPadding, 0, 0);

                final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();
                layoutParams.leftMargin = anchorHorizontalCenter - leftPadding - arrowWidth / 2;
                arrow.setLayoutParams(layoutParams);

                pivotX = anchorHorizontalCenter;
                pivotY = gravity == Gravity.TOP ? anchorTop : topPadding;
            } else {
                // gravity == Gravity.LEFT || gravity == Gravity.RIGHT

                final int width = textWidth + arrowWidth;
                final int height = Math.max(textHeight, arrowHeight);

                final int leftPadding;
                final int topPadding;
                final int rightPadding;

                if (gravity == Gravity.LEFT) {
                    leftPadding = Math.max(0, anchorLeft - width);
                    rightPadding = displayWidth - anchorLeft;
                    text.setMaxWidth(displayWidth - rightPadding - leftPadding - arrowWidth);
                } else {
                    // gravity == Gravity.RIGHT

                    leftPadding = anchorLeft + anchorWidth;
                    rightPadding = 0;
                }

                final int anchorVerticalCenter = anchorTop + anchorHeight / 2;
                final int top = anchorVerticalCenter - height / 2;
                final int bottom = top + height;
                topPadding = Math.max(0, bottom > displayHeight ? displayHeight - height : top);

                container.setPadding(leftPadding, topPadding, rightPadding, 0);

                final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();
                layoutParams.topMargin = anchorVerticalCenter - topPadding - arrowHeight / 2;
                arrow.setLayoutParams(layoutParams);

                pivotX = gravity == Gravity.LEFT ? anchorLeft : leftPadding;
                pivotY = anchorVerticalCenter;
            }

            container.setAlpha(0.0F);
            container.setPivotX(pivotX);
            container.setPivotY(pivotY);
            container.setScaleX(0.0F);
            container.setScaleY(0.0F);
            container.animate()
                    .setDuration(ANIMATION_DURATION).alpha(1.0F).scaleX(1.0F).scaleY(1.0F);

        return false;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onToolTipClicked(this);
        }
//        removeAll();
        remove(v);
    }

    /**
     * Used to build a tool tip view.
     */
    public static class Builder {
        private final Context context;
        private View anchorView;
        private int menu;
        private ToolTip toolTip;
        private int gravity = Gravity.BOTTOM;

        /**
         * Creates a new builder.
         */
        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Sets the view that the tool tip view will try to anchor.
         */
        public Builder withAnchor(View anchorView) {
            this.anchorView = anchorView;
            return this;
        }

//        public Builder withAnchor(int menu){
//            this.menu = menu;
//            return this;
//        }

        /**
         * Sets the tool tip that will be shown.
         */
        public Builder withToolTip(ToolTip toolTip) {
            this.toolTip = toolTip;
            return this;
        }

        /**
         * Sets the tool tip gravity. By default, it will be anchored to bottom of the anchor view.
         * <p/>
         * Only the following are supported: Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT,
         * Gravity.START, and Gravity.END.
         */
        public Builder withGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * Creates a tool tip view.
         */
        @UiThread
        public ToolTipView build() {
            if (context == null) {
                throw new IllegalArgumentException("Missing context");
            }
            if (anchorView == null) {
                throw new IllegalArgumentException("Missing anchor");
            }
            if (toolTip == null) {
                throw new IllegalArgumentException("Missing tooltip");
            }

            if (gravity == Gravity.START || gravity == Gravity.END) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && anchorView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    gravity = gravity == Gravity.START ? Gravity.RIGHT : Gravity.LEFT;
                } else {
                    gravity &= Gravity.HORIZONTAL_GRAVITY_MASK;
                }
            }
            if (gravity != Gravity.TOP && gravity != Gravity.BOTTOM
                    && gravity != Gravity.LEFT && gravity != Gravity.RIGHT) {
                throw new IllegalArgumentException("Unsupported gravity - " + gravity);
            }

//            if (menu == BottomNavigationView.NO_ID){
//                throw new IllegalArgumentException("Unsupported menu item - " + menu);
//            }

            return new ToolTipView(context, anchorView, gravity, toolTip);
        }
    }
}