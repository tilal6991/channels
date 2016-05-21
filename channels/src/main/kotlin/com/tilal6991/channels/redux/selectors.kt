package com.tilal6991.channels.redux

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.brianegan.bansa.Subscription
import com.tilal6991.channels.base.store
import com.tilal6991.channels.redux.state.Client
import com.tilal6991.channels.redux.state.ClientChild
import com.tilal6991.channels.redux.state.GlobalState
import com.tilal6991.channels.redux.util.binarySearch
import com.tilal6991.channels.redux.util.getOrNull
import com.tilal6991.reselect.Reselect.createSelector
import trikita.anvil.Anvil
import java.util.*

var currentState = initialState
private val handler = Handler(Looper.getMainLooper())
private val mainThreadSubscribers = ArrayList<(GlobalState) -> Unit>()
private var s: Subscription? = null

fun subscribe(context: Context, fn: (GlobalState) -> Unit): Runnable {
    if (s == null) {
        s = context.store.subscribe { state ->
            handler.post {
                if (currentState !== state) {
                    currentState = state
                    mainThreadSubscribers.forEach { it(currentState) }
                    Anvil.render()
                }
            }
        }
        currentState = context.store.state
        Anvil.render()
        fn(currentState)
    }

    mainThreadSubscribers.add(fn)
    return Runnable {
        mainThreadSubscribers.remove(fn)
    }
}

private val selectedClientSelector = createSelector(
        { state: GlobalState, p: Unit -> state.selectedClients },
        { state, p -> state.clients },
        { selected, clients ->
            val configuration = selected.getOrNull(0) ?: return@createSelector null
            val index = clients.binarySearch(configuration) { it.configuration }
            if (index < 0) null else clients[index]
        })

fun selectedClient(): Client? {
    return selectedClientSelector(currentState, Unit)
}

private val selectedChildSelector = createSelector(
        { state: GlobalState, p: Unit -> selectedClient() },
        {
            if (it == null) return@createSelector null
            when (it.selectedType) {
                Client.SELECTED_SERVER -> it.server
                Client.SELECTED_CHANNEL -> it.channels[it.selectedIndex]
                else -> null
            }
        }
)

fun selectedChild(): ClientChild? {
    return selectedChildSelector(currentState, Unit)
}

fun message(child: ClientChild?): CharSequence? {
    val buffer = child?.buffer ?: return null
    return buffer.getOrNull(buffer.size() - 1) ?: "No items to show"
}
