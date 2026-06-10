package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowSearchItemsBinding;
import org.edx.mobile.discovery.model.CombinationOfSeachResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SearchViewHolder> {
    private final Context context;
    private final List<CombinationOfSeachResult> flatResults = new ArrayList<>();
    private final List<SearchCourseGroup> groups = new ArrayList<>();
    private final OnRecyclerItemClickListener listener;

    public SearchListAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    private static final class SearchCourseGroup {
        String courseName;
        boolean anyEnrolled;
        final LinkedHashMap<String, CombinationOfSeachResult> unitKeyToModel = new LinkedHashMap<>();
        final List<String> programNames = new ArrayList<>();
    }

    private void rebuildGroups() {
        groups.clear();
        if (flatResults.isEmpty()) {
            return;
        }
        LinkedHashMap<String, SearchCourseGroup> byCourse = new LinkedHashMap<>();
        for (CombinationOfSeachResult r : flatResults) {
            String courseKey = !TextUtils.isEmpty(r.getCourse_id()) ? r.getCourse_id() : r.getCourseName();
            if (TextUtils.isEmpty(courseKey)) {
                courseKey = "_unknown_" + System.identityHashCode(r);
            }
            SearchCourseGroup g = byCourse.get(courseKey);
            if (g == null) {
                g = new SearchCourseGroup();
                g.courseName = r.getCourseName();
                byCourse.put(courseKey, g);
            }
            if (r.isIs_enroll()) {
                g.anyEnrolled = true;
            }
            String unitKey = !TextUtils.isEmpty(r.getUnit_id())
                    ? r.getUnit_id()
                    : ("name:" + (r.getUnitName() != null ? r.getUnitName() : ""));
            g.unitKeyToModel.putIfAbsent(unitKey, r);

            String prog = r.getProgramName();
            if (!TextUtils.isEmpty(prog) && !g.programNames.contains(prog)) {
                g.programNames.add(prog);
            }
        }
        groups.addAll(byCourse.values());
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new SearchViewHolder(RowSearchItemsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        final SearchCourseGroup group = groups.get(position);
        String sourceString = "<b>" + group.courseName + "</b>";
        holder.itemBinding.courseName.setText(Html.fromHtml(sourceString));
        String courseDesc = TextUtils.isEmpty(group.courseName) ? "" : group.courseName;
        /*if (group.anyEnrolled) {
            courseDesc += ", " + context.getString(R.string.enrolled_in);
        }*/
        holder.itemBinding.courseName.setContentDescription(courseDesc);
        clearTapNavigation(holder.itemBinding.courseName);

        ImageView enrolledIcon = holder.itemBinding.courseEnrolledIcon;
        enrolledIcon.setVisibility(group.anyEnrolled ? View.VISIBLE : View.GONE);

        LinearLayout unitsContainer = holder.itemBinding.unitsContainer;
        unitsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);
        for (CombinationOfSeachResult unitModel : group.unitKeyToModel.values()) {
            View unitRow = inflater.inflate(R.layout.row_search_unit_row, unitsContainer, false);
            TextView unitName = unitRow.findViewById(R.id.unit_name);
            TextView viewAction = unitRow.findViewById(R.id.unit_view_action);
            String uName = unitModel.getUnitName();
            unitName.setText(TextUtils.isEmpty(uName) ? "" : uName);
            unitName.setContentDescription(TextUtils.isEmpty(uName) ? "" : uName);
            viewAction.setContentDescription(context.getString(R.string.view));
            // Navigation only from VIEW (no row/course/program tap — GestureListener would also navigate)
            viewAction.setFocusable(true);
            viewAction.setClickable(true);
            viewAction.setOnClickListener(v -> listener.onItemClick(v, unitModel));
            unitsContainer.addView(unitRow);
        }

        LinearLayout programsContainer = holder.itemBinding.programsContainer;
        programsContainer.removeAllViews();
        for (String programName : group.programNames) {
            TextView tv = new TextView(context);
            tv.setText(programName);
            tv.setTextSize(15);
            tv.setTextColor(ContextCompat.getColor(context, R.color.black));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = (int) (4 * context.getResources().getDisplayMetrics().density);
            tv.setLayoutParams(lp);
            programsContainer.addView(tv);
        }
    }

    /** Remove navigation from recycled course title (only VIEW opens content). */
    private void clearTapNavigation(TextView textView) {
        textView.setOnTouchListener(null);
        textView.setOnClickListener(null);
        ViewCompat.setAccessibilityDelegate(textView, null);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    /**
     * Count of unique units (those shown with a View button) across all course groups.
     */
    public int getFlatResultCount() {
        int count = 0;
        for (SearchCourseGroup g : groups) {
            count += g.unitKeyToModel.size();
        }
        return count;
    }

    public void setSearchResult(List<CombinationOfSeachResult> searchResultLists) {
        flatResults.clear();
        if (searchResultLists != null) {
            flatResults.addAll(searchResultLists);
        }
        rebuildGroups();
        notifyDataSetChanged();
    }

    public void updateSearchResult(List<CombinationOfSeachResult> searchResultLists) {
        if (searchResultLists != null) {
            flatResults.addAll(searchResultLists);
        }
        rebuildGroups();
        notifyDataSetChanged();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final RowSearchItemsBinding itemBinding;

        public SearchViewHolder(RowSearchItemsBinding rowSearchItemsBinding) {
            super(rowSearchItemsBinding.getRoot());
            this.itemBinding = rowSearchItemsBinding;
        }
    }
}
