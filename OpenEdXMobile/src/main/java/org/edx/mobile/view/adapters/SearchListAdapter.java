package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.comparator.TalkBackDetector.CustomAccessibilityDelegate;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.RowSearchItemsBinding;
import org.edx.mobile.discovery.model.CombinationOfSeachResult;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.util.LocaleManager;

import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SearchViewHolder> {
    private Context context;
    private List<CombinationOfSeachResult>searchResultLists;
    private OnRecyclerItemClickListener listener;
    ClipboardService clipboardService;
    OnNavigateListener onNavigateListener;
    public SearchListAdapter(Context context, OnRecyclerItemClickListener listener, OnNavigateListener onNavigateListener) {
        this.context = context;
        this.listener = listener;
        this.onNavigateListener=onNavigateListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        clipboardService = ClipboardServiceHolder.getClipboardService(context.getApplicationContext());
        return new SearchListAdapter.SearchViewHolder(RowSearchItemsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        final CombinationOfSeachResult model = searchResultLists.get(position);
        String sourceString = "<b>" + model.getCourseName() + "</b> ";
        holder.itemBinding.courseName.setText(Html.fromHtml(sourceString));
        holder.itemBinding.courseLanguage.setText(context.getString(R.string.course_language)+" : "+LocaleManager.getLanguageResourceName(context, model.getLanguage()));
        holder.itemBinding.courseUnit.setText(model.getUnitName());
        holder.itemBinding.programName.setText(model.getProgramName());
        holder.itemBinding.tagName.setText(model.getTagName());
        holder.itemBinding.searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view,model);
            }
        });
        holder.itemBinding.arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view,model);
            }
        });
      /*  holder.itemBinding.courseName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view,model);
            }
        });
        holder.itemBinding.programName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view,model);
            }
        });
        holder.itemBinding.tagName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view,model);
            }
        });
        holder.itemBinding.courseName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = holder.itemBinding.courseName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        holder.itemBinding.programName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = holder.itemBinding.programName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        holder.itemBinding.tagName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = holder.itemBinding.tagName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
        setGestureListeners(holder.itemBinding.courseName,model);
        setGestureListeners(holder.itemBinding.programName,model);
        setGestureListeners(holder.itemBinding.tagName,model);
        setGestureListeners(holder.itemBinding.courseLanguage,model);
    }

    private void setGestureListeners(TextView textView, Object object) {
        ViewCompat.setAccessibilityDelegate(textView, new CustomAccessibilityDelegate(object,onNavigateListener));
        GestureListener gestureListener = new GestureListener(textView,object,context,onNavigateListener);
        GestureDetector gestureDetector = new GestureDetector(context, gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public int getItemCount() {
        return searchResultLists!=null? searchResultLists.size() : 0;
    }
    public void setSearchResult(List<CombinationOfSeachResult> searchResultLists) {
        this.searchResultLists = searchResultLists;
        notifyDataSetChanged();
    }
    public void updateSearchResult(List<CombinationOfSeachResult> searchResultLists) {
        this.searchResultLists.addAll(searchResultLists);
        notifyDataSetChanged();
    }
    public class SearchViewHolder extends RecyclerView.ViewHolder {
        private RowSearchItemsBinding itemBinding;

        public SearchViewHolder(RowSearchItemsBinding rowSearchItemsBinding) {
            super(rowSearchItemsBinding.getRoot());
            this.itemBinding = rowSearchItemsBinding;

        }
    }

}
