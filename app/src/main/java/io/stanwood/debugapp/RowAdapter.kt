package io.stanwood.debugapp

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup


class RowAdapter(private val rows: MutableList<Row>) : RecyclerView.Adapter<RowViewHolder>() {

    fun addRow(row: Row) {
        rows.add(row)
        notifyItemInserted(rows.size - 1)
    }

    fun clear() {
        notifyItemRangeRemoved(0, rows.size)
        rows.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        return RowViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row, parent, false))
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val row = rows[position]
        holder.apply {
            col0.text = row.timeStamp
            col1.text = row.eventName
            col2.text = if (row.name.isNullOrEmpty()) "" else row.name
            col3.text = if (row.itemId.isNullOrEmpty()) "" else row.itemId
        }
        holder.itemView.setBackgroundColor(if (position % 2 == 0) Color.LTGRAY else Color.WHITE)
    }

    override fun getItemCount(): Int {
        return rows.size
    }
}