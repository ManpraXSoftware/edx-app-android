package org.tta.mobile.tta.task.agenda;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.agenda.AgendaList;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetMyAgendaCountTask extends Task<AgendaList> {

    @Inject
    private TaAPI taAPI;

    public GetMyAgendaCountTask(Context context) {
        super(context);
    }

    @Override
    public AgendaList call() throws Exception {
        return taAPI.getMyAgendaCount().execute().body();
    }
}
