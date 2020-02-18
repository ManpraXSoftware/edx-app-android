package org.humana.mobile.tta.ui.programs.userStatus.viewModel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.lib.mxcalendar.models.Event;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowFilterDropDownBinding;
import org.humana.mobile.databinding.TRowUnitBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.ShowIn;
import org.humana.mobile.tta.data.enums.UnitStatusType;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.SelectedFilter;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.event.program.ProgramFilterSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class UserStatusViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UserStatusAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean studentVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableField<String> name = new ObservableField<>();
    private List<DropDownFilterView.FilterItem> statusTags;

    private EnrolledCoursesResponse course;
    private String studentName;
    private List<Unit> units;
    private LinkedList<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private LinkedList<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private EnrolledCoursesResponse parentCourse;
    public ObservableField<String> toolbarTitle = new ObservableField<>();
    public Boolean showCurrentPeriodTitle;
    public Boolean isPeriodFilterSelected = false;
    public Boolean isStatusFilterSelected = false;
    private List<SelectedFilter> selectedFilter;


    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public UserStatusViewModel(BaseVMActivity activity, String studentName, EnrolledCoursesResponse course, Boolean showCurrentPeriodTitle) {
        super(activity);

        this.course = course;
        units = new ArrayList<>();
        this.studentName = studentName;
        this.showCurrentPeriodTitle = showCurrentPeriodTitle;
        name.set(studentName);
        tags = new LinkedList<>();
        filters = new LinkedList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;

        unitsAdapter = new UserStatusAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
        selectedFilter = mDataManager.getLoginPrefs().getCachedFilter();

        if (showCurrentPeriodTitle){
            toolbarTitle.set("Current Periods");
        }else {
            toolbarTitle.set("Approved Units");
        }

        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())){
            studentVisible.set(true);
        }else  studentVisible.set(false);

        unitsAdapter.setItems(units);
        unitsAdapter.setItemClickListener((view, item) -> {

            switch (view.getId()) {
                case R.id.tv_my_date:
                    String title;
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        title = mActivity.getString(R.string.proposed_date);
                    } else {
                        title = mActivity.getString(R.string.my_date);
                    }
                    showDatePicker(item, title);
                    break;
                default:
                    mActivity.showLoading();

                    boolean ssp = units.contains(item);
                    EnrolledCoursesResponse c;
                    if (ssp) {
                        c = course;
                    } else {
                        c = parentCourse;
                    }

                    if (c == null) {

                        String courseId;
                        if (ssp) {
                            courseId = mDataManager.getLoginPrefs().getProgramId();
                        } else {
                            courseId = mDataManager.getLoginPrefs().getParentId();
                        }
                        mDataManager.enrolInCourse(courseId, new OnResponseCallback<ResponseBody>() {
                            @Override
                            public void onSuccess(ResponseBody responseBody) {

                                mDataManager.getenrolledCourseByOrg("Humana", new OnResponseCallback<List<EnrolledCoursesResponse>>() {
                                    @Override
                                    public void onSuccess(List<EnrolledCoursesResponse> data) {
                                        if (courseId != null) {
                                            for (EnrolledCoursesResponse response : data) {
                                                if (response.getCourse().getId().trim().toLowerCase()
                                                        .equals(courseId.trim().toLowerCase())) {
                                                    if (ssp) {
                                                        UserStatusViewModel.this.course = response;
                                                        EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                    } else {
                                                        UserStatusViewModel.this.parentCourse = response;
                                                    }
                                                    getBlockComponent(item);
                                                    break;
                                                }
                                            }
                                            mActivity.hideLoading();
                                        } else {
                                            mActivity.hideLoading();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        mActivity.hideLoading();
                                        mActivity.showLongSnack("enroll org failure");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack("enroll failure");
                            }
                        });

                    } else {
                        getBlockComponent(item);
                    }

            }

        });

        mActivity.showLoading();
        fetchFilters();
    }

    private void showDatePicker(Unit unit, String title) {
        DateUtil.showDatePicker(mActivity, unit.getMyDate(),title, new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                mActivity.showLoading();
                mDataManager.setProposedDate(mDataManager.getLoginPrefs().getProgramId(),
                        mDataManager.getLoginPrefs().getSectionId(), data, unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
                                unit.setMyDate(data);
                                changesMade = true;
                                fetchData();
                                if (response.getSuccess()) {
                                    mActivity.showLongSnack("Proposed date set successfully");
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack(e.getLocalizedMessage());
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void getBlockComponent(Unit unit) {

        mDataManager.enrolInCourse(mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        mDataManager.getBlockComponent(unit.getId(), mDataManager.getLoginPrefs().getProgramId(),
                                new OnResponseCallback<CourseComponent>() {
                                    @Override
                                    public void onSuccess(CourseComponent data) {
                                        mActivity.hideLoading();

                                        if (UserStatusViewModel.this.course == null) {
                                            mActivity.showLongSnack("You're not enrolled in the program");
                                            return;
                                        }

                                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                    UserStatusViewModel.this.course, data.getChildren().get(0).getId(),
                                                    null, false);
                                        } else {
                                            mActivity.showLongSnack("This unit is empty");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        mActivity.hideLoading();
                                        mActivity.showLongSnack(e.getLocalizedMessage());
                                    }
                                });
                    }


                    @Override
                    public void onFailure(Exception e) {
                        mActivity.showLongSnack("error during unit enroll");
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }


    private void fetchFilters() {
        statusTags = new ArrayList<>();
        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.student_status.name(), filters,
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {
                        if (!data.isEmpty()) {
                            allFilters = data;
                            filtersVisible.set(true);
                            filtersAdapter.setItems(data);
                        } else {
                            filtersVisible.set(false);
                        }


                        if (mDataManager.getLoginPrefs().getCurrrentPeriodTitle()!=null||
                                !mDataManager.getLoginPrefs().getCurrrentPeriodTitle()
                                        .equalsIgnoreCase("")){
                            if (!isPeriodFilterSelected){
                                ProgramFilterTag tag1 = new ProgramFilterTag();
                                tag1.setDisplayName(mDataManager.getLoginPrefs().getCurrrentPeriodTitle());
                                tag1.setInternalName(mDataManager.getLoginPrefs().getCurrrentPeriodTitle());
                                tag1.setSelected(false);
                                tag1.setOrder(0);
                                tag1.setId(mDataManager.getLoginPrefs().getCurrrentPeriod());
                                SelectedFilter sf = new SelectedFilter();
                                sf.setSelected_tag(mDataManager.getLoginPrefs().getCurrrentPeriodTitle());
                                sf.setInternal_name("period_id");
                                sf.setDisplay_name("Period");
                                sf.setSelected_tag_item(tag1);
                                mDataManager.updateSelectedFilters(sf);
                            }
                        }
                        if (!isStatusFilterSelected){
                            ProgramFilterTag tag = new ProgramFilterTag();
                            tag.setDisplayName("Approved");
                            tag.setInternalName("Approved");
                            tag.setSelected(true);
                            tag.setOrder(0);
                            tag.setId(3);
                            SelectedFilter selectedFilter = new SelectedFilter();
                            selectedFilter.setSelected_tag("Approved");
                            selectedFilter.setInternal_name("status");
                            selectedFilter.setDisplay_name("Status");
                            selectedFilter.setSelected_tag_item(tag);
                            mDataManager.updateSelectedFilters(selectedFilter);
                        }

                        changesMade = true;
                        selectedFilter = mDataManager.getSelectedFilters();
                        fetchData();

                    }

                    @Override
                    public void onFailure(Exception e) {
                        filtersVisible.set(false);
                    }
                });

    }


    private void fetchData() {

        if (changesMade) {
            changesMade = false;
            skip = 0;
            unitsAdapter.reset(true);
            setUnitFilters();
        }

        fetchUnits();

    }

    private void setUnitFilters() {
        if (filters != null)
            filters.clear();
        if (allFilters == null || allFilters.isEmpty()) {
            return;
        }
        if (selectedFilter.isEmpty()) {
            for (ProgramFilter filter : allFilters) {
                for (ProgramFilterTag tag : filter.getTags()) {
                    if (tag.getSelected()) {
                        SelectedFilter sf = new SelectedFilter();
                        sf.setInternal_name(filter.getInternalName());
                        sf.setDisplay_name(filter.getDisplayName());
                        sf.setSelected_tag(tag.getDisplayName());
                        sf.setSelected_tag_item(tag);
                        mDataManager.updateSelectedFilters(sf);
                        selectedFilter.addAll(mDataManager.getSelectedFilters());
                        break;
                    }
                }
            }


        }
        for (SelectedFilter selected : selectedFilter) {
            for (ProgramFilter filter : allFilters) {
                List<ProgramFilterTag> selectedTags = new ArrayList<>();
                if (selected.getInternal_name().equalsIgnoreCase(filter.getInternalName())) {
                    for (ProgramFilterTag tag : filter.getTags()) {
                        if (selected.getSelected_tag() != null) {
                            if (selected.getSelected_tag_item()!=null) {
                                if (selected.getSelected_tag_item().equals(tag)) {
                                    selectedTags.add(tag);
                                    ProgramFilter pf = new ProgramFilter();
                                    pf.setDisplayName(filter.getDisplayName());
                                    pf.setInternalName(filter.getInternalName());
                                    pf.setId(filter.getId());
                                    pf.setOrder(filter.getOrder());
                                    pf.setShowIn(filter.getShowIn());
                                    pf.setTags(selectedTags);
                                    if (!filters.contains(pf)) {
                                        filters.add(pf);
                                    }
                                    break;
                                }
                            }else {
                                if (selected.getSelected_tag().equals(tag.getDisplayName())) {
                                    selectedTags.add(tag);
                                    ProgramFilter pf = new ProgramFilter();
                                    pf.setDisplayName(filter.getDisplayName());
                                    pf.setInternalName(filter.getInternalName());
                                    pf.setId(filter.getId());
                                    pf.setOrder(filter.getOrder());
                                    pf.setShowIn(filter.getShowIn());
                                    pf.setTags(selectedTags);
                                    if (!filters.contains(pf)) {
                                        filters.add(pf);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void fetchUnits() {
        mDataManager.getUnits(filters, "", mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole(), studentName, 0, take, skip,
                0L, 0L,
                new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
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

    private void populateUnits(List<Unit> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Unit unit : data) {
            if (!unitAlreadyAdded(unit)) {
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

    private boolean unitAlreadyAdded(Unit unit) {
        for (Unit u : units) {
            if (TextUtils.equals(u.getId(), unit.getId()) && (u.getPeriodId() == unit.getPeriodId())) {
                return true;
            }
        }
        return false;
    }

    private void toggleEmptyVisibility() {
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PeriodSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        fetchData();
    }

  @SuppressWarnings("unused")
    public void onEventMainThread(ProgramFilterSavedEvent event) {
      if (event !=null) {
          changesMade = true;
          allLoaded = false;
          filters = (LinkedList<ProgramFilter>) event.getProgramFilters();
      }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CourseEnrolledEvent event) {
        this.course = event.getCourse();
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
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
                            tag.getSelected(), R.color.white, R.drawable.t_background_tag_filled
                    ));
                }


                dropDownBinding.filterDropDown.setFilterItems(items);


//                for (int i = 0; i < items.size(); i++) {
//                    for (ProgramFilterTag tag : model.getTags()) {
//                                if (tag.getId() == mDataManager.getLoginPrefs().getCurrrentPeriod()) {
//                                    dropDownBinding.filterDropDown.setSelection(mDataManager.getLoginPrefs().getCurrrentPeriodTitle());
//                                    break;
//                        }
//                    }
//                }
//
//
//                if (model.getDisplayName().equals("Status")) {
//                    for (int i = 0; i < items.size(); i++) {
//                        if (items.get(i).getName().equals("Approved")) {
//                            dropDownBinding.filterDropDown.setSelection(i);
//                            break;
//                        }
//                    }
//                }
                if (selectedFilter != null) {
                    for (ProgramFilterTag tag : model.getTags()) {
                        for (SelectedFilter item : selectedFilter) {
                            if (item.getInternal_name().equalsIgnoreCase(model.getInternalName())) {
                                if (item.getSelected_tag_item()!=null) {
                                    if (item.getSelected_tag_item().equals(tag)) {
                                        dropDownBinding.filterDropDown.setSelection(item.getSelected_tag_item());
                                    }
                                    break;
                                }else {
                                    if (item.getSelected_tag().equals(model.getDisplayName())) {
                                        dropDownBinding.filterDropDown.setSelection(item.getDisplay_name());
                                    }

                                }
                            }
                        }
                    }
                }

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {

                    SelectedFilter sf = new SelectedFilter();
                    sf.setInternal_name(model.getInternalName());
                    sf.setDisplay_name(model.getDisplayName());
                    sf.setSelected_tag(item.getName());
                    sf.setSelected_tag_item((ProgramFilterTag) item.getItem());
                    mDataManager.updateSelectedFilters(sf);
                    if (model.getInternalName().toLowerCase().equalsIgnoreCase("period_id")){
                        isPeriodFilterSelected = true;
                        mDataManager.getLoginPrefs().setCurrrentPeriodTitle(item.getName());
                    }
                    if (model.getInternalName().toLowerCase().equalsIgnoreCase("status")){
                        isStatusFilterSelected = true;
                    }
                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    selectedFilter.clear();
                    selectedFilter = mDataManager.getSelectedFilters();

                    filters.clear();
                    for (SelectedFilter selected : selectedFilter) {
                        for (ProgramFilter filter : allFilters) {
                            List<ProgramFilterTag> selectedTags = new ArrayList<>();
                            if (selected.getInternal_name().equalsIgnoreCase(filter.getInternalName())) {
                                for (ProgramFilterTag tag : filter.getTags()) {
                                    if (selected.getSelected_tag() != null) {
                                        if (selected.getSelected_tag_item()!=null) {
                                            if (selected.getSelected_tag_item().equals(tag)) {
                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                break;
                                            }
                                        }else {
                                            if (selected.getSelected_tag().equals(tag.getDisplayName())) {
                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                break;
                                            } else if (selected.getSelected_tag().equals(filter.getDisplayName())) {
//                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    EventBus.getDefault()
                            .post(new ProgramFilterSavedEvent(filters, false));
                    fetchFilters();

                });
            }

        }
    }

    public class UserStatusAdapter extends MxInfiniteAdapter<Unit> {
        public UserStatusAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model, @Nullable OnRecyclerItemClickListener<Unit> listener) {

            if (binding instanceof TRowUnitBinding) {
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);

                unitBinding.unitCode.setText(model.getTitle());
                unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                        + model.getUnitHour() + " hrs");
                if (!model.getStatus().equals("")) {
                    if (model.getStatusDate() > 0) {
                        unitBinding.tvStaffDate.setText(model.getStatus() + ": " + DateUtil.getDisplayDate(model.getStatusDate()));
                        unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                    }
                } else {
                    unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                }
                unitBinding.tvDescription.setText(model.getDesc());

                if (!model.getStatus().equals("")) {
                    unitBinding.tvComment.setText(model.getStatus() + " comments : " + model.getComment());
                    if (model.getStatus().equals("Submitted")) {
                        unitBinding.tvComment.setVisibility(View.GONE);
                    } else {
                        unitBinding.tvComment.setVisibility(View.VISIBLE);
                    }
                } else {
                    unitBinding.tvComment.setVisibility(View.GONE);
                }


                unitBinding.checkbox.setVisibility(View.GONE);

                if (model.getMyDate() > 0) {
                    unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                } else {
                    unitBinding.tvMyDate.setText(R.string.change_date);
                }

                String role = mDataManager.getLoginPrefs().getRole();
                if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                    if (model.getStaffDate() > 0) {
                        unitBinding.tvSubmittedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                        unitBinding.tvSubmittedDate.setVisibility(View.VISIBLE);
                    } else {
                        unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                    }
                } else {
                    unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);

                }

                if (role != null && role.equals(UserRole.Student.name())) {
                    unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.student_icon,
                            0, 0, 0);
                } else {
                    unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.teacher_icon,
                            0, 0, 0);
                }


                if (model.getType().toLowerCase().equals(mActivity.getString(R.string.course).toLowerCase())) {
                    if (mDataManager.getLoginPrefs().getRole() != null
                            && mDataManager.getLoginPrefs().getRole()
                            .equals(UserRole.Student.name())) {
                        unitBinding.tvMyDate.setEnabled(false);
                    } else {
                        unitBinding.tvMyDate.setEnabled(true);
                    }
                } else {
                    unitBinding.tvMyDate.setEnabled(true);
                }


                switch (model.getStatus()) {
                    case "Submitted":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.pending));
                        break;
                    case "Approved":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_green));
                        break;
                    case "Return":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_red));
                        break;
                    case "":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        break;
                    case "None":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        break;
                }

                if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name()) &&
                        !TextUtils.isEmpty(model.getStatus())) {
                    try {
                        switch (UnitStatusType.valueOf(model.getStatus())) {
                            case Completed:
                                unitBinding.card.setBackgroundColor(
                                        ContextCompat.getColor(getContext(), R.color.secondary_green));
                                break;
                            case InProgress:
                                unitBinding.card.setBackgroundColor(
                                        ContextCompat.getColor(getContext(), R.color.humana_card_background));
                                break;
                            case Pending:
                                unitBinding.card.setBackgroundColor(ContextCompat.getColor(getContext(),
                                        R.color.material_red_500));
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        unitBinding.statusIcon.setVisibility(View.GONE);
                    }
                } else {
                    unitBinding.statusIcon.setVisibility(View.GONE);
                }

                unitBinding.tvMyDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                unitBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }


        }
    }

    public void getAllUnits() {
        mActivity.onBackPressed();
    }


}