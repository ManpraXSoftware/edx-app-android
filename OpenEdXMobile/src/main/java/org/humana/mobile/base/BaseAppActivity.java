package org.humana.mobile.base;

import android.content.Context;
import android.os.Bundle;

import org.humana.mobile.event.NewRelicEvent;

import de.greenrobot.event.EventBus;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
    }
}
