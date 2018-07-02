package io.stanwood.debugapp.databinding

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import io.stanwood.framework.databinding.recyclerview.BaseBindingAdapter


object RecyclerViewBindings {

    @BindingAdapter("items", "adapter", requireAll = false)
    @JvmStatic
    fun setAdapterItems(recyclerView: RecyclerView, items: List<*>?, adapter: RecyclerView.Adapter<*>?) {
        if (recyclerView.adapter == null) {
            if (adapter == null || items?.isEmpty() ?: true) {
                return
            }
            recyclerView.adapter = adapter
        }
        (recyclerView.adapter as BaseBindingAdapter<*>).setItems(items as List<Nothing>?)
    }
}

