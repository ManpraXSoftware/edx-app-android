package org.tta.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TFragmentCourseMaterialBinding;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.course.view_model.CourseMaterialViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.tta.utils.ToolTipView;
import org.tta.mobile.util.PermissionsUtil;

public class CourseMaterialTab extends TaBaseFragment {
    private int RANK;

    private CourseMaterialViewModel viewModel;

    private Content content;
    private EnrolledCoursesResponse courseData;
    private CourseComponent rootComponent;
    private boolean isShown = false;
    private AppPref appPref;
    private int state;

    public static CourseMaterialTab newInstance(Content content, EnrolledCoursesResponse course, CourseComponent rootComponent) {
        CourseMaterialTab courseMaterialTab = new CourseMaterialTab();
        courseMaterialTab.content = content;
        courseMaterialTab.courseData = course;
        courseMaterialTab.rootComponent = rootComponent;
        courseMaterialTab.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return courseMaterialTab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new CourseMaterialViewModel(getActivity(), this, content, courseData, rootComponent);
        viewModel.registerEventBus();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_course_material, viewModel).getRoot();
        appPref = new AppPref(view.getContext());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mBinding instanceof TFragmentCourseMaterialBinding) {
            TFragmentCourseMaterialBinding binding = (TFragmentCourseMaterialBinding) mBinding;
            binding.courseRecycler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        RecyclerView.LayoutManager layoutManager = binding.courseRecycler.getLayoutManager();
                        binding.courseRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);

                            }

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                if (dy > 0) {
                                    Log.d("BINDING", String.format("onScrolled: dx:%1d dy:%2d", dx, dy));
                                    int lastVisibleItemPosition = 0;
                                    int totalItemCount = layoutManager.getItemCount();
                                    if (layoutManager instanceof LinearLayoutManager)
                                        lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                                    Log.d("BINDING", String.format("onScrolled: lastVisible:%1d total:%2d", lastVisibleItemPosition, totalItemCount));
                                    if (!isShown && (lastVisibleItemPosition == totalItemCount - 1)) {
                                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForLayoutPosition(lastVisibleItemPosition);
                                        //scrolled to end place tooltip msg here
                                        if (holder != null) {
                                            if (!appPref.isCourseBottom()) {
                                                int lastVisiblePosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                                                if (lastVisiblePosition == lastVisibleItemPosition) {
                                                    if (!Constants.IsCertificateExits) {
                                                        ToolTipView.showToolTip(recyclerView.getContext(),
                                                                "सभी भाग देखने पर और 60% या उससे \nअधिक पाने पर आपको सर्टिफिकेट मिलेगा ",
                                                                holder.itemView.findViewById(R.id.item_btn), Gravity.TOP);
                                                    }else {
                                                        ToolTipView.showToolTip(recyclerView.getContext(),
                                                                "सभी सर्टिफिकेट्स यहाँ उपलब्ध हैं  ",
                                                                holder.itemView.findViewById(R.id.item_btn), Gravity.TOP);
                                                    }
                                                    appPref.setCourseBottom(true);
                                                    isShown = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.course_material.name()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unregisterEvnetBus();
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode) {
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                viewModel.performAction();
                break;
        }
    }

    @Override
    public void onPermissionDenied(String[] permissions, int requestCode) {
        switch (requestCode) {
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                viewModel.getActivity().showLongSnack("Permission Denied");
                break;
        }
    }
}
