package org.tta.mobile.tta.ui.search.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowAgendaContentBinding;
import org.tta.mobile.databinding.TRowFilterSectionBinding;
import org.tta.mobile.databinding.TRowFilterTagBinding;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.enums.ContentListMode;
import org.tta.mobile.tta.data.enums.SourceType;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.ContentList;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.tta.data.model.search.FilterSection;
import org.tta.mobile.tta.data.model.search.FilterTag;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.data.model.search.TagSourceCount;
import org.tta.mobile.tta.event.ContentStatusReceivedEvent;
import org.tta.mobile.tta.event.ContentStatusesReceivedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.assistant.AssistantFragment;
import org.tta.mobile.tta.ui.base.BaseArrayAdapter;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.tta.mobile.tta.ui.course.CourseDashboardActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.ContentSourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class SearchViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;
    private static final String TAG_VALUE_SOURCE = "tag_value_source";
    private static final String TAG_VALUE_CLASS_RANGE = "tag_value_class_range";

    private int take, skip;
    private boolean isPriority;
    private boolean filtersReceived, contentListsReceived;
    private boolean changesMade;
    private boolean switchedPriority;

    private Content selectedContent;
    private List<FilterTag> tags;
    private List<Content> contents;
    private Category defaultCategory;
    private Category selectedCategory;
    private SearchFilter searchFilter;
    private List<ContentList> defaultContentLists;
    private List<ContentList> currentContentLists;
    private ContentList selectedContentList;
    private List<FilterSection> filterSections;
    private List<Content> tempContents;
    private Map<Long, ContentStatus> contentStatusMap;

    public ObservableField<String> searchText = new ObservableField<>("");
    public ObservableField<String> contentListText = new ObservableField<>();
    public ObservableBoolean filterSelected = new ObservableBoolean();
    public ObservableBoolean contentListSelected = new ObservableBoolean();
    public ObservableBoolean tagsLayoutVisible = new ObservableBoolean();
    public ObservableInt selectedContentListPosition = new ObservableInt(0);
    public ObservableBoolean emptyVisible = new ObservableBoolean();


    public ObservableField<String> searchToolTip;
    public ObservableInt toolTipGravity;

    public ObservableField<String> selectedToolTip;
    public ObservableInt selectedToolTipGravity;

    public SearchedContentsAdapter contentsAdapter;
    public RecyclerView.LayoutManager contentsLayoutManager;
    public TagsAdapter tagsAdapter;
    public SearchFilterAdapter filterAdapter;
    public RecyclerView.LayoutManager filterLayoutManager;
    public ContentListsAdapter contentListsAdapter;
    private boolean isAllLoaded=false;

    public SourcesAdapter sourcesAdapter;
    public ObservableInt selectedSourcePosition = new ObservableInt(-1);
    private Source selectedSource;
    private List<Source> sources;
    private FilterTag sourceTag;

    public ClassesAdapter classesAdapter;
    private Map<String, List<FilterTag>> classTagsMap;
    private FilterSection classSection;
    private List<String> selectedClassRanges;

    private List<FilterSection> currentSections;

    private CollectionConfigResponse cr;

    public AdapterView.OnItemClickListener contentListClickListener = (parent, view, position, id) -> {
        selectedContentListPosition.set(position);
        selectedContentList = (ContentList) parent.getItemAtPosition(position);
//        contentListText.set(selectedContentList.getName());
        changesMade = true;
    };

    public AdapterView.OnItemClickListener sourceClickListener = (parent, view, position, id) -> {
        changesMade = true;
        if (selectedSourcePosition.get() != position) {
            selectedSourcePosition.set(position);
            selectedSource = (Source) parent.getItemAtPosition(position);

            tags.remove(sourceTag);
            sourceTag.setDisplay_name(selectedSource.getTitle());
            tags.add(0, sourceTag);
            populateTags();
        } else {
            selectedSourcePosition.set(-1);
            selectedSource = null;

            tags.remove(sourceTag);
            populateTags();
        }

        setSelectedContentList();
        populateFilters();
    };

    public AdapterView.OnItemClickListener classClickListener = (parent, view, position, id) -> {
        String range = classesAdapter.getItem(position);
        if (selectedClassRanges.contains(range)){
            selectedClassRanges.remove(range);
        } else {
            selectedClassRanges.add(range);
        }
        changesMade = true;
    };

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (isAllLoaded)
            return false;
        skip++;
        search();
        return true;
    };

    public SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (!searchText.get().equalsIgnoreCase(query)) {
                searchText.set(query);
                changesMade = true;
            }
            hideFilters();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            searchText.set(newText);
            changesMade = true;
            if (newText == null || newText.equals("")){
                hideFilters();
            }
            return false;
        }
    };

    public SearchViewModel(Context context, TaBaseFragment fragment, Category category,
                           List<ContentList> contentLists, ContentList selectedContentList,
                           Source source, CollectionConfigResponse cr) {
        super(context, fragment);
        tags = new ArrayList<>();
        contents = new ArrayList<>();
        tempContents = new ArrayList<>();
        selectedCategory = category;
        currentContentLists = contentLists;
        this.selectedContentList = selectedContentList;
        if (selectedContentList != null){
            contentListText.set(selectedContentList.getName());
        } else {
            contentListText.set("");
        }
        selectedSource = source;
        this.cr = cr;
        contentStatusMap = new HashMap<>();

        classTagsMap = new LinkedHashMap<>();
        setClassTags();
        selectedClassRanges = new ArrayList<>();

        currentSections = new ArrayList<>();

        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        isPriority = true;
        changesMade = true;

        sourcesAdapter = new SourcesAdapter(mActivity, R.layout.t_row_single_choice_item);
        classesAdapter = new ClassesAdapter(mActivity, R.layout.t_row_multi_choice_item);
        classesAdapter.setItems(new ArrayList<>(classTagsMap.keySet()));

        contentsAdapter = new SearchedContentsAdapter(mActivity);
        tagsAdapter = new TagsAdapter(mActivity);
        filterAdapter = new SearchFilterAdapter(mActivity);
        contentListsAdapter = new ContentListsAdapter(mActivity, R.layout.t_row_single_choice_item);

        contentsAdapter.setItemClickListener((view, item) -> {
            selectedContent = item;
            showContentDashboard();
        });

        sourceTag = new FilterTag();
        sourceTag.setValue(TAG_VALUE_SOURCE);
        if (selectedSource != null){
            sourceTag.setDisplay_name(selectedSource.getTitle());
            tags.add(sourceTag);
        }

        tagsAdapter.setItems(tags);
        populateTags();
        tagsAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.tag_card:
                    changesMade = true;
                    tags.remove(item);

                    if (item.getValue().equals(TAG_VALUE_SOURCE)){
                        /*selectedCategory = defaultCategory;
                        currentContentLists = defaultContentLists;
                        contentListsAdapter.setItems(currentContentLists);
                        int i;
                        for (i = 0; i < currentContentLists.size(); i++){
                            if (this.selectedContentList.getName().equalsIgnoreCase(currentContentLists.get(i).getName())){
                                this.selectedContentList = currentContentLists.get(i);
                                selectedContentListPosition.set(i);
                                break;
                            }
                        }
                        if (i >= currentContentLists.size()){
                            this.selectedContentList = currentContentLists.get(0);
                            selectedContentListPosition.set(0);
                        }
                        contentListText.set(this.selectedContentList.getName());*/

                        selectedSource = null;
                        selectedSourcePosition.set(-1);
                        setSelectedContentList();
                        populateFilters();
                    } else {
                        filterAdapter.notifyDataSetChanged();
                    }

                    populateTags();
                    take = DEFAULT_TAKE;
                    skip = DEFAULT_SKIP;
                    isPriority = true;
                    isAllLoaded = false;
                    mActivity.showLoading();
                    search();
                    break;
            }
        });

        loadFilters();
        loadDefaultCategory();

        loadSources();
    }

    private void setToolTip(){
        if (!mDataManager.getAppPref().isSearchVisited()){
            searchToolTip = new ObservableField<>("विशिष्ट सामग्री चुनने के\n लिए यहाँ दबाये");
            selectedToolTip = new ObservableField<>("चुनिंदा क्रम में देखे");
            toolTipGravity = new ObservableInt(Gravity.BOTTOM);
            selectedToolTipGravity = new ObservableInt(Gravity.BOTTOM);
            mDataManager.getAppPref().setSearchVisited(true);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        contentsLayoutManager = new GridLayoutManager(mActivity, 2);
        filterLayoutManager = new LinearLayoutManager(mActivity);
    }

    private void setClassTags() {

        List<FilterTag> tags;
        FilterTag tag;

        tags = new ArrayList<>();
        tag = new FilterTag();
        tag.setDisplay_name("1");
        tag.setValue("1");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("2");
        tag.setValue("2");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("3");
        tag.setValue("3");
        tags.add(tag);
        classTagsMap.put("KG-3", tags);

        tags = new ArrayList<>();
        tag = new FilterTag();
        tag.setDisplay_name("4");
        tag.setValue("4");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("5");
        tag.setValue("5");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("6");
        tag.setValue("6");
        tags.add(tag);
        classTagsMap.put("4-6", tags);

        tags = new ArrayList<>();
        tag = new FilterTag();
        tag.setDisplay_name("7");
        tag.setValue("7");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("8");
        tag.setValue("8");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("9");
        tag.setValue("9");
        tags.add(tag);
        classTagsMap.put("7-9", tags);

        tags = new ArrayList<>();
        tag = new FilterTag();
        tag.setDisplay_name("10");
        tag.setValue("10");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("11");
        tag.setValue("11");
        tags.add(tag);
        tag = new FilterTag();
        tag.setDisplay_name("12");
        tag.setValue("12");
        tags.add(tag);
        classTagsMap.put("10-12", tags);

    }

    private void loadSources() {

        mDataManager.getSources(new OnResponseCallback<List<Source>>() {
            @Override
            public void onSuccess(List<Source> data) {
                sources = data;
                sourcesAdapter.setItems(sources);
                if (selectedSource == null) {
                    selectedSourcePosition.set(-1);
                } else {
                    selectedSourcePosition.set(sources.indexOf(selectedSource));
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void loadDefaultCategory() {

        mDataManager.getCategoryFromLocal(0, new OnResponseCallback<Category>() {
            @Override
            public void onSuccess(Category data) {
                defaultCategory = data;
                if (selectedCategory == null){
                    selectedCategory = defaultCategory;
                }
                loadDefaultContentLists();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void loadDefaultContentLists() {

        mDataManager.getContentListsFromLocal(defaultCategory.getId(), ContentListMode.auto.name(),
                new OnResponseCallback<List<ContentList>>() {
            @Override
            public void onSuccess(List<ContentList> data) {
                defaultContentLists = data;
                if (currentContentLists == null){
                    currentContentLists = defaultContentLists;
                }
                /*if (selectedContentList == null){
                    selectedContentList = currentContentLists.get(0);
                }
                contentListText.set(selectedContentList.getName());*/
                populateContentLists();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void setSelectedContentList(){

        if (selectedContentList != null && cr != null){
            long sourceId = selectedSource == null ? 0 : selectedSource.getId();
            for (Category cat: cr.getCategory()){
                if (cat.getSource_id() == sourceId){
                    int i = 0;
                    for (ContentList list: cr.getContent_list()){
                        if (list.getCategory_id() == cat.getId() &&
                                list.getName().equals(selectedContentList.getName())){
                            selectedContentList = list;
                            break;
                        }
                        i++;
                    }
                    if (i == cr.getContent_list().size()){
                        selectedContentList = null;
                        contentListText.set("");
                    }
                    break;
                }
            }
        }

    }

    private void loadFilters() {
        mActivity.showLoading();
        setToolTip();
        mDataManager.getSearchFilter(new OnResponseCallback<SearchFilter>() {
            @Override
            public void onSuccess(SearchFilter data) {

                List<FilterSection> removables = new ArrayList<>();
                for (FilterSection section: data.getResult()){
                    if (section.isIn_profile()){
                        removables.add(section);
                    }
                    if (classSection == null && section.getName().contains("कक्षा")){
                        classSection = section;
                    }
                }
                for (FilterSection section: removables){
                    data.getResult().remove(section);
                }
                removables.clear();

                searchFilter = data;
                populateFilters();

                filtersReceived = true;
                if (contentListsReceived){
                    isAllLoaded = false;
                    search();
                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    public void showContentDashboard(){

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
        if (selectedContent.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                selectedContent.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);
        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }

    public void showFilters(){
        contentListSelected.set(false);
        filterSelected.set(!filterSelected.get());

        if (!filterSelected.get()){

        }
    }

    public void showContentLists(){
        filterSelected.set(false);
        contentListSelected.set(!contentListSelected.get());

        if (!contentListSelected.get()){

        }
    }

    private void populateFilters(){
        currentSections.clear();
        List<FilterTag> currentTags = new ArrayList<>();
        for (FilterSection section: searchFilter.getResult()){

            List<FilterTag> tags = new ArrayList<>();
            for (FilterTag tag: section.getTags()){
                if (tag.getSources() != null && !tag.getSources().isEmpty()) {
                    if (selectedSource == null){
                        tags.add(tag);
                        currentTags.add(tag);
                    } else {
                        for (TagSourceCount sourceCount: tag.getSources()){
                            if (sourceCount.getSource_id() == selectedSource.getId()){
                                tags.add(tag);
                                currentTags.add(tag);
                                break;
                            }
                        }
                    }
                }
            }

            if (!tags.isEmpty()){
                FilterSection s = new FilterSection();
                s.setId(section.getId());
                s.setName(section.getName());
                s.setTags(tags);
                currentSections.add(s);
            }
        }
        filterAdapter.setItems(currentSections);

        List<FilterTag> removableTags = new ArrayList<>();
        for (FilterTag tag: tags){
            if (!tag.getValue().equals(TAG_VALUE_SOURCE) &&
                    !tag.getValue().equals(TAG_VALUE_SOURCE) && !currentTags.contains(tag)){
                removableTags.add(tag);
            }
        }
        for (FilterTag tag: removableTags){
            tags.remove(tag);
        }

        populateTags();
        filterAdapter.notifyDataSetChanged();

    }

    private void populateContentLists(){
        /*if (!currentContentLists.contains(selectedContentList)){
            currentContentLists.add(selectedContentList);
        }
        contentListsAdapter.setItems(currentContentLists);
        selectedContentListPosition.set(currentContentLists.indexOf(selectedContentList));*/

        contentListsReceived = true;
        if (filtersReceived){
            isAllLoaded = false;
            search();
        }
    }

    private void populateTags(){
        tagsAdapter.notifyDataSetChanged();
        if (tags.isEmpty()){
            tagsLayoutVisible.set(false);
        } else {
            tagsLayoutVisible.set(true);
        }
    }

    private void setFilterSections(){

        if (filterSections == null){
            filterSections = new ArrayList<>();
        } else {
            filterSections.clear();
        }

        for (FilterSection section: currentSections){
            for (FilterTag tag: section.getTags()){
                if (tags.contains(tag)){
                    if (filterSections.contains(section)){
                        FilterSection s = filterSections.get(filterSections.indexOf(section));
                        s.getTags().add(tag);
                    } else {
                        FilterSection s = new FilterSection();
                        s.setId(section.getId());
                        s.setName(section.getName());
                        s.setOrder(section.getOrder());
                        List<FilterTag> tags = new ArrayList<>();
                        tags.add(tag);
                        s.setTags(tags);
                        filterSections.add(s);
                    }
                }
            }
        }

        if (!selectedClassRanges.isEmpty()){
            classSection.getTags().clear();
            for (String range: selectedClassRanges){
                classSection.getTags().addAll(classTagsMap.get(range));
            }
            filterSections.add(classSection);
        }

    }

    public void hideFilters(){
        filterSelected.set(false);
        contentListSelected.set(false);
        if (changesMade) {
            isAllLoaded = false;
            mActivity.showLoading();
            search();
        }
    }

    private void search(){
        /*if (selectedContentList == null){
            return;
        }*/

        if (changesMade){
            skip = 0;
            isPriority = true;
            contentsAdapter.reset(true);

            setFilterSections();

            StringBuilder builder = new StringBuilder();
            if (selectedContentList != null) {
                builder.append(selectedContentList.getName()).append(", ");
            }
            builder.append(searchText.get()).append(", ");

            for (FilterSection section: filterSections){
                if (section.getTags() != null){
                    for (FilterTag tag: section.getTags()){
                        builder.append(section.getName()).append("_").append(tag).append(", ");
                    }
                }
            }

            if (builder.length() > 0){
                builder.delete(builder.length() - 2, builder.length());
            }

            mActivity.analytic.addMxAnalytics_db(builder.toString(), Action.Search, Nav.search.name(),
                    org.tta.mobile.tta.analytics.analytics_enums.Source.Mobile, null);

        }

        getSearchedContents();

    }

    private void getSearchedContents() {

        mDataManager.search(take, skip, isPriority,
                selectedContentList != null ? selectedContentList.getId() : 0,
                searchText.get(), filterSections,
                selectedSource != null ? selectedSource.getId() : 0,
                new OnResponseCallback<List<Content>>() {
                    @Override
                    public void onSuccess(List<Content> data) {
                        if (selectedContentList == null ||
                                selectedContentList.getMode().equalsIgnoreCase(ContentListMode.auto.name())) {
                            mActivity.hideLoading();
                            if (data.size() < take){
                                isAllLoaded = true;
                            }
                            populateContents(data);
                            contentsAdapter.setLoadingDone();
                        } else {
                            if (isPriority){
                                if (data.size() >= take){
                                    mActivity.hideLoading();
                                    populateContents(data);
                                    contentsAdapter.setLoadingDone();
                                } else {
                                    switchedPriority = true;
                                    tempContents.clear();
                                    tempContents.addAll(data);
                                    isPriority = false;
                                    skip = 0;
                                    getSearchedContents();
                                }
                            } else {
                                mActivity.hideLoading();
                                if (data.size() < take){
                                    isAllLoaded=true;
                                }
                                if (switchedPriority){
                                    switchedPriority = false;
                                    tempContents.addAll(data);
                                    populateContents(tempContents);
                                } else {
                                    populateContents(data);
                                }
                                contentsAdapter.setLoadingDone();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        isAllLoaded = true;
                        contentsAdapter.setLoadingDone();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                        toggleEmptyVisibility();
                    }
                });

    }

    private void populateContents(List<Content> contents) {

        if (changesMade){
            this.contents = contents;
            changesMade = false;
        } else {
            for (Content content: contents){
                if (!this.contents.contains(content)) {
                    this.contents.add(content);
                }
            }
        }
        contentsAdapter.setItems(this.contents);
        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility(){
        if (contents == null || contents.isEmpty()){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusesReceivedEvent event){

        for (ContentStatus status: event.getStatuses()){
            contentStatusMap.put(status.getContent_id(), status);
        }
        contentsAdapter.notifyDataSetChanged();

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event){
        ContentStatus contentStatus = event.getContentStatus();
        if (contentStatusMap.containsKey(contentStatus.getContent_id())){
            ContentStatus prev = contentStatusMap.get(contentStatus.getContent_id());
            if (prev.getCompleted() == null && contentStatus.getCompleted() != null){
                prev.setCompleted(contentStatus.getCompleted());
            }
            if (prev.getStarted() == null && contentStatus.getStarted() != null){
                prev.setStarted(contentStatus.getStarted());
            }
        } else {
            contentStatusMap.put(contentStatus.getContent_id(), contentStatus);
        }
        contentsAdapter.notifyDataSetChanged();
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class SearchedContentsAdapter extends MxInfiniteAdapter<Content> {
        public SearchedContentsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowAgendaContentBinding){
                TRowAgendaContentBinding contentBinding = (TRowAgendaContentBinding) binding;
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

    public class TagsAdapter extends MxFiniteAdapter<FilterTag> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public TagsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull FilterTag model, @Nullable OnRecyclerItemClickListener<FilterTag> listener) {
            if (binding instanceof TRowFilterTagBinding){
                TRowFilterTagBinding tagBinding = (TRowFilterTagBinding) binding;
                tagBinding.tagCard.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
                tagBinding.tagText.setText(model.toString());
            }
        }
    }

    public class SearchFilterAdapter extends MxFiniteAdapter<FilterSection> {

        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public SearchFilterAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull FilterSection model, @Nullable OnRecyclerItemClickListener<FilterSection> listener) {
            if (binding instanceof TRowFilterSectionBinding){
                TRowFilterSectionBinding sectionBinding = (TRowFilterSectionBinding) binding;
                sectionBinding.setViewModel(model);
                sectionBinding.tagsMultiChoiceList.setAdapter(new ArrayAdapter<>(
                        mActivity, R.layout.t_row_multi_choice_item, model.getTags()
                ));

                for (int i = 0; i < model.getTags().size(); i++){
                    FilterTag tag = model.getTags().get(i);
                    if (tags.contains(tag)){
                        sectionBinding.tagsMultiChoiceList.setItemChecked(i, true);
                    }
                }

                sectionBinding.tagsMultiChoiceList.setOnItemClickListener((parent, view, position, id) -> {
                    changesMade = true;
                    FilterTag tag = (FilterTag) parent.getItemAtPosition(position);
                    if (tags.contains(tag)){
                        tags.remove(tag);
                    } else {
                        tags.add(0, tag);
                    }
                    populateTags();
                });
            }

        }

        @Override
        public int getItemLayout() {
            return R.layout.t_row_filter_section;
        }
    }

    public class ContentListsAdapter extends BaseArrayAdapter<ContentList> {
        public ContentListsAdapter(@androidx.annotation.NonNull Context context, int resource) {
            super(context, resource);
        }
    }

    public class SourcesAdapter extends BaseArrayAdapter<Source> {
        public SourcesAdapter(@androidx.annotation.NonNull Context context, int resource) {
            super(context, resource);
        }
    }

    public class ClassesAdapter extends BaseArrayAdapter<String> {
        public ClassesAdapter(@androidx.annotation.NonNull Context context, int resource) {
            super(context, resource);
        }
    }

    public void openAssistant(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new AssistantFragment(),
                R.id.dashboard_fragment,
                AssistantFragment.TAG,
                true, null

        );
    }
}
