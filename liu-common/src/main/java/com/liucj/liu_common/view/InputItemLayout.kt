package com.liucj.liu_common.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputType.*
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.liucj.liu_common.R


open class InputItemLayout : LinearLayout {
    private lateinit var topLine: Line
    private lateinit var bottomLine: Line
    private lateinit var editText: EditText
    private lateinit var textView: TextView
    private var isRequired: Boolean = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        orientation = LinearLayout.HORIZONTAL
        dividerDrawable = ColorDrawable()
        showDividers = SHOW_DIVIDER_BEGINNING
        gravity = Gravity.CENTER_VERTICAL

        //解析标题资源
        var array: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.InputItemLayout)
        val titleRequiredTypeId: Int =
            array.getResourceId(R.styleable.InputItemLayout_titleRequiredAppearance, 0)
        parseTitleRequiredStyle(titleRequiredTypeId)
        val titleTypeId: Int =
            array.getResourceId(R.styleable.InputItemLayout_titleTextAppearance, 0)
        val title: String? = array.getString(R.styleable.InputItemLayout_title)
        parseTitleStyle(titleTypeId, title)
        //解析右边输入资源
        val inputTypeId: Int =
            array.getResourceId(R.styleable.InputItemLayout_inputTextAppearance, 0)
        val hint: String? = array.getString(R.styleable.InputItemLayout_hint)
        val inputType: Int = array.getInteger(R.styleable.InputItemLayout_inputType, 0)
        parseInputStyle(inputTypeId, hint, inputType)
        //解析上下线资源
        val topLineTypeId: Int =
            array.getResourceId(R.styleable.InputItemLayout_topLineAppearance, 0)
        val bottomLineTypeId: Int =
            array.getResourceId(R.styleable.InputItemLayout_bottomLineAppearance, 0)
        topLine = parseLineStyle(topLineTypeId)
        bottomLine = parseLineStyle(bottomLineTypeId)
        if (topLine.enable) {
            topPaint.color = topLine.color
            topPaint.style = Paint.Style.FILL_AND_STROKE
            topPaint.strokeWidth = topLine.height.toFloat()
        }
        if (bottomLine.enable) {
            bottomPaint.color = bottomLine.color
            bottomPaint.style = Paint.Style.FILL_AND_STROKE
            bottomPaint.strokeWidth = bottomLine.height.toFloat()
        }
        array.recycle()
    }

    private fun parseTitleRequiredStyle(resId: Int) {
        val array: TypedArray =
            context.obtainStyledAttributes(resId, R.styleable.titleRequiredAppearance)
        var titleColor: Int = array.getColor(
            R.styleable.titleRequiredAppearance_titleRequiredColor,
            resources.getColor(R.color.common_black)
        )
        isRequired = array.getBoolean(
            R.styleable.titleRequiredAppearance_isRequired,
            false
        )
        var titleSize: Int = array.getDimensionPixelSize(
            R.styleable.titleRequiredAppearance_titleRequiredSize,
            applyUnit(TypedValue.COMPLEX_UNIT_SP, 14f)
        )
        var leftMargin = array.getDimensionPixelOffset(
            R.styleable.titleRequiredAppearance_titleRequired_marginLeft,
            0
        )

        textView = TextView(context)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        params.setMargins(leftMargin, 0, 0, 0)
        textView.layoutParams = params
        if (isRequired) {
            textView.text = "*"
        } else {
            textView.text = ""
        }
        textView.setTextColor(titleColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
        textView.gravity = Gravity.CENTER_VERTICAL
        addView(textView)
        array.recycle()
    }

    private fun parseTitleStyle(resId: Int, title: String?) {
        val array: TypedArray =
            context.obtainStyledAttributes(resId, R.styleable.titleTextAppearance)
        var titleColor: Int = array.getColor(
            R.styleable.titleTextAppearance_titleColor,
            resources.getColor(R.color.common_black)
        )
        var titleSize: Int = array.getDimensionPixelSize(
            R.styleable.titleTextAppearance_titleSize,
            applyUnit(TypedValue.COMPLEX_UNIT_SP, 14f)
        )
        var leftMargin =
            array.getDimensionPixelOffset(R.styleable.titleTextAppearance_title_marginLeft, 0)
        var rightMargin =
            array.getDimensionPixelOffset(R.styleable.titleTextAppearance_title_marginRight, 0)


        var minWidth: Int = array.getDimensionPixelSize(R.styleable.titleTextAppearance_minWidth, 0)
        val textView = TextView(context)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        params.setMargins(leftMargin, 0, rightMargin, 0)
        textView.layoutParams = params
        textView.minWidth = minWidth
        textView.text = title
        textView.setTextColor(titleColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
        textView.gravity = Gravity.CENTER_VERTICAL
        addView(textView)
        array.recycle()

    }

    private fun parseInputStyle(inputTypeId: Int, hint: String?, inputType: Int) {
        val array: TypedArray =
            context.obtainStyledAttributes(inputTypeId, R.styleable.inputTextAppearance)
        var hintColor: Int = array.getColor(
            R.styleable.inputTextAppearance_hintColor,
            resources.getColor(R.color.common_black)
        )
        var inputColor: Int = array.getColor(
            R.styleable.inputTextAppearance_inputColor,
            resources.getColor(R.color.common_black)
        )
        var textSize: Int = array.getDimensionPixelSize(
            R.styleable.inputTextAppearance_textSize,
            applyUnit(TypedValue.COMPLEX_UNIT_SP, 14f)
        )
        var leftMargin =
            array.getDimensionPixelOffset(R.styleable.inputTextAppearance_input_marginLeft, 0)
        var rightMargin =
            array.getDimensionPixelOffset(R.styleable.inputTextAppearance_input_marginRight, 0)

        editText = EditText(context)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        params.weight = 1f
        params.setMargins(leftMargin, 0, rightMargin, 0)
        editText.layoutParams = params
        editText.hint = hint
        editText.setHintTextColor(hintColor)
        editText.setTextColor(inputColor)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                //s--最终内容
            }
        })
        editText.setBackgroundColor(Color.TRANSPARENT)//去掉背景
        editText.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL

        when (inputType) {
            0 -> {
                editText.inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_NORMAL
            }
            1 -> {
                editText.inputType =
                    TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }
            2 -> {
                editText.inputType =
                    TYPE_CLASS_NUMBER or TYPE_NUMBER_VARIATION_NORMAL
            }
        }
        addView(editText)
        array.recycle()
    }

    open fun requiredText(): String {
        if (isRequired) {
            if (TextUtils.isEmpty(editText.text.toString())) {
                Toast.makeText(context, "请输入" + textView.text, Toast.LENGTH_SHORT).show()
                return ""
            }
        }
        return editText.text.toString()
    }

    private fun parseLineStyle(resId: Int): Line {
        var line = Line()
        val array: TypedArray =
            context.obtainStyledAttributes(resId, R.styleable.lineAppearance)
        line.color =
            array.getColor(R.styleable.lineAppearance_color, resources.getColor(R.color.line))
        line.height = array.getDimensionPixelOffset(R.styleable.lineAppearance_height, 0)
        line.leftMargin = array.getDimensionPixelOffset(R.styleable.lineAppearance_leftMargin, 0)
        line.rightMargin = array.getDimensionPixelOffset(R.styleable.lineAppearance_rightMargin, 0)
        line.enable = array.getBoolean(R.styleable.lineAppearance_enable, false)
        array.recycle()

        return line
    }

    inner class Line {
        var color = 0
        var height = 0;
        var leftMargin = 0
        var rightMargin = 0
        var enable = false
    }

    private var topPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var bottomPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (topLine.enable) {
            canvas!!.drawLine(
                topLine.leftMargin.toFloat(),
                0f,
                (measuredWidth - topLine.rightMargin).toFloat(),
                0f,
                topPaint
            )
        }
        if (bottomLine.enable) {
            canvas!!.drawLine(
                bottomLine.leftMargin.toFloat(),
                (height - bottomLine.height).toFloat(),
                (measuredWidth - bottomLine.rightMargin).toFloat(),
                (height - bottomLine.height).toFloat(),
                bottomPaint
            )
        }
    }

    private fun applyUnit(unit: Int, value: Float): Int {
        return TypedValue.applyDimension(unit, value, resources.displayMetrics).toInt()
    }

}