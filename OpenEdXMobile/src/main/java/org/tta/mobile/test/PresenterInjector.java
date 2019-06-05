package org.tta.mobile.test;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.tta.mobile.view.Presenter;

@VisibleForTesting
public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
