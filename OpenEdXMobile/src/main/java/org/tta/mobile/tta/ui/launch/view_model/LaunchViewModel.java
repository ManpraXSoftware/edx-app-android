package org.tta.mobile.tta.ui.launch.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import org.tta.mobile.R;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;

public class LaunchViewModel extends BaseViewModel {

    public ObservableInt image = new ObservableInt(0);
    public ObservableField<String> text = new ObservableField<>("");

    public LaunchViewModel(Context context, TaBaseFragment fragment, int imageId, String text) {
        super(context, fragment);
        this.image.set(imageId);
        this.text.set(text);
    }
}
