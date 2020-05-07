package org.tta.mobile.tta.ui.base.mvvm;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;
    public BaseViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding=binding;
    }

    public ViewDataBinding getBinding() {
        return binding;
    }

}
