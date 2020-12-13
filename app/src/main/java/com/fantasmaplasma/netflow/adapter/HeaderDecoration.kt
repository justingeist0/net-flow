package com.fantasmaplasma.netflow.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fantasmaplasma.netflow.R

class HeaderDecoration(
        private val context: Context,
        private val monthChecker: MonthChecker
) : RecyclerView.ItemDecoration() {
    private var headerView : View? = null
    private lateinit var headerText : TextView
    private var headerHeight = 0
    private var topHeaderTranslationY = 0f
    private val itemHeaderMarginTop = context.resources.getDimension(R.dimen.log_item_header_margin)
    private val itemMarginBottom = context.resources.getDimension(R.dimen.log_item_bottom_margin)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if(position >= 0 && monthChecker.requiresHeader(position)) {
            outRect.top = headerHeight
        }
        outRect.bottom = itemMarginBottom.toInt()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        topHeaderTranslationY = 0f
        val headerView = getHeaderView(parent)
        for(i in 1 until parent.childCount) {
            val childView = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(childView)
            if(monthChecker.requiresHeader(position)) {
                drawHeader( //May set topHeaderTranslationY
                        c,
                        childView,
                        headerView,
                        monthChecker.getHeader(position)
                )
            }
        }
        val topLogAdapterPosition = parent.getChildAdapterPosition(
                parent.getChildAt(0)
        )
        val topHeaderExist = topLogAdapterPosition >= 0
        if(topHeaderExist) {
            drawHeader(
                    c,
                    topHeaderTranslationY,
                    headerView,
                    monthChecker.getHeader(
                            topLogAdapterPosition
                    )
            )
        }
    }

    private fun getHeaderView(parent: RecyclerView) : View {
        if(headerView == null) {
            headerView = LayoutInflater.from(context).inflate(R.layout.list_log_header, parent, false)
            fixLayoutSize(headerView!!, parent)
            headerHeight = headerView!!.height
            headerText = headerView!!.findViewById(R.id.header_tv)
            parent.invalidateItemDecorations()
        }
        return headerView!!
    }

    private fun drawHeader(c: Canvas, childView: View, headerView: View, text: String) {
        val tranY = (childView.top - headerView.height).toFloat()+childView.translationY
        if(tranY-headerView.height < topHeaderTranslationY - itemHeaderMarginTop)
            topHeaderTranslationY = tranY-headerView.height+itemHeaderMarginTop
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