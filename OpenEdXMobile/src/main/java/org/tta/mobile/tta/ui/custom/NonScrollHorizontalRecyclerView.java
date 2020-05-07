package org.tta.mobile.tta.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class NonScrollHorizontalRecyclerView extends RecyclerView {

    public NonScrollHorizontalRecyclerView(Context context) {
        super(context);
    }

    public NonScrollHorizontalRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollHorizontalRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {

        int widthSpecCustom = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        super.onMeasure(widthSpecCustom, heightSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = getMeasuredWidth();
    }

}
