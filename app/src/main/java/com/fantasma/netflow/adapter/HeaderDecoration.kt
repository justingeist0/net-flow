package com.fantasma.netflow.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fantasma.netflow.R

class HeaderDecoration(
        private val context: Context,
        private val monthChecker: MonthChecker
) : RecyclerView.ItemDecoration() {

    private var headerView : View? = null
    private lateinit var headerText : TextView
    private var headerHeight = 0
    private var topHeaderTranslationY = 0f

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if(monthChecker.requiresHeader(position)) {
            outRect.top = headerHeight
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        topHeaderTranslationY = 0f
        val headerView = getHeaderView(parent)
        for(i in 1 until parent.childCount) {
            val childView = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(childView)
            if(monthChecker.requiresHeader(position)) {
                drawHeader(
                        c,
                        childView,
                        headerView,
                        monthChecker.getHeader(position)
                )
            }
        }
        drawHeader( //Top Most Header
                c,
                topHeaderTranslationY,
                headerView,
                monthChecker.getHeader(
                        parent.getChildAdapterPosition(
                            parent.getChildAt(0)
                        )
                )
        )
    }

    private fun getHeaderView(parent: RecyclerView) : View {
        if(headerView == null) {
            headerView = LayoutInflater.from(context).inflate(R.layout.log_item_header, parent, false)
            fixLayoutSize(headerView!!, parent)
            headerHeight = headerView!!.height
            headerText = headerView!!.findViewById(R.id.header_tv)
            parent.invalidateItemDecorations()
        }
        return headerView!!
    }

    private fun drawHeader(c: Canvas, childView: View, headerView: View, text: String) {
        val tranY = (childView.top - headerView.height).toFloat()
        if(tranY-headerView.height < topHeaderTranslationY)
            topHeaderTranslationY = tranY-headerView.height
        drawHeader(c, tranY, headerView, text)
    }

    private fun drawHeader(c: Canvas, translationY : Float, headerView: View, text: String) {
        c.save()
        c.translate(0f, translationY)
        headerText.text = text
        headerView.draw(c)
        c.restore()
    }

    private fun fixLayoutSize(view: View, viewGroup: ViewGroup) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(viewGroup.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewGroup.height, View.MeasureSpec.UNSPECIFIED)
        val childWidth = ViewGroup.getChildMeasureSpec(widthSpec, viewGroup.paddingLeft + viewGroup.paddingRight, view.layoutParams.width)
        val childHeight = ViewGroup.getChildMeasureSpec(heightSpec, viewGroup.paddingTop + viewGroup.paddingBottom, view.layoutParams.height)
        view.measure(childWidth, childHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    interface MonthChecker {
        fun requiresHeader(position: Int): Boolean
        fun getHeader(position: Int): String
    }
}