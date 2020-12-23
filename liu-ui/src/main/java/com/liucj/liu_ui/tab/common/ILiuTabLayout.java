package com.liucj.liu_ui.tab.common;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface ILiuTabLayout<Tab extends ViewGroup ,D> {
    Tab findTab(@NonNull D data);

    void addTabSelectedChangeListener(onTabSelectedListener<D> listener);

    void defaultSelected(@NonNull D defaultInfo);

    void inflateInfo(@NonNull List<D> infoList);

    interface onTabSelectedListener<D>{
        void onTabSelectedChange(int index, @Nullable D prevInfo, @NonNull  D nextInfo);
    }
}
