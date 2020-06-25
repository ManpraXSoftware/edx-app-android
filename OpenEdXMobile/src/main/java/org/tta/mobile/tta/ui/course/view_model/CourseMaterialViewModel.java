package org.tta.mobile.tta.ui.course.view_model;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowCourseMaterialFooterBinding;
import org.tta.mobile.databinding.TRowCourseMaterialHeaderBinding;
import org.tta.mobile.databinding.TRowCourseMaterialItemBinding;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.VideoModel;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.model.course.BlockType;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.course.IBlock;
import org.tta.mobile.module.storage.DownloadCompletedEvent;
import org.tta.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.tta.mobile.services.VideoDownloadHelper;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.CertificateStatus;
import org.tta.mobile.tta.data.enums.DownloadType;
import org.tta.mobile.tta.data.enums.UnitStatusType;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.UnitStatus;
import org.tta.mobile.tta.data.model.StatusResponse;
import org.tta.mobile.tta.data.model.content.BookmarkResponse;
import org.tta.mobile.tta.data.model.content.CertificateStatusResponse;
import org.tta.mobile.tta.data.model.content.TotalLikeResponse;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.event.ContentBookmarkChangedEvent;
import org.tta.mobile.tta.event.ContentStatusReceivedEvent;
import org.tta.mobile.tta.event.DownloadFailedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.scorm.PDFBlockModel;
import org.tta.mobile.tta.scorm.ScormBlockModel;
import org.tta.mobile.tta.scorm.ScormStartResponse;
import org.tta.mobile.tta.tincan.Tincan;
import org.tta.mobile.tta.ui.base.BaseRecyclerAdapter;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.certificate.CertificateActivity;
import org.tta.mobile.tta.ui.course.CourseScormViewActivity;
import org.tta.mobile.tta.ui.interfaces.OnTaItemClickListener;
import org.tta.mobile.tta.utils.ActivityUtil;

import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.PermissionsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class CourseMaterialViewModel extends BaseViewModel {

    private static final int ACTION_DOWNLOAD = 1;
    private static final int ACTION_DELETE = 2;
    private static final int ACTION_DELETE_MANY = 3;
    private static final int ACTION_PLAY = 4;

    private static final String javascript = "javascript:document.getElementsByid('xblock.xblock-student_view.xblock-student_view-html.xmodule_display.xmodule_HtmlModule.xblock-initialized').html();";

    private Content content;
    private EnrolledCoursesResponse course;
    private CourseComponent rootComponent;
    private CourseComponent assessmentComponent;
    private List<ScormBlockModel> remainingScorms;
    private ScormBlockModel selectedScormForDownload, selectedScormForDelete, selectedScormForPlay;
    private ContentStatus contentStatus;
    private Map<String, UnitStatus> unitStatusMap;

    public CourseMaterialAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    //Header details
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableInt bookmarkIcon = new ObservableInt(R.drawable.t_icon_bookmark);
    public ObservableInt allDownloadStatusIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableBoolean allDownloadIconVisible = new ObservableBoolean(false);
    public ObservableBoolean allDownloadProgressVisible = new ObservableBoolean(false);
    public ObservableField<String> description = new ObservableField<>("");
    public ObservableField<String> likes = new ObservableField<>("0");
    public ObservableField<String> likeToolTip = new ObservableField<>();
    public ObservableField<String> downloadToolTip = new ObservableField<>();
    public ObservableField<String> understandToolTip;
    public ObservableInt toolTipGravity;
    public ObservableInt understandToolTipGravity;

    //Footer details
    public ObservableField<String> footerImageUrl = new ObservableField<>();
    public ObservableBoolean footerTitleVisible = new ObservableBoolean();
    public ObservableField<String> footerTitle = new ObservableField<>();
    public ObservableInt footerDownloadIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableBoolean footerDownloadIconVisible = new ObservableBoolean(false);
    public ObservableBoolean footerDownloadProgressVisible = new ObservableBoolean(false);
    public ObservableField<String> footerBtnText = new ObservableField<>("");
    public ObservableBoolean footerBtnVisible = new ObservableBoolean(false);
    public ObservableInt footerBtnBackground = new ObservableInt(R.drawable.btn_selector_hollow);
    public ObservableInt footerBtnTextColor = new ObservableInt(R.color.primary_cyan);

    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public String certificateStatus = "certificate";

    private int numberOfDownloadingVideos;
    private int numberOfDownloadedVideos;
    private boolean downloadModeIsAll;
    private int actionMode;
    private boolean firstDownload;
    private boolean somethingIsDownloading;
    private boolean scormsChecked;
    private int nDeleted;
    private List<ScormBlockModel> toBeDeleted;

    public CourseMaterialViewModel(Context context, TaBaseFragment fragment, Content content, EnrolledCoursesResponse course, CourseComponent rootComponent) {
        super(context, fragment);
        this.content = content;
        this.course = course;
        this.rootComponent = rootComponent;

        toggleEmptyVisibility();
        if (rootComponent == null){
            return;
        }

        adapter = new CourseMaterialAdapter();
        firstDownload = true;
        unitStatusMap = new HashMap<>();
        loadData();
//        setToolTip();
    }

    private void setToolTip(){
        if (!mDataManager.getAppPref().isCourseVisited()){
            likeToolTip.set(mActivity.getResources().getString(R.string.course_button));
            downloadToolTip.set(mActivity.getResources().getString(R.string.course_download_button));
//            understandToolTip = new ObservableField<>("सभी भाग देखने पर और 60% या उससे \nअधिक पाने पर आपको सर्टिफिकेट मिलेगा ");
            toolTipGravity = new ObservableInt(Gravity.BOTTOM);
            understandToolTipGravity = new ObservableInt(Gravity.TOP);
            mDataManager.getAppPref().setCourseVisited(true);
        }
        else {
            if (likeToolTip!=null)
                likeToolTip.set("");

            if (downloadToolTip!=null)
                downloadToolTip.set("");

//            if (understandToolTip!=null)
//                understandToolTip.set("");
        }

    }



    public void loadData(){
        if (content == null){
            return;
        }

        getContentStatus();
        setToolTip();

        mDataManager.getTotalLikes(content.getId(), new OnResponseCallback<TotalLikeResponse>() {
            @Override
            public void onSuccess(TotalLikeResponse data) {
                likes.set(String.valueOf(data.getLike_count()));
            }

            @Override
            public void onFailure(Exception e) {
                likes.set("");
            }
        });

        mDataManager.isLike(content.getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                likeIcon.set(data.getStatus() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);
            }

            @Override
            public void onFailure(Exception e) {
                likeIcon.set(R.drawable.t_icon_like);
            }
        });

        mDataManager.isContentMyAgenda(content.getId(), content.getSource().getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                bookmarkIcon.set(data.getStatus() ? R.drawable.t_icon_bookmark_filled : R.drawable.t_icon_bookmark);
            }

            @Override
            public void onFailure(Exception e) {
                bookmarkIcon.set(R.drawable.t_icon_bookmark);
            }
        });

        getUnitStatus();

        fetchCourseComponent();

        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.item_delete_download:
                    if (item.isContainer() && item.getChildren() != null &&
                            !item.getChildren().isEmpty()){
                        CourseComponent component = (CourseComponent) item.getChildren().get(0);
                        if (component instanceof PDFBlockModel || component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    deleteScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
                default:
                    if (item.isContainer() && item.getChildren() != null &&
                            !item.getChildren().isEmpty()){
                        CourseComponent component = (CourseComponent) item.getChildren().get(0);
                        if (component instanceof PDFBlockModel || component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    showScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
            }
        });

    }

    private void getUnitStatus(){

        mDataManager.getUnitStatus(content.getSource_identity(), new OnResponseCallback<List<UnitStatus>>() {
            @Override
            public void onSuccess(List<UnitStatus> data) {
                for (UnitStatus status: data){
                    unitStatusMap.put(status.getUnit_id(), status);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void startScorm(String blockId){

        mDataManager.startScorm(content.getSource_identity(), blockId,
                new OnResponseCallback<ScormStartResponse>() {
                    @Override
                    public void onSuccess(ScormStartResponse data) {
                        if (data != null && data.isSuccess()) {
                            getUnitStatus();
                            getContentStatus();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });

    }

    private void deleteScorm(ScormBlockModel scorm) {
        setToolTip();
        selectedScormForDelete = scorm;
        actionMode = ACTION_DELETE;
        mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void deleteScorm() {

        mActivity.showAlertDailog(mActivity.getString(R.string.delete),
                String.format(mActivity.getString(R.string.delete_question),
                        selectedScormForDelete.getParent().getDisplayName()),
                (dialog, which) -> mDataManager.deleteScorm(selectedScormForDelete),
                null);

    }

    private void deleteToBeDeleted(){
        actionMode = ACTION_DELETE_MANY;
        mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void deleteMany(){
        if (toBeDeleted != null && !toBeDeleted.isEmpty()){
            nDeleted = 0;
            for (ScormBlockModel scorm: toBeDeleted){
                mDataManager.deleteScorm(scorm);
            }
        }
    }

    private void showScorm(ScormBlockModel scorm) {
        setToolTip();
        selectedScormForPlay = scorm;
        actionMode = ACTION_PLAY;
        mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void showScorm() {
        if (content == null){
            return;
        }


        Bundle parameters = new Bundle();
        String filePath = selectedScormForPlay.getDownloadEntry(mDataManager.getEdxEnvironment().getStorage()).getFilePath();
        if (selectedScormForPlay.getType().equals(BlockType.SCORM)) {
            if (filePath.contains(".zip")){
                filePath = filePath.substring(0, filePath.length()-4);
            }
            parameters.putString(Constants.KEY_FILE_PATH, filePath);
            parameters.putString(Constants.KEY_COURSE_NAME, selectedScormForPlay.getRoot().getDisplayName());
            parameters.putString(Constants.KEY_COURSE_ROOT_ID, selectedScormForPlay.getRoot().getId());
            parameters.putString(Constants.KEY_COURSE_ID, content.getSource_identity());
            parameters.putString(Constants.KEY_UNIT_ID, selectedScormForPlay.getId());
            parameters.putLong(Constants.KEY_CONTENT_ID, content.getId());
            parameters.putString(Constants.KEY_ARTICULATE_TYPE, selectedScormForPlay.getArticulateType());

            ActivityUtil.gotoPage(mActivity, CourseScormViewActivity.class, parameters);

            startScorm(selectedScormForPlay.getId());

        } else if (selectedScormForPlay.getType().equals(BlockType.PDF)) {
            ActivityUtil.viewPDF(mActivity, new File(filePath));
        }

        mActivity.analytic.addMxAnalytics_db(
                selectedScormForPlay.getInternalName(), Action.ViewSection, content.getName(),
                Source.Mobile, selectedScormForPlay.getId(),
                content.getSource_identity(), content.getId());

    }

    private void enableHeader(){

        allDownloadStatusIcon.set(R.drawable.t_icon_done);
        description.set(course == null ? null : course.getCourse().getShort_description());

        adapter.setHeaderLayout(R.layout.t_row_course_material_header);
        adapter.setHeaderClickListener(v -> {
            switch (v.getId()) {
                case R.id.like_layout:
                    like();
                    break;
                case R.id.bookmark_layout:
                    bookmark();
                    break;
                case R.id.all_download_layout:
                    if (allDownloadStatusIcon.get() == R.drawable.t_icon_download && allDownloadIconVisible.get()) {
                        downloadAllRemaining();
                    }
                    break;
            }
        });

    }

    private void enableFooter(){
        if (content == null){
            return;
        }

        if (assessmentComponent.isContainer()) {
            CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
            if (component instanceof PDFBlockModel || component instanceof ScormBlockModel) {
                ScormBlockModel scorm = (ScormBlockModel) component;
                footerImageUrl.set(scorm.getData().scormImageUrl == null ? content.getIcon() : scorm.getData().scormImageUrl);
            } else {
                footerImageUrl.set(content.getIcon());
            }
        } else {
            footerImageUrl.set(content.getIcon());
        }


        footerTitle.set(assessmentComponent.getDisplayName());
        footerTitleVisible.set(true);
        footerDownloadIcon.set(R.drawable.t_icon_download);

        getCertificateStatus();

        adapter.setFooterLayout(R.layout.t_row_course_material_footer);

        adapter.setFooterClickListener(v -> {

            ScormBlockModel scorm = null;
            if (assessmentComponent.isContainer()) {
                CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
                if (component instanceof PDFBlockModel || component instanceof ScormBlockModel) {
                    scorm = (ScormBlockModel) component;
                }
            }

            switch (v.getId()) {
                case R.id.item_delete_download:
                    if (scorm != null) {
                        switch (mDataManager.getScormStatus(scorm)){
                            case not_downloaded:
                                downloadSingle(scorm);
                                break;
                            case downloaded:
                            case watching:
                            case watched:
                                deleteScorm(scorm);
                                break;
                            case downloading:
                                //Do nothing
                                break;
                        }
                    }
                    break;
                case R.id.item_btn:
                    if (footerBtnText.get().equalsIgnoreCase(mActivity.getString(R.string.generate_certificate))){
                        if (!NetworkUtil.isConnected(mActivity)){
                            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
                            return;
                        }

                        ScormBlockModel finalScorm = scorm;

                        View view = LayoutInflater.from(mActivity)
                                .inflate(R.layout.t_dialog_change_name, null, false);
                        TextInputEditText etName = view.findViewById(R.id.et_name);
                        etName.setText(mDataManager.getLoginPrefs().getDisplayName());

                        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                                .setTitle(mActivity.getString(R.string.name_on_certificate))
                                .setView(view)
                                .setPositiveButton(mActivity.getString(R.string.save), null)
                                .setNegativeButton(mActivity.getString(R.string.cancel), null)
                                .create();

                        dialog.setOnShowListener(dialogInterface -> {

                            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(view1 -> {
                                String name = etName.getText().toString().trim();
                                if (name.equals("") ||
                                        name.equals(mDataManager.getLoginPrefs().getUsername())){
                                    etName.setError(mActivity.getString(R.string.error_name));
                                    return;
                                }

                                dialog.dismiss();
                                mActivity.showLoading();

                                Bundle parameters = new Bundle();
                                parameters.putString("name", name);
                                mDataManager.updateProfile(parameters,
                                        new OnResponseCallback<UpdateMyProfileResponse>() {
                                            @Override
                                            public void onSuccess(UpdateMyProfileResponse data) {
                                                generateCertificate(finalScorm);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                mActivity.hideLoading();
                                                mActivity.showLongSnack(e.getLocalizedMessage());
                                            }
                                        });

                            });
                        });

                        dialog.show();

                        break;
                    } else if (footerBtnText.get().equalsIgnoreCase(mActivity.getString(R.string.certificate))){
                        mActivity.showIndefiniteSnack(
                                "आपके सर्टिफिकेट की मांग दर्ज हो गयी है| " +
                                        "सर्टिफिकेट तैयार होने पर हम आपको ऐप्प द्वारा सूचित करेंगे|"
                        );
                        break;
                    } else if (footerBtnText.get().equalsIgnoreCase(mActivity.getString(R.string.view_certificate))){
                        mActivity.showLoading();

                        ScormBlockModel finalScorm = scorm;
                        mDataManager.getCertificate(content.getSource_identity(), new OnResponseCallback<Certificate>() {
                            @Override
                            public void onSuccess(Certificate data) {
                                mActivity.hideLoading();

                                Bundle parameters = new Bundle();
                                parameters.putParcelable(Constants.KEY_CERTIFICATE, data);
                                ActivityUtil.gotoPage(mActivity, CertificateActivity.class, parameters);

                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack(e.getLocalizedMessage());
                            }
                        });

                        break;
                    }
                default:
                    if (scorm != null) {
                        switch (mDataManager.getScormStatus(scorm)){
                            case not_downloaded:
                                downloadSingle(scorm);
                                break;
                            case downloaded:
                            case watching:
                            case watched:
                                showScorm(scorm);
                                break;
                            case downloading:
                                //Do nothing
                                break;
                        }
                    }
                    break;
            }
        });

    }

    private void generateCertificate(ScormBlockModel scorm){
        if (content == null){
            return;
        }

        mDataManager.generateCertificate(content.getSource_identity(), new OnResponseCallback<CertificateStatusResponse>() {
            @Override
            public void onSuccess(CertificateStatusResponse data) {
                mActivity.hideLoading();
                setButtonText(data);

                mActivity.analytic.addMxAnalytics_db(
                        scorm.getInternalName(), Action.GenerateCertificate, content.getName(),
                        Source.Mobile, content.getSource_identity(),
                        content.getSource_identity(), content.getId());

                getContentStatus();

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void getContentStatus(){
        if (content == null){
            return;
        }

        mDataManager.getUserContentStatus(Collections.singletonList(content.getId()),
                new OnResponseCallback<List<ContentStatus>>() {
                    @Override
                    public void onSuccess(List<ContentStatus> data) {
                        if (data.size() > 0){
                            firstDownload = false;
                            contentStatus = data.get(0);
                            EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });

    }

    private void getCertificateStatus(){
        if (content == null){
            return;
        }

        mDataManager.getCertificateStatus(content.getSource_identity(), new OnResponseCallback<CertificateStatusResponse>() {
            @Override
            public void onSuccess(CertificateStatusResponse data) {
                setButtonText(data);
                footerBtnVisible.set(true);
            }

            @Override
            public void onFailure(Exception e) {
                footerBtnText.set(mActivity.getString(R.string.assessment));
                footerBtnVisible.set(true);
            }
        });

    }

    private void setButtonText(CertificateStatusResponse data) {
        switch (CertificateStatus.getEnumFromString(data.getStatus())){
            case FAIL:
            case NONE:
                footerBtnText.set(mActivity.getString(R.string.assessment));
                footerBtnBackground.set(R.drawable.btn_selector_hollow);
                footerBtnTextColor.set(R.color.primary_cyan);
                break;

            case APPLICABLE:
                footerBtnText.set(mActivity.getString(R.string.generate_certificate));
                footerBtnBackground.set(R.drawable.t_btn_backgound_filled_blue);
                footerBtnTextColor.set(R.color.white);
                certificateStatus = "progress";

                mDataManager.addPendingCertificate(content.getSource_identity(), content.getName(),
                        content.getIcon());
                break;

            case PROGRESS:
                footerBtnText.set(mActivity.getString(R.string.certificate));
                footerBtnBackground.set(R.drawable.t_btn_backgound_filled_blue);
                footerBtnTextColor.set(R.color.white);
                certificateStatus = "progress";
                break;

            case GENERATED:
                footerBtnText.set(mActivity.getString(R.string.view_certificate));
                footerBtnBackground.set(R.drawable.btn_selector_filled);
                footerBtnTextColor.set(R.color.white);
                certificateStatus = "generated";
                if (footerBtnText.get().equals(mActivity.getResources().getString(R.string.view_certificate))){
                }
                break;
        }
    }

    private void bookmark() {
        if (content == null){
            return;
        }

        mActivity.showLoading();
        mDataManager.setBookmark(content.getId(), content.getSource().getId(), new OnResponseCallback<BookmarkResponse>() {
            @Override
            public void onSuccess(BookmarkResponse data) {
                mActivity.hideLoading();
                bookmarkIcon.set(data.isIs_active() ? R.drawable.t_icon_bookmark_filled : R.drawable.t_icon_bookmark);

                if (data.isIs_active()){
                    mActivity.analytic.addMxAnalytics_db(
                            content.getName() , Action.BookmarkCourse, content.getName(),
                            Source.Mobile, content.getSource_identity(),
                            content.getSource_identity(), content.getId());
                } else {
                    mActivity.analytic.addMxAnalytics_db(
                            content.getName() , Action.UnbookmarkCourse, content.getName(),
                            Source.Mobile, content.getSource_identity(),
                            content.getSource_identity(), content.getId());
                }

                EventBus.getDefault().post(new ContentBookmarkChangedEvent(content, data.isIs_active()));

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });
    }

    private void like() {
        if (content == null){
            return;
        }

        mActivity.showLoading();
        mDataManager.setLike(content.getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                mActivity.hideLoading();
                likeIcon.set(data.getStatus() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);
                int n = 0;
                if (likes.get() != null) {
                    try {
                        n = Integer.parseInt(likes.get());
                    } catch (Exception e) {
                        Bundle parameters = new Bundle();
                        parameters.putString(Constants.KEY_CLASS_NAME, CourseMaterialViewModel.class.getName());
                        parameters.putString(Constants.KEY_FUNCTION_NAME, "like");
                        parameters.putString(Constants.KEY_DATA, "Content id = " + content.getId() +
                                ", likes = " + likes.get());
                        Logger.logCrashlytics(e, parameters);
                        n = 0;
                    }
                }
                if (data.getStatus()){
                    n++;
                } else {
                    n--;
                }
                likes.set(String.valueOf(n));

                mActivity.analytic.addMxAnalytics_db(
                        content.getName() ,
                        data.getStatus() ? Action.CourseLike : Action.CourseUnlike,
                        content.getName(),
                        Source.Mobile, content.getSource_identity(),
                        content.getSource_identity(), content.getId());

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });
    }

    public void performAction(){

        switch (actionMode){
            case ACTION_DOWNLOAD:
                download();
                break;
            case ACTION_DELETE:
                deleteScorm();
                break;
            case ACTION_DELETE_MANY:
                deleteMany();
                break;
            case ACTION_PLAY:
                showScorm();
                break;
        }

    }

    public void download(){

        if (downloadModeIsAll){
            downloadMany();
        } else {
            downloadSingle();
        }

    }

    private void downloadSingle(ScormBlockModel scorm){
        setToolTip();
        downloadModeIsAll = false;
        selectedScormForDownload = scorm;
        actionMode = ACTION_DOWNLOAD;
        mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void downloadSingle() {
        if (content == null){
            return;
        }

        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }

        mActivity.showLoading();
        downloadModeIsAll = false;
        mDataManager.downloadSingle(selectedScormForDownload, content.getId(), mActivity, new VideoDownloadHelper.DownloadManagerCallback() {
            @Override
            public void onDownloadStarted(Long result) {
                mActivity.hideLoading();
                somethingIsDownloading = true;
                remainingScorms.remove(selectedScormForDownload);
                if (remainingScorms.isEmpty()){
                    allDownloadIconVisible.set(false);
                    allDownloadProgressVisible.set(true);
                }
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }

                /*if (contentStatus == null && firstDownload){
                    firstDownload = false;
                    ContentStatus status = new ContentStatus();
                    status.setContent_id(content.getId());
                    status.setStarted(String.valueOf(System.currentTimeMillis()));
                    mDataManager.setUserContent(Collections.singletonList(status),
                            new OnResponseCallback<List<ContentStatus>>() {
                                @Override
                                public void onSuccess(List<ContentStatus> data) {
                                    if (data.size() > 0){
                                        contentStatus = data.get(0);
                                        EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                }*/

                startScorm(selectedScormForDownload.getId());

                mActivity.analytic.addMxAnalytics_db(
                        selectedScormForDownload.getInternalName(), Action.StartScormDownload, content.getName(),
                        Source.Mobile, selectedScormForDownload.getId(),
                        content.getSource_identity(), content.getId());

            }

            @Override
            public void onDownloadFailedToStart() {
                mActivity.hideLoading();
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void showProgressDialog(int numDownloads) {
                mActivity.hideLoading();
            }

            @Override
            public void updateListUI() {
                mActivity.hideLoading();
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public boolean showInfoMessage(String message) {
                mActivity.hideLoading();
                return false;
            }
        });

    }

    private void downloadAllRemaining(){
        setToolTip();
        downloadModeIsAll = true;
        actionMode = ACTION_DOWNLOAD;
        mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void downloadMany(){
        if (content == null){
            return;
        }

        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }

        mActivity.showLoading();
        numberOfDownloadingVideos = remainingScorms.size();
        numberOfDownloadedVideos = 0;

        mDataManager.downloadMultiple(remainingScorms, content.getId(), mActivity,
                new VideoDownloadHelper.DownloadManagerCallback() {
                    @Override
                    public void onDownloadStarted(Long result) {
                        mActivity.hideLoading();
                        somethingIsDownloading = true;
                        numberOfDownloadingVideos = remainingScorms.size();
                        numberOfDownloadedVideos = 0;
                        allDownloadIconVisible.set(false);
                        allDownloadProgressVisible.set(true);
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }

                        if (contentStatus == null && firstDownload){
                            firstDownload = false;
                            ContentStatus status = new ContentStatus();
                            status.setContent_id(content.getId());
                            status.setStarted(String.valueOf(System.currentTimeMillis()));
                            mDataManager.setUserContent(Collections.singletonList(status),
                                    new OnResponseCallback<List<ContentStatus>>() {
                                        @Override
                                        public void onSuccess(List<ContentStatus> data) {
                                            if (data.size() > 0){
                                                contentStatus = data.get(0);
                                                EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {

                                        }
                                    });
                        }

                        for (ScormBlockModel model: remainingScorms){

                            startScorm(model.getId());

                            mActivity.analytic.addMxAnalytics_db(
                                    model.getInternalName(), Action.StartScormDownload, content.getName(),
                                    Source.Mobile, model.getId(),
                                    content.getSource_identity(), content.getId());

                        }
                    }

                    @Override
                    public void onDownloadFailedToStart() {
                        mActivity.hideLoading();
                        numberOfDownloadingVideos = 0;
                        allDownloadIconVisible.set(true);
                        allDownloadProgressVisible.set(false);
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void showProgressDialog(int numDownloads) {
                        mActivity.hideLoading();
                        Log.d("Download", "showProgressDialog " + numDownloads);
                    }

                    @Override
                    public void updateListUI() {
                        mActivity.hideLoading();
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public boolean showInfoMessage(String message) {
                        mActivity.hideLoading();
                        Log.d("Download", "showInfoMessage " + message);
                        return false;
                    }
                });

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        if (content == null){
            return;
        }

        if (e.getEntry() != null && e.getEntry().content_id == content.getId() && e.getEntry().type != null &&
                (e.getEntry().type.equalsIgnoreCase(DownloadType.Scrom.name()) ||
                e.getEntry().type.equalsIgnoreCase(DownloadType.Pdf.name()))
        ) {
            if (downloadModeIsAll) {
                numberOfDownloadedVideos++;
                if (numberOfDownloadedVideos == numberOfDownloadingVideos){
                    numberOfDownloadingVideos = 0;
                    allDownloadProgressVisible.set(false);
                    allDownloadStatusIcon.set(R.drawable.t_icon_done);
                    allDownloadIconVisible.set(true);
                    somethingIsDownloading = false;
                }
            } else if (remainingScorms == null || remainingScorms.isEmpty()){
                allDownloadProgressVisible.set(false);
                allDownloadStatusIcon.set(R.drawable.t_icon_done);
                allDownloadIconVisible.set(true);
                somethingIsDownloading = false;
            }
            if (adapter != null){
                adapter.notifyDataSetChanged();
            }

            mActivity.analytic.addMxAnalytics_db(
                    e.getEntry().videoId, Action.ScromDownloadCompleted, content.getName(),
                    Source.Mobile, e.getEntry().videoId,
                    content.getSource_identity(), content.getId());

            mDataManager.getdownloadedCourseContents(new OnResponseCallback<List<Content>>() {
                @Override
                public void onSuccess(List<Content> data) {
                    mActivity.analytic.addMxAnalytics_db(String.valueOf(data.size()), Action.OfflineSections, Nav.profile.name(),
                            Source.Mobile, null,
                            content.getSource_identity(), content.getId());
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        if (content == null){
            return;
        }

        if (e.getModel() != null && e.getModel().getContent_id() == content.getId() &&
                e.getModel().getDownloadType() != null &&
                (e.getModel().getDownloadType().equalsIgnoreCase(DownloadType.Scrom.name()) ||
                        e.getModel().getDownloadType().equalsIgnoreCase(DownloadType.Pdf.name()))) {
            if (actionMode == ACTION_DELETE) {
                fetchCourseComponent();
            } else {
                nDeleted++;
                if (toBeDeleted != null && nDeleted == toBeDeleted.size()) {
                    fetchCourseComponent();
                }
            }
            allDownloadStatusIcon.set(R.drawable.t_icon_download);

            mActivity.analytic.addMxAnalytics_db(
                    e.getModel().getVideoId(), Action.DeleteSection, content.getName(),
                    Source.Mobile, e.getModel().getVideoId(),
                    content.getSource_identity(), content.getId());

            //delete resume cache
            Tincan tincan=new Tincan();
            tincan.deleteResumePayload(content.getSource_identity(), e.getModel().getVideoId());
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadFailedEvent event){
        if (content == null){
            return;
        }

        VideoModel downloadEntry = event.getDownloadEntry();
        if (downloadEntry != null && downloadEntry.getContent_id() == content.getId() &&
                downloadEntry.getDownloadType() != null &&
                (downloadEntry.getDownloadType().equalsIgnoreCase(DownloadType.Scrom.name()) ||
                        downloadEntry.getDownloadType().equalsIgnoreCase(DownloadType.Pdf.name()))) {

            switch (event.getErrorCode()){
                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    mActivity.showLongSnack("Could not download " + downloadEntry.getTitle() +
                            ". Insufficient memory");
                    break;
                default:
                    mActivity.showLongSnack("Could not download " + downloadEntry.getTitle());
            }

            fetchCourseComponent();
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unregisterEvnetBus(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
        getCertificateStatus();
    }

    public void fetchCourseComponent() {
        somethingIsDownloading = false;
        populateData();
        if (remainingScorms.isEmpty()){
            if (!somethingIsDownloading) {
                allDownloadStatusIcon.set(R.drawable.t_icon_done);
                allDownloadIconVisible.set(true);
                allDownloadProgressVisible.set(false);
            } else {
                allDownloadIconVisible.set(false);
                allDownloadProgressVisible.set(true);
            }
        } else {
            allDownloadStatusIcon.set(R.drawable.t_icon_download);
            allDownloadIconVisible.set(true);
            allDownloadProgressVisible.set(false);
        }
    }

    private void populateData(){

        adapter.setData(rootComponent);

    }

    private void toggleEmptyVisibility() {
        if (rootComponent == null) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public class CourseMaterialAdapter extends BaseRecyclerAdapter<CourseComponent> {

        private CourseComponent rootComponent;
        private int numOfTotalUnits = 0;
        private List<CourseComponent> components;

        public CourseMaterialAdapter() {
            components = new ArrayList<>();
        }

        private void setData(CourseComponent component){
            components.clear();
            if (remainingScorms == null){
                remainingScorms = new ArrayList<>();
            } else {
                remainingScorms.clear();
            }
            rootComponent = component;
            if (rootComponent != null){
                List<IBlock> children = rootComponent.getChildren();
                this.numOfTotalUnits = children.size();

                for (IBlock block : children) {
                    CourseComponent comp = (CourseComponent) block;

                    if (comp.isContainer()){
                        for (IBlock childBlock : comp.getChildren()) {
                            CourseComponent child = (CourseComponent) childBlock;

                            if (child.isContainer() && child.getChildren() != null &&
                                    !child.getChildren().isEmpty()){
                                CourseComponent childComp = (CourseComponent) child.getChildren().get(0);
                                if (childComp instanceof PDFBlockModel || childComp instanceof ScormBlockModel){

                                    if (!scormsChecked && mDataManager.scormNeedsDeletion((ScormBlockModel) childComp)){
                                        if (toBeDeleted == null){
                                            toBeDeleted = new ArrayList<>();
                                        }
                                        toBeDeleted.add((ScormBlockModel) childComp);
                                    }

                                    if (toBeDeleted != null && !toBeDeleted.isEmpty()){
                                        deleteToBeDeleted();
                                    }

                                    if (child.getDisplayName().contains("अपनी समझ को परखें")){
                                        assessmentComponent = child;
                                        enableFooter();
                                    } else if (!child.getDisplayName().contains("कोर्स के बारे में")){
                                        components.add(child);
                                    }

                                    if (mDataManager.scormNotDownloaded((ScormBlockModel) childComp)){
                                        remainingScorms.add((ScormBlockModel) childComp);
                                    } else if (mDataManager.scormDownloading((ScormBlockModel) childComp)){
                                        somethingIsDownloading = true;
                                    }
                                }
                            }
                        }
                    }else {
                        if (comp.getDisplayName().contains("अपनी समझ को परखें")){
                            if (comp.isContainer() && comp.getChildren() != null &&
                                    !comp.getChildren().isEmpty()) {
                                assessmentComponent = comp;
                                enableFooter();
                            }
                        } else if (!comp.getDisplayName().contains("कोर्स के बारे में")){
                            components.add(comp);
                        }
                    }
                }
            }
            scormsChecked = true;
            enableHeader();
            set(components);
        }

        @Override
        public void onBind(ViewDataBinding binding, CourseComponent item, OnHeaderClickListener headerClickListener, OnFooterClickListener footerClickListener, OnTaItemClickListener<CourseComponent> itemClickListener) {

            if (binding instanceof TRowCourseMaterialHeaderBinding) {
                TRowCourseMaterialHeaderBinding headerBinding = (TRowCourseMaterialHeaderBinding) binding;
                headerBinding.setViewModel(CourseMaterialViewModel.this);
                /*headerBinding.descriptionWebview.setOnTouchListener((v, event) -> (event.getAction() == MotionEvent.ACTION_MOVE));
                if (!headerBinding.descriptionWebview.isInitiated()){
                    headerBinding.descriptionWebview.initWebView(mActivity, false, false);
                    headerBinding.descriptionWebview.loadUrl(true, aboutComponent.getChildren().get(0).getBlockUrl());
                }*/

                headerBinding.likeLayout.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

                headerBinding.bookmarkLayout.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

                headerBinding.allDownloadLayout.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

            } else if (binding instanceof TRowCourseMaterialFooterBinding) {
                TRowCourseMaterialFooterBinding footerBinding = (TRowCourseMaterialFooterBinding) binding;
                footerBinding.setViewModel(CourseMaterialViewModel.this);
                footerDownloadProgressVisible.set(false);

                if (assessmentComponent.isContainer()){
                    CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
                    if (component instanceof PDFBlockModel || component instanceof ScormBlockModel){
                        ScormBlockModel scorm = (ScormBlockModel) component;
                        switch (mDataManager.getScormStatus(scorm)){
                            case not_downloaded:
                                footerDownloadIcon.set(R.drawable.t_icon_download);
                                footerDownloadIconVisible.set(true);
//                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case downloading:
                                footerDownloadIconVisible.set(false);
                                footerDownloadProgressVisible.set(true);
//                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case downloaded:
                                footerDownloadIcon.set(R.drawable.t_icon_delete);
                                footerDownloadIconVisible.set(true);
//                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case watching:
                                footerDownloadIcon.set(R.drawable.t_icon_delete);
                                footerDownloadIconVisible.set(true);
//                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case watched:
                                footerDownloadIcon.set(R.drawable.t_icon_delete);
                                footerDownloadIconVisible.set(true);
//                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                        }
                    }
                }

                footerBinding.itemDeleteDownload.setOnClickListener(v -> {
                    if (footerClickListener != null) {
                        footerClickListener.onClick(v);
                    }
                });

                footerBinding.itemBtn.setOnClickListener(v -> {
                    if (footerClickListener != null) {
                        footerClickListener.onClick(v);
                    }
                });

                footerBinding.getRoot().setOnClickListener(v -> {
                    if (footerClickListener != null) {
                        footerClickListener.onClick(v);
                    }
                });

            } else {
                TRowCourseMaterialItemBinding itemBinding = (TRowCourseMaterialItemBinding) binding;
                itemBinding.loadingIndicator.setVisibility(View.GONE);
                itemBinding.itemStatus.setVisibility(View.GONE);
                if (item.isContainer() && item.getChildren() != null &&
                        !item.getChildren().isEmpty()){
                    CourseComponent component = (CourseComponent) item.getChildren().get(0);
                    if (component instanceof PDFBlockModel || component instanceof ScormBlockModel){
                        ScormBlockModel scorm = (ScormBlockModel) component;
                        switch (mDataManager.getScormStatus(scorm)){
                            case not_downloaded:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_download));
                                break;
                            case downloading:
                                itemBinding.itemDeleteDownload.setVisibility(View.GONE);
                                itemBinding.loadingIndicator.setVisibility(View.VISIBLE);
                                break;
                            case downloaded:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_delete));
                                break;
                            case watching:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_delete));
                                break;
                            case watched:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_delete));
                                break;
                        }

                        itemBinding.itemDuration.setText(mActivity.getString(
                                R.string.estimated_duration) + " : " +
                                (scorm.getData().scormDuration == null ? "N/A" : scorm.getData().scormDuration));
                        Glide.with(mActivity)
                                .load(scorm.getData().scormImageUrl == null ? content.getIcon() : scorm.getData().scormImageUrl)
                                .placeholder(R.drawable.placeholder_course_card_image)
                                .into(itemBinding.itemImage);

                        if (unitStatusMap.containsKey(scorm.getId())){
                            try {
                                switch (UnitStatusType.valueOf(unitStatusMap.get(scorm.getId()).getStatus())){
                                    case InProgress:
                                        itemBinding.itemStatus.setText(mActivity.getString(R.string.viewing));
                                        itemBinding.itemStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                R.drawable.t_icon_refresh, 0, 0, 0
                                        );
//                                        itemBinding.itemStatus.setVisibility(View.VISIBLE);
                                        break;

                                    case Completed:
                                        itemBinding.itemStatus.setText(mActivity.getString(R.string.viewed));
                                        itemBinding.itemStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                                R.drawable.t_icon_done, 0, 0, 0
                                        );
//                                        itemBinding.itemStatus.setVisibility(View.VISIBLE);
                                        break;

                                    default:
                                        itemBinding.itemStatus.setVisibility(View.GONE);
                                }
                            } catch (IllegalArgumentException e) {
                                Bundle parameters = new Bundle();
                                parameters.putString(Constants.KEY_CLASS_NAME, CourseMaterialViewModel.class.getName());
                                parameters.putString(Constants.KEY_FUNCTION_NAME, "onBind");
                                parameters.putString(Constants.KEY_DATA, "Content id = " + content.getId() +
                                        ", unit id = " + item.getId());
                                Logger.logCrashlytics(e, parameters);
                                e.printStackTrace();
                                itemBinding.itemStatus.setVisibility(View.GONE);
                            }
                        } else {
                            itemBinding.itemStatus.setVisibility(View.GONE);
                        }
                    } else {
                        itemBinding.itemDuration.setText(mActivity.getString(
                                R.string.estimated_duration) + " : " +
                                (item.getDuration() == null ? "N/A" : item.getDuration()));
                        Glide.with(mActivity)
                                .load(content.getIcon())
                                .placeholder(R.drawable.placeholder_course_card_image)
                                .into(itemBinding.itemImage);
                    }
                } else {
                    itemBinding.itemDuration.setText(mActivity.getString(
                            R.string.estimated_duration) + " : " +
                            (item.getDuration() == null ? "N/A" : item.getDuration()));
                    Glide.with(mActivity)
                            .load(content.getIcon())
                            .placeholder(R.drawable.placeholder_course_card_image)
                            .into(itemBinding.itemImage);
                }

                itemBinding.itemTitle.setText(item.getDisplayName());

                itemBinding.itemDeleteDownload.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, item);
                    }
                });
                itemBinding.getRoot().setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, item);
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeaderEnabled() && position == 0) {
                return R.layout.t_row_course_material_header;
            } else if (isFooterEnabled() && position == getItemCount() - 1) {
                return R.layout.t_row_course_material_footer;
            } else {
                return R.layout.t_row_course_material_item;
            }
        }

    }

}
