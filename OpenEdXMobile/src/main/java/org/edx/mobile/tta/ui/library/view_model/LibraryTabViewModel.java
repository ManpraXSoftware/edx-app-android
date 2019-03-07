package org.edx.mobile.tta.ui.library.view_model;

import android.Manifest;
import android.content.Context;
import android.databinding.ViewDataBinding;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxBaseAdapter;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowContentBinding;
import org.edx.mobile.databinding.TRowContentListBinding;
import org.edx.mobile.databinding.TRowContentSliderBinding;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.ContentListMode;
import org.edx.mobile.tta.data.enums.ContentListType;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.library.CollectionItemsResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.edx.mobile.tta.ui.course.CourseDashboardFragment;
import org.edx.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.edx.mobile.tta.ui.search.SearchFragment;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.ContentSourceUtil;
import org.edx.mobile.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryTabViewModel extends BaseViewModel {

    private Category category;
    private List<ContentList> contentLists;
    private Content selectedContent;
    private SearchPageOpenedListener searchPageOpenedListener;

    private Map<Long, List<Content>> contentListMap;

    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public LibraryTabViewModel(Context context, TaBaseFragment fragment, CollectionConfigResponse cr, Category category,
                               SearchPageOpenedListener searchPageOpenedListener) {
        super(context, fragment);
        this.category = category;
        this.searchPageOpenedListener = searchPageOpenedListener;

        contentLists = new ArrayList<>();
        for (ContentList list: cr.getContent_list()){
            if (list.getCategory_id() == category.getId()){
                contentLists.add(list);
            }
        }

        Collections.sort(contentLists);
        getContents();
        adapter = new ListingRecyclerAdapter(mActivity);
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

        if (selectedContent.getSource().getType().equalsIgnoreCase("edx") ||
                selectedContent.getSource().getType().equalsIgnoreCase("course")) {
            ActivityUtil.replaceFragmentInActivity(
                    mActivity.getSupportFragmentManager(),
                    CourseDashboardFragment.newInstance(selectedContent),
                    R.id.dashboard_fragment,
                    CourseDashboardFragment.TAG,
                    true,
                    null
            );
        } else {
            Bundle parameters = new Bundle();
            parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

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
                        view.setOnClickListener(v -> {
//                            Toast.makeText(mActivity, contentListMap.get(model.getId()).get(position).getName(), Toast.LENGTH_SHORT).show();
                            selectedContent = contentListMap.get(model.getId()).get(position);
                            mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
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
                    mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
                });

                listBinding.contentFiniteList.setmMoreButtonVisible(true);
                listBinding.contentFiniteList.setOnMoreButtonClickListener(v -> {
                    ActivityUtil.replaceFragmentInActivity(
                            mActivity.getSupportFragmentManager(),
                            SearchFragment.newInstance(category, getAutoLists(), model),
                            R.id.dashboard_fragment,
                            SearchFragment.TAG,
                            false,
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
            }
        }
    }
}
