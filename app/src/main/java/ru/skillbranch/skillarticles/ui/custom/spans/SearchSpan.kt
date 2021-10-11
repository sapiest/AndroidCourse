package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import androidx.core.graphics.ColorUtils

open class SearchSpan() : BackgroundColorSpan(Color.WHITE) {
//    private val alpha by lazy {
//        ColorUtils.setAlphaComponent(backgroundColor, 160)
//    }
//
//    override fun updateDrawState(textPaint: TextPaint) {
//        textPaint.bgColor = alpha
//        textPaint.color = fgColor
//    }
}