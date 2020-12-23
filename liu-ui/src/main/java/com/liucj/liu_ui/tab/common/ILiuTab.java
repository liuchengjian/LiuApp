package com.liucj.liu_ui.tab.common;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

public interface ILiuTab<D> extends ILiuTabLayout.onTabSelectedListener<D> {
    void setLiuTabInfo(@NonNull D data);

    /**
     * 动态设置某个item的高度
     * @param height
     */
    void resetHeight(@Px int height);
}
