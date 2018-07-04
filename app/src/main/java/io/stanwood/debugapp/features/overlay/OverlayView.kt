package io.stanwood.debugapp.features.overlay

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import io.stanwood.debugapp.R
import kotlinx.android.synthetic.main.view_overlay.view.*

class OverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var isResizing = false
    private val windowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val screenSize by lazy {
        arrayOf(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
    }
    var viewChangedCallback: ((Int, Int, Int, Int) -> Unit)? = null

    private fun viewChanged() {
        (layoutParams as WindowManager.LayoutParams).apply {
            viewChangedCallback?.invoke(x, y, width, height)
        }
    }

    private var isExpanded
        get() = floatingView.visibility != View.VISIBLE
        set(value) {
            if (!value && isResizeEnabled) {
                isResizeEnabled = false
            }
            floatingView.visibility = if (value) View.GONE else View.VISIBLE
            expandedLayout.visibility = if (value) View.VISIBLE else View.GONE
            isActivated = isExpanded
        }

    private var isResizeEnabled = false
        set(value) {
            field = value
            resizeOverlay?.visibility = if (value) View.VISIBLE else View.GONE
            requestLayout()
        }

    private val minSize by lazy {
        resources.getDimension(R.dimen.overlay_expanded_min_size).toInt()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        floatingView.setOnClickListener { isExpanded = true }
        floatingView.setOnTouchListener(moveTouchListener)
        btnHome.setOnClickListener { isExpanded = false }
        btnHome.setOnTouchListener(moveTouchListener)
        resize.setOnClickListener { isResizeEnabled = !isResizeEnabled }
        resizeBottom.setOnTouchListener(resizeTouchListener)
        resizeRight.setOnTouchListener(resizeTouchListener)
        resizeTop.setOnTouchListener(resizeTouchListener)
        resizeLeft.setOnTouchListener(resizeTouchListener)
        isResizeEnabled = false
        isExpanded = false
        if (isInEditMode) {
            isResizeEnabled = false
            isExpanded = true
        }
    }

    private val moveTouchListener = object : OnTouchListener {
        private var viewPosition = intArrayOf(0, 0)
        private var touchPosition = floatArrayOf(0f, 0f)
        private val touchSlope = ViewConfiguration.get(context).scaledTouchSlop
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val params = this@OverlayView.layoutParams as WindowManager.LayoutParams
                    viewPosition[0] = params.x
                    viewPosition[1] = params.y
                    touchPosition[0] = event.rawX
                    touchPosition[1] = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val params = this@OverlayView.layoutParams as WindowManager.LayoutParams
                    params.x = viewPosition[0] + (event.rawX - touchPosition[0]).toInt()
                    params.y = viewPosition[1] + (event.rawY - touchPosition[1]).toInt()
                    updateLayout(params)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val yDiff = Math.abs(event.rawX - touchPosition[0]).toInt()
                    val xDiff = Math.abs(event.rawY - touchPosition[1]).toInt()
                    if (yDiff < touchSlope && xDiff < touchSlope) {
                        view.performClick()
                    } else {
                        viewChanged()
                    }
                    return true
                }
                else -> return false
            }
        }
    }

    private val resizeTouchListener = object : OnTouchListener {
        private var startTouch = floatArrayOf(0f, 0f)
        private var tmpRct = Rect()
        override fun onTouch(v: View, event: MotionEvent) =
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startTouch[0] = event.rawX
                        startTouch[1] = event.rawY
                        (layoutParams as WindowManager.LayoutParams).apply {
                            tmpRct.set(x, y, x + this@OverlayView.width, y + this@OverlayView.height)
                        }
                        isResizing = true
                        resizeOverlay?.visibility = View.INVISIBLE
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        when (v.id) {
                            R.id.resizeBottom -> setLayoutBottomRight(tmpRct.height() + (event.rawY - startTouch[1]).toInt(), tmpRct.width())
                            R.id.resizeRight -> setLayoutBottomRight(tmpRct.height(), tmpRct.width() + (event.rawX - startTouch[0]).toInt())
                            R.id.resizeTop -> setLayoutTopLeft(tmpRct.top + Math.round(event.rawY - startTouch[1].toInt()), tmpRct.left)
                            R.id.resizeLeft -> setLayoutTopLeft(tmpRct.top, tmpRct.left + (event.rawX - startTouch[0]).toInt())
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        isResizing = false
                        resizeOverlay?.visibility = View.VISIBLE
                        requestLayout()
                        viewChanged()
                        true
                    }
                    else -> false
                }
    }

    private fun setLayoutTopLeft(top: Int, left: Int) {
        val params = layoutParams as WindowManager.LayoutParams
        val curY = params.y
        val curX = params.x
        val height = Math.max(if (params.height > 0) params.height else screenSize[1], minSize)
        val constrainedTop = Math.min(Math.max(top, 0), curY + height)
        val constrainedHeight = Math.max(height - (constrainedTop - curY), minSize)
        if (constrainedHeight > minSize) {
            params.y = constrainedTop
            params.height = constrainedHeight
        }
        val width = Math.max(if (params.width > 0) params.width else screenSize[0], minSize)
        val constrainedLeft = Math.min(Math.max(left, 0), curX + width)
        val constrainedWidth = Math.max(width - (constrainedLeft - curX), minSize)
        if (constrainedWidth > minSize) {
            params.x = constrainedLeft
            params.width = constrainedWidth
        }
        updateLayout(params)
    }

    private fun setLayoutBottomRight(bottom: Int, right: Int) {
        (layoutParams as WindowManager.LayoutParams).apply {
            height = bottom
            width = right
            updateLayout(this)
        }
    }

    private fun updateLayout(params: WindowManager.LayoutParams) {
        params.apply {
            x = Math.min(Math.max(x, 0), screenSize[0] - 60)
            y = Math.min(Math.max(y, 0), screenSize[1] - 60)
            width = Math.min(Math.max(width, minSize), screenSize[0])
            height = Math.min(Math.max(height, minSize), screenSize[1])
            windowManager.updateViewLayout(this@OverlayView, this)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (isResizing) {
            setMeasuredDimension(width, height)
        } else {
            (if (isExpanded) MeasureSpec.EXACTLY else MeasureSpec.AT_MOST)
                    .apply {
                        super.onMeasure(MeasureSpec.makeMeasureSpec(width, this), MeasureSpec.makeMeasureSpec(height, this))
                    }
        }
    }
}





