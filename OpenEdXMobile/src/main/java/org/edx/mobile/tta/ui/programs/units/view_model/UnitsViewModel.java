package org.edx.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFilterDropDownBinding;
import org.edx.mobile.databinding.TRowUnitBinding;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramFilterTag;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;

import java.util.ArrayList;
import java.util.List;

public class UnitsViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    private List<CourseComponent> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public UnitsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        units = new ArrayList<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;

        unitsAdapter = new UnitsAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
        unitsAdapter.setItems(units);

        mActivity.showLoading();
        fetchFilters();
        fetchData();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void fetchFilters() {

        mDataManager.getProgramFilters(new OnResponseCallback<List<ProgramFilter>>() {
            @Override
            public void onSuccess(List<ProgramFilter> data) {
                List<ProgramFilter> removables = new ArrayList<>();
                for (ProgramFilter filter: data){
                    if (filter.getShowIn() == null || filter.getShowIn().isEmpty() ||
                            !filter.getShowIn().contains("schedule")){
                        removables.add(filter);
                    }
                }
                for (ProgramFilter filter: removables){
                    data.remove(filter);
                }

                if (!data.isEmpty()) {
                    allFilters = data;
                    filtersVisible.set(true);
                    filtersAdapter.setItems(data);
                } else {
                    filtersVisible.set(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                filtersVisible.set(false);
            }
        });

    }

    private void fetchData(){

        if (changesMade){
            skip = 0;
            unitsAdapter.reset(true);
            setUnitFilters();
        }

        fetchUnits();

    }

    private void setUnitFilters(){
        filters.clear();
        if (tags.isEmpty() || allFilters == null || allFilters.isEmpty()){
            return;
        }

        for (ProgramFilter filter: allFilters){

            List<ProgramFilterTag> selectedTags = new ArrayList<>();
            for (ProgramFilterTag tag: filter.getTags()){
                if (tags.contains(tag)){
                    selectedTags.add(tag);
                }
            }

            if (!selectedTags.isEmpty()){
                ProgramFilter pf = new ProgramFilter();
                pf.setDisplayName(filter.getDisplayName());
                pf.setInternalName(filter.getInternalName());
                pf.setId(filter.getId());
                pf.setOrder(filter.getOrder());
                pf.setShowIn(filter.getShowIn());
                pf.setTags(selectedTags);

                filters.add(pf);
            }
        }
    }

    private void fetchUnits() {

        mDataManager.getUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), take, skip,
                new OnResponseCallback<List<CourseComponent>>() {
                    @Override
                    public void onSuccess(List<CourseComponent> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        populateUnits(data);
                        unitsAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        unitsAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });

    }

    private void populateUnits(List<CourseComponent> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (CourseComponent unit : data) {
            if (!units.contains(unit)) {
                units.add(unit);
                newItemsAdded = true;
                n++;
            }
        }

        if (newItemsAdded) {
            unitsAdapter.notifyItemRangeInserted(units.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model, @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof TRowFilterDropDownBinding) {
                TRowFilterDropDownBinding dropDownBinding = (TRowFilterDropDownBinding) binding;

                List<DropDownFilterView.FilterItem> items = new ArrayList<>();
                items.add(new DropDownFilterView.FilterItem(model.getDisplayName(), null,
                        true, R.color.primary_cyan, R.drawable.t_background_tag_hollow
                ));
                for (ProgramFilterTag tag : model.getTags()) {
                    items.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                            false, R.color.white, R.drawable.t_background_tag_filled
                    ));
                }
                dropDownBinding.filterDropDown.setFilterItems(items);

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
                    if (prev != null && prev.getItem() != null){
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null){
                        tags.add((ProgramFilterTag) item.getItem());
                    }

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    fetchData();
                });
            }
        }
    }

    public class UnitsAdapter extends MxInfiniteAdapter<CourseComponent> {
        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull CourseComponent model, @Nullable OnRecyclerItemClickListener<CourseComponent> listener) {
            if (binding instanceof TRowUnitBinding) {
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);

                unitBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
