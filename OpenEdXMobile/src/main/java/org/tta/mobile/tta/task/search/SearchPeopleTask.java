package org.tta.mobile.tta.task.search;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.feed.SuggestedUser;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class SearchPeopleTask extends Task<List<SuggestedUser>> {

    private int take, skip;
    private String searchText;

    @Inject
    private TaAPI taAPI;

    public SearchPeopleTask(Context context, int take, int skip, String searchText) {
        super(context);
        this.take = take;
        this.skip = skip;
        this.searchText = searchText;
    }

    @Override
    public List<SuggestedUser> call() throws Exception {
        return taAPI.searchPeople(take, skip, searchText).execute().body();
    }
}
