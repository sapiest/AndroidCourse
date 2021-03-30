package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToPx
import kotlin.math.hypot

class ArticleSubmenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var isOpen = false
    private var centerX: Float = context.dpToPx(200)
    private var centerY: Float = context.dpToPx(96)

    init {
        View.inflate(context, R.layout.layout_submenu, this)
        //add material bg for handle elevation and color surface
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    fun open() {
        if (isOpen || !isAttachedToWindow) return
        isOpen = !isOpen
        animateShow()
    }

    fun close() {
        if (!isOpen || !isAttachedToWindow) return
        isOpen = !isOpen
        animatedHide()
    }

    private fun animatedHide(){
        val endRadius = hypot(centerX, centerY).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            centerX.toInt(),
            centerY.toInt(),
            endRadius.toFloat(),
            0f
        )
        anim.doOnEnd {
            visibility = View.GONE
        }
        anim.start()
    }

    private fun animateShow(){
        val endRadius = hypot(centerX, centerY).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            centerX.toInt(),
            centerY.toInt(),
            0f,
            endRadius.toFloat()
        )
        anim.doOnStart {
            visibility = View.VISIBLE
        }
        anim.start()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.ssIsOpen = isOpen
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if(state is SavedState){
            isOpen = state.ssIsOpen
            visibility = if(isOpen) View.VISIBLE else View.GONE
        }
    }

    private class SavedState: BaseSavedState, Parcelable{
        var ssIsOpen: Boolean = false

        constructor(superState: Parcelable?): super(superState)

        constructor(src: Parcel): super(src){
            ssIsOpen = src.readInt() == 1
        }

        override fun describeContents() = 0

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if(ssIsOpen) 1 else 0)
        }

        companion object CREATER: Parcelable.Creator<SavedState>{
            override fun createFromParcel(source: Parcel) = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}