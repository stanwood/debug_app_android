package io.stanwood.debugapp

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView


class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val col0 = view.findViewById<TextView>(R.id.col0)
    val col1 = view.findViewById<TextView>(R.id.col1)
    val col2 = view.findViewById<TextView>(R.id.col2)
    val col3 = view.findViewById<TextView>(R.id.col3)
}