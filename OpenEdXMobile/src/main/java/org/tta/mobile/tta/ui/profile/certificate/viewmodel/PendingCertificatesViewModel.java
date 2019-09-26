package org.tta.mobile.tta.ui.profile.certificate.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowCertificateBinding;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.CertificateStatus;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.model.content.CertificateStatusResponse;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.event.CertificateGeneratedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class PendingCertificatesViewModel extends BaseViewModel {
    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public CertificatesAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    List<Certificate> pendingCertificates;
    private int take, skip;
    private boolean allLoaded;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchPendingCertificates();
        return true;
    };

    public PendingCertificatesViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        pendingCertificates = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;

        adapter = new CertificatesAdapter(mActivity);
        adapter.setItems(pendingCertificates);
        adapter.setItemClickListener((view, item) -> {
            showChangeNameDialog(item);
        });

        mActivity.showLoading();
        fetchPendingCertificates();
    }

    private void showChangeNameDialog(Certificate certificate) {

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

    private void generateCertificate(Certificate certificate){

        mDataManager.generateCertificate(certificate.getCourse_id(),
                new OnResponseCallback<CertificateStatusResponse>() {
            @Override
            public void onSuccess(CertificateStatusResponse data) {
                mActivity.hideLoading();

                mActivity.analytic.addMxAnalytics_db(
                        certificate.getCourse_id(), Action.GenerateCertificate, certificate.getCourse_name(),
                        Source.Mobile, certificate.getCourse_id());

                adapter.remove(certificate);
                toggleEmptyVisibility();

                switch (CertificateStatus.getEnumFromString(data.getStatus())){
                    case GENERATED:
                        mActivity.showLongSnack(mActivity.getString(R.string.certificate_successful));
                        EventBus.getDefault().post(new CertificateGeneratedEvent(certificate.getCourse_id()));
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

        mDataManager.getPendingCertificates(take, skip, new OnResponseCallback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> data) {
                mActivity.hideLoading();
                if (data.size() < take) {
                    allLoaded = true;
                }
                populateCertificates(data);
                adapter.setLoadingDone();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                allLoaded = true;
                adapter.setLoadingDone();
                toggleEmptyVisibility();
            }
        });

    }

    private void populateCertificates(List<Certificate> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Certificate certificate : data) {
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

    public class CertificatesAdapter extends MxInfiniteAdapter<Certificate> {
        public CertificatesAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Certificate model, @Nullable OnRecyclerItemClickListener<Certificate> listener) {
            if (binding instanceof TRowCertificateBinding) {
                TRowCertificateBinding certificateBinding = (TRowCertificateBinding) binding;
                certificateBinding.contentTitle.setText(model.getCourse_name());
                Glide.with(getContext())
                        .load(mDataManager.getConfig().getApiHostURL() + model.getImage())
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
