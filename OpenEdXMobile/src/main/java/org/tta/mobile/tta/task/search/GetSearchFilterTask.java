package org.tta.mobile.tta.task.search;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetSearchFilterTask extends Task<SearchFilter> {

    @Inject
    private TaAPI taAPI;

    public GetSearchFilterTask(Context context) {
        super(context);
    }

    @Override
    public SearchFilter call() throws Exception {
        return taAPI.getSearchFilter().execute().body();
    }
}
