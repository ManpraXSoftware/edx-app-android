package org.tta.mobile.tta.data.model.profile;

import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.PendingCertificate;

import java.util.List;

public class AllCertificatesResponse {

    private List<Certificate> generated;

    private List<PendingCertificate> pending;

    public List<Certificate> getGenerated() {
        return generated;
    }

    public void setGenerated(List<Certificate> generated) {
        this.generated = generated;
    }

    public List<PendingCertificate> getPending() {
        return pending;
    }

    public void setPending(List<PendingCertificate> pending) {
        this.pending = pending;
    }
}
