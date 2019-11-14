package com.marozzi.calgenda.util

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

/**
 * Created by amarozzi on 2019-11-07
 */
internal class PagerSnapWithSpanCountHelper : SnapHelper {

    private var mSpanCount: Int = 0
    private var mIsEnableCenter: Boolean = false

    // Orientation helpers are lazily created per LayoutManager.
    private var mVerticalHelper: OrientationHelper? = null
    private var mHorizontalHelper: OrientationHelper? = null

    constructor(mSpanCount: Int, mIsEnableCenter: Boolean) {
        // spanCount always >=1
        this.mSpanCount = mSpanCount
        this.mIsEnableCenter = mIsEnableCenter
    }

    constructor(mSpanCount: Int) {
        // spanCount always >=1
        this.mSpanCount = mSpanCount
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = if (mIsEnableCenter) distanceToCenter(layoutManager,
                targetView,
                getHorizontalHelper(layoutManager))
            else distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = if (mIsEnableCenter) distanceToCenter(layoutManager,
                targetView,
                getVerticalHelper(layoutManager))
            else distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {

        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()

            val isLastItem = layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }

            val child = layoutManager.findViewByPosition(firstChild)
            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2 && helper.getDecoratedEnd(
                    child) > 0
            ) {

                child
            } else {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {

                    null
                } else {

                    layoutManager.findViewByPosition(firstChild + 1)
                }
            }
        }

        return findSnapView(layoutManager)
    }

    /*
    center item.
    * */
    private fun findCenterView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        val center: Int
        if (layoutManager.clipToPadding) {
            center = helper.startAfterPadding + helper.totalSpace / 2
        } else {
            center = helper.end / 2
        }
        var absClosest = Integer.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = Math.abs(childCenter - center)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager.canScrollVertically()) {
            return if (mIsEnableCenter) findCenterView(layoutManager,
                getVerticalHelper(layoutManager))
            else findStartView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            return if (mIsEnableCenter) findCenterView(layoutManager,
                getHorizontalHelper(layoutManager))
            else findStartView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        var mStartMostChildView: View? = null
        if (layoutManager.canScrollVertically()) {
            mStartMostChildView = if (mIsEnableCenter) findCenterView(layoutManager,
                getVerticalHelper(layoutManager))
            else findStartView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            mStartMostChildView = if (mIsEnableCenter) findCenterView(layoutManager,
                getHorizontalHelper(layoutManager))
            else findStartView(layoutManager, getHorizontalHelper(layoutManager))
        }

        if (mStartMostChildView == null) {
            return RecyclerView.NO_POSITION
        }
        val centerPosition = layoutManager.getPosition(mStartMostChildView)
        if (centerPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val forwardDirection: Boolean
        if (layoutManager.canScrollHorizontally()) {
            forwardDirection = velocityX > 0
        } else {
            forwardDirection = velocityY > 0
        }
        var reverseLayout = false
        if (layoutManager is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider
            val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            if (vectorForEnd != null) {
                reverseLayout = vectorForEnd.x < 0 || vectorForEnd.y < 0
            }
        }

        return if (reverseLayout) if (forwardDirection) centerPosition - mSpanCount
        else centerPosition
        else if (forwardDirection) centerPosition + mSpanCount
        else centerPosition
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    /*
     * distance to start.
     * */
    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {

        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    /*
     * distance to center.
     * */
    private fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View, helper: OrientationHelper): Int {
        val childCenter = helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(
            targetView) / 2
        val containerCenter: Int
        if (layoutManager.clipToPadding) {
            containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        } else {
            containerCenter = helper.end / 2
        }
        return childCenter - containerCenter
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }

}