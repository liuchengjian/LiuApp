package com.liucj.lib_picture_selector.listener;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

public interface PictureSelectListener {
    void pictureSelect(List<LocalMedia> selectList);
}
