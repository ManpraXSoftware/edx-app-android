package com.maurya.mx.mxlib.core;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class BaseDiffCallback<I> extends DiffUtil.Callback {
    String TAG = this.getClass().getSimpleName();

    public interface Listener<I> {
        boolean areItemAreSame(I oldItem, I newItem);

        boolean areContentsTheSame(I oldItem, I newItem);
    }

    private final List<I> oldList;
    private final List<I> newList;
    Listener<I> listener;

    public BaseDiffCallback(List<I> oldList, List<I> newList, Listener<I> listener) {
        this.oldList = oldList;
        this.newList = newList;
        this.listener = listener;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return listener.areItemAreSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return listener.areContentsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }
}
