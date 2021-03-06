package com.tilal6991.channels.adapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.tilal6991.channels.util.failAssert
import timber.log.Timber
import java.util.*

abstract class SectionAdapter<CVH : RecyclerView.ViewHolder,
        HVH : RecyclerView.ViewHolder> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var setupComplete = false

    private val sectionAbsolutePositions: MutableList<Int>
    private var count: Int = 0

    init {
        sectionAbsolutePositions = ArrayList(getSectionCount())
    }

    fun setup() {
        setupComplete = true
        notifySectionedDataSetChanged()
    }

    abstract fun isHeaderDisplayedForSection(section: Int): Boolean

    abstract fun onBindItemViewHolder(holder: CVH, section: Int, offset: Int)

    abstract fun onBindHeaderViewHolder(holder: HVH, section: Int)

    abstract fun getSectionCount(): Int

    abstract fun getItemCountInSection(section: Int): Int

    open fun getSectionedItemViewType(section: Int, sectionOffset: Int): Int {
        return ITEM_VIEW_TYPE
    }

    open fun getHeaderViewType(section: Int): Int {
        return HEADER_VIEW_TYPE
    }

    fun notifySectionedDataSetChanged() {
        checkSetup()

        count = 0
        sectionAbsolutePositions.clear()
        for (i in 0..getSectionCount() - 1) {
            sectionAbsolutePositions.add(count)
            val itemCountInSection = getItemCountInSection(i)
            count += itemCountInSection
        }

        notifyDataSetChanged()
    }

    fun notifyItemRangeInsertedInSection(section: Int, offset: Int, insertCount: Int) {
        checkSetup()

        val sectionStart = sectionAbsolutePositions[section] + getSectionHeadersBefore(section)
        val sectionCount = getItemCountInSection(section)
        val headerOffsetForSection = getHeaderOffsetForSection(section)
        if (sectionCount == insertCount) {
            if (offset != 0) {
                Timber.asTree().failAssert()
            }
            notifyItemRangeInserted(sectionStart, headerOffsetForSection + headerOffsetForSection)
        } else {
            notifyItemRangeInserted(sectionStart + headerOffsetForSection + offset, insertCount)
        }
        for (i in section + 1..getSectionCount() - 1) {
            sectionAbsolutePositions[i] += insertCount
        }
        count += insertCount
    }

    fun notifyItemRangeMovedInSection(section: Int, fromPosition: Int, toPosition: Int, itemCount: Int) {
        checkSetup()

        val sectionItemStart = sectionAbsolutePositions[section] + getSectionHeadersBefore(section) + getHeaderOffsetForSection(section)
        for (i in 0..itemCount - 1) {
            notifyItemMoved(sectionItemStart + fromPosition + i, sectionItemStart + toPosition + i)
        }
    }

    fun notifyItemRangeChangedInSection(section: Int, positionStart: Int, itemCount: Int) {
        checkSetup()

        val sectionItemStart = sectionAbsolutePositions[section] + getSectionHeadersBefore(section) + getHeaderOffsetForSection(section)
        notifyItemRangeChanged(sectionItemStart + positionStart, itemCount)
    }

    fun notifyItemRangeChangedInSection(section: Int, positionStart: Int, itemCount: Int, payload: Any?) {
        checkSetup()

        val sectionItemStart = sectionAbsolutePositions[section] + getSectionHeadersBefore(section) + getHeaderOffsetForSection(section)
        notifyItemRangeChanged(sectionItemStart + positionStart, itemCount, payload)
    }

    fun notifyItemRangeRemovedInSection(section: Int, offset: Int, removeCount: Int) {
        checkSetup()

        val sectionStart = sectionAbsolutePositions[section] + getSectionHeadersBefore(section)
        val sectionCount = getItemCountInSection(section)
        val headerOffsetForSection = getHeaderOffsetForSection(section)
        if (sectionCount == 0) {
            if (offset != 0) {
                Timber.asTree().failAssert()
            }
            notifyItemRangeRemoved(sectionStart, removeCount + headerOffsetForSection)
        } else {
            notifyItemRangeRemoved(sectionStart + headerOffsetForSection + offset, removeCount)
        }

        for (i in section + 1..getSectionCount() - 1) {
            sectionAbsolutePositions[i] -= removeCount
        }
        count -= removeCount
    }

    override final fun onBindViewHolder(holder: RecyclerView.ViewHolder, adapterPosition: Int) {
        checkSetup()

        val section = getSectionForAdapterPosition(adapterPosition)
        val sectionOffset = getSectionOffsetForAdapterPosition(section, adapterPosition)

        if (sectionOffset == -1) {
            onBindHeaderViewHolder(holder as HVH, section)
        } else {
            onBindItemViewHolder(holder as CVH, section, sectionOffset)
        }
    }

    override final fun getItemViewType(adapterPosition: Int): Int {
        checkSetup()

        val section = getSectionForAdapterPosition(adapterPosition)
        val sectionOffset = getSectionOffsetForAdapterPosition(section, adapterPosition)

        if (sectionOffset == -1) {
            return getHeaderViewType(section)
        }
        return getSectionedItemViewType(section, sectionOffset)
    }

    override final fun getItemCount(): Int {
        checkSetup()
        return count + getSectionHeadersBefore(getSectionCount())
    }

    fun getWrappedSpanSizeLookup(local: GridSpanSizeLookup): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(adapterPosition: Int): Int {
                checkSetup()
                val section = getSectionForAdapterPosition(adapterPosition)
                val sectionOffset = getSectionOffsetForAdapterPosition(section, adapterPosition)
                if (sectionOffset == -1) {
                    return local.getHeaderSpanSize(section)
                } else {
                    return local.getItemSpanSize(section, sectionOffset)
                }
            }
        }
    }

    private fun checkSetup() {
        if (!setupComplete) {
            throw IllegalStateException("setup() was not called")
        }
    }

    private fun getHeaderOffsetForSection(section: Int): Int {
        return if (isHeaderDisplayedForSection(section)) 1 else 0
    }

    private fun getSectionOffsetForAdapterPosition(section: Int, adapterPosition: Int): Int {
        val nonEmptySections = getSectionHeadersBefore(section)
        val absolutePositionIncludingHeader = adapterPosition - nonEmptySections
        return absolutePositionIncludingHeader - sectionAbsolutePositions[section] - getHeaderOffsetForSection(section)
    }

    private fun getSectionForAdapterPosition(adapterPosition: Int): Int {
        var count = 0
        for (i in 0..getSectionCount() - 1) {
            if (adapterPosition < count) {
                return i - 1
            }
            val itemCountInSection = getItemCountInSection(i)
            if (itemCountInSection > 0) {
                count += itemCountInSection + getHeaderOffsetForSection(i)
            }
        }
        return getSectionCount() - 1
    }

    private fun getSectionHeadersBefore(section: Int): Int {
        var count = 0
        for (i in 0..section - 1) {
            if (getItemCountInSection(i) > 0 && isHeaderDisplayedForSection(i)) {
                count++
            }
        }
        return count
    }

    class ObserverProxy(private val section: Int,
                        private val adapter: SectionAdapter<*, *>) : RecyclerView.AdapterDataObserver() {

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            adapter.notifyItemRangeMovedInSection(section, fromPosition, toPosition, itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            adapter.notifyItemRangeInsertedInSection(section, positionStart, itemCount)
        }

        override fun onChanged() {
            adapter.notifySectionedDataSetChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            adapter.notifyItemRangeRemovedInSection(section, positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            adapter.notifyItemRangeChangedInSection(section, positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            adapter.notifyItemRangeChangedInSection(section, positionStart, itemCount, payload)
        }
    }

    interface GridSpanSizeLookup {
        fun getHeaderSpanSize(section: Int): Int
        fun getItemSpanSize(section: Int, position: Int): Int
    }

    companion object {
        const val HEADER_VIEW_TYPE = 5
        const val ITEM_VIEW_TYPE = 10
    }
}