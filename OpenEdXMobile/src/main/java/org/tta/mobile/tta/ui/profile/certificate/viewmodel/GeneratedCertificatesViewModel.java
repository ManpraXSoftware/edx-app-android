package org.tta.mobile.tta.ui.profile.certificate.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowCertificateBinding;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.event.CertificateGeneratedEvent;
import org.tta.mobile.tta.exception.TaException;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.certificate.CertificateActivity;
import org.tta.mobile.tta.utils.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class GeneratedCertificatesViewModel extends BaseViewModel {
    public CertificatesAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private List<Certificate> certificates;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public GeneratedCertificatesViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        certificates = new ArrayList<>();
        adapter = new CertificatesAdapter(mActivity);
        adapter.setItems(certificates);
        adapter.setItemClickListener((view, item) -> {
            Bundle parameters = new Bundle();
            parameters.putParcelable(Constants.KEY_CERTIFICATE, item);
            ActivityUtil.gotoPage(mActivity, CertificateActivity.class, parameters);
        });

        fetchCertificates();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void fetchCertificates() {
        mActivity.showLoading();
        mDataManager.getMyCertificatesFromLocal(new OnResponseCallback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> data) {
                mActivity.hideLoading();
                certificates.addAll(data);
                adapter.notifyDataSetChanged();
                toggleEmptyVisibility();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                toggleEmptyVisibility();
            }
        }, new TaException(mActivity.getString(R.string.empty_certificates_message)));

    }

    private void toggleEmptyVisibility(){
        if (certificates == null || certificates.isEmpty()){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CertificateGeneratedEvent event) {
        mDataManager.getCertificate(event.getCourseId(), new OnResponseCallback<Certificate>() {
            @Override
            public void onSuccess(Certificate data) {
                certificates.add(0, data);
                adapter.notifyItemInserted(0);
                toggleEmptyVisibility();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
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
