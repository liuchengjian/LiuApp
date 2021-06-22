package com.liucj.liu_ui.tab.bottom;

import android.graphics.Bitmap;

import androidx.fragment.app.Fragment;

public class LiuTabBottomInfo<Color> {
    public enum TabType {
        BITMAP, ICON
    }

    public Class<? extends Fragment> fragment;
    public String name;
    public Bitmap defaultBitmap;
    public Bitmap selectBitmap;
    public String iconFont;
    /**
     * 提示，在java中直接设置字体无效，需要在string中设置
     */
    public String defaultIconName;
    public String selectIconName;

    public Color defaultColor;
    public Color tintColor;
    public TabType tabType;

    public LiuTabBottomInfo(String name, Bitmap defaultBitmap, Bitmap selectBitmap) {
        this.name = name;
        this.defaultBitmap = defaultBitmap;
        this.selectBitmap = selectBitmap;
    }

    public LiuTabBottomInfo(String name, String iconFont, String defaultIconName, String selectIconName, Color defaultColor, Color tintColor) {
        this.name = name;
        this.iconFont = iconFont;
        this.defaultIconName = defaultIconName;
        this.selectIconName = selectIconName;
        this.defaultColor = defaultColor;
        this.tintColor = tintColor;
        this.tabType = TabType.ICON;
    }
}
