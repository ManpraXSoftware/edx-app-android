package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.comparator.TalkBackDetector.CustomAccessibilityDelegate;
import org.edx.mobile.R;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.RowTagsBinding;
import org.edx.mobile.discovery.model.TagTermResult;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.util.GestureListener;

import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    private Context context;
    private int colorCode;
    private List<TagTermResult> tagTermResults;
    private OnRecyclerItemClickListener listener;
    String userType;
    ClipboardService clipboardService;

    OnNavigateListener onNavigateListener;

    public TagsAdapter(Context context, OnRecyclerItemClickListener listener, int colorCode,OnNavigateListener onNavigateListener) {
        this.context = context;
        this.listener = listener;
        this.colorCode = colorCode;
        this.onNavigateListener=onNavigateListener;
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        clipboardService = ClipboardServiceHolder.getClipboardService(context.getApplicationContext());
        return new TagsAdapter.TagsViewHolder(RowTagsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        final TagTermResult model = tagTermResults.get(position);
        LayerDrawable layerDrawable = (LayerDrawable) context.getResources()
                .getDrawable(R.drawable.tags_side_background);
        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(R.id.gradientDrawble);
        if (userType != null) {
            if (userType.equals("teacher")) {
                int newColor = Color.parseColor("#147682");
                gradientDrawable.setColor(newColor);
            } else {
                int newColor = Color.parseColor("#FFB700");
                gradientDrawable.setColor(newColor);
            }
        } else {
            int newColor = Color.parseColor("#C8A1DE");
            gradientDrawable.setColor(newColor);
        }
        //  gradientDrawable.setColor(colorCode);
        holder.itemBinding.tagColorCode.setBackground(gradientDrawable);
        if (model.getConverted_term() != null && !model.getConverted_term().isEmpty()) {
            holder.itemBinding.tagsName.setText(model.getConverted_term());
        } else {
            holder.itemBinding.tagsName.setText(model.getTerm());
        }
        holder.itemBinding.tagCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
        setGestureListeners(holder.itemBinding.tagsName,model);
       /* holder.itemBinding.tagsName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
        holder.itemBinding.tagsName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = holder.itemBinding.tagsName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
    }

    private void setGestureListeners(TextView textView, Object object) {
        ViewCompat.setAccessibilityDelegate(textView, new CustomAccessibilityDelegate(object,onNavigateListener));
        GestureListener gestureListener = new GestureListener(textView,object,context,onNavigateListener);
        GestureDetector gestureDetector = new GestureDetector(context, gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public int getItemCount() {
        return tagTermResults == null ? 0 : tagTermResults.size();
    }

    public void setTags(List<TagTermResult> tagTermResults, String userType) {
        this.tagTermResults = tagTermResults;
        this.userType = userType;
        notifyDataSetChanged();
    }

    public class TagsViewHolder extends RecyclerView.ViewHolder {
        private RowTagsBinding itemBinding;

        public TagsViewHolder(RowTagsBinding rowTagsBinding) {
            super(rowTagsBinding.getRoot());
            this.itemBinding = rowTagsBinding;

        }
    }
}


