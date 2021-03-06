package com.tilal6991.channels.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import com.tilal6991.channels.R
import com.tilal6991.channels.configuration.ChannelsConfiguration
import com.tilal6991.channels.databinding.NavigationClientBinding
import com.tilal6991.channels.util.failAssert
import com.tilal6991.channels.viewmodel.RelayVM
import timber.log.Timber

class NavigationClientAdapter(
        private val context: Context,
        private val relayVM: RelayVM,
        private val addClick: (View) -> Unit,
        private val manageClick: (ChannelsConfiguration) -> Unit,
        private val settingsClick: (View) -> Unit,
        private val clientClickListener: (ChannelsConfiguration) -> Unit) :
        SectionAdapter<NavigationClientAdapter.ViewHolder, HeaderViewHolder>() {

    private val inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER_VIEW_TYPE) {
            return HeaderViewHolder(inflater.inflate(R.layout.recycler_header, parent, false))
        } else if (viewType == 25) {
            return FooterViewHolder(inflater.inflate(R.layout.navigation_footer, parent, false))
        }
        return ClientViewHolder(NavigationClientBinding.inflate(inflater, parent, false))
    }

    override fun onBindItemViewHolder(holder: ViewHolder, section: Int, offset: Int) {
        holder.bind(section, offset)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder, section: Int) {
        if (section == 0) {
            holder.bind(context.getString(R.string.header_active_clients))
        } else if (section == 1) {
            holder.bind(context.getString(R.string.header_inactive_clients))
        } else {
            Timber.asTree().failAssert()
        }
    }

    override fun getItemCountInSection(section: Int): Int {
        if (section == 2) {
            return 2
        }
        return getListForSection(section).size
    }

    override fun isHeaderDisplayedForSection(section: Int): Boolean {
        return section != 2
    }

    override fun getSectionedItemViewType(section: Int, sectionOffset: Int): Int {
        if (section == 2) {
            return 25
        }
        return 20
    }

    override fun getSectionCount(): Int {
        return 3
    }

    private fun getListForSection(section: Int): List<ChannelsConfiguration> = when (section) {
        0 -> relayVM.activeConfigs
        1 -> relayVM.inactiveConfigs
        else -> {
            Timber.asTree().failAssert()
            relayVM.inactiveConfigs
        }
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(section: Int, offset: Int)
    }

    inner class ClientViewHolder(private val binding: NavigationClientBinding) : ViewHolder(binding.root) {
        override fun bind(section: Int, offset: Int) {
            val item = getListForSection(section)[offset]

            binding.configuration = item
            binding.client = relayVM.configActiveClients[item]

            binding.root.setOnClickListener { clientClickListener(item) }
            binding.drawerClientManage.setOnClickListener { manageClick(item) }

            binding.executePendingBindings()
        }
    }

    inner class FooterViewHolder(private val view: View) : ViewHolder(view) {
        private val image by bindView<ImageView>(R.id.image)
        private val text by bindView<TextView>(R.id.text)

        override fun bind(section: Int, offset: Int) {
            view.setOnClickListener(if (offset == 0) addClick else settingsClick)
            text.setText(if (offset == 0) R.string.add_server else R.string.settings)
            image.setImageResource(if (offset == 0) R.drawable.ic_add else R.drawable.ic_settings)
        }
    }
}