package com.liucj.liu_library.picture.listener;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnItemLongClickListener {
    void onItemLongClick(RecyclerView.ViewHolder holder, int position, View v);
}
