package com.tilal6991.channels.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tilal6991.channels.R
import com.tilal6991.channels.base.relayVM
import com.tilal6991.channels.databinding.NavigationClientChildrenBinding
import com.tilal6991.channels.util.failAssert
import com.tilal6991.channels.viewmodel.ClientChildVM
import com.tilal6991.channels.viewmodel.ClientVM
import timber.log.Timber

class NavigationChildAdapter(
        private val context: Context,
        private val childClickListener: (ClientChildVM) -> Unit) :
        SectionAdapter<NavigationChildAdapter.ChildViewHolder, HeaderViewHolder>() {

    private val inflater: LayoutInflater
    private val selectedClient: ClientVM?
        get() = context.relayVM.selectedClients.latest

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER_VIEW_TYPE) {
            return HeaderViewHolder(inflater.inflate(R.layout.recycler_header, parent, false))
        }
        return ChildViewHolder(NavigationClientChildrenBinding.inflate(inflater, parent, false))
    }

    override fun getSectionedItemViewType(section: Int, sectionOffset: Int): Int {
        return 10
    }

    override fun getItemCountInSection(section: Int): Int {
        if (section == 0) {
            return 1
        }
        return selectedClient?.channelManager?.channels?.size ?: 0
    }

    override fun isHeaderDisplayedForSection(section: Int): Boolean {
        return section == 1
    }

    override fun onBindItemViewHolder(holder: ChildViewHolder, section: Int, offset: Int) {
        holder.bind(section, offset)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder, section: Int) {
        if (section == 1) {
            holder.bind(context.getString(R.string.header_channels))
        } else if (section == 2) {
            holder.bind(context.getString(R.string.header_private_messages))
        } else {
            Timber.asTree().failAssert()
        }
    }

    // TODO(tilal6991) make this 3 when PMs come into play.
    override fun getSectionCount(): Int {
        return 2
    }

    inner class ChildViewHolder(private val binding: NavigationClientChildrenBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(section: Int, position: Int) {
            if (section == 0) {
                binding.child = selectedClient?.server
            } else {
                binding.child = selectedClient!!.channelManager.channels[position]
            }
            binding.executePendingBindings()

            binding.root.setOnClickListener { childClickListener(binding.child) }
        }
    }
}