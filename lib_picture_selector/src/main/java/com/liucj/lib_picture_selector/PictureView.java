package com.liucj.lib_picture_selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.liucj.lib_picture_selector.adapter.GridImageAdapter;
import com.liucj.lib_picture_selector.listener.PictureSelectListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureCropParameterStyle;
import com.luck.picture.lib.style.PictureParameterStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PictureView extends LinearLayout {
    private final static String TAG = PictureView.class.getSimpleName();
    private boolean isOnlyCamera = true;
    private int themeId;//主题
    private int chooseMode = PictureMimeType.ofAll();//类型
    private boolean isWeChatStyle;
    private int language = -1;
    private boolean isUpward;
    private boolean needScaleBig = true;
    private boolean needScaleSmall = true;
    private int maxSelectNum = 9;
    private boolean isMultiple = true;

    private boolean isUseCustomCamera = true;// 是否使用自定义相机
    private boolean isPreviewImage = true;// 是否可预览图片
    private boolean isPreviewVideo = true;// 是否可预览视频
    private boolean isEnablePreviewAudio = true;// 是否可播放音频= false;// 是否可预览视频
    private boolean isCamera = true;//是否显示拍照按钮
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

    private GridImageAdapter mAdapter;
    private PictureParameterStyle mPictureParameterStyle;// 动态自定义相册主题
    private PictureCropParameterStyle mCropParameterStyle;// 动态自定义裁剪主题
    private PictureWindowAnimationStyle mWindowAnimationStyle;// 自定义相册启动退出动画
    private int aspect_ratio_x = 16;
    private int aspect_ratio_y = 9;
    private List<LocalMedia> dataList = new ArrayList<>();

    public PictureView(Context context) {
        this(context, null);
        init();
    }

    public PictureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_picture_view, this);
        RecyclerView mRecyclerView = view.findViewById(R.id.mRecyclerView);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(getContext(),
                4, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4,
                ScreenUtils.dip2px(getContext(), 8), false));
        mWindowAnimationStyle = new PictureWindowAnimationStyle();
        mPictureParameterStyle = PictureStyle.getDefaultStyle(getContext());
        mCropParameterStyle = PictureStyle.pictureCropParameterStyle(getContext(), mPictureParameterStyle);
        mAdapter = new GridImageAdapter(getContext(), onAddPicClickListener);
        mAdapter.setList(dataList);
        mAdapter.setSelectMax(maxSelectNum);
        mRecyclerView.setAdapter(mAdapter);
    }


    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            if (isOnlyCamera) {
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
                        .isWithVideoImage(true)// 图片和视频是否可以同选
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        //.minSelectNum(1)// 最小选择数量
                        //.minVideoSelectNum(1)// 视频最小选择数量，如果没有单独设置的需求则可以不设置，同用minSelectNum字段
                        .maxVideoSelectNum(1) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                        .imageSpanCount(4)// 每行显示个数
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
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
                        //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
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
                        .minSelectNum(1)// 最小选择数量
                        .isUseCustomCamera(isUseCustomCamera)// 是否使用自定义相机
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
    private static class PictureResultCallback implements OnResultCallbackListener<LocalMedia> {
        private WeakReference<GridImageAdapter> mAdapterWeakReference;

        public PictureResultCallback(GridImageAdapter adapter) {
            super();
            this.mAdapterWeakReference = new WeakReference<>(adapter);
        }

        @Override
        public void onResult(List<LocalMedia> result) {
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
     * 回调
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
                        Log.i(TAG, "是否压缩:" + media.isCompressed());
                        Log.i(TAG, "压缩:" + media.getCompressPath());
                        Log.i(TAG, "原图:" + media.getPath());
                        Log.i(TAG, "绝对路径:" + media.getRealPath());
                        Log.i(TAG, "是否裁剪:" + media.isCut());
                        Log.i(TAG, "裁剪:" + media.getCutPath());
                        Log.i(TAG, "是否开启原图:" + media.isOriginal());
                        Log.i(TAG, "原图路径:" + media.getOriginalPath());
                        Log.i(TAG, "Android Q 特有Path:" + media.getAndroidQToPath());

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

    public void setPhotoAnimation(boolean isDefault) {
        if (isDefault) {
            mWindowAnimationStyle = new PictureWindowAnimationStyle();
        } else {
            mWindowAnimationStyle = new PictureWindowAnimationStyle();
            mWindowAnimationStyle.ofAllAnimation(R.anim.picture_anim_up_in, R.anim.picture_anim_down_out);
        }
    }

    public void setList(List<LocalMedia> dataList) {
        this.dataList = dataList;
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

}
