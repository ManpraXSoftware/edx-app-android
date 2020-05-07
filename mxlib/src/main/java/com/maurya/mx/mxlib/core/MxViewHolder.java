package com.maurya.mx.mxlib.core;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by mukesh on 8/9/18.
 */

public class MxViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;
    public MxViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding=binding;
    }

    public ViewDataBinding getBinding() {
        return binding;
    }
}
