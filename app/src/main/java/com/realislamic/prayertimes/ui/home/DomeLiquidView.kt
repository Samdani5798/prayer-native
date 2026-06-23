package com.realislamic.prayertimes.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.realislamic.prayertimes.R
import kotlin.math.sin

class DomeLiquidView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var fillProgress: Float = 0f
    private var wavePhase: Float = 0f
    private val waveAnimator: ValueAnimator

    private val domePath = Path()
    private val wavePath = Path()

    private val shellShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dome_shell_shadow)
        style = Paint.Style.FILL
    }

    private val shellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dome_shell)
        style = Paint.Style.FILL
    }

    private val shellStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.divider)
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val liquidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        alpha = 60
        style = Paint.Style.FILL
    }

    private val baseRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dome_shell)
        style = Paint.Style.FILL
    }

    private val baseWindowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        waveAnimator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                wavePhase = it.animatedValue as Float
                invalidate()
            }
        }
        waveAnimator.start()
    }

    fun setFillProgress(progress: Float, animate: Boolean = true) {
        val target = progress.coerceIn(0f, 1f)
        if (animate) {
            ValueAnimator.ofFloat(fillProgress, target).apply {
                duration = 600L
                addUpdateListener {
                    fillProgress = it.animatedValue as Float
                    invalidate()
                }
            }.start()
        } else {
            fillProgress = target
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val baseRingHeight = h * 0.11f
        val domeBottom = h - baseRingHeight
        val domeTop = h * 0.04f

        buildDomePath(w, domeTop, domeBottom)

        canvas.save()
        canvas.translate(0f, 4f)
        canvas.drawPath(domePath, shellShadowPaint)
        canvas.restore()

        canvas.drawPath(domePath, shellPaint)

        canvas.save()
        canvas.clipPath(domePath)
        drawLiquid(canvas, w, domeTop, domeBottom)
        canvas.restore()

        canvas.drawPath(domePath, shellStrokePaint)

        drawBaseRing(canvas, w, domeBottom, h)
    }

    private fun buildDomePath(w: Float, top: Float, bottom: Float) {
        domePath.reset()

        val centerX = w / 2f
        val domeHeight = bottom - top

        val apexY = top
        val apexWidth = w * 0.04f

        val shoulderY = top + domeHeight * 0.50f
        val shoulderHalfWidth = w * 0.46f

        val baseHalfWidth = w * 0.40f

        domePath.moveTo(centerX, apexY)

        domePath.cubicTo(
            centerX + apexWidth, apexY + domeHeight * 0.04f,
            centerX + shoulderHalfWidth * 0.92f, shoulderY - domeHeight * 0.30f,
            centerX + shoulderHalfWidth, shoulderY
        )

        domePath.cubicTo(
            centerX + shoulderHalfWidth * 0.98f, shoulderY + domeHeight * 0.22f,
            centerX + baseHalfWidth * 1.05f, bottom - domeHeight * 0.06f,
            centerX + baseHalfWidth, bottom
        )

        domePath.quadTo(centerX, bottom + domeHeight * 0.015f, centerX - baseHalfWidth, bottom)

        domePath.cubicTo(
            centerX - baseHalfWidth * 1.05f, bottom - domeHeight * 0.06f,
            centerX - shoulderHalfWidth * 0.98f, shoulderY + domeHeight * 0.22f,
            centerX - shoulderHalfWidth, shoulderY
        )

        domePath.cubicTo(
            centerX - shoulderHalfWidth * 0.92f, shoulderY - domeHeight * 0.30f,
            centerX - apexWidth, apexY + domeHeight * 0.04f,
            centerX, apexY
        )

        domePath.close()
    }

    private fun drawLiquid(canvas: Canvas, w: Float, domeTop: Float, domeBottom: Float) {
        val domeHeight = domeBottom - domeTop
        val liquidTopY = domeBottom - (domeHeight * fillProgress)

        val waveAmplitude = (domeHeight * 0.012f).coerceAtMost(6f)
        val waveLength = w / 1.4f

        liquidPaint.shader = LinearGradient(
            0f, liquidTopY, 0f, domeBottom,
            ContextCompat.getColor(context, R.color.dome_liquid_top),
            ContextCompat.getColor(context, R.color.dome_liquid_bottom),
            Shader.TileMode.CLAMP
        )

        wavePath.reset()
        wavePath.moveTo(0f, domeBottom + 10f)
        wavePath.lineTo(0f, liquidTopY)

        var x = 0f
        while (x <= w) {
            val y = liquidTopY + waveAmplitude * sin((x / waveLength) * 2 * Math.PI + wavePhase).toFloat()
            wavePath.lineTo(x, y)
            x += 4f
        }

        wavePath.lineTo(w, domeBottom + 10f)
        wavePath.close()

        canvas.drawPath(wavePath, liquidPaint)

        val highlightRect = RectF(0f, liquidTopY, w, liquidTopY + waveAmplitude * 2.5f)
        canvas.drawRect(highlightRect, highlightPaint)
    }

    private fun drawBaseRing(canvas: Canvas, w: Float, domeBottom: Float, totalHeight: Float) {
        val ringRect = RectF(0f, domeBottom, w, totalHeight)
        canvas.drawRect(ringRect, baseRingPaint)
        canvas.drawLine(0f, domeBottom, w, domeBottom, shellStrokePaint)

        val windowCount = 9
        val windowWidth = w / windowCount
        baseWindowPaint.color = ContextCompat.getColor(context, R.color.dome_liquid_top)
        baseWindowPaint.alpha = 140

        for (i in 0 until windowCount) {
            val cx = windowWidth * i + windowWidth / 2f
            val archRect = RectF(
                cx - windowWidth * 0.22f,
                domeBottom + (totalHeight - domeBottom) * 0.30f,
                cx + windowWidth * 0.22f,
                totalHeight - (totalHeight - domeBottom) * 0.18f
            )
            canvas.drawRoundRect(archRect, 8f, 8f, baseWindowPaint)
        }
    }
}
