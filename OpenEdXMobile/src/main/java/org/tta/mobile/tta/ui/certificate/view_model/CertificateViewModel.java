package org.tta.mobile.tta.ui.certificate.view_model;

import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;

public class CertificateViewModel extends BaseViewModel {

    public Certificate certificate;

    public CertificateViewModel(BaseVMActivity activity, Certificate certificate) {
        super(activity);
        this.certificate = certificate;
    }
}
