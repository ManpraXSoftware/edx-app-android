package org.tta.mobile.tta.task.agenda;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.agenda.AgendaList;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetStateAgendaCountTask extends Task<List<AgendaList>> {

    @Inject
    private TaAPI taAPI;

    public GetStateAgendaCountTask(Context context) {
        super(context);
    }

    @Override
    public List<AgendaList> call() throws Exception {
        return taAPI.getStateAgendaCount().execute().body();
    }
}
