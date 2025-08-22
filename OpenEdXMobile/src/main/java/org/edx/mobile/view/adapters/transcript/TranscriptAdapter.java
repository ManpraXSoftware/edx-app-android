package org.edx.mobile.view.adapters.transcript;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

import subtitleFile.Caption;

public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.ViewHolder> {
    private final Context context;
    private final IEdxEnvironment environment;
    private final List<Caption> items = new ArrayList<>();

    @ColorInt
    private final int SELECTED_TRANSCRIPT_COLOR;
    @ColorInt
    private final int UNSELECTED_TRANSCRIPT_COLOR;

    private int selectedPosition = -1;

    private OnTranscriptClickListener onTranscriptClickListener;

    @Inject
    public TranscriptAdapter(Context context, IEdxEnvironment environment, OnTranscriptClickListener listener) {
        this.context = context;
        this.environment = environment;
        this.onTranscriptClickListener = listener;
        SELECTED_TRANSCRIPT_COLOR = ContextCompat.getColor(context, R.color.edx_brand_gray_dark);
        UNSELECTED_TRANSCRIPT_COLOR = ContextCompat.getColor(context, R.color.edx_brand_primary_base);
    }

    public void setItems(List<Caption> captions) {
        items.clear();
        if (captions != null) {
            items.addAll(captions);
        }
        notifyDataSetChanged();
    }

    public Caption getItem(int position) {
        return items.get(position);
    }

    public void unselectAll() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void select(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public boolean isSelected(int position) {
        return position == selectedPosition;
    }

    @NonNull
    @Override
    public TranscriptAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_transcript_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TranscriptAdapter.ViewHolder holder, int position) {
        Caption model = items.get(position);
        String captionText = model.content;
        if (captionText.endsWith("<br />")) {
            captionText = captionText.substring(0, captionText.length() - 6);
        }
        holder.transcriptTv.setText(TextUtils.formatHtml(captionText));

        if (isSelected(position)) {
            holder.transcriptTv.setTextColor(SELECTED_TRANSCRIPT_COLOR);
            holder.transcriptTv.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            holder.transcriptTv.setTextColor(UNSELECTED_TRANSCRIPT_COLOR);
            holder.transcriptTv.setTypeface(Typeface.DEFAULT);
        }
        holder.transcriptTv.setOnClickListener(v -> {
                if (onTranscriptClickListener != null) {
                    onTranscriptClickListener.onTranscriptClicked(position, items.get(position));
                }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView transcriptTv;

        public ViewHolder(View itemView) {
            super(itemView);
            transcriptTv = itemView.findViewById(R.id.transcript_item);
        }
    }

    public interface OnTranscriptClickListener {
        void onTranscriptClicked(int position, Caption caption);
    }


}

