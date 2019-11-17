package com.example.testcompiler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.example.annotation.ViewHolderBinder;
import com.example.api.IViewHolder;

@ViewHolderBinder(xml = R.layout.item_bean1)
public class Bean1ViewHolder extends RecyclerView.ViewHolder implements IViewHolder<Bean1> {
    public Bean1ViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBindData(Bean1 bean) {
    }
}
