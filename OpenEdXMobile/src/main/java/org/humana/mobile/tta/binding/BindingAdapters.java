package org.humana.mobile.tta.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lib.mxcalendar.models.Event;
import com.lib.mxcalendar.view.MxCalenderView;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.view.MxFiniteRecyclerView;
import com.maurya.mx.mxlib.view.MxRecyclerView;

import org.humana.mobile.R;
import org.humana.mobile.tta.tutorials.MxTooltip;
import org.humana.mobile.tta.ui.base.BaseArrayAdapter;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.tta.ui.custom.FormEditText;
import org.humana.mobile.tta.ui.custom.NonScrollListView;
import org.humana.mobile.tta.utils.BottomNavigationViewHelper;
import org.humana.mobile.tta.utils.OnSearchInSoftKeyboardListener;
import org.humana.mobile.util.SoftKeyboardUtil;

import java.lang.reflect.Constructor;
import java.util.List;


/**
 * Created by Arjun on 2018/9/18.
 */

public class BindingAdapters {
    @BindingAdapter({"android:src"})
    public static void imageSrcLoader(ImageView imageView, int id) {
        imageView.setImageResource(id);
    }

    @BindingAdapter({"android:background"})
    public static void backgroundLoader(View view, int id) {
        Context context = view.getContext();
        Drawable drawable = ContextCompat.getDrawable(context, id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    @BindingAdapter({"android:visibility"})
    public static void visibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    @BindingAdapter({"android:if"})
    public static void setViewShowOrGone(View view, Boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"android:showLoading"})
    public static void setViewShowOrHide(View view, Boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setLayoutMarginTop(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp != null) {
            lp.setMargins(lp.leftMargin, (int) margin, lp.rightMargin, lp.bottomMargin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setLayoutMarginLeft(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp != null) {
            lp.setMargins((int) margin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setLayoutMarginBottom(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp != null) {
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, (int) margin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setLayoutMarginRight(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp != null) {
            lp.setMargins(lp.leftMargin, lp.topMargin, (int) margin, lp.bottomMargin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_width")
    public static void setLayoutWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter({"android:src", "placeholder"})
    public static void setImageSrcFromUrl(ImageView imageView, String url, int placeholder) {

        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(placeholder)
                .into(imageView);
    }

    @BindingAdapter({"android:src"})
    public static void setImageSrcFromUrl(ImageView imageView, String url) {
        if (url == null) {
            return;
        }

        Glide.with(imageView.getContext())
                .load(url)
                .into(imageView);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter({"android:widthScale", "android:heightScale"})
    public static void setViewRatio(View view, int widthScale, int heightScale) {
        view.post(() -> {
            int width = view.getWidth();
            Class<?> cls = view.getLayoutParams().getClass();
            Constructor<?> constructor = null;
            try {
                constructor = cls.getConstructor(int.class, int.class);
                view.setLayoutParams((ViewGroup.LayoutParams) constructor.newInstance(width, width * heightScale / widthScale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @BindingAdapter({"android:widthScale", "android:heightScale"})
    public static void setViewGroupRatio(ViewGroup viewGroup, int widthScale, int heightScale) {
        viewGroup.post(() -> {
            int width = viewGroup.getWidth();
            Class<?> cls = viewGroup.getLayoutParams().getClass();
            Constructor<?> constructor = null;
            try {
                constructor = cls.getConstructor(int.class, int.class);
                viewGroup.setLayoutParams((ViewGroup.LayoutParams) constructor.newInstance(width, width * heightScale / widthScale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @BindingAdapter({"android:onKeyEvent"})
    public static void onKeyEvent(View view, View.OnKeyListener onKeyListener) {
        view.setOnKeyListener(onKeyListener);
    }

    @BindingAdapter("android:onEditorAction")
    public static void onEditorAction(TextView view, TextView.OnEditorActionListener onEditorActionListener) {
        view.setOnEditorActionListener(onEditorActionListener);
    }

    @BindingAdapter({"pager_adapter"})
    public static void bindViewPagerAdapter(ViewPager view, PagerAdapter adapter) {
        view.setAdapter(adapter);
    }

    @BindingAdapter({"recycler_adapter"})
    public static void bindRecyclerAdapter(RecyclerView view, RecyclerView.Adapter adapter) {
        view.setAdapter(adapter);
    }

    @BindingAdapter({"layout_manager"})
    public static void bindLayoutManager(RecyclerView view, RecyclerView.LayoutManager layoutManager) {
        view.setLayoutManager(layoutManager);
    }

    @BindingAdapter({"layout_manager"})
    public static void bindLayoutManager(MxRecyclerView view, RecyclerView.LayoutManager layoutManager) {
        view.setLayoutManager(layoutManager);
    }

    @BindingAdapter({"pager"})
    public static void bindViewPagerTabs(TabLayout view, ViewPager pagerView) {
        view.setupWithViewPager(pagerView, true);
    }

    @BindingAdapter({"page_change_listener"})
    public static void addOnPageChangeListener(ViewPager view, ViewPager.OnPageChangeListener listener) {
        view.addOnPageChangeListener(listener);
    }

    @BindingAdapter({"selected_tab_position"})
    public static void setSelectedItemPosition(ViewPager view, int position) {
        view.setCurrentItem(position);
    }

    @BindingAdapter({"text_changed_listener"})
    public static void addTextChangedListener(EditText view, TextWatcher watcher) {
        view.addTextChangedListener(watcher);
    }
    @BindingAdapter({"view_enabled"})
    public static void enableView(View view, boolean b) {
        view.setEnabled(b);
    }

    @BindingAdapter({"drawable_right"})
    public static void setDrawableRight(EditText view, int drawableId) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableId, 0);
    }

    @BindingAdapter({"on_touch_listener"})
    public static void setOnTouchListener(View view, View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @BindingAdapter({"input_type"})
    public static void setInputType(EditText view, int inputType) {
        view.setInputType(inputType);
    }

    @BindingAdapter({"password_toggle_enabled"})
    public static void setPasswordToggleEnabled(TextInputLayout view, boolean enabled) {
        view.setPasswordVisibilityToggleEnabled(enabled);
    }

    @BindingAdapter({"password_toggle_drawable"})
    public static void setPasswordToggleDrawable(TextInputLayout view, int id) {
        view.setPasswordVisibilityToggleDrawable(id);
    }

    @BindingAdapter({"bottom_nav_item_selected_listener"})
    public static void setBottomNavItemSelectedListener(BottomNavigationView view, BottomNavigationView.OnNavigationItemSelectedListener listener) {
        view.setOnNavigationItemSelectedListener(listener);
    }

    @BindingAdapter({"bottom_nav_item_selected_id"})
    public static void setBottomNavItemSelectedId(BottomNavigationView view, int id) {
        view.setSelectedItemId(id);
    }

    @BindingAdapter({"bottom_nav_enable_shift_mode"})
    public static void enableBottomNavShiftMode(BottomNavigationView view, boolean b) {
        if (!b) {
            BottomNavigationViewHelper.disableShiftMode(view);
        }
    }
    // Provide the notification badge with count created by ankit
    @BindingAdapter({"add_badge_to_bottom_nav","bottom_nav_badge_count"})
    public static void addBadgeBottomNav(BottomNavigationView view, int position, Long count) {
        if (view !=null) {
            BottomNavigationViewHelper bottomNavigationViewHelper = new BottomNavigationViewHelper();
            bottomNavigationViewHelper.addBadgeToBottomNav(view, position, count);
        }
    }


    @BindingAdapter({"finite_recycler_adapter"})
    public static void setFiniteAdapter(MxFiniteRecyclerView view, MxFiniteAdapter adapter) {
        view.setAdapter(adapter);
    }

    @BindingAdapter({"infinite_recycler_adapter"})
    public static void setInfiniteAdapter(MxRecyclerView view, MxInfiniteAdapter adapter) {
        view.setAdapter(adapter);
    }

    @BindingAdapter({"request_focus"})
    public static void requestFocus(View view, boolean b) {
        if (b) {
            view.requestFocus();
            if (view instanceof EditText) {
                ((EditText) view).setSelection(((EditText) view).getText().length());
                SoftKeyboardUtil.show((EditText) view);
            }
        }
    }

    @BindingAdapter({"form_edittext_input_type"})
    public static void setInputType(FormEditText view, int type) {
        view.setInputType(type);
    }

    @BindingAdapter({"form_password_toggle_enabled"})
    public static void setFormPasswordToggleEnabled(FormEditText view, boolean enabled) {
        view.setPasswordVisibilityToggleEnabled(enabled);
    }

    @BindingAdapter({"form_password_toggle_drawable"})
    public static void setFormPasswordToggleDrawable(FormEditText view, int id) {
        view.setPasswordVisibilityToggleDrawable(id);
    }

    @BindingAdapter({"on_key_listener"})
    public static void onSetKeyListener(View view, View.OnKeyListener listener) {
        view.setOnKeyListener(listener);
    }

    @BindingAdapter({"divider_item_decoration"})
    public static void addDividerItemDecoration(RecyclerView view, DividerItemDecoration decoration) {
        view.addItemDecoration(decoration);
    }

    @BindingAdapter({"infinite_recycler_adapter", "infinite_load_more_listener"})
    public static void setOnLoadMoreListener(MxRecyclerView view, MxInfiniteAdapter adapter, MxInfiniteAdapter.OnLoadMoreListener listener) {
        adapter.setOnLoadMoreListener(view.getRecyclerView(), listener);
        view.setAdapter(adapter);
    }

    @BindingAdapter({"on_query_text_listener"})
    public static void setOnQueryTextListener(SearchView view, SearchView.OnQueryTextListener listener) {
        view.setOnQueryTextListener(listener);
    }

    @BindingAdapter({"selected"})
    public static void setChecked(View view, boolean b) {
        view.setSelected(b);
    }

    @BindingAdapter({"selected_position"})
    public static void setSelectedPosition(NonScrollListView view, int position) {
        view.setItemChecked(position, true);
    }

    @BindingAdapter({"item_click_listener"})
    public static void setItemClickListener(NonScrollListView view, AdapterView.OnItemClickListener listener) {
        view.setOnItemClickListener(listener);
    }

    @BindingAdapter({"array_adapter"})
    public static void setArrayAdapter(NonScrollListView view, BaseArrayAdapter adapter) {
        view.setAdapter(adapter);
    }

    @BindingAdapter({"array_adapter"})
    public static void setArrayAdapter(ListView view, BaseArrayAdapter adapter) {
        view.setAdapter(adapter);
    }

    @BindingAdapter({"on_close_listener"})
    public static void setOnCloseListener(SearchView view, SearchView.OnCloseListener listener) {
        view.setOnCloseListener(listener);
    }

    @BindingAdapter({"scroll_to_position"})
    public static void setScrollToPosition(MxRecyclerView view, final int pos) {
        view.getRecyclerView().post(() -> {
            view.getRecyclerView().smoothScrollToPosition(pos);
        });
    }

    @BindingAdapter({"android:text"})
    public static void setText(TextView view, SpannableString string){
        view.setText(string);
    }

    @BindingAdapter({"on_view_more_click_listener"})
    public static void setOnViewMoreClickListener(MxFiniteRecyclerView view, View.OnClickListener listener){
        view.setOnMoreButtonClickListener(listener);
    }

    @BindingAdapter({"android:textColor"})
    public static void setTextColor(Button view, int id) {
        Context context = view.getContext();
        view.setTextColor(ContextCompat.getColor(context, id));
    }

    @BindingAdapter({"tab_position"})
    public static void setTabPosition(TabLayout view, int position){
        view.post(() -> {
            try {
                view.getTabAt(position).select();
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @BindingAdapter({"drop_down_items"})
    public static void setDropDownItems(DropDownFilterView view, List<DropDownFilterView.FilterItem> items){
        view.setFilterItems(items);
    }

    @BindingAdapter({"drop_down_listener"})
    public static void setDropDownListener(DropDownFilterView view, DropDownFilterView.OnFilterClickListener listener){
        view.setOnFilterItemListener(listener);
    }


    // Tooltip
    @BindingAdapter({"tool_tip", "tool_tip_gravity"})
    public static void showToolTip(View view, String message, int gravity) {
        if (!MxTooltip.isToolTipAdded(view))
            if (message != null && !message.trim().equals("")) {
                if (view instanceof MxFiniteRecyclerView) {
                    MxFiniteRecyclerView mxView = (MxFiniteRecyclerView) view;
                    if (gravity == Gravity.TOP) {
                        new MxTooltip.Builder(mxView.getContext())
                                .anchorView(mxView.getTitleTextView())
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.down_arrow)
                                .build()
                                .show();
                    }else {
                        new MxTooltip.Builder(mxView.getContext())
                                .anchorView(mxView.getTitleTextView())
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.up_arrow)
                                .build()
                                .show();
                    }
                } else {
                    if (gravity == Gravity.TOP) {
                        new MxTooltip.Builder(view.getContext())
                                .anchorView(view)
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.down_arrow)
                                .build()
                                .show();
                    }else {
                        new MxTooltip.Builder(view.getContext())
                                .anchorView(view)
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.up_arrow)
                                .build()
                                .show();
                    }
                }
            }
    }

    @BindingAdapter({"tool_tip", "tool_tip_gravity", "tool_tip_position"})
    public static void showToolTipPos(TabLayout view, String message, int gravity, int position) {
        view.post(() -> {
            View v = view.getChildAt(position);
            if (v != null && !MxTooltip.isToolTipAdded(v))
                if (message != null && !message.trim().equals(""))
//                    ToolTipView.showToolTip(v.getContext(), message.trim(), v, gravity);
                    if (gravity == Gravity.TOP) {
                        new MxTooltip.Builder(v.getContext())
                                .anchorView(v)
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.down_arrow)
                                .build()
                                .show();
                    }else {
                        new MxTooltip.Builder(v.getContext())
                                .anchorView(v)
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.up_arrow)
                                .build()
                                .show();
                    }
        });
    }

    @BindingAdapter({"tool_tip", "tool_tip_gravity", "tool_tip_position"})
    public static void showToolTipPos(BottomNavigationView view, String message, int gravity,
                                      int position) {
        view.post(() -> {
            View v = view.findViewById(R.id.action_library);
            if (v != null && !MxTooltip.isToolTipAdded(v))
                if (message != null && !message.trim().equals(""))
//                    ToolTipView.showToolTip(v.getContext(), message.trim(), v, gravity);
                    if (gravity == Gravity.TOP) {
                        new MxTooltip.Builder(v.getContext())
                                .anchorView(v)
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.down_arrow)
                                .build()
                                .show();
                    }else {
                        new MxTooltip.Builder(v.getContext())
                                .anchorView(v)
                                .text(message.trim())
                                .gravity(gravity)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.up_arrow)
                                .build()
                                .show();
                    }
        });
    }


    @BindingAdapter("onSearchInSoftKeyboard") // I like it to match the listener method name
    public static void setOnOkInSoftKeyboardListener(TextView view,
                                                     final OnSearchInSoftKeyboardListener listener) {
        if (listener == null) {
            view.setOnEditorActionListener(null);
        } else {
            view.setOnEditorActionListener((v, actionId, event) -> {
                listener.onSearchInSoftKeyboard();
                return true;
            });

        }
    }
   /* @BindingAdapter({"onCalendarEventListener","onCalendarDate"})
    public static void setOnCalenderEventListener(MxCalenderView view, List<Event> events,  long date) {
        if (events != null) {
            view.setEvents(events, date);
        }
    }*/

    @BindingAdapter("onCalendarRefresh")
    public static void setOnCalenderRefreshListner(MxCalenderView view, List<Event> events){
        if (events!=null){
            view.setEvents(events);
        }
    }
}
