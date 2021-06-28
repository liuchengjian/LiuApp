package com.liucj.liu_ui.refresh;

public interface LiuRefresh {
    /**
     * 刷新时是否禁止滚动
     *
     * @param disableRefreshScroll 否禁止滚动
     */
    void setDisableRefreshScroll(boolean disableRefreshScroll);

    /**
     * 刷新完成
     */
    void refreshFinished();

    /**
     * 设置下拉刷新的监听器
     *
     * @param liuRefreshListener 刷新的监听器
     */
    void setRefreshListener(LiuRefreshListener liuRefreshListener);

    /**
     * 设置下拉刷新的视图
     *
     * @param liuOverView 下拉刷新的视图
     */
    void setRefreshOverView(LiuOverView liuOverView);

    interface LiuRefreshListener {

        void onRefresh();

        boolean enableRefresh();
    }
}
