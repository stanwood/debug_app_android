package io.stanwood.debugapp

abstract class ItemSwapHelper<T, T2> {
    abstract fun mapAll(newItems: List<T2>): List<T>
    abstract fun isSame(item: T, newItem: T2): Boolean
    abstract fun map(item: T2): T
    fun swap(items: MutableList<T>, newItems: List<T2>) {
        if (items.isEmpty()) {
            items.addAll(mapAll(newItems))
            return
        }
        for (i in items.size - 1 downTo 0) {
            if (!newItems.any { isSame(items[i], it) }) {
                items.removeAt(i)
            }
        }
        for (i in 0 until newItems.size) {
            if (!items.any { isSame(it, newItems[i]) }) {
                items.add(i, map(newItems[i]))
            }
        }
        for (i in newItems.size - 1 downTo 0) {
            newItems[i]
                    .let { item ->
                        if (isSame(items.get(i), item)) i else items.indexOfFirst { isSame(it, item) }
                    }
                    .apply {
                        if (this >= 0 && this != i) {
                            items.add(i, items.removeAt(this))
                        }
                    }
        }
    }
}