package org.tta.mobile.util.observer;

import androidx.annotation.NonNull;

public interface Observable<T> {
    @NonNull
    Subscription subscribe(@NonNull final Observer<T> observer);
}
