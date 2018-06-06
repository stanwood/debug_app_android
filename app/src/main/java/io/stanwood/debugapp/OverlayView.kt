package io.stanwood.debugapp

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout


class OverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var rcv: RecyclerView? = null
    private var adapter: RowAdapter = RowAdapter(mutableListOf())
    override fun onFinishInflate() {
        super.onFinishInflate()
        rcv = findViewById<RecyclerView>(R.id.rcv)
        rcv?.apply {
            layoutManager = LinearLayoutManager(context)
                    .apply { stackFromEnd = true }
            adapter = this@OverlayView.adapter
                    .apply {
                        registerAdapterDataObserver(scrollToBottomDataObserver)
                    }
        }
        findViewById<View>(R.id.btn).apply {
            setOnClickListener { rcv?.visibility = if (rcv?.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
        }
        findViewById<View>(R.id.clear).apply {
            setOnClickListener {
                adapter.clear()
            }
        }
        findViewById<View>(R.id.snapshot).apply {
            setOnClickListener {
                val intent = Intent()
                intent.action = "io.stanwood.uitesting.DUMP_WINDOW_HIERARCHY_INTENT"
                context.sendBroadcast(intent)
            }
        }

    }

    fun addRow(row: Row) {
        adapter.addRow(row)
    }

    private val scrollToBottomDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            val lm = rcv?.getLayoutManager() as LinearLayoutManager
            val lastVisiblePos = lm.findLastVisibleItemPosition()
            if (lastVisiblePos >= positionStart - 1) {
                lm.scrollToPositionWithOffset(positionStart, 0)
            }
        }
    }
}





