/*
 * Copyright (c) 2018 stanwood GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.stanwood.debugapp.databinding

import android.databinding.ListChangeRegistry
import android.databinding.ObservableList
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback

open class ObservableArrayListEx<T> : ArrayList<T>(), ObservableList<T>, ListUpdateCallback {

    @Transient
    private val listeners: ListChangeRegistry = ListChangeRegistry()

    fun swapItems(newItems: List<T>, diffResult: DiffUtil.DiffResult?) {
        super.clear()
        super.addAll(newItems)
        diffResult?.apply { dispatchUpdatesTo(this@ObservableArrayListEx) }
                ?: listeners.apply { notifyChanged(this@ObservableArrayListEx) }
    }

    fun swapItem(from: Int, to: Int) {
        super.set(from, super.set(to, super.get(from)))
        onMoved(from, to)
    }

    fun moveItem(from: Int, to: Int) {
        val pos = if (to > from) to - 1 else to
        super.add(pos, super.removeAt(from))
        onMoved(from, pos)
    }

    private fun notifyAdd(start: Int, count: Int) = listeners.notifyInserted(this, start, count)

    private fun notifyRemove(start: Int, count: Int) = listeners.notifyRemoved(this, start, count)

    override fun onInserted(position: Int, count: Int) = notifyAdd(position, count)

    override fun onRemoved(position: Int, count: Int) = notifyRemove(position, count)

    override fun onMoved(fromPosition: Int, toPosition: Int) = listeners.notifyMoved(this@ObservableArrayListEx, fromPosition, toPosition, 1)

    override fun onChanged(position: Int, count: Int, payload: Any?) = listeners.notifyChanged(this@ObservableArrayListEx, position, count)

    override fun removeOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>?) = listeners.remove(listener)

    override fun addOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>?) = listeners.add(listener)

    override fun add(element: T): Boolean {
        return super.add(element).apply {
            notifyAdd(size - 1, 1)
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        notifyAdd(index, 1)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val oldSize = size
        return super.addAll(elements)
                .apply {
                    if (this) {
                        notifyAdd(oldSize, size - oldSize)
                    }
                }

    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements)
                .apply {
                    if (this) {
                        notifyAdd(index, elements.size)
                    }
                }
    }

    override fun clear() {
        val oldSize = size
        super.clear()
        if (oldSize != 0) {
            notifyRemove(0, oldSize)
        }
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        } else {
            return false
        }
    }

    override fun removeAt(index: Int): T {
        return super.removeAt(index)
                .apply {
                    notifyRemove(index, 1)
                }
    }

    override fun set(index: Int, element: T): T {
        return super.set(index, element)
                .apply {
                    listeners.notifyChanged(this@ObservableArrayListEx, index, 1)
                }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        notifyRemove(fromIndex, toIndex - fromIndex)
    }
}