package org.tta.mobile.tta.interfaces;

public interface OnResponseCallback<T> {

    void onSuccess(T data);

    void onFailure(Exception e);

}
