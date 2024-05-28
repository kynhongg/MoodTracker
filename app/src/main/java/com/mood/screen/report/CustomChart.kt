package com.mood.screen.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.mood.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider
import com.github.mikephil.charting.utils.MPPointD

class CustomChart : LineChart {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    var axisBaselines = listOf<EmojiBaseLine>()
        set(value) {
            field = value
            invalidate()
        }
    private var baseLineTextSize = EmojiBaseLine.DEFAULT_BASELINE_TEXT_SIZE.spToPx(context!!)
        set(value) {
            field = value
            invalidate()
        }
    private var baseLineLabelFont: Typeface? = ResourcesCompat.getFont(context!!, R.font.nunito_regular_400)
        set(value) {
            field = value
            invalidate()
        }

    init {
        setDrawGridBackground(false)
    }

//    override fun drawGridBackground(c: Canvas?) {
//        if (c == null) return
//        if (mDrawGridBackground) {
//            // draw the grid background
//            ContextCompat.getDrawable(context, R.drawable.bg_gradient)?.let {
//                mViewPortHandler.contentRect?.let { rect ->
//                    it.setBounds(
//                        rect.left.toInt(),
//                        rect.top.toInt(),
//                        rect.right.toInt() + extraRightOffset.dpToPx(context).toInt(),
//                        rect.bottom.toInt()
//                    )
//                }
//                it.draw(c)
//            }
//        }
//        if (mDrawBorders) {
//            c.drawRect(mViewPortHandler.contentRect, mBorderPaint)
//        }
//        drawBaseLines(c)
//    }

    private fun drawBaseLine(canvas: Canvas, baseLine: EmojiBaseLine) {
        val y = valueToPixel(xAxis.axisMinimum, baseLine.y).y.toFloat()
        val textPaint = Paint()
        textPaint.color = baseLine.textColor
        textPaint.textSize = baseLineTextSize
        textPaint.typeface = baseLineLabelFont
        val bounds = textPaint.getTextBound(baseLine.label)
        val offsetLeft = bounds.width().toFloat() + baseLine.labelOffset
        if (extraLeftOffset < offsetLeft) {
            extraLeftOffset = (bounds.width().toFloat() + baseLine.labelOffset).pxToDp(context)
            invalidate()
        }
        val labelTopExtra = bounds.height() / 2
        val startX = extraLeftOffset
        val endX = width.toFloat()
        canvas.drawText(
            baseLine.label,
            startX - baseLine.labelOffset - bounds.width(),
            y + labelTopExtra,
            textPaint
        )
        canvas.drawDashPath(
            startAt = PointF(startX, y),
            endAt = PointF(endX, y),
            dashConfig = baseLine.dashConfig,
            pathColor = baseLine.color
        )
    }

    private fun BarLineScatterCandleBubbleDataProvider.valueToPixel(x: Float = 0F, y: Float = 0F): MPPointD =
        getTransformer(YAxis.AxisDependency.LEFT).getPixelForValues(x, y)

    private fun drawBaseLines(canvas: Canvas) {
        axisBaselines.forEach {
            drawBaseLine(canvas, it)
        }
    }

    class EmojiBaseLine(
        var y: Float,
        val label: String = "",
        val color: Int = Color.BLACK,
        val textColor: Int = Color.BLACK,
        val dashConfig: FloatArray = floatArrayOf(8F, 8F),
        val labelOffset: Float = 20F
    ) {
        companion object {
            const val DEFAULT_BASELINE_TEXT_SIZE = 12F /*sp*/
        }

//        val labelBound = Rect()
    }

    override fun onDraw(canvas: Canvas) {
        drawBaseLines(canvas)
        super.onDraw(canvas)
    }

    private fun Float.spToPx(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            context.resources.displayMetrics
        )
    }

    private fun Float.pxToDp(context: Context): Float {
        return this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun Float.dpToPx(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            context.resources.displayMetrics
        )
    }

    private fun Paint.getTextBound(text: String): Rect {
        val bounds = Rect()
        getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun Canvas.drawDashPath(
        startAt: PointF,
        endAt: PointF,
        controlAt: PointF? = null,
        dashConfig: FloatArray = floatArrayOf(10f, 20f),
        pathColor: Int = Color.BLACK
    ) {
        val paint = Paint()
        paint.apply {
            color = pathColor
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(dashConfig, 0f)
        }

        val mPath = Path()
        mPath.moveTo(startAt.x, startAt.y)

        val controlPoint = controlAt ?: PointF((startAt.x + endAt.x) / 2, (startAt.y + endAt.y) / 2)

        mPath.quadTo(controlPoint.x, controlPoint.y, endAt.x, endAt.y)
        drawPath(mPath, paint)
    }

}