package org.humana.mobile.tta.ui.programs.selectprogram.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowSelectProgSectionBinding;
import org.humana.mobile.http.constants.ApiConstants;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.data.constants.Constants;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.feed.FeedFragment;
import org.humana.mobile.tta.ui.landing.LandingActivity;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.profile.ProfileFragment;
import org.humana.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.humana.mobile.tta.ui.search.SearchFragment;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.view.AccountFragment;
import org.humana.mobile.view.MyCoursesListFragment;
import org.humana.mobile.view.dialog.NativeFindCoursesFragment;


import java.util.ArrayList;
import java.util.List;

public class SelectProgramViewModel2 extends BaseViewModel {
    public List<Program> programs;
    public List<Program> selectPrograms;
    private String programId;
    public ProgramsAdapter programsAdapter;
    public RecyclerView.LayoutManager layoutManager;
    public Boolean prevVisible = false;
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean navShiftMode = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();
    private final String KEY_PROGRAM = "program";
    private final String KEY_PREVVISIBLE = "prevVisible";





    public SelectProgramViewModel2(Context context, TaBaseFragment fragment,
                                   EnrolledCoursesResponse course) {
        super(context, fragment);

        mActivity.showLoading();
        programId = mDataManager.getLoginPrefs().getProgramId();
        programs = new ArrayList<>();
        programsAdapter = new ProgramsAdapter(mActivity);
        programsAdapter.setItems(programs);
        selectPrograms = new ArrayList<>();
        navShiftMode.set(false);

        boolean tabview = mActivity.getResources().getBoolean(R.bool.isTablet);
        if (tabview){
            layoutManager = new GridLayoutManager(mActivity, 2);
        }else {
            layoutManager = new LinearLayoutManager(mActivity);
        }

        fetchPrograms();

        programsAdapter.setItemClickListener((view, item) -> {
            if (selectPrograms.size() > 0) {


                    programId = item.getId();
                    org.humana.mobile.tta.Constants.PROGRAM_ID = programId;
                    mDataManager.getLoginPrefs().setProgramId(programId);
                    mDataManager.getLoginPrefs().setParentId(item.getParent_id());
                    mDataManager.getLoginPrefs().setProgramTitle(item.getTitle());
                    mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(),
                            new OnResponseCallback<List<Section>>() {
                        @Override
                        public void onSuccess(List<Section> data) {

                            if (data.size() == 1) {
                                mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                                mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                                Constants.isSingleRow = true;
                                ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                                mActivity.finish();
                                mActivity.hideLoading();

                            } else {
                                Bundle b = new Bundle();
                                b.putCharSequence(KEY_PROGRAM, programId);
                                b.putBoolean(KEY_PREVVISIBLE,prevVisible);
                                Constants.isSinglePrg = false;
                                ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
                                mActivity.finish();
                                mActivity.hideLoading();

                            }

                        }

                        @Override
                        public void onFailure(Exception e) {
                            ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                        }
                    });


            } else {
                selectPrograms.add(item);
                programId = item.getId();
                mDataManager.getLoginPrefs().setProgramId(programId);
                mDataManager.getLoginPrefs().setProgramTitle(item.getTitle());
                mDataManager.getLoginPrefs().setParentId(item.getParent_id());
                mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(),
                        new OnResponseCallback<List<Section>>() {
                            @Override
                            public void onSuccess(List<Section> data) {

                                if (data.size() == 1) {
                                    mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                                    mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                                    Constants.isSingleRow = true;
                                    ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                                    mActivity.finish();

                                } else {
                                    Bundle b = new Bundle();
                                    b.putCharSequence(KEY_PROGRAM, programId);
                                    b.putBoolean(KEY_PREVVISIBLE,prevVisible);
                                    Constants.isSinglePrg = false;
                                    ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
                                    mActivity.finish();

                                }

                            }

                            @Override
                            public void onFailure(Exception e) {
                                ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                            }
                        });

            }

        });
    }

    public void fetchPrograms() {
        mActivity.showLoading();
        mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
            @Override
            public void onSuccess(List<Program> data) {

                populatePrograms(data);
                programsAdapter.setLoadingDone();
                mActivity.hideLoading();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                toggleEmptyVisibility();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void populatePrograms(List<Program> data) {
        boolean newItemsAdded = false;
        int n = 0;

        if (data.size() == 1){
            programId = data.get(0).getId();
            prevVisible = true;
            mDataManager.getLoginPrefs().setProgramTitle(data.get(0).getTitle());
            Bundle b = new Bundle();
            b.putCharSequence(KEY_PROGRAM, programId);
            b.putBoolean(KEY_PREVVISIBLE,prevVisible);
            ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
            mDataManager.getLoginPrefs().setProgramId(programId);
            mActivity.finish();
        }else {
            for (Program user : data) {
                if (!programs.contains(user)) {
                    programs.add(user);
                    newItemsAdded = true;
                    n++;
                }
            }


            if (newItemsAdded) {
                programsAdapter.notifyItemRangeInserted(programs.size() - n, n);
            }
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (programs == null || programs.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }


    public class ProgramsAdapter extends MxInfiniteAdapter<Program> {

        public ProgramsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Program model,
                           @Nullable OnRecyclerItemClickListener<Program> listener) {
            if (binding instanceof TRowSelectProgSectionBinding) {
                TRowSelectProgSectionBinding itemBinding = (TRowSelectProgSectionBinding) binding;

                String str = model.getId();
                String[] parts = str.split("[+]");
                String part1 = parts[0];
                String part2 = parts[1];
                String part3 = parts[2];

                itemBinding.textPrograms.setText(model.getTitle());
                itemBinding.textCourse.setText("Program No : "+part2+" | " + part3);


                Glide.with(mActivity)
                        .load(mDataManager.getEdxEnvironment().getConfig().getApiHostURL() + model.getImage())
                        .placeholder(ContextCompat.getDrawable(mActivity, R.drawable.placeholder_course_card_image))
                        .into(itemBinding.image);

                itemBinding.btnView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });


            }
        }
    }


}
