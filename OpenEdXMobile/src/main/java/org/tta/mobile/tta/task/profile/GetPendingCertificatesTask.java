package org.tta.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetPendingCertificatesTask extends Task<List<Certificate>> {

    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetPendingCertificatesTask(Context context, int take, int skip) {
        super(context);
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Certificate> call() throws Exception {
        return taAPI.getPendingCertificates(take, skip).execute().body();
    }
}
