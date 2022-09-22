package ru.netology.nmedia.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.utils.AndroidUtils
import java.lang.Integer.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var mRadius: Float = 0f
    private var mCenter: PointF = PointF(0f, 0f)
    private var mOval: RectF = RectF(0f, 0f, 0f, 0f)

    private var mLineWidth: Float = AndroidUtils.dpToPx(context, 5f).toFloat()
    private var mFontSize: Float = AndroidUtils.dpToPx(context, 40f).toFloat()
    private var mColors: List<Int> = emptyList()

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            mLineWidth = getDimension(R.styleable.StatsView_lineWidth, mLineWidth)
            mFontSize = getDimension(R.styleable.StatsView_fontSize, mFontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            mColors = resources.getIntArray(resId).toList()
        }
    }

    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = mLineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = mFontSize
    }
    private var mProgress: Float = 0f
    private var mAnimator: ValueAnimator? = null

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mRadius = min(w, h) / 2f - mLineWidth / 2
        mCenter = PointF(w / 2f, h / 2f)
        mOval = RectF(
            mCenter.x - mRadius, mCenter.y - mRadius,
            mCenter.x + mRadius, mCenter.y + mRadius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startFrom = -90f
        val currentSum = data.sum()
        data.forEachIndexed { index, datum ->
            val angle = 360f * (datum / currentSum)
            mPaint.color = mColors.getOrNull(index) ?: randomColor()
            canvas.drawArc(mOval, startFrom + (mProgress * 360f), angle * mProgress, false, mPaint)
            startFrom += angle
        }
        if (mProgress == 1f) {
            mPaint.color = mColors.first()
            canvas.drawCircle(mCenter.x, mCenter.y - mRadius, 1f, mPaint)
        }

        canvas.drawText(
            "%.2f%%".format(100f),
            mCenter.x,
            mCenter.y + mTextPaint.textSize / 4,
            mTextPaint,
        )
    }

    private fun update() {
        mAnimator?.apply {
            removeAllListeners()
            cancel()
        }
        mProgress = 0f

        mAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { animator ->
                mProgress = animator.animatedValue as Float
                invalidate()
            }
            duration = 2_000L
            interpolator = LinearInterpolator()
        }.also { animator ->
            animator.start()
        }
    }

    private fun randomColor(): Int = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}