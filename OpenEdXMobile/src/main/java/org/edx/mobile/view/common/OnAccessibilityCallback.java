package org.edx.mobile.view.common;

public interface OnAccessibilityCallback<T> {
    void shiftAccessibilityFocusToFirstItemText(int position, T item);
}
