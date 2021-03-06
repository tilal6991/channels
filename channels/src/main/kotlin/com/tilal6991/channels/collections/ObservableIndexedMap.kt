package com.tilal6991.channels.collections

import android.databinding.ObservableList

interface ObservableIndexedMap<K, V> : IndexedMap<K, V> {

    val valuesList: ObservableList<V>

    fun addOnIndexedMapChangedCallback(
            callback: OnIndexedMapChangedCallback<out ObservableIndexedMap<K, V>, K, V>)

    fun removeOnIndexedMapChangedCallback(
            callback: OnIndexedMapChangedCallback<out ObservableIndexedMap<K, V>, K, V>)

    interface OnIndexedMapChangedCallback<T : ObservableIndexedMap<K, V>, K, V> {
        fun onChanged(sender: T)
        fun onItemChanged(sender: T, position: Int, key: K, oldValue: V, newValue: V)
        fun onItemInserted(sender: T, position: Int, key: K, value: V)
        fun onItemMoved(sender: T, fromPosition: Int, toPosition: Int, key: K, value: V)
        fun onItemRemoved(sender: T, position: Int, key: K, value: V)
    }
}