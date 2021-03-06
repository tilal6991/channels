package com.tilal6991.channels.relay

import android.util.Base64
import com.tilal6991.relay.ClientGenerator
import com.tilal6991.relay.EventListener
import com.tilal6991.relay.RelayClient
import java.nio.charset.StandardCharsets

interface AuthHandler : EventListener {

    /**
     * Called by other listeners to decide who should end a CAP session.
     *
     * @param caps optional list of caps which are being advertised by the server.
     * @return whether the handler will send CAP END.
     */
    fun endsCap(caps: List<String>? = null): Boolean = false
}

val EMPTY_AUTH_HANDLER: AuthHandler by lazy { object : AuthHandler {} }

abstract class SASLHandler(protected val client: RelayClient) : AuthHandler {

    private var handling = false

    override fun onCapLs(caps: List<String>, values: List<String?>, finalLine: Boolean) {
        handling = caps.contains("sasl")
        if (handling) {
            client.send(ClientGenerator.cap("REQ", listOf("sasl")))
        }
    }

    override fun onCapAck(caps: List<String>) {
        if (!caps.contains("sasl")) return
        onCapAck()
    }

    override fun onCapNak(caps: List<String>) {
        if (!caps.contains("sasl")) return
        endHandling()
    }

    override fun onOtherCode(code: Int, arguments: List<String>) {
        when (code) {
        // TODO(tilal6991) utilise the codes specified by Relay
            902, 903, 904, 905, 906, 907 -> endHandling()
        }
    }

    override fun endsCap(caps: List<String>?): Boolean {
        return handling || (caps?.contains("sasl") ?: false)
    }

    protected fun endHandling() {
        handling = false
        client.send(ClientGenerator.cap("END"))
    }

    abstract fun onCapAck()
}

class PlainSASLHandler(client: RelayClient,
                       val username: String,
                       val password: String) : SASLHandler(client) {

    override fun onCapAck() {
        client.send(ClientGenerator.authenticate("PLAIN"))
    }

    override fun onAuthenticate(data: String) {
        if (data == "+") {
            val authentication = "\\0$username\\0$password"
            val authBytes = authentication.toByteArray(StandardCharsets.UTF_8)
            val encoded = Base64.encodeToString(authBytes, Base64.DEFAULT)
            client.send(ClientGenerator.authenticate(encoded))
        }
    }
}

class NickServHandler(private val client: RelayClient,
                      private val password: String) : AuthHandler {

    override fun onWelcome(target: String, text: String) {
        client.send(ClientGenerator.privmsg("NickServ", "IDENTIFY $password"))
    }
}