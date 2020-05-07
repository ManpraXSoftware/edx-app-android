package org.tta.mobile.tta.ui.profile;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.tta.mobile.R;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.profile.view_model.OtherProfileViewModel;

public class OtherProfileActivity extends BaseVMActivity {

    private OtherProfileViewModel viewModel;

    private String username;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getExtras();
        viewModel = new OtherProfileViewModel(this, username);
        binding(R.layout.t_activity_other_profile, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey(Constants.KEY_USERNAME)){
                username = parameters.getString(Constants.KEY_USERNAME);
            }
        }
    }
}
