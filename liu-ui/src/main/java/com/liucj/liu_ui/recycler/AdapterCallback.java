package com.liucj.liu_ui.recycler;

/**
 * 更新数据
 */
public interface AdapterCallback<Data> {
    void update(Data data, LiuRecyclerAdapter.ViewHolder<Data> holder);
}
