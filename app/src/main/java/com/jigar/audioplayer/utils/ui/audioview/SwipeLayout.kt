package com.jigar.audioplayer.utils.ui.audioview


import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListAdapter
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.jigar.audioplayer.R


/**
 * The Class SwipeLayout.
 */
class SwipeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    FrameLayout(context, attrs, defStyle) {
    /** The m touch slop.  */
    private val mTouchSlop: Int

    /** The m left index.  */
    private var mLeftIndex = 0

    /** The m right index.  */
    private var mRightIndex = 0

    /** The m top index.  */
    private var mTopIndex = 0

    /** The m bottom index.  */
    private var mBottomIndex = 0

    /** The m current direction index.  */
    private var mCurrentDirectionIndex = 0

    /** The m drag helper.  */
    private val mDragHelper: ViewDragHelper

    /** The m drag distance.  */
    private var mDragDistance = 0

    /** The m drag edges.  */
    private var mDragEdges: MutableList<DragEdge> = arrayListOf()

    /** The m show mode.  */
    private var mShowMode: ShowMode = ShowMode.PullOut

    /** The m left edge swipe offset.  */
    private val mLeftEdgeSwipeOffset: Float

    /** The m right edge swipe offset.  */
    private val mRightEdgeSwipeOffset: Float

    /** The m top edge swipe offset.  */
    private val mTopEdgeSwipeOffset: Float

    /** The m bottom edge swipe offset.  */
    private val mBottomEdgeSwipeOffset: Float

    /** The m bottom view id map.  */
    private val mBottomViewIdMap: MutableMap<DragEdge, Int> = HashMap()

    /** The m bottom view ids set.  */
    private var mBottomViewIdsSet = false

    /** The m swipe listeners.  */
    private val mSwipeListeners: MutableList<SwipeListener> = ArrayList()

    /** The m swipe deniers.  */
    private val mSwipeDeniers: MutableList<SwipeDenier> = ArrayList()

    /** The m reveal listeners.  */
    private val mRevealListeners: MutableMap<View, ArrayList<OnRevealListener>?> = HashMap()

    /** The m show entirely.  */
    private val mShowEntirely: MutableMap<View, Boolean> = HashMap()

    /** The m double click listener.  */
    private var mDoubleClickListener: DoubleClickListener? = null
    /**
     * Checks if is swipe enabled.
     *
     * @return true, if is swipe enabled
     */
    /**
     * Sets the swipe enabled.
     *
     * @param enabled the new swipe enabled
     */
    /** The m swipe enabled.  */
    var isSwipeEnabled = true
    /**
     * Checks if is left swipe enabled.
     *
     * @return true, if is left swipe enabled
     */
    /**
     * Sets the left swipe enabled.
     *
     * @param leftSwipeEnabled the new left swipe enabled
     */
    /** The m left swipe enabled.  */
    var isLeftSwipeEnabled = true
    /**
     * Checks if is right swipe enabled.
     *
     * @return true, if is right swipe enabled
     */
    /**
     * Sets the right swipe enabled.
     *
     * @param rightSwipeEnabled the new right swipe enabled
     */
    /** The m right swipe enabled.  */
    var isRightSwipeEnabled = true
    /**
     * Checks if is top swipe enabled.
     *
     * @return true, if is top swipe enabled
     */
    /**
     * Sets the top swipe enabled.
     *
     * @param topSwipeEnabled the new top swipe enabled
     */
    /** The m top swipe enabled.  */
    var isTopSwipeEnabled = true
    /**
     * Checks if is bottom swipe enabled.
     *
     * @return true, if is bottom swipe enabled
     */
    /**
     * Sets the bottom swipe enabled.
     *
     * @param bottomSwipeEnabled the new bottom swipe enabled
     */
    /** The m bottom swipe enabled.  */
    var isBottomSwipeEnabled = true

    /**
     * The Enum DragEdge.
     */
    enum class DragEdge {
        /** The Left.  */
        Left,

        /** The Right.  */
        Right,

        /** The Top.  */
        Top,

        /** The Bottom.  */
        Bottom
    }

    /**
     * The Enum ShowMode.
     */
    enum class ShowMode {
        /** The Lay down.  */
        LayDown,

        /** The Pull out.  */
        PullOut
    }

    /**
     * The listener interface for receiving swipe events.
     * The class that is interested in processing a swipe
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's `addSwipeListener` method. When
     * the swipe event occurs, that object's appropriate
     * method is invoked.
     *
     * @see SwipeEvent
    `` */
    interface SwipeListener {
        /**
         * On start open.
         *
         * @param layout the layout
         */
        fun onStartOpen(layout: SwipeLayout?)

        /**
         * On open.
         *
         * @param layout the layout
         */
        fun onOpen(layout: SwipeLayout?)

        /**
         * On start close.
         *
         * @param layout the layout
         */
        fun onStartClose(layout: SwipeLayout?)

        /**
         * On close.
         *
         * @param layout the layout
         */
        fun onClose(layout: SwipeLayout?)

        /**
         * On update.
         *
         * @param layout the layout
         * @param leftOffset the left offset
         * @param topOffset the top offset
         */
        fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int)

        /**
         * On hand release.
         *
         * @param layout the layout
         * @param xvel the xvel
         * @param yvel the yvel
         */
        fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float)
    }

    /**
     * Adds the swipe listener.
     *
     * @param l the l
     */
    fun addSwipeListener(l: SwipeListener) {
        mSwipeListeners.add(l)
    }

    /**
     * Removes the swipe listener.
     *
     * @param l the l
     */
    fun removeSwipeListener(l: SwipeListener) {
        mSwipeListeners.remove(l)
    }

    /**
     * The Interface SwipeDenier.
     */
    interface SwipeDenier {
        /*
         * Called in onInterceptTouchEvent Determines if this swipe event should
         * be denied Implement this interface if you are using views with swipe
         * gestures As a child of SwipeLayout
         *
         * @return true deny false allow
         */
        /**
         * Should deny swipe.
         *
         * @param ev the ev
         * @return true, if successful
         */
        fun shouldDenySwipe(ev: MotionEvent?): Boolean
    }

    /**
     * Adds the swipe denier.
     *
     * @param denier the denier
     */
    fun addSwipeDenier(denier: SwipeDenier) {
        mSwipeDeniers.add(denier)
    }

    /**
     * Removes the swipe denier.
     *
     * @param denier the denier
     */
    fun removeSwipeDenier(denier: SwipeDenier) {
        mSwipeDeniers.remove(denier)
    }

    /**
     * Removes the all swipe deniers.
     */
    fun removeAllSwipeDeniers() {
        mSwipeDeniers.clear()
    }

    /**
     * The listener interface for receiving onReveal events.
     * The class that is interested in processing a onReveal
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's `addOnRevealListener` method. When
     * the onReveal event occurs, that object's appropriate
     * method is invoked.
     *
     * @see OnRevealEvent
    `` */
    interface OnRevealListener {
        /**
         * On reveal.
         *
         * @param child the child
         * @param edge the edge
         * @param fraction the fraction
         * @param distance the distance
         */
        fun onReveal(child: View?, edge: DragEdge?, fraction: Float, distance: Int)
    }

    /**
     * bind a view with a specific
     *
     * @param childId the view id.
     * @param l       the target
     */
    fun addRevealListener(childId: Int, l: OnRevealListener) {
        val child = findViewById<View>(childId)
            ?: throw IllegalArgumentException("Child does not belong to SwipeListener.")
        if (!mShowEntirely.containsKey(child)) {
            mShowEntirely[child] = false
        }
        if (mRevealListeners[child] == null) mRevealListeners[child] = ArrayList()
        mRevealListeners[child]!!.add(l)
    }

    /**
     * bind multiple views with an
     *
     * @param childIds the view id.
     */
    fun addRevealListener(childIds: IntArray, l: OnRevealListener) {
        for (i in childIds) addRevealListener(i, l)
    }

    /**
     * Removes the reveal listener.
     *
     * @param childId the child id
     * @param l the l
     */
    fun removeRevealListener(childId: Int, l: OnRevealListener) {
        val child = findViewById<View>(childId) ?: return
        mShowEntirely.remove(child)
        if (mRevealListeners.containsKey(child)) mRevealListeners[child]!!.remove(l)
    }

    /**
     * Removes the all reveal listeners.
     *
     * @param childId the child id
     */
    fun removeAllRevealListeners(childId: Int) {
        val child = findViewById<View>(childId)
        if (child != null) {
            mRevealListeners.remove(child)
            mShowEntirely.remove(child)
        }
    }

    /** The m drag helper callback.  */
    private val mDragHelperCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            if (child === surfaceView) {
                when (mDragEdges[mCurrentDirectionIndex]) {
                    DragEdge.Top, DragEdge.Bottom -> return paddingLeft
                    DragEdge.Left -> {
                        if (left < paddingLeft) return paddingLeft
                        if (left > paddingLeft + mDragDistance) return paddingLeft + mDragDistance
                    }
                    DragEdge.Right -> {
                        if (left > paddingLeft) return paddingLeft
                        if (left < paddingLeft - mDragDistance) return paddingLeft - mDragDistance
                    }
                }
            } else if (bottomViews[mCurrentDirectionIndex] === child) {
                when (mDragEdges[mCurrentDirectionIndex]) {
                    DragEdge.Top, DragEdge.Bottom -> return paddingLeft
                    DragEdge.Left -> if (mShowMode == ShowMode.PullOut) {
                        if (left > paddingLeft) return paddingLeft
                    }
                    DragEdge.Right -> if (mShowMode == ShowMode.PullOut) {
                        if (left < measuredWidth - mDragDistance) {
                            return measuredWidth - mDragDistance
                        }
                    }
                }
            }
            return left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (child === surfaceView) {
                when (mDragEdges[mCurrentDirectionIndex]) {
                    DragEdge.Left, DragEdge.Right -> return paddingTop
                    DragEdge.Top -> {
                        if (top < paddingTop) return paddingTop
                        if (top > paddingTop + mDragDistance) return paddingTop + mDragDistance
                    }
                    DragEdge.Bottom -> {
                        if (top < paddingTop - mDragDistance) {
                            return paddingTop - mDragDistance
                        }
                        if (top > paddingTop) {
                            return paddingTop
                        }
                    }
                }
            } else {
                when (mDragEdges[mCurrentDirectionIndex]) {
                    DragEdge.Left, DragEdge.Right -> return paddingTop
                    DragEdge.Top -> if (mShowMode == ShowMode.PullOut) {
                        if (top > paddingTop) return paddingTop
                    } else {
                        if (surfaceView.top + dy < paddingTop) return paddingTop
                        if (surfaceView.top + dy > paddingTop + mDragDistance) return paddingTop + mDragDistance
                    }
                    DragEdge.Bottom -> if (mShowMode == ShowMode.PullOut) {
                        if (top < measuredHeight - mDragDistance) return measuredHeight - mDragDistance
                    } else {
                        if (surfaceView.top + dy >= paddingTop) return paddingTop
                        if (surfaceView.top + dy <= paddingTop - mDragDistance) return paddingTop - mDragDistance
                    }
                }
            }
            return top
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === surfaceView || bottomViews.contains(child)
        }

//        fun getViewHorizontalDragRange(child: View?): Int {
//            return mDragDistance
//        }
//
//        fun getViewVerticalDragRange(child: View?): Int {
//            return mDragDistance
//        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            for (l in mSwipeListeners) l.onHandRelease(this@SwipeLayout, xvel, yvel)
            if (releasedChild === surfaceView) {
                processSurfaceRelease(xvel, yvel)
            } else if (bottomViews.contains(releasedChild)) {
                if (showMode == ShowMode.PullOut) {
                    processBottomPullOutRelease(xvel, yvel)
                } else if (showMode == ShowMode.LayDown) {
                    processBottomLayDownMode(xvel, yvel)
                }
            }
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            val evLeft = surfaceView.left
            val evRight = surfaceView.right
            val evTop = surfaceView
                .top
            val evBottom = surfaceView.bottom
            if (changedView === surfaceView) {
                if (mShowMode == ShowMode.PullOut) {
                    if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left
                        || mDragEdges[mCurrentDirectionIndex] == DragEdge.Right
                    ) bottomViews[mCurrentDirectionIndex].offsetLeftAndRight(dx) else bottomViews[mCurrentDirectionIndex].offsetTopAndBottom(
                        dy
                    )
                }
            } else if (bottomViews.contains(changedView)) {
                if (mShowMode == ShowMode.PullOut) {
                    surfaceView.offsetLeftAndRight(dx)
                    surfaceView.offsetTopAndBottom(dy)
                } else {
                    val rect = computeBottomLayDown(mDragEdges[mCurrentDirectionIndex])
                    bottomViews[mCurrentDirectionIndex].layout(
                        rect.left,
                        rect.top,
                        rect.right,
                        rect.bottom
                    )
                    var newLeft = surfaceView.left + dx
                    var newTop = surfaceView.top + dy
                    if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left && newLeft < paddingLeft) newLeft =
                        paddingLeft else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Right && newLeft > paddingLeft) newLeft =
                        paddingLeft else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top && newTop < paddingTop) newTop =
                        paddingTop else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Bottom && newTop > paddingTop) newTop =
                        paddingTop
                    surfaceView
                        .layout(newLeft, newTop, newLeft + measuredWidth, newTop + measuredHeight)
                }
            }
            dispatchRevealEvent(evLeft, evTop, evRight, evBottom)
            dispatchSwipeEvent(evLeft, evTop, dx, dy)
            invalidate()
        }
    }

    /**
     * the dispatchRevealEvent method may not always get accurate position, it
     * makes the view may not always get the event when the view is totally
     * show( fraction = 1), so , we need to calculate every time.
     *
     * @param child the child
     * @param relativePosition the relative position
     * @param edge the edge
     * @param surfaceLeft the surface left
     * @param surfaceTop the surface top
     * @param surfaceRight the surface right
     * @param surfaceBottom the surface bottom
     * @return true, if is view totally first showed
     */
    protected fun isViewTotallyFirstShowed(
        child: View, relativePosition: Rect, edge: DragEdge, surfaceLeft: Int,
        surfaceTop: Int, surfaceRight: Int, surfaceBottom: Int
    ): Boolean {
        if (mShowEntirely[child]!!) return false
        val childLeft = relativePosition.left
        val childRight = relativePosition.right
        val childTop = relativePosition.top
        val childBottom = relativePosition.bottom
        var r = false
        if (showMode == ShowMode.LayDown) {
            if (edge == DragEdge.Right && surfaceRight <= childLeft
                || edge == DragEdge.Left && surfaceLeft >= childRight
                || edge == DragEdge.Top && surfaceTop >= childBottom
                || edge == DragEdge.Bottom && surfaceBottom <= childTop
            ) r = true
        } else if (showMode == ShowMode.PullOut) {
            if (edge == DragEdge.Right && childRight <= width
                || edge == DragEdge.Left && childLeft >= paddingLeft
                || edge == DragEdge.Top && childTop >= paddingTop
                || edge == DragEdge.Bottom && childBottom <= height
            ) r = true
        }
        return r
    }

    /**
     * Checks if is view showing.
     *
     * @param child the child
     * @param relativePosition the relative position
     * @param availableEdge the available edge
     * @param surfaceLeft the surface left
     * @param surfaceTop the surface top
     * @param surfaceRight the surface right
     * @param surfaceBottom the surface bottom
     * @return true, if is view showing
     */
    protected fun isViewShowing(
        child: View?, relativePosition: Rect, availableEdge: DragEdge?, surfaceLeft: Int,
        surfaceTop: Int, surfaceRight: Int, surfaceBottom: Int
    ): Boolean {
        val childLeft = relativePosition.left
        val childRight = relativePosition.right
        val childTop = relativePosition.top
        val childBottom = relativePosition.bottom
        if (showMode == ShowMode.LayDown) {
            when (availableEdge) {
                DragEdge.Right -> if (surfaceRight > childLeft && surfaceRight <= childRight) {
                    return true
                }
                DragEdge.Left -> if (surfaceLeft < childRight && surfaceLeft >= childLeft) {
                    return true
                }
                DragEdge.Top -> if (surfaceTop >= childTop && surfaceTop < childBottom) {
                    return true
                }
                DragEdge.Bottom -> if (surfaceBottom > childTop && surfaceBottom <= childBottom) {
                    return true
                }
            }
        } else if (showMode == ShowMode.PullOut) {
            when (availableEdge) {
                DragEdge.Right -> if (childLeft <= width && childRight > width) return true
                DragEdge.Left -> if (childRight >= paddingLeft && childLeft < paddingLeft) return true
                DragEdge.Top -> if (childTop < paddingTop && childBottom >= paddingTop) return true
                DragEdge.Bottom -> if (childTop < height && childTop >= paddingTop) return true
            }
        }
        return false
    }

    /**
     * Gets the relative position.
     *
     * @param child the child
     * @return the relative position
     */
    protected fun getRelativePosition(child: View): Rect {
        var t = child
        val r = Rect(t.left, t.top, 0, 0)
        while (t.parent != null && t !== rootView) {
            t = t.parent as View
            if (t === this) break
            r.left += t.left
            r.top += t.top
        }
        r.right = r.left + child.measuredWidth
        r.bottom = r.top + child.measuredHeight
        return r
    }

    /** The m event counter.  */
    private var mEventCounter = 0

    /**
     * Dispatch swipe event.
     *
     * @param surfaceLeft the surface left
     * @param surfaceTop the surface top
     * @param dx the dx
     * @param dy the dy
     */
    protected fun dispatchSwipeEvent(surfaceLeft: Int, surfaceTop: Int, dx: Int, dy: Int) {
        val edge = dragEdge
        var open = true
        if (edge == DragEdge.Left) {
            if (dx < 0) open = false
        } else if (edge == DragEdge.Right) {
            if (dx > 0) open = false
        } else if (edge == DragEdge.Top) {
            if (dy < 0) open = false
        } else if (edge == DragEdge.Bottom) {
            if (dy > 0) open = false
        }
        dispatchSwipeEvent(surfaceLeft, surfaceTop, open)
    }

    /**
     * Dispatch swipe event.
     *
     * @param surfaceLeft the surface left
     * @param surfaceTop the surface top
     * @param open the open
     */
    protected fun dispatchSwipeEvent(surfaceLeft: Int, surfaceTop: Int, open: Boolean) {
        safeBottomView()
        val status = openStatus
        if (!mSwipeListeners.isEmpty()) {
            mEventCounter++
            for (l in mSwipeListeners) {
                if (mEventCounter == 1) {
                    if (open) {
                        l.onStartOpen(this)
                    } else {
                        l.onStartClose(this)
                    }
                }
                l.onUpdate(this@SwipeLayout, surfaceLeft - paddingLeft, surfaceTop - paddingTop)
            }
            if (status == Status.Close) {
                for (l in mSwipeListeners) {
                    l.onClose(this@SwipeLayout)
                }
                mEventCounter = 0
            }
            if (status == Status.Open) {
                bottomViews[mCurrentDirectionIndex].isEnabled = true
                for (l in mSwipeListeners) {
                    l.onOpen(this@SwipeLayout)
                }
                mEventCounter = 0
            }
        }
    }

    /**
     * prevent bottom view get any touch event. Especially in LayDown mode.
     */
    private fun safeBottomView() {
        val status = openStatus
        val bottoms = bottomViews
        if (status == Status.Close) {
            for (bottom in bottoms) {
                if (bottom.visibility != INVISIBLE) bottom.visibility = INVISIBLE
            }
        } else {
            if (bottoms[mCurrentDirectionIndex].visibility != VISIBLE) bottoms[mCurrentDirectionIndex].visibility =
                VISIBLE
        }
    }

    /**
     * Dispatch reveal event.
     *
     * @param surfaceLeft the surface left
     * @param surfaceTop the surface top
     * @param surfaceRight the surface right
     * @param surfaceBottom the surface bottom
     */
    protected fun dispatchRevealEvent(
        surfaceLeft: Int, surfaceTop: Int, surfaceRight: Int,
        surfaceBottom: Int
    ) {
        if (mRevealListeners.isEmpty()) return

        mRevealListeners.forEach {
            val child : View = it.key
            val value = it.value
            val rect = getRelativePosition(child)
            if (isViewShowing(
                    child, rect,
                    mDragEdges[mCurrentDirectionIndex], surfaceLeft, surfaceTop,
                    surfaceRight, surfaceBottom
                )
            ) {
                mShowEntirely[child] = false
                var distance = 0
                var fraction = 0f
                if (showMode == ShowMode.LayDown) {
                    when (mDragEdges[mCurrentDirectionIndex]) {
                        DragEdge.Left -> {
                            distance = rect.left - surfaceLeft
                            fraction = distance / child.width.toFloat()
                        }
                        DragEdge.Right -> {
                            distance = rect.right - surfaceRight
                            fraction = distance / child.width.toFloat()
                        }
                        DragEdge.Top -> {
                            distance = rect.top - surfaceTop
                            fraction = distance / child.height.toFloat()
                        }
                        DragEdge.Bottom -> {
                            distance = rect.bottom - surfaceBottom
                            fraction = distance / child.height.toFloat()
                        }
                    }
                } else if (showMode == ShowMode.PullOut) {
                    when (mDragEdges[mCurrentDirectionIndex]) {
                        DragEdge.Left -> {
                            distance = rect.right - paddingLeft
                            fraction = distance / child.width.toFloat()
                        }
                        DragEdge.Right -> {
                            distance = rect.left - width
                            fraction = distance / child.width.toFloat()
                        }
                        DragEdge.Top -> {
                            distance = rect.bottom - paddingTop
                            fraction = distance / child.height.toFloat()
                        }
                        DragEdge.Bottom -> {
                            distance = rect.top - height
                            fraction = distance / child.height.toFloat()
                        }
                    }
                }
                for (l in value!!) {
                    l.onReveal(
                        child,
                        mDragEdges[mCurrentDirectionIndex], Math.abs(fraction), distance
                    )
                    if (Math.abs(fraction) == 1f) {
                        mShowEntirely[child] = true
                    }
                }
            }
            if (isViewTotallyFirstShowed(
                    child, rect,
                    mDragEdges[mCurrentDirectionIndex], surfaceLeft, surfaceTop,
                    surfaceRight, surfaceBottom
                )
            ) {
                mShowEntirely[child] = true
                for (l in value!!) {
                    if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left
                        || mDragEdges[mCurrentDirectionIndex] == DragEdge.Right
                    ) l.onReveal(
                        child,
                        mDragEdges[mCurrentDirectionIndex], 1f, child.width
                    ) else l.onReveal(
                        child,
                        mDragEdges[mCurrentDirectionIndex], 1f, child.height
                    )
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see android.view.View#computeScroll()
     */
    override fun computeScroll() {
        super.computeScroll()
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * [OnLayoutChangeListener] added in API 11. I need
     * to support it from API 8.
     */
    interface OnLayout {
        /**
         * On layout.
         *
         * @param v the v
         */
        fun onLayout(v: SwipeLayout?)
    }

    /** The m on layout listeners.  */
    private var mOnLayoutListeners: MutableList<OnLayout>? = null

    /**
     * Adds the on layout listener.
     *
     * @param l the l
     */
    fun addOnLayoutListener(l: OnLayout) {
        if (mOnLayoutListeners == null) mOnLayoutListeners = ArrayList()
        mOnLayoutListeners!!.add(l)
    }

    /**
     * Removes the on layout listener.
     *
     * @param l the l
     */
    fun removeOnLayoutListener(l: OnLayout) {
        if (mOnLayoutListeners != null) mOnLayoutListeners!!.remove(l)
    }

    /* (non-Javadoc)
     * @see android.widget.FrameLayout#onLayout(boolean, int, int, int, int)
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        check(childCount == 1 + mDragEdges.size) { "You need to have one surface view plus one view for each of your drag edges" }
        for (i in 0 until childCount) {
            require(getChildAt(i) is ViewGroup) { "All the children in SwipeLayout must be an instance of ViewGroup" }
        }
        if (mShowMode == ShowMode.PullOut) layoutPullOut() else if (mShowMode == ShowMode.LayDown) layoutLayDown()
        safeBottomView()
        if (mOnLayoutListeners != null) for (i in mOnLayoutListeners!!.indices) {
            mOnLayoutListeners!![i].onLayout(this)
        }
    }

    /**
     * Layout pull out.
     */
    fun layoutPullOut() {
        var rect = computeSurfaceLayoutArea(false)
        surfaceView.layout(rect.left, rect.top, rect.right, rect.bottom)
        rect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect)
        bottomViews[mCurrentDirectionIndex].layout(rect.left, rect.top, rect.right, rect.bottom)
        bringChildToFront(surfaceView)
    }

    /**
     * Layout lay down.
     */
    fun layoutLayDown() {
        var rect = computeSurfaceLayoutArea(false)
        surfaceView.layout(rect.left, rect.top, rect.right, rect.bottom)
        rect = computeBottomLayoutAreaViaSurface(ShowMode.LayDown, rect)
        bottomViews[mCurrentDirectionIndex].layout(rect.left, rect.top, rect.right, rect.bottom)
        bringChildToFront(surfaceView)
    }

    /* (non-Javadoc)
     * @see android.widget.FrameLayout#onMeasure(int, int)
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mDragDistance = if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left
            || mDragEdges[mCurrentDirectionIndex] == DragEdge.Right
        ){
            bottomViews[mCurrentDirectionIndex].measuredWidth - dp2px(currentOffset)
        }else{
            bottomViews[mCurrentDirectionIndex].measuredHeight
            - dp2px(currentOffset)
        }
    }

    /** The m touch consumed by child.  */
    private var mTouchConsumedByChild = false

    /* (non-Javadoc)
     * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled || !isEnabledInAdapterView) {
            return true
        }
        if (!isSwipeEnabled) {
            return false
        }
        for (denier in mSwipeDeniers) {
            if (denier != null && denier.shouldDenySwipe(ev)) {
                return false
            }
        }
        //
        // if a child wants to handle the touch event,
        // then let it do it.
        //
        val action = ev.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val status = openStatus
                if (status == Status.Close) {
                    mTouchConsumedByChild = childNeedHandleTouchEvent(surfaceView, ev) != null
                } else if (status == Status.Open) {
                    mTouchConsumedByChild =
                        childNeedHandleTouchEvent(bottomViews[mCurrentDirectionIndex], ev) != null
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mTouchConsumedByChild = false
        }
        return if (mTouchConsumedByChild) false else mDragHelper.shouldInterceptTouchEvent(ev)
    }

    /**
     * if the ViewGroup children want to handle this event.
     *
     * @param v the v
     * @param event the event
     * @return the view
     */
    private fun childNeedHandleTouchEvent(v: ViewGroup?, event: MotionEvent): View? {
        if (v == null) return null
        if (v.onTouchEvent(event)) return v
        val childCount = v.childCount
        for (i in childCount - 1 downTo 0) {
            val child = v.getChildAt(i)
            if (child is ViewGroup) {
                val grandChild = childNeedHandleTouchEvent(child, event)
                if (grandChild != null) return grandChild
            } else {
                if (childNeedHandleTouchEvent(v.getChildAt(i), event)) return v.getChildAt(i)
            }
        }
        return null
    }

    /**
     * if the view (v) wants to handle this event.
     *
     * @param v the v
     * @param event the event
     * @return true, if successful
     */
    private fun childNeedHandleTouchEvent(v: View?, event: MotionEvent): Boolean {
        if (v == null) return false
        val loc = IntArray(2)
        v.getLocationOnScreen(loc)
        val left = loc[0]
        val top = loc[1]
        return if (event.rawX > left && event.rawX < left + v.width && event.rawY > top && event.rawY < top + v.height
        ) {
            v.onTouchEvent(event)
        } else false
    }

    /** The s y.  */
    private var sX = -1f
    private var sY = -1f

    /**
     * Should allow swipe.
     *
     * @return true, if successful
     */
    private fun shouldAllowSwipe(): Boolean {
        if (mCurrentDirectionIndex == mLeftIndex && !isLeftSwipeEnabled) return false
        if (mCurrentDirectionIndex == mRightIndex && !isRightSwipeEnabled) return false
        if (mCurrentDirectionIndex == mTopIndex && !isTopSwipeEnabled) return false
        return if (mCurrentDirectionIndex == mBottomIndex && !isBottomSwipeEnabled) false else true
    }

    /* (non-Javadoc)
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabledInAdapterView || !isEnabled) return true
        if (!isSwipeEnabled) return super.onTouchEvent(event)
        val action = event.actionMasked
        val parent = parent
        gestureDetector.onTouchEvent(event)
        val status = openStatus
        var touching: ViewGroup? = null
        if (status == Status.Close) {
            touching = surfaceView
        } else if (status == Status.Open) {
            touching = bottomViews[mCurrentDirectionIndex]
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mDragHelper.processTouchEvent(event)
                parent.requestDisallowInterceptTouchEvent(true)
                sX = event.rawX
                sY = event.rawY
                if (touching != null) touching.isPressed = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val distanceX = event.rawX - sX
                val distanceY = event.rawY - sY
                var angle = Math.abs(distanceY / distanceX)
                angle = Math.toDegrees(Math.atan(angle.toDouble())).toFloat()
                if (openStatus == Status.Close) {
                    val lastCurrentDirectionIndex = mCurrentDirectionIndex
                    if (angle < 45) {
                        if (mLeftIndex != -1 && distanceX > 0 && isLeftSwipeEnabled) {
                            mCurrentDirectionIndex = mLeftIndex
                        } else if (mRightIndex != -1 && distanceX < 0 && isRightSwipeEnabled) {
                            mCurrentDirectionIndex = mRightIndex
                        }
                    } else {
                        if (mTopIndex != -1 && distanceY > 0 && isTopSwipeEnabled) {
                            mCurrentDirectionIndex = mTopIndex
                        } else if (mBottomIndex != -1 && distanceY < 0 && isBottomSwipeEnabled) {
                            mCurrentDirectionIndex = mBottomIndex
                        }
                    }
                    if (lastCurrentDirectionIndex != mCurrentDirectionIndex) {
                        updateBottomViews()
                    }
                }
                if (!shouldAllowSwipe()) return super.onTouchEvent(event)
                var doNothing = false
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) {
                    var suitable = (status == Status.Open && distanceX > mTouchSlop
                            || status == Status.Close && distanceX < -mTouchSlop)
                    suitable = suitable || status == Status.Middle
                    if (angle > 30 || !suitable) {
                        doNothing = true
                    }
                }
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) {
                    var suitable = (status == Status.Open && distanceX < -mTouchSlop
                            || status == Status.Close && distanceX > mTouchSlop)
                    suitable = suitable || status == Status.Middle
                    if (angle > 30 || !suitable) {
                        doNothing = true
                    }
                }
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) {
                    var suitable = (status == Status.Open && distanceY < -mTouchSlop
                            || status == Status.Close && distanceY > mTouchSlop)
                    suitable = suitable || status == Status.Middle
                    if (angle < 60 || !suitable) {
                        doNothing = true
                    }
                }
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Bottom) {
                    var suitable = (status == Status.Open && distanceY > mTouchSlop
                            || status == Status.Close && distanceY < -mTouchSlop)
                    suitable = suitable || status == Status.Middle
                    if (angle < 60 || !suitable) {
                        doNothing = true
                    }
                }
                if (doNothing) {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                } else {
                    if (touching != null) {
                        touching.isPressed = false
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                    mDragHelper.processTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                run {
                    sX = -1f
                    sY = -1f
                    if (touching != null) {
                        touching.isPressed = false
                    }
                }
                parent.requestDisallowInterceptTouchEvent(true)
                mDragHelper.processTouchEvent(event)
            }
            else -> {
                parent.requestDisallowInterceptTouchEvent(true)
                mDragHelper.processTouchEvent(event)
            }
        }
        return true
    }

    /**
     * if working in [AdapterView], we should response
     * [Adapter] isEnable(int position).
     *
     * @return true when item is enabled, else disabled.
     */
    private val isEnabledInAdapterView: Boolean
        private get() {
            val adapterView = adapterView
            var enable = true
            if (adapterView != null) {
                val adapter = adapterView.adapter
                if (adapter != null) {
                    val p = adapterView.getPositionForView(this@SwipeLayout)
                    if (adapter is BaseAdapter) {
                        enable = adapter.isEnabled(p)
                    } else if (adapter is ListAdapter) {
                        enable = adapter.isEnabled(p)
                    }
                }
            }
            return enable
        }

    /**
     * Inside adapter view.
     *
     * @return true, if successful
     */
    private fun insideAdapterView(): Boolean {
        return adapterView != null
    }

    /**
     * Gets the adapter view.
     *
     * @return the adapter view
     */
    private val adapterView: AdapterView<*>?
        private get() {
            var t = parent
            while (t != null) {
                if (t is AdapterView<*>) {
                    return t
                }
                t = t.parent
            }
            return null
        }

    /**
     * Perform adapter view item click.
     *
     * @param e the e
     */
    private fun performAdapterViewItemClick(e: MotionEvent) {
        var t = parent
        while (t != null) {
            if (t is AdapterView<*>) {
                val view = t
                val p = view.getPositionForView(this@SwipeLayout)
                if (p != AdapterView.INVALID_POSITION
                    && view.performItemClick(
                        view.getChildAt(p - view.firstVisiblePosition), p, view
                            .adapter.getItemId(p)
                    )
                ) return
            } else {
                if (t is View && (t as View).performClick()) return
            }
            t = t.parent
        }
    }

    /** The gesture detector.  */
    private val gestureDetector = GestureDetector(getContext(), SwipeDetector())

    /**
     * The Class SwipeDetector.
     */
    internal inner class SwipeDetector : SimpleOnGestureListener() {
        /* (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDown(android.view.MotionEvent)
         */
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        /**
         * Simulate the touch event lifecycle. If you use SwipeLayout in
         * [AdapterView] ([android.widget.ListView],
         * [android.widget.GridView] etc.) It will manually call
         * [AdapterView].performItemClick,
         * performItemLongClick.
         *
         * @param e the e
         * @return true, if successful
         */
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (mDoubleClickListener == null) {
                performAdapterViewItemClick(e)
            }
            return true
        }

        /* (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
         */
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (mDoubleClickListener != null) {
                performAdapterViewItemClick(e)
            }
            return true
        }

        /* (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
         */
        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        /* (non-Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
         */
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (mDoubleClickListener != null) {
                val target: View
                val bottom = bottomViews[mCurrentDirectionIndex]
                val surface = surfaceView
                target =
                    if (e.x > bottom.left && e.x < bottom.right && e.y > bottom.top && e.y < bottom.bottom) {
                        bottom
                    } else {
                        surface
                    }
                mDoubleClickListener!!.onDoubleClick(this@SwipeLayout, target === surface)
            }
            return true
        }
    }
    /**
     * Gets the drag edge.
     *
     * @return the drag edge
     */
    /**
     * Sets the drag edge.
     *
     * @param dragEdge the new drag edge
     */
    var dragEdge: DragEdge
        get() = mDragEdges[mCurrentDirectionIndex]
        set(dragEdge) {
            mDragEdges = ArrayList()
            mDragEdges.add(dragEdge)
            mCurrentDirectionIndex = 0
            populateIndexes()
            requestLayout()
            updateBottomViews()
        }
    /**
     * Gets the drag distance.
     *
     * @return the drag distance
     */
    /**
     * set the drag distance, it will force set the bottom view's width or
     * height via this value.
     *
     * @param max the new drag distance
     */
    var dragDistance: Int
        get() = mDragDistance
        set(max) {
            require(max >= 0) { "Drag distance can not be < 0" }
            mDragDistance = dp2px(max.toFloat())
            requestLayout()
        }
    /**
     * Gets the show mode.
     *
     * @return the show mode
     */
    /**
     * There are 2 diffirent show mode.
     *
     * @param mode the new show mode
     */
    var showMode: ShowMode
        get() = mShowMode
        set(mode) {
            mShowMode = mode
            requestLayout()
        }

    /**
     * Gets the surface view.
     *
     * @return the surface view
     */
    val surfaceView: ViewGroup
        get() = getChildAt(childCount - 1) as ViewGroup// If the user has provided a map for views to

    /**
     * Gets the bottom views.
     *
     * @return the bottom views
     */
    val bottomViews: List<ViewGroup>
        get() {
            val lvg: MutableList<ViewGroup> = ArrayList()
            // If the user has provided a map for views to
            if (mBottomViewIdsSet) {
                if (mDragEdges.contains(DragEdge.Left)) {
                    lvg.add(
                        mLeftIndex,
                        findViewById<View>(mBottomViewIdMap[DragEdge.Left]!!) as ViewGroup
                    )
                }
                if (mDragEdges.contains(DragEdge.Right)) {
                    lvg.add(
                        mRightIndex,
                        findViewById<View>(mBottomViewIdMap[DragEdge.Right]!!) as ViewGroup
                    )
                }
                if (mDragEdges.contains(DragEdge.Top)) {
                    lvg.add(
                        mTopIndex,
                        findViewById<View>(mBottomViewIdMap[DragEdge.Top]!!) as ViewGroup
                    )
                }
                if (mDragEdges.contains(DragEdge.Bottom)) {
                    lvg.add(
                        mBottomIndex,
                        findViewById<View>(mBottomViewIdMap[DragEdge.Bottom]!!) as ViewGroup
                    )
                }
            } else {
                for (i in 0 until childCount - 1) {
                    lvg.add(getChildAt(i) as ViewGroup)
                }
            }
            return lvg
        }
    // Pass the id of the view if set, otherwise pass -1
    /**
     * Sets the bottom view ids.
     *
     * @param left the left
     * @param right the right
     * @param top the top
     * @param bottom the bottom
     */
    fun setBottomViewIds(left: Int, right: Int, top: Int, bottom: Int) {
        if (mDragEdges.contains(DragEdge.Left)) {
            if (left == EMPTY_LAYOUT) {
                mBottomViewIdsSet = false
            } else {
                mBottomViewIdMap[DragEdge.Left] = left
                mBottomViewIdsSet = true
            }
        }
        if (mDragEdges.contains(DragEdge.Right)) {
            if (right == EMPTY_LAYOUT) {
                mBottomViewIdsSet = false
            } else {
                mBottomViewIdMap[DragEdge.Right] = right
                mBottomViewIdsSet = true
            }
        }
        if (mDragEdges.contains(DragEdge.Top)) {
            if (top == EMPTY_LAYOUT) {
                mBottomViewIdsSet = false
            } else {
                mBottomViewIdMap[DragEdge.Top] = top
                mBottomViewIdsSet = true
            }
        }
        if (mDragEdges.contains(DragEdge.Bottom)) {
            if (bottom == EMPTY_LAYOUT) {
                mBottomViewIdsSet = false
            } else {
                mBottomViewIdMap[DragEdge.Bottom] = bottom
                mBottomViewIdsSet = true
            }
        }
    }

    /**
     * The Enum Status.
     */
    enum class Status {
        /** The Middle.  */
        Middle,

        /** The Open.  */
        Open,

        /** The Close.  */
        Close
    }

    /**
     * get the open status.
     *
     * Middle.
     */
    val openStatus: Status
        get() {
            val surfaceLeft = surfaceView.left
            val surfaceTop = surfaceView.top
            if (surfaceLeft == paddingLeft && surfaceTop == paddingTop) return Status.Close
            return if (surfaceLeft == paddingLeft - mDragDistance || surfaceLeft == paddingLeft + mDragDistance || surfaceTop == paddingTop - mDragDistance || surfaceTop == paddingTop + mDragDistance
            ) Status.Open else Status.Middle
        }

    /**
     * Process the surface release event.
     *
     * @param xvel the xvel
     * @param yvel the yvel
     */
    private fun processSurfaceRelease(xvel: Float, yvel: Float) {
        if (xvel == 0f && openStatus == Status.Middle) close()
        if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left
            || mDragEdges[mCurrentDirectionIndex] == DragEdge.Right
        ) {
            if (xvel > 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) open() else close()
            }
            if (xvel < 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) close() else open()
            }
        } else {
            if (yvel > 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) open() else close()
            }
            if (yvel < 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) close() else open()
            }
        }
    }

    /**
     * process bottom (PullOut mode) hand release event.
     *
     * @param xvel the xvel
     * @param yvel the yvel
     */
    private fun processBottomPullOutRelease(xvel: Float, yvel: Float) {
        if (xvel == 0f && openStatus == Status.Middle) close()
        if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left
            || mDragEdges[mCurrentDirectionIndex] == DragEdge.Right
        ) {
            if (xvel > 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) open() else close()
            }
            if (xvel < 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) close() else open()
            }
        } else {
            if (yvel > 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) open() else close()
            }
            if (yvel < 0) {
                if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) close() else open()
            }
        }
    }

    /**
     * process bottom (LayDown mode) hand release event.
     *
     * @param xvel the xvel
     * @param yvel the yvel
     */
    private fun processBottomLayDownMode(xvel: Float, yvel: Float) {
        if (xvel == 0f && openStatus == Status.Middle) close()
        var l = paddingLeft
        var t = paddingTop
        if (xvel < 0 && mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) l -= mDragDistance
        if (xvel > 0 && mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) l += mDragDistance
        if (yvel > 0 && mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) t += mDragDistance
        if (yvel < 0 && mDragEdges[mCurrentDirectionIndex] == DragEdge.Bottom) t -= mDragDistance
        mDragHelper.smoothSlideViewTo(surfaceView, l, t)
        invalidate()
    }
    /**
     * Open.
     *
     * @param smooth the smooth
     * @param notify the notify
     */
    /**
     * smoothly open surface.
     */
    /**
     * Open.
     *
     * @param smooth the smooth
     */
    @JvmOverloads
    fun open(smooth: Boolean = true, notify: Boolean = true) {
        val surface = surfaceView
        val bottom = bottomViews[mCurrentDirectionIndex]
        val dx: Int
        val dy: Int
        val rect = computeSurfaceLayoutArea(true)
        if (smooth) {
            mDragHelper.smoothSlideViewTo(surfaceView, rect.left, rect.top)
        } else {
            dx = rect.left - surface.left
            dy = rect.top - surface.top
            surface.layout(rect.left, rect.top, rect.right, rect.bottom)
            if (showMode == ShowMode.PullOut) {
                val bRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect)
                bottom.layout(bRect.left, bRect.top, bRect.right, bRect.bottom)
            }
            if (notify) {
                dispatchRevealEvent(rect.left, rect.top, rect.right, rect.bottom)
                dispatchSwipeEvent(rect.left, rect.top, dx, dy)
            } else {
                safeBottomView()
            }
        }
        invalidate()
    }

    /**
     * Open.
     *
     * @param edge the edge
     */
    fun open(edge: DragEdge?) {
        when (edge) {
            DragEdge.Left -> {
                mCurrentDirectionIndex = mLeftIndex
                mCurrentDirectionIndex = mRightIndex
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Right -> {
                mCurrentDirectionIndex = mRightIndex
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Top -> {
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Bottom -> mCurrentDirectionIndex = mBottomIndex
        }
        open(true, true)
    }

    /**
     * Open.
     *
     * @param smooth the smooth
     * @param edge the edge
     */
    fun open(smooth: Boolean, edge: DragEdge?) {
        when (edge) {
            DragEdge.Left -> {
                mCurrentDirectionIndex = mLeftIndex
                mCurrentDirectionIndex = mRightIndex
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Right -> {
                mCurrentDirectionIndex = mRightIndex
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Top -> {
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Bottom -> mCurrentDirectionIndex = mBottomIndex
        }
        open(smooth, true)
    }

    /**
     * Open.
     *
     * @param smooth the smooth
     * @param notify the notify
     * @param edge the edge
     */
    fun open(smooth: Boolean, notify: Boolean, edge: DragEdge?) {
        when (edge) {
            DragEdge.Left -> {
                mCurrentDirectionIndex = mLeftIndex
                mCurrentDirectionIndex = mRightIndex
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Right -> {
                mCurrentDirectionIndex = mRightIndex
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Top -> {
                mCurrentDirectionIndex = mTopIndex
                mCurrentDirectionIndex = mBottomIndex
            }
            DragEdge.Bottom -> mCurrentDirectionIndex = mBottomIndex
        }
        open(smooth, notify)
    }
    /**
     * close surface.
     *
     * @param smooth smoothly or not.
     * @param notify if notify all the listeners.
     */
    /**
     * smoothly close surface.
     */
    /**
     * Close.
     *
     * @param smooth the smooth
     */
    @JvmOverloads
    fun close(smooth: Boolean = true, notify: Boolean = true) {
        val surface = surfaceView
        val dx: Int
        val dy: Int
        if (smooth) mDragHelper.smoothSlideViewTo(surfaceView, paddingLeft, paddingTop) else {
            val rect = computeSurfaceLayoutArea(false)
            dx = rect.left - surface.left
            dy = rect.top - surface.top
            surface.layout(rect.left, rect.top, rect.right, rect.bottom)
            if (notify) {
                dispatchRevealEvent(rect.left, rect.top, rect.right, rect.bottom)
                dispatchSwipeEvent(rect.left, rect.top, dx, dy)
            } else {
                safeBottomView()
            }
        }
        invalidate()
    }
    /**
     * Toggle.
     *
     * @param smooth the smooth
     */
    /**
     * Toggle.
     */
    @JvmOverloads
    fun toggle(smooth: Boolean = true) {
        if (openStatus == Status.Open) close(smooth) else if (openStatus == Status.Close) open(
            smooth
        )
    }

    /**
     * a helper function to compute the Rect area that surface will hold in.
     *
     * @param open open status or close status.
     * @return the rect
     */
    private fun computeSurfaceLayoutArea(open: Boolean): Rect {
        var l = paddingLeft
        var t = paddingTop
        if (open) {
            if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) l =
                paddingLeft + mDragDistance else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) l =
                paddingLeft - mDragDistance else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) t =
                paddingTop + mDragDistance else t = paddingTop - mDragDistance
        }
        return Rect(l, t, l + measuredWidth, t + measuredHeight)
    }

    /**
     * Compute bottom layout area via surface.
     *
     * @param mode the mode
     * @param surfaceArea the surface area
     * @return the rect
     */
    private fun computeBottomLayoutAreaViaSurface(mode: ShowMode, surfaceArea: Rect): Rect {
        var bl = surfaceArea.left
        var bt = surfaceArea.top
        var br = surfaceArea.right
        var bb = surfaceArea.bottom
        if (mode == ShowMode.PullOut) {
            if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) bl =
                surfaceArea.left - mDragDistance else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) bl =
                surfaceArea.right else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) bt =
                surfaceArea.top - mDragDistance else bt = surfaceArea.bottom
            if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left || mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) {
                bb = surfaceArea.bottom
                br = bl + bottomViews[mCurrentDirectionIndex].measuredWidth
            } else {
                bb = bt + bottomViews[mCurrentDirectionIndex].measuredHeight
                br = surfaceArea.right
            }
        } else if (mode == ShowMode.LayDown) {
            if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) br =
                bl + mDragDistance else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) bl =
                br - mDragDistance else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) bb =
                bt + mDragDistance else bt = bb - mDragDistance
        }
        return Rect(bl, bt, br, bb)
    }

    /**
     * Compute bottom lay down.
     *
     * @param dragEdge the drag edge
     * @return the rect
     */
    private fun computeBottomLayDown(dragEdge: DragEdge): Rect {
        var bl = paddingLeft
        var bt = paddingTop
        val br: Int
        val bb: Int
        if (dragEdge == DragEdge.Right) {
            bl = measuredWidth - mDragDistance
        } else if (dragEdge == DragEdge.Bottom) {
            bt = measuredHeight - mDragDistance
        }
        if (dragEdge == DragEdge.Left || dragEdge == DragEdge.Right) {
            br = bl + mDragDistance
            bb = bt + measuredHeight
        } else {
            br = bl + measuredWidth
            bb = bt + mDragDistance
        }
        return Rect(bl, bt, br, bb)
    }

    /**
     * Sets the on double click listener.
     *
     * @param doubleClickListener the new on double click listener
     */
    fun setOnDoubleClickListener(doubleClickListener: DoubleClickListener?) {
        mDoubleClickListener = doubleClickListener
    }

    /**
     * The listener interface for receiving doubleClick events.
     * The class that is interested in processing a doubleClick
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's `addDoubleClickListener` method. When
     * the doubleClick event occurs, that object's appropriate
     * method is invoked.
     *
     * @see DoubleClickEvent
    `` */
    interface DoubleClickListener {
        /**
         * On double click.
         *
         * @param layout the layout
         * @param surface the surface
         */
        fun onDoubleClick(layout: SwipeLayout?, surface: Boolean)
    }

    /**
     * Dp2px.
     *
     * @param dp the dp
     * @return the int
     */
    private fun dp2px(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    /**
     * Gets the drag edges.
     *
     * @return the drag edges
     */
    val dragEdges: List<DragEdge>
        get() = mDragEdges

    /**
     * Sets the drag edges.
     *
     * @param mDragEdges the new drag edges
     */
    fun setDragEdges(mDragEdges: MutableList<DragEdge>) {
        this.mDragEdges = mDragEdges
        mCurrentDirectionIndex = 0
        populateIndexes()
        updateBottomViews()
    }

    /**
     * Sets the drag edges.
     *
     * @param mDragEdges the new drag edges
     */
    fun setDragEdges(vararg mDragEdges: DragEdge) {
        this.mDragEdges = ArrayList()
        for (e in mDragEdges) {
            this.mDragEdges.add(e)
        }
        mCurrentDirectionIndex = 0
        populateIndexes()
        updateBottomViews()
    }

    /**
     * Populate indexes.
     */
    private fun populateIndexes() {
        mLeftIndex = mDragEdges.indexOf(DragEdge.Left)
        mRightIndex = mDragEdges.indexOf(DragEdge.Right)
        mTopIndex = mDragEdges.indexOf(DragEdge.Top)
        mBottomIndex = mDragEdges.indexOf(DragEdge.Bottom)
    }

    /**
     * Gets the current offset.
     *
     * @return the current offset
     */
    private val currentOffset: Float
        get() = if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Left) mLeftEdgeSwipeOffset else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Right) mRightEdgeSwipeOffset else if (mDragEdges[mCurrentDirectionIndex] == DragEdge.Top) mTopEdgeSwipeOffset else mBottomEdgeSwipeOffset

    /**
     * Update bottom views.
     */
    private fun updateBottomViews() {
        if (mShowMode == ShowMode.PullOut) layoutPullOut() else if (mShowMode == ShowMode.LayDown) layoutLayDown()
        safeBottomView()
        if (mOnLayoutListeners != null) for (i in mOnLayoutListeners!!.indices) {
            mOnLayoutListeners!![i].onLayout(this)
        }
    }

    companion object {
        /** The Constant EMPTY_LAYOUT.  */
        const val EMPTY_LAYOUT = -1

        /** The Constant DRAG_LEFT.  */
        private const val DRAG_LEFT = 1

        /** The Constant DRAG_RIGHT.  */
        private const val DRAG_RIGHT = 2

        /** The Constant DRAG_TOP.  */
        private const val DRAG_TOP = 4

        /** The Constant DRAG_BOTTOM.  */
        private const val DRAG_BOTTOM = 8
    }
    /**
     * Instantiates a new swipe layout.
     *
     * @param context the context
     * @param attrs the attrs
     * @param defStyle the def style
     */
    /**
     * Instantiates a new swipe layout.
     *
     * @param context the context
     * @param attrs the attrs
     */
    /**
     * Instantiates a new swipe layout.
     *
     * @param context the context
     */
    init {
        mDragHelper = ViewDragHelper.create(this, mDragHelperCallback)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        val a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout)
        val dragEdgeChoices = a.getInt(R.styleable.SwipeLayout_drag_edge, DRAG_RIGHT)
        mLeftEdgeSwipeOffset = a.getDimension(R.styleable.SwipeLayout_leftEdgeSwipeOffset, 0f)
        mRightEdgeSwipeOffset = a.getDimension(R.styleable.SwipeLayout_rightEdgeSwipeOffset, 0f)
        mTopEdgeSwipeOffset = a.getDimension(R.styleable.SwipeLayout_topEdgeSwipeOffset, 0f)
        mBottomEdgeSwipeOffset = a.getDimension(R.styleable.SwipeLayout_bottomEdgeSwipeOffset, 0f)
        mDragEdges = ArrayList()
        if (dragEdgeChoices and DRAG_LEFT == DRAG_LEFT) {
            mDragEdges.add(DragEdge.Left)
        }
        if (dragEdgeChoices and DRAG_RIGHT == DRAG_RIGHT) {
            mDragEdges.add(DragEdge.Right)
        }
        if (dragEdgeChoices and DRAG_TOP == DRAG_TOP) {
            mDragEdges.add(DragEdge.Top)
        }
        if (dragEdgeChoices and DRAG_BOTTOM == DRAG_BOTTOM) {
            mDragEdges.add(DragEdge.Bottom)
        }
        populateIndexes()
        val ordinal = a.getInt(R.styleable.SwipeLayout_show_mode, ShowMode.PullOut.ordinal)
        mShowMode = ShowMode.values()[ordinal]
        a.recycle()
    }
}
