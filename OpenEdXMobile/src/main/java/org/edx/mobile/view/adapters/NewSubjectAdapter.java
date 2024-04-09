package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.comparator.TalkBackDetector.CustomAccessibilityDelegate;
import org.edx.mobile.R;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.RowSubjectsBinding;
import org.edx.mobile.discovery.model.DiscoverySubjectResult;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.util.GestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.widget.TextView;

public class NewSubjectAdapter extends RecyclerView.Adapter<NewSubjectAdapter.NewSubjectViewHolder> {
    private Context context;
    private List<DiscoverySubjectResult> discoverySubjectResults;
    private OnRecyclerItemClickListener listener;

    private ClipboardService clipboardService ;
    private int count = 0;

    OnNavigateListener onNavigateListener;

    public NewSubjectAdapter(Context context, OnRecyclerItemClickListener listener, OnNavigateListener onNavigateListener) {
        this.context = context;
        this.listener = listener;
        this.onNavigateListener=onNavigateListener;
    }

    @NonNull
    @Override
    public NewSubjectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        clipboardService = ClipboardServiceHolder.getClipboardService(context);
        return new NewSubjectAdapter.NewSubjectViewHolder(RowSubjectsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewSubjectViewHolder holder, int position) {
        final DiscoverySubjectResult model = discoverySubjectResults.get(position);
        String[] colorsTxt = context.getResources().getStringArray(R.array.subject_colors_name);
        List<Integer> colors = new ArrayList<Integer>();
        for (int i = 0; i < colorsTxt.length; i++) {
            int newColor = Color.parseColor(colorsTxt[i]);
            colors.add(newColor);
        }
        int rand = new Random().nextInt(colors.size());
        Integer color;
        if (count<=9){
             color = colors.get(count);
             count++;
        }else{
            count = 0;
            color = colors.get(count);
            count++;
        }
        model.setCardColorName(color);
        holder.itemBinding.lnSubjects.setBackgroundColor(color);
        String sourceString = "<b>" + model.getName() + "</b> ";
        holder.itemBinding.subjectName.setText(Html.fromHtml(sourceString));
        holder.itemBinding.subjectName.setFocusable(true);
        holder.itemBinding.subjectName.setClickable(true);
        holder.itemBinding.lnSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
        /*holder.itemBinding.lnSubjects.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Get the text to copy
                String textToCopy = holder.itemBinding.subjectName.getText().toString();
                // Copy the text using the clipboard service
                clipboardService.copyText(textToCopy);
                // Indicate copying success (optional)
                Toast.makeText(context, context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();

                return true;
            }
        });*/
        setGestureListeners(holder.itemBinding.subjectName,model);
    }
    private void setGestureListeners(TextView textView, Object object) {
        ViewCompat.setAccessibilityDelegate(textView, new CustomAccessibilityDelegate(object,onNavigateListener));
        GestureListener gestureListener = new GestureListener(textView,object,context,onNavigateListener);
        GestureDetector gestureDetector = new GestureDetector(context, gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }
    @Override
    public int getItemCount() {
        return discoverySubjectResults==null ? 0 : discoverySubjectResults.size();
    }

    public void setSubjects(List<DiscoverySubjectResult> discoverySubjectResults) {
        this.discoverySubjectResults = discoverySubjectResults;
        notifyDataSetChanged();
    }

    public class NewSubjectViewHolder extends RecyclerView.ViewHolder {
        private RowSubjectsBinding itemBinding;

        public NewSubjectViewHolder(RowSubjectsBinding rowSubjectsBinding) {
            super(rowSubjectsBinding.getRoot());
            this.itemBinding = rowSubjectsBinding;

        }
    }
}
