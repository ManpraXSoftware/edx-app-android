package org.tta.mobile.tta.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tta.mobile.R;
import org.tta.mobile.base.CustomTypefaceSpan;

import java.util.ArrayList;
import java.util.List;

public class DropDownFilterView extends FrameLayout
        implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
    private List<FilterItem> filterItems = new ArrayList<>();
    private OnFilterClickListener mListener;
    private MxSpinnerAdapter mxSpinnerAdapter;
    private AppCompatSpinner appCompatSpinner;
    private String mSelectedFilter;
    private boolean fromUser = false;

    public DropDownFilterView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DropDownFilterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DropDownFilterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DropDownFilterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        appCompatSpinner = new AppCompatSpinner(context);
        appCompatSpinner.setBackgroundResource(R.drawable.btn_selector_hollow);
        mxSpinnerAdapter = new MxSpinnerAdapter(context, new ArrayList<>());
        appCompatSpinner.setAdapter(mxSpinnerAdapter);
        appCompatSpinner.setOnTouchListener(this);
        appCompatSpinner.setOnItemSelectedListener(this);
        addView(appCompatSpinner);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        fromUser = true;
        return false;
    }

    public void setFilterItems(List<FilterItem> filterItems) {
        if (mxSpinnerAdapter == null)
            return;
        this.filterItems.clear();
        this.filterItems = filterItems;
        mxSpinnerAdapter.clear();
        mxSpinnerAdapter.addAll(filterItems);
        mxSpinnerAdapter.notifyDataSetChanged();
    }

    public void setOnFilterItemListener(OnFilterClickListener mListener) {
        this.mListener = mListener;
    }

    private void prepareSelected(int position) {
        for (int i = 0; i < filterItems.size(); i++) {
            filterItems.get(i).setSelected(i == position);
        }
    }

    public void setSelection(String s) {
        if (TextUtils.isEmpty(s))
            return;
        mSelectedFilter = s;
        FilterItem item = getSelectedFilter(s);
        if (item == null)
            return;
        appCompatSpinner.setBackgroundResource(item.selectedBackground);
        appCompatSpinner.setOnItemSelectedListener(null);
        appCompatSpinner.setSelection(filterItems.indexOf(item), false);
        appCompatSpinner.setOnItemSelectedListener(this);
    }

    public void setSelection(int position) {
        if (position < 0 || position > filterItems.size())
            return;

        mSelectedFilter = filterItems.get(position).name;
        appCompatSpinner.setBackgroundResource(filterItems.get(position).selectedBackground);
        appCompatSpinner.setOnItemSelectedListener(null);
        appCompatSpinner.setSelection(position, false);
        appCompatSpinner.setOnItemSelectedListener(this);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
//        parcelable.writeToParcel();
        return parcelable;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (fromUser) {
            if (parent.getItemAtPosition(position) instanceof FilterItem) {
                FilterItem filterItem = (FilterItem) parent.getItemAtPosition(position);
                mSelectedFilter = filterItem.getName();
                appCompatSpinner.setBackgroundResource(filterItem.getSelectedBackground());
                if (mListener != null) {
                    mListener.onClick(view, filterItem, position);
                }
            }
            fromUser = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private FilterItem getSelectedFilter(String filterName) {
        for (int i = 0; i < filterItems.size(); i++)
            if (filterItems.get(i).getName().trim().equalsIgnoreCase(filterName.trim()))
                return filterItems.get(i);
        return null;
    }

    public interface OnFilterClickListener {
        void onClick(View v, FilterItem item, int position);
    }

    public static class FilterItem {
        private String name;
        private Object item;
        private boolean isSelected;
        @ColorRes
        private int selectedColor;
        @DrawableRes
        private int selectedBackground;

        public FilterItem(String name, Object item, boolean isSelected, @ColorRes int selectedColor, @DrawableRes int selectedBackground) {
            this.name = name;
            this.item = item;
            this.isSelected = isSelected;
            this.selectedColor = selectedColor;
            this.selectedBackground = selectedBackground;
        }

        public FilterItem(String name, boolean isSelected, @ColorRes int selectedColor, @DrawableRes int selectedBackground) {
            this(name, null, isSelected, selectedColor, selectedBackground);
        }

        public FilterItem(String name, Object item, @ColorRes int selectedColor, @DrawableRes int selectedBackground) {
            this(name, item, false, selectedColor, selectedBackground);
        }

        public FilterItem(String name, @ColorRes int selectedColor, @DrawableRes int selectedBackground) {
            this(name, false, selectedColor, selectedBackground);
        }

        public Object getItem() {
            return item;
        }

        public void setItem(Object item) {
            this.item = item;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        @ColorRes
        public int getSelectedColor() {
            return selectedColor;
        }

        @DrawableRes
        public int getSelectedBackground() {
            return selectedBackground;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterItem item = (FilterItem) o;

            return name.equals(item.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    class MxSpinnerAdapter extends ArrayAdapter<FilterItem> {


        public MxSpinnerAdapter(@NonNull Context context, @NonNull List<FilterItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent, createTextView(parent.getContext(), 6, 6, 6, 6));
        }

        private View createView(int position, View convertView, ViewGroup parent, TextView textView) {
            final View view;

            if (convertView == null) {
                view = textView;//LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            } else {
                view = convertView;
            }
            FilterItem item = getItem(position);
            textView = (TextView) view;
            textView.setText(item.name);
          /*  if ()
            textView.setWidth(1000);
            textView.setMaxLines(1);
            textView.setHorizontallyScrolling(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);*/

//            if (item.isSelected()) {
//                textView.setBackgroundResource(item.getSelectedBackground());
//                textView.setTextColor(ContextCompat.getColor(textView.getContext(), item.getSelectedColor()));
//            } else {
//                textView.setBackgroundResource(unselectedDrawable);
//                textView.setTextColor(ContextCompat.getColor(textView.getContext(), unselectedTextColor));
//            }
            return view;
        }

        private TextView createTextView(Context context, int left, int top, int right, int bottom) {
            TextView textView = new TextView(context);
            textView.setTextSize(14);
            textView.setTypeface(new CustomTypefaceSpan(context, "Hind-Medium.ttf").getTypeface());
            textView.setTextColor(ContextCompat.getColor(context, R.color.gray_5));
            textView.setPadding(convertDpToPixel(left, context), convertDpToPixel(top, context), convertDpToPixel(right, context), convertDpToPixel(bottom, context));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            return textView;
        }

        private int convertDpToPixel(float dp, Context context) {
            return (int) (dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        }

        private float textSizeInSp(Resources res, int size) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, res.getDisplayMetrics());
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent, createTextView(parent.getContext(), 12, 6, 12, 6));
        }
    }
}