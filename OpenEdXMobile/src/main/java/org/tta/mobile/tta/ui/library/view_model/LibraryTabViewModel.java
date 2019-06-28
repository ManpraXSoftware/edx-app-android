package org.tta.mobile.tta.ui.library.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxBaseAdapter;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowContentBinding;
import org.tta.mobile.databinding.TRowContentListBinding;
import org.tta.mobile.databinding.TRowContentSliderBinding;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.enums.ContentListMode;
import org.tta.mobile.tta.data.enums.ContentListType;
import org.tta.mobile.tta.data.enums.SourceType;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.tta.data.model.library.CollectionItemsResponse;
import org.tta.mobile.tta.event.ContentStatusReceivedEvent;
import org.tta.mobile.tta.event.ContentStatusesReceivedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.ContentList;
import org.tta.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.tta.mobile.tta.ui.course.CourseDashboardActivity;
import org.tta.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.tta.mobile.tta.ui.search.SearchFragment;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.tta.utils.ContentSourceUtil;
import org.tta.mobile.tta.utils.ToolTipView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class LibraryTabViewModel extends BaseViewModel {

    private Category category;
    private List<ContentList> contentLists;
    private Content selectedContent;
    private SearchPageOpenedListener searchPageOpenedListener;
    private Source source;

    private Map<Long, List<Content>> contentListMap;
    private Map<Long, ContentStatus> contentStatusMap;

    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableInt emptyImage = new ObservableInt(R.drawable.t_icon_course_130);
    public ObservableField<String> emptyMessage = new ObservableField<>();

    public LibraryTabViewModel(Context context, TaBaseFragment fragment, CollectionConfigResponse cr, Category category,
                               SearchPageOpenedListener searchPageOpenedListener) {
        super(context, fragment);
        this.category = category;
        this.searchPageOpenedListener = searchPageOpenedListener;

        contentStatusMap = new HashMap<>();
        contentLists = new ArrayList<>();
        adapter = new ListingRecyclerAdapter(mActivity);

        if (cr != null) {
            for (ContentList list: cr.getContent_list()){
                if (list.getCategory_id() == category.getId()){
                    contentLists.add(list);
                }
            }

            for (Source source: cr.getSource()){
                if (source.getId() == category.getSource_id()){
                    this.source = source;
                    break;
                }
            }
        }

        Collections.sort(contentLists);
        setEmptyView();
        getContents();
    }

    private void setEmptyView() {
        emptyMessage.set(String.format(mActivity.getString(R.string.not_available_message), category.getName()));
        emptyImage.set(ContentSourceUtil.getSourceDrawable_130x130(source == null ? "" : source.getName()));
    }

    @Override
    public void onResume() {
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void getContents(){
        mActivity.showLoading();

        Long[] listIds = new Long[contentLists.size()];
        for (int i = 0; i < contentLists.size(); i++){
            listIds[i] = contentLists.get(i).getId();
        }

        mDataManager.getCollectionItems(listIds, 0, 5,
                new OnResponseCallback<List<CollectionItemsResponse>>() {
                    @Override
                    public void onSuccess(List<CollectionItemsResponse> data) {
                        setContents(data);
                        mActivity.hideLoading();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showShortSnack(e.getLocalizedMessage());
                        toggleEmptyVisibility();
                    }
                });

    }

    private void setContents(List<CollectionItemsResponse> data){
        contentListMap = new HashMap<>();
        List<ContentList> emptyLists = new ArrayList<>();

        if (data != null){
            for (CollectionItemsResponse response: data){

                if (response.getContent() != null && !response.getContent().isEmpty()){
                    contentListMap.put(response.getId(), response.getContent());
                } else {

                    for (ContentList contentList: contentLists){
                        if (contentList.getId() == response.getId()){
                            emptyLists.add(contentList);
                            break;
                        }
                    }

                }
            }
        }
        for (ContentList contentList: emptyLists){
            contentLists.remove(contentList);
        }

        adapter.setItems(contentLists);
        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (contentLists == null || contentLists.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    private List<ContentList> getAutoLists(){
        List<ContentList> autoLists = new ArrayList<>();
        for (ContentList list: contentLists){
            if (list.getMode().equalsIgnoreCase(ContentListMode.auto.name())){
                autoLists.add(list);
            }
        }
        return autoLists;
    }

    public void showContentDashboard(){

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
        if (selectedContent.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                selectedContent.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);

            mActivity.analytic.addMxAnalytics_db(
                    selectedContent.getName(), Action.CourseOpen, Nav.library.name(),
                    org.tta.mobile.tta.analytics.analytics_enums.Source.Mobile, selectedContent.getSource_identity());

        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusesReceivedEvent event){

        for (ContentStatus status: event.getStatuses()){
            contentStatusMap.put(status.getContent_id(), status);
        }
        adapter.notifyDataSetChanged();

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event){
        boolean statusChanged = false;
        ContentStatus contentStatus = event.getContentStatus();
        if (contentStatusMap.containsKey(contentStatus.getContent_id())){
            ContentStatus prev = contentStatusMap.get(contentStatus.getContent_id());
            if (prev.getCompleted() == null && contentStatus.getCompleted() != null){
                statusChanged = true;
                prev.setCompleted(contentStatus.getCompleted());
            }
            if (prev.getStarted() == null && contentStatus.getStarted() != null){
                statusChanged = true;
                prev.setStarted(contentStatus.getStarted());
            }
        } else {
            statusChanged = true;
            contentStatusMap.put(contentStatus.getContent_id(), contentStatus);
        }
        if (statusChanged) {
            adapter.notifyDataSetChanged();
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class ListingRecyclerAdapter extends MxBaseAdapter<ContentList> {
        public ListingRecyclerAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ContentList model, @Nullable OnRecyclerItemClickListener<ContentList> listener) {
            if (binding instanceof TRowContentSliderBinding){
                TRowContentSliderBinding sliderBinding = (TRowContentSliderBinding) binding;
                sliderBinding.contentViewPager.setAdapter(new PagerAdapter() {
                    @Override
                    public int getCount() {
                        try {
                            return contentListMap.get(model.getId()).size();
                        } catch (Exception e) {
                            return 0;
                        }
                    }

                    @Override
                    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                        return view == o;
                    }

                    @Override
                    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
                        container.removeView((View) view);
                    }

                    @NonNull
                    @Override
                    public Object instantiateItem(@NonNull ViewGroup container, int position) {
                        View view = LayoutInflater.from(mActivity)
                                .inflate(R.layout.t_row_slider_item, container, false);
                        ImageView imageView = view.findViewById(R.id.slider_image);
                        Glide.with(mActivity)
                                .load(contentListMap.get(model.getId()).get(position).getIcon())
                                .placeholder(R.drawable.placeholder_course_card_image)
                                .into(imageView);
                        container.addView(view);

                        ToolTipView.showToolTip(mActivity, "fsjfjdsbfjds",imageView,Gravity.BOTTOM);

                        view.setOnClickListener(v -> {
//                            Toast.makeText(mActivity, contentListMap.get(model.getId()).get(position).getName(), Toast.LENGTH_SHORT).show();
                            selectedContent = contentListMap.get(model.getId()).get(position);
                            showContentDashboard();
                        });
                        return view;
                    }
                });
                sliderBinding.contentTabLayout.setupWithViewPager(sliderBinding.contentViewPager);

            } else if (binding instanceof TRowContentListBinding){

                TRowContentListBinding listBinding = (TRowContentListBinding) binding;
                listBinding.contentFiniteList.setTitleText(model.getName());

                ContentListAdapter listAdapter = new ContentListAdapter(mActivity);
                listAdapter.addAll(contentListMap.get(model.getId()));
                listAdapter.setItemClickListener((view, item) -> {
                    selectedContent = item;
                    showContentDashboard();
                });

                listBinding.contentFiniteList.setmMoreButtonVisible(true);
                listBinding.contentFiniteList.setOnMoreButtonClickListener(v -> {

                    int rank = BreadcrumbUtil.getCurrentRank() + 1;
                    mActivity.logD("TTA Nav ======> " +
                            BreadcrumbUtil.setBreadcrumb(rank, model.getInternal_name()));

                    mActivity.logD("TTA Nav ======> " +
                            BreadcrumbUtil.setBreadcrumb(rank + 1, Nav.see_more.name()));

                    ActivityUtil.replaceFragmentInActivity(
                            mActivity.getSupportFragmentManager(),
                            SearchFragment.newInstance(category, getAutoLists(), model),
                            R.id.dashboard_fragment,
                            SearchFragment.TAG,
                            true,
                            null
                    );
                    if (searchPageOpenedListener != null){
                        searchPageOpenedListener.onSearchPageOpened();
                    }
                });
                listBinding.contentFiniteList.setAdapter(listAdapter);
                /*listBinding.contentFiniteList.addItemDecoration(new DividerItemDecoration(
                        listBinding.contentFiniteList.getContext(),
                        listBinding.contentFiniteList.getRecyclerViewOrientation()) {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

                    }
                });*/

            }
        }

        @Override
        public int getItemLayout(int position) {
            if (getItem(position).getFormat_type().equals(ContentListType.feature.toString())){
                return R.layout.t_row_content_slider;
            } else {
                return R.layout.t_row_content_list;
            }
        }
    }

    public class ContentListAdapter extends MxFiniteAdapter<Content> {

        public ContentListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowContentBinding){
                TRowContentBinding contentBinding = (TRowContentBinding) binding;
                contentBinding.contentCategory.setText(model.getSource().getTitle());
                contentBinding.contentCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContentSourceUtil.getSourceDrawable_10x10(model.getSource().getName()),
                        0, 0, 0);
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(mActivity)
                        .load(model.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(contentBinding.contentImage);
                contentBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                if (contentStatusMap.containsKey(model.getId())){
                    ContentStatus status = contentStatusMap.get(model.getId());
                    if (status.getCompleted() != null){
                        contentBinding.contentStatusImage.setImageResource(R.drawable.t_icon_done);
                        contentBinding.contentStatusImage.setVisibility(View.VISIBLE);
                    } else if (status.getStarted() != null){
                        contentBinding.contentStatusImage.setImageResource(R.drawable.t_icon_refresh);
                        contentBinding.contentStatusImage.setVisibility(View.VISIBLE);
                    } else {
                        contentBinding.contentStatusImage.setVisibility(View.GONE);
                    }
                } else {
                    contentBinding.contentStatusImage.setVisibility(View.GONE);
                }
            }
        }
    }
}
