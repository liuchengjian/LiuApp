package com.liucj.liu_common.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.Typeface.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * 用于支持全局iconfont资源的应用
 */
class IconFontTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int
) : AppCompatTextView(context, attributeSet, defStyleAttr) {
    init {
        val typeface: Typeface = createFromAsset(context.assets, "/fonts/iconfont.ttf")
        setTypeface(typeface)
    }
}