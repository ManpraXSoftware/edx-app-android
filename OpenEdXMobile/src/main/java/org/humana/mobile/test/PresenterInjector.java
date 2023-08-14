package org.humana.mobile.test;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.humana.mobile.view.Presenter;

@VisibleForTesting
public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
