package org.tta.mobile.tta.ui.profile.certificate.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowCertificateBinding;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.CertificateStatus;
import org.tta.mobile.tta.data.local.db.table.PendingCertificate;
import org.tta.mobile.tta.data.model.content.CertificateStatusResponse;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.event.CertificateGeneratedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class PendingCertificatesViewModel extends BaseViewModel {

    public CertificatesAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private List<PendingCertificate> pendingCertificates;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public PendingCertificatesViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        pendingCertificates = new ArrayList<>();

        adapter = new CertificatesAdapter(mActivity);
        adapter.setItems(pendingCertificates);
        adapter.setItemClickListener((view, item) -> {
            if (!NetworkUtil.isConnected(mActivity)){
                mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
                return;
            }
            showChangeNameDialog(item);
        });

        mActivity.showLoading();
        fetchPendingCertificates();
    }

    private void showChangeNameDialog(PendingCertificate certificate) {

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
                                generateCertificate(certificate);
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

    }

    private void generateCertificate(PendingCertificate certificate){

        mDataManager.generateCertificate(certificate.getCourseId(),
                new OnResponseCallback<CertificateStatusResponse>() {
            @Override
            public void onSuccess(CertificateStatusResponse data) {
                mActivity.hideLoading();

                mActivity.analytic.addMxAnalytics_db(
                        certificate.getCourseId(), Action.GenerateCertificate, certificate.getCourseName(),
                        Source.Mobile, certificate.getCourseId());

                adapter.remove(certificate);
                toggleEmptyVisibility();

                switch (CertificateStatus.getEnumFromString(data.getStatus())){
                    case GENERATED:
                        mActivity.showLongSnack(mActivity.getString(R.string.certificate_successful));
                        EventBus.getDefault().post(new CertificateGeneratedEvent(certificate.getCourseId()));
                        break;
                    case PROGRESS:
                        mActivity.showIndefiniteSnack(
                                "आपके सर्टिफिकेट की मांग दर्ज हो गयी है| " +
                                        "सर्टिफिकेट तैयार होने पर हम आपको ऐप्प द्वारा सूचित करेंगे|"
                        );
                        break;
                    default:
                        mActivity.showLongSnack(mActivity.getString(R.string.certificate_error));
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
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void fetchPendingCertificates() {

        mDataManager.getPendingCertificatesFromLocal(new OnResponseCallback<List<PendingCertificate>>() {
            @Override
            public void onSuccess(List<PendingCertificate> data) {
                mActivity.hideLoading();
                populateCertificates(data);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                adapter.setLoadingDone();
                toggleEmptyVisibility();
            }
        });

    }

    private void populateCertificates(List<PendingCertificate> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (PendingCertificate certificate : data) {
            if (!pendingCertificates.contains(certificate)) {
                pendingCertificates.add(certificate);
                newItemsAdded = true;
                n++;
            }
        }
        if (newItemsAdded) {
            adapter.notifyItemRangeInserted(pendingCertificates.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility(){
        if (pendingCertificates == null || pendingCertificates.isEmpty()){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public class CertificatesAdapter extends MxInfiniteAdapter<PendingCertificate> {
        public CertificatesAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull PendingCertificate model, @Nullable OnRecyclerItemClickListener<PendingCertificate> listener) {
            if (binding instanceof TRowCertificateBinding) {
                TRowCertificateBinding certificateBinding = (TRowCertificateBinding) binding;
                certificateBinding.contentTitle.setText(model.getCourseName());

                String imageUrl = model.getImage();
                if (!imageUrl.startsWith(mDataManager.getConfig().getApiHostURL())){
                    imageUrl = mDataManager.getConfig().getApiHostURL() + imageUrl;
                }
                Glide.with(getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(certificateBinding.contentImage);

                certificateBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

            }
        }
    }
}
