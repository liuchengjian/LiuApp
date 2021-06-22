package com.liucj.liu_library.picture;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.liucj.liu_library.R;
import com.liucj.liu_library.picture.adapter.GridImageAdapter;
import com.liucj.liu_library.picture.listener.DragListener;
import com.liucj.liu_library.picture.listener.OnClickDelListener;
import com.liucj.liu_library.picture.listener.PictureSelectListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.broadcast.BroadcastAction;
import com.luck.picture.lib.broadcast.BroadcastManager;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.style.PictureCropParameterStyle;
import com.luck.picture.lib.style.PictureParameterStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PictureSelectView extends LinearLayout {
    private final static String TAG = PictureSelectView.class.getSimpleName();
    private boolean isNoOnlyCamera = true;
    private int themeId;//主题
    private int chooseMode = PictureMimeType.ofAll();//类型
    private boolean isWeChatStyle = false;//是否开启微信图片选择风格
    private int language = -1;
    private boolean isUpward;
    private boolean needScaleBig = true;
    private boolean needScaleSmall = true;
    private int maxSelectNum = 9;//最大选择图片
    private int minSelectNum = 1;//最小选择图片
    private int maxVideoSelectNum = 1;// 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
    private boolean isMultiple = true;

    private boolean isUseCustomCamera = true;// 是否使用自定义相机
    private boolean isWithVideoImage = true;// 图片和视频是否可以同选
    private boolean isPreviewImage = true;// 是否可预览图片
    private boolean isPreviewVideo = true;// 是否可预览视频
    private boolean isEnablePreviewAudio = true;// 是否可播放音频
    private boolean isCamera = true;//是否显示拍照按钮
    private String imageFormat = PictureMimeType.JPEG;//拍照保存图片格式后缀,默认jpeg
    private boolean hideBottomControls = false;// 是否显示uCrop工具栏，默认不显示
    private boolean isGif = false;// 是否显示gif图片
    private boolean isFreeStyleCropEnabled = false;// 裁剪框是否可拖拽
    private boolean isCircleDimmedLayer = false;// 是否圆形裁剪
    private boolean isOriginalImageControl = false;//  是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效

    private boolean isShowCropFrame = false;//  是否显示裁剪矩形边框 圆形裁剪时建议设为false
    private boolean isShowCropGrid = false;// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
    private boolean isOpenClickSound = false;// 是否开启点击声音

    private boolean isEnableCrop = false;// 是否裁剪
    private boolean isCompress = false;// 是否压缩
    private int imageSpanCount = 4;//每行显示个数
    private int spanCount = 4;//列表每行显示个数
    private boolean isReturnEmpty = false;// 未选择数据时点击按钮是否可以返回

    private GridImageAdapter mAdapter;
    private PictureParameterStyle mPictureParameterStyle = PictureStyle.getDefaultStyle(getContext());
    ;// 动态自定义相册主题
    private PictureCropParameterStyle mCropParameterStyle;// 动态自定义裁剪主题
    private PictureWindowAnimationStyle mWindowAnimationStyle;// 自定义相册启动退出动画

    private ItemTouchHelper mItemTouchHelper;
    private DragListener mDragListener;
    private TextView tvDeleteText;
    private int aspect_ratio_x = 1;
    private int aspect_ratio_y = 1;
    private boolean isShowTopDel = true;
    private boolean isShowTouchDel = true;
    private List<LocalMedia> dataList = new ArrayList<>();

    public PictureSelectView(Context context) {
        this(context, null);
//        init();
    }

    public PictureSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PictureSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化
     */
    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_picture_view, this);
        RecyclerView mRecyclerView = view.findViewById(R.id.mRecyclerView);
        tvDeleteText = view.findViewById(R.id.tv_delete_text);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(),
                spanCount, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount,
                ScreenUtils.dip2px(getContext(), 8), false));
        mCropParameterStyle = PictureStyle.pictureCropParameterStyle(getContext(), mPictureParameterStyle);
        mAdapter = new GridImageAdapter(getContext(), onAddPicClickListener, isShowTopDel);
        mAdapter.setList(dataList);
        mAdapter.setSelectMax(maxSelectNum);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((v, position) -> {
            List<LocalMedia> selectList = mAdapter.getData();
            if (selectList.size() > 0) {
                LocalMedia media = selectList.get(position);
                String mimeType = media.getMimeType();
                int mediaType = PictureMimeType.getMimeType(mimeType);
                switch (mediaType) {
                    case PictureConfig.TYPE_VIDEO:
                        // 预览视频
                        PictureSelector.create((Activity) getContext()).externalPictureVideo(media.getPath());
                        break;
                    case PictureConfig.TYPE_AUDIO:
                        // 预览音频
                        PictureSelector.create((Activity) getContext()).externalPictureAudio(media.getPath());
                        break;
                    default:
                        // 预览图片 可自定长按保存路径
//                        PictureWindowAnimationStyle animationStyle = new PictureWindowAnimationStyle();
//                        animationStyle.activityPreviewEnterAnimation = R.anim.picture_anim_up_in;
//                        animationStyle.activityPreviewExitAnimation = R.anim.picture_anim_down_out;
                        PictureSelector.create((Activity) getContext())
                                //.themeStyle(themeId) // xml设置主题
                                .setPictureStyle(mPictureParameterStyle)// 动态自定义相册主题
                                //.setPictureWindowAnimationStyle(animationStyle)// 自定义页面启动动画
                                .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                                .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                                .openExternalPreview(position, selectList);
                        break;
                }
            }
        });
        mAdapter.setOnClickDelListener(new OnClickDelListener() {
            @Override
            public void OnClickDel(List<LocalMedia> list, int position) {
                if (mPictureSelectListener != null) {
                    mPictureSelectListener.pictureSelect(list);
                }
            }
        });
        mAdapter.setItemLongClickListener((holder, position, v) -> {
            //如果item不是最后一个，则执行拖拽
            needScaleBig = true;
            needScaleSmall = true;
            int size = mAdapter.getData().size();
            if (size != maxSelectNum) {
                mItemTouchHelper.startDrag(holder);
                return;
            }
            if (holder.getLayoutPosition() != size - 1) {
                mItemTouchHelper.startDrag(holder);
            }
        });

        mDragListener = new DragListener() {
            @Override
            public void deleteState(boolean isDelete) {
                if (!isShowTouchDel) {
                    return;
                }
                if (isDelete) {
                    tvDeleteText.setText(getContext().getString(R.string.app_let_go_drag_delete));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        tvDeleteText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_let_go_delete, 0, 0);
                    }
                } else {
                    tvDeleteText.setText(getContext().getString(R.string.app_drag_delete));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        tvDeleteText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.picture_icon_delete, 0, 0);
                    }
                }
            }

            @Override
            public void dragState(boolean isStart) {
                int visibility = tvDeleteText.getVisibility();
                if (isStart) {
                    if (visibility == View.GONE && isShowTouchDel) {
                        tvDeleteText.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
                        tvDeleteText.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (visibility == View.VISIBLE) {
                        tvDeleteText.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateInterpolator());
                        tvDeleteText.setVisibility(View.GONE);
                    }
                }
            }
        };

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int itemViewType = viewHolder.getItemViewType();
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    viewHolder.itemView.setAlpha(0.7f);
                }
                return makeMovementFlags(ItemTouchHelper.DOWN | ItemTouchHelper.UP
                        | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //得到item原来的position
                try {
                    int fromPosition = viewHolder.getAdapterPosition();
                    //得到目标position
                    int toPosition = target.getAdapterPosition();
                    int itemViewType = target.getItemViewType();
                    if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                        if (fromPosition < toPosition) {
                            for (int i = fromPosition; i < toPosition; i++) {
                                Collections.swap(mAdapter.getData(), i, i + 1);
                            }
                        } else {
                            for (int i = fromPosition; i > toPosition; i--) {
                                Collections.swap(mAdapter.getData(), i, i - 1);
                            }
                        }
                        mAdapter.notifyItemMoved(fromPosition, toPosition);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                int itemViewType = viewHolder.getItemViewType();
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    if (null == mDragListener) {
                        return;
                    }
                    if (needScaleBig) {
                        //如果需要执行放大动画
                        viewHolder.itemView.animate().scaleXBy(0.1f).scaleYBy(0.1f).setDuration(100);
                        //执行完成放大动画,标记改掉
                        needScaleBig = false;
                        //默认不需要执行缩小动画，当执行完成放大 并且松手后才允许执行
                        needScaleSmall = false;
                    }
                    int sh = recyclerView.getHeight() + tvDeleteText.getHeight();
                    int ry = sh - tvDeleteText.getTop();
                    if (dY >= ry) {
                        //拖到删除处
                        if (!isShowTouchDel) {
                            mDragListener.dragState(false);
                            return;
                        }
                        mDragListener.deleteState(true);
                        if (isUpward) {
                            //在删除处放手，则删除item
                            viewHolder.itemView.setVisibility(View.INVISIBLE);
                            mAdapter.delete(viewHolder.getAdapterPosition());
                            resetState();
                            return;
                        }
                    } else {//没有到删除处
                        if (View.INVISIBLE == viewHolder.itemView.getVisibility()) {
                            //如果viewHolder不可见，则表示用户放手，重置删除区域状态
                            mDragListener.dragState(false);
                        }
                        if (needScaleSmall) {//需要松手后才能执行
                            viewHolder.itemView.animate().scaleXBy(1f).scaleYBy(1f).setDuration(100);
                        }
                        mDragListener.deleteState(false);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                int itemViewType = viewHolder != null ? viewHolder.getItemViewType() : GridImageAdapter.TYPE_CAMERA;
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    if (ItemTouchHelper.ACTION_STATE_DRAG == actionState && mDragListener != null) {
                        mDragListener.dragState(true);
                    }
                    super.onSelectedChanged(viewHolder, actionState);
                }
            }

            @Override
            public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
                needScaleSmall = true;
                isUpward = true;
                return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int itemViewType = viewHolder.getItemViewType();
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    viewHolder.itemView.setAlpha(1.0f);
                    super.clearView(recyclerView, viewHolder);
                    mAdapter.notifyDataSetChanged();
                    resetState();
                }
            }
        });

        // 绑定拖拽事件
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        // 注册外部预览图片删除按钮回调
        if (getContext() != null) {
            BroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                    BroadcastAction.ACTION_DELETE_PREVIEW_POSITION);
        }
    }

    /**
     * 重置
     */
    private void resetState() {
        if (mDragListener != null) {
            mDragListener.deleteState(false);
            mDragListener.dragState(false);
        }
        isUpward = false;
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            if (isNoOnlyCamera) {
                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create((Activity) getContext())
                        .openGallery(chooseMode)// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .theme(themeId)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                        .isWeChatStyle(isWeChatStyle)// 是否开启微信图片选择风格
                        .isUseCustomCamera(isUseCustomCamera)// 是否使用自定义相机
                        .setLanguage(language)// 设置语言，默认中文
                        .setPictureStyle(mPictureParameterStyle)// 动态自定义相册主题
                        .setPictureCropStyle(mCropParameterStyle)// 动态自定义裁剪主题
                        .setPictureWindowAnimationStyle(mWindowAnimationStyle)// 自定义相册启动退出动画
                        .isWithVideoImage(isWithVideoImage)// 图片和视频是否可以同选
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
//                        .minSelectNum(minSelectNum)// 最小选择数量
                        //.minVideoSelectNum(1)// 视频最小选择数量，如果没有单独设置的需求则可以不设置，同用minSelectNum字段
                        .maxVideoSelectNum(maxVideoSelectNum) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                        .imageSpanCount(imageSpanCount)// 每行显示个数
                        .isReturnEmpty(isReturnEmpty)// 未选择数据时点击按钮是否可以返回
                        //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对.isCompress(false); && .isEnableCrop(false);有效,默认处理
                        .loadCacheResourcesCallback(GlideCacheEngine.createCacheEngine())// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                        .isOriginalImageControl(isOriginalImageControl)// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                        //.cameraFileName("test.png")    // 重命名拍照文件名、注意这个只在使用相机时可以使用，如果使用相机又开启了压缩或裁剪 需要配合压缩和裁剪文件名api
                        //.renameCompressFile("test.png")// 重命名压缩文件名、 注意这个不要重复，只适用于单张图压缩使用
                        //.renameCropFileName("test.png")// 重命名裁剪文件名、 注意这个不要重复，只适用于单张图裁剪使用
                        .selectionMode(isMultiple ? PictureConfig.MULTIPLE : PictureConfig.SINGLE)// 多选 or 单选
                        .isSingleDirectReturn(isMultiple)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                        .isPreviewImage(isPreviewImage)// 是否可预览图片
                        .isPreviewVideo(isPreviewVideo)// 是否可预览视频
                        //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                        .isEnablePreviewAudio(isEnablePreviewAudio) // 是否可播放音频
                        .isCamera(isCamera)// 是否显示拍照按钮
                        //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .imageFormat(imageFormat)// 拍照保存图片格式后缀,默认jpeg
                        .isEnableCrop(isEnableCrop)// 是否裁剪
                        .isCompress(isCompress)// 是否压缩
                        .compressQuality(80)// 图片压缩后输出质量 0~ 100
                        .synOrAsy(true)//同步false或异步true 压缩 默认同步
                        //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
                        //.compressSavePath(getPath())//压缩图片保存地址
                        //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效 注：已废弃
                        //.glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度 注：已废弃
                        .withAspectRatio(aspect_ratio_x, aspect_ratio_y)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .hideBottomControls(hideBottomControls)// 是否显示uCrop工具栏，默认不显示
                        .isGif(isGif)// 是否显示gif图片
                        .freeStyleCropEnabled(isFreeStyleCropEnabled)// 裁剪框是否可拖拽
                        .circleDimmedLayer(isCircleDimmedLayer)// 是否圆形裁剪
                        //.setCircleDimmedColor(ContextCompat.getColor(this, R.color.app_color_white))// 设置圆形裁剪背景色值
                        //.setCircleDimmedBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color_white))// 设置圆形裁剪边框色值
                        //.setCircleStrokeWidth(3)// 设置圆形裁剪边框粗细
                        .showCropFrame(isShowCropFrame)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .showCropGrid(isShowCropGrid)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .isOpenClickSound(isOpenClickSound)// 是否开启点击声音
                        .selectionData(mAdapter.getData())// 是否传入已选图片
                        //.isDragFrame(false)// 是否可拖动裁剪框(固定)
                        //.videoMaxSecond(15)
                        //.videoMinSecond(10)
                        .isPreviewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 注：已废弃 改用cutOutQuality()
                        .cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.rotateEnabled(true) // 裁剪是否可旋转图片
                        //.scaleEnabled(true)// 裁剪是否可放大缩小图片
                        //.videoQuality()// 视频录制质量 0 or 1
                        //.recordVideoSecond()//录制视频秒数 默认60s
                        //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径  注：已废弃
                        //.forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                        .forResult(new PictureResultCallback(mAdapter));

            } else {
                // 单独拍照
                PictureSelector.create((Activity) getContext())
                        .openCamera(chooseMode)// 单独拍照，也可录像或也可音频 看你传入的类型是图片or视频
                        .theme(themeId)// 主题样式设置 具体参考 values/styles
                        .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .setPictureStyle(mPictureParameterStyle)// 动态自定义相册主题
                        .setPictureCropStyle(mCropParameterStyle)// 动态自定义裁剪主题
                        .setPictureWindowAnimationStyle(mWindowAnimationStyle)// 自定义相册启动退出动画
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        .minSelectNum(minSelectNum)// 最小选择数量
                        .isUseCustomCamera(isUseCustomCamera)// 是否使用自定义相机
                        .imageFormat(imageFormat)// 拍照保存图片格式后缀,默认jpeg
                        .loadCacheResourcesCallback(GlideCacheEngine.createCacheEngine())// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                        //.querySpecifiedFormatSuffix(PictureMimeType.ofPNG())// 查询指定后缀格式资源
                        .selectionMode(isMultiple ?
                                PictureConfig.MULTIPLE : PictureConfig.SINGLE)// 多选 or 单选
                        //.cameraFileName("test.png")// 使用相机时保存至本地的文件名称,注意这个只在拍照时可以使用
                        //.renameCompressFile("test.png")// 重命名压缩文件名、 注意这个不要重复，只适用于单张图压缩使用
                        //.renameCropFileName("test.png")// 重命名裁剪文件名、 注意这个不要重复，只适用于单张图裁剪使用
                        .isPreviewImage(isPreviewImage)// 是否可预览图片
                        .isPreviewVideo(isPreviewVideo)// 是否可预览视频
                        .isEnablePreviewAudio(isEnablePreviewAudio) // 是否可播放音频
                        .isCamera(isCamera)// 是否显示拍照按钮
                        .isEnableCrop(isEnableCrop)// 是否裁剪
                        .isCompress(isCompress)// 是否压缩
                        .compressQuality(60)// 图片压缩后输出质量
                        .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                        .withAspectRatio(aspect_ratio_x, aspect_ratio_y)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .hideBottomControls(hideBottomControls)// 是否显示uCrop工具栏，默认不显示
                        .isGif(isGif)// 是否显示gif图片
                        .freeStyleCropEnabled(isFreeStyleCropEnabled)// 裁剪框是否可拖拽
                        .circleDimmedLayer(isCircleDimmedLayer)// 是否圆形裁剪
                        //.setCircleDimmedColor(ContextCompat.getColor(this, R.color.app_color_white))// 设置圆形裁剪背景色值
                        //.setCircleDimmedBorderColor(ContextCompat.getColor(this, R.color.app_color_white))// 设置圆形裁剪边框色值
                        //.setCircleStrokeWidth(3)// 设置圆形裁剪边框粗细
                        .showCropFrame(isShowCropFrame)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .showCropGrid(isShowCropGrid)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .isOpenClickSound(isOpenClickSound)// 是否开启点击声音
                        .selectionData(mAdapter.getData())// 是否传入已选图片
                        .isPreviewEggs(false)//预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 废弃 改用cutOutQuality()
                        .cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.rotateEnabled() // 裁剪是否可旋转图片
                        //.scaleEnabled()// 裁剪是否可放大缩小图片
                        //.videoQuality()// 视频录制质量 0 or 1
                        //.forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                        .forResult(new PictureResultCallback(mAdapter));
            }
        }

    };

    /**
     * 返回结果回调
     */
    private class PictureResultCallback implements OnResultCallbackListener<LocalMedia> {
        private WeakReference<GridImageAdapter> mAdapterWeakReference;

        public PictureResultCallback(GridImageAdapter adapter) {
            super();
            this.mAdapterWeakReference = new WeakReference<>(adapter);
        }

        @Override
        public void onResult(List<LocalMedia> result) {
            if (mPictureSelectListener != null) {
                mPictureSelectListener.pictureSelect(result);
            }
            for (LocalMedia media : result) {
                Log.i(TAG, "是否压缩:" + media.isCompressed());
                Log.i(TAG, "压缩:" + media.getCompressPath());
                Log.i(TAG, "原图:" + media.getPath());
                Log.i(TAG, "绝对路径:" + media.getRealPath());
                Log.i(TAG, "是否裁剪:" + media.isCut());
                Log.i(TAG, "裁剪:" + media.getCutPath());
                Log.i(TAG, "是否开启原图:" + media.isOriginal());
                Log.i(TAG, "原图路径:" + media.getOriginalPath());
                Log.i(TAG, "Android Q 特有Path:" + media.getAndroidQToPath());
                Log.i(TAG, "宽高: " + media.getWidth() + "x" + media.getHeight());
                Log.i(TAG, "Size: " + media.getSize());
                // TODO 可以通过PictureSelectorExternalUtils.getExifInterface();方法获取一些额外的资源信息，如旋转角度、经纬度等信息
            }
            if (mAdapterWeakReference.get() != null) {
                mAdapterWeakReference.get().setList(result);
                mAdapterWeakReference.get().notifyDataSetChanged();
            }
        }

        @Override
        public void onCancel() {
            Log.i(TAG, "PictureSelector Cancel");
        }
    }

    /**
     * 没啥用 回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回五种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 4.media.getOriginalPath()); media.isOriginal());为true时此字段才有值
                    // 5.media.getAndroidQToPath();为Android Q版本特有返回的字段，此字段有值就用来做上传使用
                    // 如果同时开启裁剪和压缩，则取压缩路径为准因为是先裁剪后压缩
                    if (mPictureSelectListener != null) {
                        mPictureSelectListener.pictureSelect(selectList);
                    }
                    for (LocalMedia media : selectList) {
                        Log.i(TAG, "Activity回调__是否压缩:" + media.isCompressed());
                        Log.i(TAG, "Activity回调__压缩:" + media.getCompressPath());
                        Log.i(TAG, "Activity回调__原图:" + media.getPath());
                        Log.i(TAG, "Activity回调__绝对路径:" + media.getRealPath());
                        Log.i(TAG, "Activity回调__是否裁剪:" + media.isCut());
                        Log.i(TAG, "Activity回调__裁剪:" + media.getCutPath());
                        Log.i(TAG, "Activity回调__是否开启原图:" + media.isOriginal());
                        Log.i(TAG, "Activity回调__原图路径:" + media.getOriginalPath());
                        Log.i(TAG, "Activity回调__Android Q 特有Path:" + media.getAndroidQToPath());

                    }
                    mAdapter.setList(selectList);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }


    /**
     * 设置宽高比例
     *
     * @param aspect_ratio_x
     * @param aspect_ratio_y
     */
    public void setAspectRatio(int aspect_ratio_x, int aspect_ratio_y) {
        this.aspect_ratio_x = aspect_ratio_x;
        this.aspect_ratio_y = aspect_ratio_y;
    }

    /**
     * 设置图片进场动画
     *
     * @param isDefault
     */
    public void setPhotoAnimation(boolean isDefault) {
        if (isDefault) {
            mWindowAnimationStyle = new PictureWindowAnimationStyle();
        } else {
            mWindowAnimationStyle = new PictureWindowAnimationStyle();
            mWindowAnimationStyle.ofAllAnimation(R.anim.picture_anim_up_in, R.anim.picture_anim_down_out);
        }
    }

    /**
     * 设置数据
     *
     * @param dataList
     */
    public void setList(List<LocalMedia> dataList) {
        this.dataList = dataList;
    }

    /**
     * 是否只拍照
     *
     * @param isNoOnlyCamera
     */
    public void setNoOnlyCamera(boolean isNoOnlyCamera) {
        isNoOnlyCamera = isNoOnlyCamera;
    }

    /**
     * 主题
     *
     * @param themeId
     */
    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    /**
     * 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
     *
     * @param maxVideoSelectNum 默认是 1
     */
    public void setMaxVideoSelectNum(int maxVideoSelectNum) {
        this.maxVideoSelectNum = maxVideoSelectNum;
    }

    /**
     * 图片和视频是否可以同选
     *
     * @param withVideoImage 默认是 true
     */
    public void setWithVideoImage(boolean withVideoImage) {
        isWithVideoImage = withVideoImage;
    }

    /**
     * 拍照保存图片格式后缀 [PictureMimeType.JPEG、PictureMimeType.PNG]等
     *
     * @param imageFormat 默认jpeg
     */
    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    /**
     * 类型： [ PictureMimeType.ofAll()、ofImage、ofVideo、ofAudio、ofPNG、ofJPEG、ofBMP、ofGIF
     * <p>
     * ofWEBP、of3GP、ofMP4、ofMPEG 、ofAVI]
     *
     * @param chooseMode
     */

    public void setChooseMode(int chooseMode) {
        this.chooseMode = chooseMode;
    }

    /**
     * 是否开启微信图片选择风格
     *
     * @param weChatStyle 默认是 false
     */
    public void setWeChatStyle(boolean weChatStyle) {
        isWeChatStyle = weChatStyle;
    }

    /**
     * 设置语音，默认中文
     *
     * @param language [LanguageConfig.JAPAN、 LanguageConfig.TRADITIONAL_CHINESE
     *                 LanguageConfig.ENGLISH、LanguageConfig.KOREA、LanguageConfig.GERMANY
     *                 LanguageConfig.FRANCE、LanguageConfig.SPANISH、LanguageConfig.SPANISH]
     */

    public void setLanguage(int language) {
        this.language = language;
    }

    /**
     * @param upward
     */
    public void setUpward(boolean upward) {
        isUpward = upward;
    }

    /**
     * @param needScaleBig
     */
    public void setNeedScaleBig(boolean needScaleBig) {
        this.needScaleBig = needScaleBig;
    }

    /**
     * @param needScaleSmall
     */
    public void setNeedScaleSmall(boolean needScaleSmall) {
        this.needScaleSmall = needScaleSmall;
    }

    /**
     * 最大选择图片
     *
     * @param maxSelectNum 默认是9
     */
    public void setMaxSelectNum(int maxSelectNum) {
        this.maxSelectNum = maxSelectNum;
    }

    /**
     * 最小选择图片
     *
     * @param minSelectNum 默认是1
     */
    public void setMinSelectNum(int minSelectNum) {
        this.minSelectNum = minSelectNum;
    }

    /**
     * 是否多选图片
     *
     * @param multiple 默认是 true
     */
    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }

    /**
     * 是否使用自定义相机
     *
     * @param useCustomCamera 默认是 true
     */
    public void setUseCustomCamera(boolean useCustomCamera) {
        isUseCustomCamera = useCustomCamera;
    }

    /**
     * 是否可预览图片
     *
     * @param previewImage 默认是 true
     */
    public void setPreviewImage(boolean previewImage) {
        isPreviewImage = previewImage;
    }

    /**
     * 是否可预览视频
     *
     * @param previewVideo 默认是 true
     */
    public void setPreviewVideo(boolean previewVideo) {
        isPreviewVideo = previewVideo;
    }

    /**
     * 是否可播放音频
     *
     * @param enablePreviewAudio 默认是 true
     */
    public void setEnablePreviewAudio(boolean enablePreviewAudio) {
        isEnablePreviewAudio = enablePreviewAudio;
    }

    /**
     * 是否显示拍照按钮
     *
     * @param camera 默认是 true
     */
    public void setCamera(boolean camera) {
        isCamera = camera;
    }

    /**
     * 是否显示uCrop工具栏，默认不显示
     *
     * @param hideBottomControls 默认是 false
     */
    public void setHideBottomControls(boolean hideBottomControls) {
        this.hideBottomControls = hideBottomControls;
    }

    /**
     * 是否显示gif图片
     *
     * @param gif 默认是 false
     */
    public void isShowGif(boolean gif) {
        isGif = gif;
    }

    /**
     * 裁剪框是否可拖拽
     *
     * @param freeStyleCropEnabled 默认是 false
     */
    public void setFreeStyleCropEnabled(boolean freeStyleCropEnabled) {
        isFreeStyleCropEnabled = freeStyleCropEnabled;
    }

    /**
     * 是否圆形裁剪
     *
     * @param circleDimmedLayer 默认是 false
     */
    public void setCircleDimmedLayer(boolean circleDimmedLayer) {
        isCircleDimmedLayer = circleDimmedLayer;
    }

    /**
     * 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
     *
     * @param originalImageControl 默认是 false
     */
    public void setOriginalImageControl(boolean originalImageControl) {
        isOriginalImageControl = originalImageControl;
    }

    /**
     * 是否显示裁剪矩形边框 圆形裁剪时建议设为false
     *
     * @param showCropFrame 默认是 false
     */
    public void setShowCropFrame(boolean showCropFrame) {
        isShowCropFrame = showCropFrame;
    }

    /**
     * 是否显示裁剪矩形网格 圆形裁剪时建议设为false
     *
     * @param showCropGrid 默认是 false
     */
    public void setShowCropGrid(boolean showCropGrid) {
        isShowCropGrid = showCropGrid;
    }

    /**
     * 是否开启点击声音
     *
     * @param openClickSound 默认是 false
     */
    public void setOpenClickSound(boolean openClickSound) {
        isOpenClickSound = openClickSound;
    }

    /**
     * 是否裁剪
     *
     * @param enableCrop 默认是 false
     */
    public void setEnableCrop(boolean enableCrop) {
        isEnableCrop = enableCrop;
    }

    /**
     * 是否压缩
     *
     * @param compress 默认是 false
     */
    public void setCompress(boolean compress) {
        isCompress = compress;
    }

    /**
     * 相册每行显示个数
     *
     * @param imageSpanCount 默认是 4
     */
    public void setImageSpanCount(int imageSpanCount) {
        this.imageSpanCount = imageSpanCount;
    }


    /**
     * 列表显示个数
     *
     * @param spanCount
     */
    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    /**
     * 是否显示右上按钮
     *
     * @param showTopDel 默认是显示
     */
    public void setShowTopDel(boolean showTopDel) {
        isShowTopDel = showTopDel;
    }

    /**
     * 是否显示按压删除
     *
     * @param isShowTouchDel 默认是显示
     */
    public void setShowTouchDel(boolean isShowTouchDel) {
        this.isShowTouchDel = isShowTouchDel;
    }


    /**
     * 动态自定义相册主题
     *
     * @param type
     */
    public void setPictureParameterStyle(String type, String color) {
        switch (type) {
            case "default":
                mPictureParameterStyle = PictureStyle.getDefaultStyle(getContext());
                break;
            case "white":
                mPictureParameterStyle = PictureStyle.getWhiteStyle(getContext());
                break;
            case "sina":
                mPictureParameterStyle = PictureStyle.getSinaStyle(getContext());
                break;
            case "weChat":
                mPictureParameterStyle = PictureStyle.getWeChatStyle(getContext());
            case "num":
                mPictureParameterStyle = PictureStyle.getNumStyle(getContext());
                break;
            case "custom":
                mPictureParameterStyle = PictureStyle.getCustomParameterStyle(getContext(), false, true, false, isShowTopDel, color);
                break;
        }
    }

    /**
     * 动态自定义裁剪主题
     *
     * @param cropParameterStyle
     */
    public void setPictureCropParameterStyle(PictureCropParameterStyle cropParameterStyle) {
        this.mCropParameterStyle = cropParameterStyle;
    }

    /**
     * 自定义相册启动退出动画
     *
     * @param windowAnimationStyle
     */
    public void setPictureWindowAnimationStyle(PictureWindowAnimationStyle windowAnimationStyle) {
        this.mWindowAnimationStyle = windowAnimationStyle;
    }

    /**
     * 未选择数据时点击按钮是否可以返回
     *
     * @param returnEmpty 默认是 false
     */
    public void setReturnEmpty(boolean returnEmpty) {
        isReturnEmpty = returnEmpty;
    }

    private PictureSelectListener mPictureSelectListener;

    /**
     * 选择图片后的回调
     *
     * @param pictureSelectListener
     */
    public void setPictureSelectListener(PictureSelectListener pictureSelectListener) {
        this.mPictureSelectListener = pictureSelectListener;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (BroadcastAction.ACTION_DELETE_PREVIEW_POSITION.equals(action)) {// 外部预览删除按钮回调
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    int position = extras.getInt(PictureConfig.EXTRA_PREVIEW_DELETE_POSITION);
//                    ToastUtils.s(context, "delete image index:" + position);
                    mAdapter.remove(position);
                    mAdapter.notifyItemRemoved(position);
                }
            }
        }
    };

    /**
     * 清空缓存包括裁剪、压缩、AndroidQToPath所生成的文件，注意调用时机必须是处理完本身的业务逻辑后调用；非强制性
     */
    public void clearCache() {
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        if (getContext() != null) {
            if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //PictureFileUtils.deleteCacheDirFile(this, PictureMimeType.ofImage());
                PictureFileUtils.deleteAllCacheDirFile(getContext());
            } else {
                PermissionChecker.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
            }
        }
    }

    /**
     * 注销广播
     */
    public void onDestroy() {
        if (broadcastReceiver != null) {
            if (getContext() != null) {
                BroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver,
                        BroadcastAction.ACTION_DELETE_PREVIEW_POSITION);
            }
        }
    }

}
