package com.tilal6991.channels.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import android.databinding.ObservableList
import com.tilal6991.channels.BR
import com.tilal6991.channels.R
import com.tilal6991.channels.configuration.ChannelsConfiguration
import com.tilal6991.channels.viewmodel.helper.UserMessageParser
import com.tilal6991.relay.EventListener
import com.tilal6991.relay.RelayClient
import com.tilal6991.relay.protocol.ClientGenerator

class ClientVM(private val context: Context,
               private val client: RelayClient,
               private val userMessageParser: UserMessageParser,
               val configuration: ChannelsConfiguration,
               val server: ServerVM,
               val channels: ObservableList<ChannelVM>) : BaseObservable(), EventListener {

    val name: CharSequence
        get() = configuration.name
    val hostname: CharSequence
        get() = configuration.server.hostname

    var status: String = context.getString(CONNECTING)
        @Bindable get

    var statusInt: Int = CONNECTING
        @Bindable get

    val selectedChild: ObservableField<ClientChildVM>

    init {
        selectedChild = ObservableField(server)

        client.init()
        client.connect()

        server.onConnecting()
    }

    fun select(child: ClientChildVM) {
        selectedChild.set(child)
    }

    fun reconnect() {
        if (statusInt != DISCONNECTED) {
            return
        }
        updateStatus(CONNECTING)
        client.connect()

        server.onConnecting()
    }

    fun disconnect() {
        if (statusInt == DISCONNECTED) {
            return
        }
        client.send(ClientGenerator.quit())
        client.disconnect()
    }

    fun close() {
        client.close()
    }

    private fun updateStatus(newStatus: Int) {
        statusInt = newStatus
        status = context.getString(newStatus)

        notifyPropertyChanged(BR.statusInt)
        notifyPropertyChanged(BR.status)
    }

    fun sendUserMessage(message: String, context: ClientChildVM) {
        val line = userMessageParser.parse(message, context, server) ?: return
        client.send(line)
    }

    override fun onSocketConnect() {
        updateStatus(SOCKET_CONNECTED)
    }

    override fun onDisconnect(triggered: Boolean) {
        updateStatus(DISCONNECTED)
    }

    override fun onWelcome(target: String, text: String) {
        updateStatus(CONNECTED)
    }

    companion object {
        const val CONNECTING: Int = R.string.status_connecting
        const val SOCKET_CONNECTED: Int = R.string.status_socket_connected
        const val CONNECTED: Int = R.string.status_connected
        const val DISCONNECTED: Int = R.string.status_disconnected
    }
}