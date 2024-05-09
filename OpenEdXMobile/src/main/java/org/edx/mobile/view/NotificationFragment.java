
package org.edx.mobile.view;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.notification.NotificationReadTask;
import org.edx.mobile.model.notification.NotificationTask;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.myCourse.ParticularCourseTask;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.programs.NotificationModel;
import org.edx.mobile.programs.NotificationReadModel;
import org.edx.mobile.util.GestureListener;
import org.edx.mobile.view.adapters.NotificationAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static org.edx.mobile.view.Router.EXTRA_BUNDLE;
import static org.edx.mobile.view.Router.EXTRA_COURSE_COMPONENT_ID;
import static org.edx.mobile.view.Router.EXTRA_COURSE_DATA;
import static org.edx.mobile.view.Router.EXTRA_COURSE_UPGRADE_DATA;
import static org.edx.mobile.view.Router.EXTRA_IS_VIDEOS_MODE;

public class NotificationFragment extends Fragment implements OnNavigateListener {

    private RecyclerView notificationRecyclerViewNew;
    private NotificationAdapter notificationAdapterNew;

    private RecyclerView notificationRecyclerViewOlder;
    private NotificationAdapter notificationAdapterOlder;
    NotificationModel notificationModel;

    ImageButton imageCloseButton;

    LinearLayout linearLayout;

    TextView textViewNotification;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    LoginPrefs loginPrefs;

    ArrayList<EnrolledCoursesResponse> enrolledCoursesResponses = new ArrayList<>();

    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;

    int pageIndex=0;

    NotificationFragment(NotificationModel notificationModel){
        this.notificationModel=notificationModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        notificationRecyclerViewNew = view.findViewById(R.id.notificationRecyclerNew);
        notificationRecyclerViewOlder = view.findViewById(R.id.notificationRecyclerViewOlder);
        imageCloseButton=view.findViewById(R.id.close_btn);
        textViewNotification=view.findViewById(R.id.textview_notification);
        linearLayout=view.findViewById(R.id.notification_more_linear_layout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        imageCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationModel.isNext()){
                    getNotification();
                }
            }
        });
        ViewCompat.setAccessibilityDelegate(linearLayout, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                host.setLongClickable(false);
                info.setContentDescription("button");
            }
        });

        ViewCompat.setAccessibilityDelegate(textViewNotification, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                host.setLongClickable(false);
                info.setContentDescription(textViewNotification.getText()+" heading");
            }
        });
        setGestureListeners(textViewNotification);

        notificationAdapterNew = new NotificationAdapter(getContext(),this::navigateToAnotherScreen);
        notificationRecyclerViewNew.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationRecyclerViewNew.setAdapter(notificationAdapterNew);

        notificationAdapterOlder = new NotificationAdapter(getContext(), this::navigateToAnotherScreen);
        notificationRecyclerViewOlder.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationRecyclerViewOlder.setAdapter(notificationAdapterOlder);

        setAdapterData(notificationModel);

    }

    public void setAdapterData(NotificationModel notificationModel){
        List<NotificationModel.NotificationData> notificationDataNew=new ArrayList<>();
        List<NotificationModel.NotificationData> notificationDataViewed=new ArrayList<>();

        for(NotificationModel.NotificationData notificationData:notificationModel.getData()){

            if(!notificationData.isRead()){
                notificationDataNew.add(notificationData);
            }
            else {
                notificationDataViewed.add(notificationData);
            }
        }
        notificationAdapterNew.setNotifications(notificationDataNew);
        notificationAdapterOlder.setNotifications(notificationDataViewed);
    }
    private void setGestureListeners(TextView textView) {
        GestureListener gestureListener = new GestureListener( textView,null,getContext(),this::navigateToAnotherScreen);
        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public void navigateToAnotherScreen(Object item) {
        if (item instanceof NotificationModel.NotificationData) {
            NotificationModel.NotificationData notificationData = (NotificationModel.NotificationData) item;
            checkTypeNotification(notificationData);
        }
    }

    void checkTypeNotification(NotificationModel.NotificationData notificationData){
        if(notificationData.getNotificationType().equals("general")){
            enrolledStatus(notificationData);
        }
        else {

            getParticularCourse(notificationData);
        }
    }

    private void enrolledStatus(NotificationModel.NotificationData notificationData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(notificationData.getMessage()).setTitle(notificationData.getTitle());

        // Setting message manually and performing action on button click
        builder.setMessage(notificationData.getMessage())
                .setCancelable(false)
                .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        markNotificationRead(notificationData.getId());
                        dialog.dismiss();
                    }
                });
        // Creating dialog box
        AlertDialog alert = builder.create();
        // Setting the title manually
        alert.setTitle(notificationData.getTitle());
        alert.show();
    }

    public void getParticularCourse(NotificationModel.NotificationData notificationData) {
        String courseId="course-v1:VisionEmpower+VE_TIK_Math_G1-02+2019";
        try {
            ParticularCourseTask particularCourseTask = new ParticularCourseTask(getActivity().getApplicationContext(),"sushant@gmail.com",
                    courseId) {
                @Override
                public void onSuccess(@NonNull ArrayList<EnrolledCoursesResponse> result) {
                    try {
                        if (result != null) {
                            ArrayList<EnrolledCoursesResponse> data = result;
                            if (data != null) {
                                enrolledCoursesResponses = data;
                                if (enrolledCoursesResponses != null) {
                                    for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                                        if (enrolledCoursesResponse.getCourse() != null) {
                                            if (enrolledCoursesResponse.getCourse().getId().equals(courseId)) {
                                                markNotificationRead(notificationData.getId());
                                                courseNavigation(notificationData,enrolledCoursesResponse);
                                            }
                                        }
                                    }
                                }
                                Log.d("enrolledCoursesResponses", enrolledCoursesResponses.get(0).getCourse().getId() + "" + enrolledCoursesResponses.get(0).getCourse().getName());
                            } else {
                                Log.e("enrolledCoursesResponses", "Response body is null");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("enrolledCoursesResponses", "Exception in onResponse", e);
                    }
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    } else {

                    }
                }
            };
            particularCourseTask.execute();


        } catch (Exception e) {
            Log.e("enrolledCoursesResponses", "Exception in getParticularCourse", e);
        }
    }

    public void getNotification() {
        try {
            NotificationTask particularTask = new NotificationTask(getActivity().getApplicationContext(),pageIndex) {
                @Override
                public void onSuccess(@NonNull NotificationModel result) {
                    try {
                        if (result != null) {
                            NotificationModel data = result;
                            notificationModel=data;
                            pageIndex+=1;
                            if (data != null) {
                                setAdapterData(data);
                                Log.d("enrolledCoursesResponses",data.toString() );
                            } else {
                                Log.e("enrolledCoursesResponses", "Response body is null");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("enrolledCoursesResponses", "Exception in onResponse", e);
                    }
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    } else {

                    }
                }
            };
            particularTask.execute();


        } catch (Exception e) {
            Log.e("enrolledCoursesResponses", "Exception in getParticularCourse", e);
        }
    }


    public void markNotificationRead(int id) {

            try {
                NotificationReadTask particularTask = new NotificationReadTask(getActivity().getApplicationContext(),id) {
                    @Override
                    public void onSuccess(@NonNull NotificationReadModel result) {
                        try {
                            if (result != null) {
                                // Handle successful response
                                NotificationReadModel data = result;

                                if (data != null) {

                                    Log.d("NotificationReadModel",data.toString() );
                                } else {
                                    Log.e("NotificationReadModel", "Response body is null");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("NotificationReadModel", "Exception in onResponse", e);
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        if (ex instanceof HttpStatusException &&
                                ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        } else {

                        }
                    }
                };
                particularTask.execute();


            } catch (Exception e) {
                Log.e("NotificationReadModel", "Exception in getParticularCourse", e);
            }
        }

        public void courseNavigation(NotificationModel.NotificationData notificationData,EnrolledCoursesResponse enrolledCoursesResponses){
        if (notificationData.getCourse().contains("sequential")) {
                showCourseContainerOutline(NotificationFragment.this,
                        REQUEST_SHOW_COURSE_UNIT_DETAIL, enrolledCoursesResponses, null,
                        notificationData.getCourse(), null, false);
            } else {

                showCourseUnitDetail(NotificationFragment.this,
                        REQUEST_SHOW_COURSE_UNIT_DETAIL,enrolledCoursesResponses, null,
                        notificationData.getCourse(), false);
            }

    }

    public void showCourseContainerOutline(Fragment fragment, int requestCode,
                                           EnrolledCoursesResponse courseData,
                                           CourseUpgradeResponse courseUpgradeData,
                                           String courseComponentId,
                                           String lastAccessedId, boolean isVideosMode) {
        Intent courseDetail = CourseOutlineActivity.newIntent(fragment.getActivity(),
                courseData, courseUpgradeData, courseComponentId, lastAccessedId, isVideosMode);
        //TODO - what's the most suitable FLAG?
        // courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(courseDetail, requestCode);
    }

    public void showCourseUnitDetail(Fragment fragment, int requestCode, EnrolledCoursesResponse model,
                                     CourseUpgradeResponse courseUpgradeData,
                                     String courseComponentId, boolean isVideosMode) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(EXTRA_COURSE_DATA, model);
        courseBundle.putParcelable(EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData);
        courseBundle.putSerializable(EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        Intent courseDetail = new Intent(fragment.getActivity(), CourseUnitNavigationActivity.class);
        courseDetail.putExtra(EXTRA_BUNDLE, courseBundle);
        courseDetail.putExtra(EXTRA_IS_VIDEOS_MODE, isVideosMode);
        courseDetail.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(courseDetail, requestCode);
    }
}