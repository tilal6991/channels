package com.tilal6991.channels.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tilal6991.channels.R
import com.tilal6991.channels.adapter.HeaderViewHolder
import com.tilal6991.channels.adapter.SectionAdapter
import com.tilal6991.channels.ui.helper.ClientChildListener
import com.tilal6991.channels.viewmodel.ChannelVM
import com.tilal6991.channels.viewmodel.ClientChildVM

class UserListPresenter(override val context: Activity,
                        private val drawerLayout: DrawerLayout,
                        private val userDrawerView: View) : Presenter {
    override val id: String
        get() = "user_list"

    private val recycler: RecyclerView by userDrawerView.bindView(R.id.user_list_recycler)
    private val childListener = object : ClientChildListener(context) {
        override fun onChildChange(clientChild: ClientChildVM?) {
            onChildChanged(clientChild)
        }
    }
    private val adapter = Adapter(context)

    override fun setup(savedState: Bundle?) {
        adapter.setup()

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
    }

    override fun bind() {
        childListener.bind()
        onChildChanged(selectedChild?.get())
    }

    override fun unbind() {
        childListener.unbind()
    }

    private fun onChildChanged(clientChild: ClientChildVM?) {
        adapter.onChildChanged(clientChild)

        if (clientChild is ChannelVM) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, userDrawerView)
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, userDrawerView)
        }
    }

    class Adapter(private val context: Context) : SectionAdapter<Adapter.ViewHolder, HeaderViewHolder>() {
        private val inflater = LayoutInflater.from(context)

        private var displayedChild: ChannelVM? = null

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
            if (viewType == HEADER_VIEW_TYPE) {
                return HeaderViewHolder(inflater.inflate(R.layout.recycler_header, parent, false))
            }
            return ViewHolder(inflater.inflate(R.layout.user_list_item, parent, false))
        }

        override fun isHeaderDisplayedForSection(section: Int): Boolean {
            return true
        }

        override fun onBindHeaderViewHolder(holder: HeaderViewHolder, section: Int) {
            val userMap = displayedChild?.userMap
            val key = userMap?.getKeyAt(section) ?: return
            val value = userMap?.getValueAt(section) ?: return
            holder.bind("${value.size} $key users")
        }

        override fun onBindItemViewHolder(holder: Adapter.ViewHolder, section: Int, offset: Int) {
            val valueAt = displayedChild?.userMap?.getValueAt(section)
            val displayString = valueAt?.get(offset)?.handle
            holder.bind(displayString ?: "Broken")
        }

        override fun getSectionCount(): Int {
            return displayedChild?.userMap?.size ?: 0
        }

        override fun getItemCountInSection(section: Int): Int {
            return displayedChild?.userMap?.getValueAt(section)?.size ?: 0
        }

        fun onChildChanged(clientChild: ClientChildVM?) {
            if (clientChild == displayedChild) return

            displayedChild = if (clientChild is ChannelVM) clientChild else null
            notifySectionedDataSetChanged()
        }

        class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            fun bind(text: String) {
                (view as TextView).text = text
            }
        }
    }
}