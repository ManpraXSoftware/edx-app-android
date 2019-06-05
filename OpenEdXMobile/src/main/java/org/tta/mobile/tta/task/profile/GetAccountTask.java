package org.tta.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.user.Account;
import org.tta.mobile.user.UserAPI;

public class GetAccountTask extends Task<Account> {

    private String username;

    @Inject
    private UserAPI userAPI;

    public GetAccountTask(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    public Account call() throws Exception {
        return userAPI.getAccount(username).execute().body();
    }
}
