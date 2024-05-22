package org.citruscircuits.viewer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getDataFromWebsite
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Manages updates to the data and triggering refreshes in the UI
 */
class RefreshManager {
    private val listeners = mutableMapOf<String, () -> Unit>()
    /* Function to start coroutine for timed refreshing**/
    fun start(scope: CoroutineScope) {
        if (!Constants.USE_TEST_DATA) {
            tickerFlow(
                Constants.REFRESH_INTERVAL.seconds, Constants.REFRESH_INTERVAL.seconds
            ).onEach {
                Log.d("data-refresh", "tick")
                try {
                    getDataFromWebsite()
                    Log.i("data-refresh", "Fetched data from website successfully")
                    refresh()
                } catch (e: Throwable) {
                    Log.e("data-refresh", "Error fetching data $it")
                }
            }.launchIn(scope)
        }
    }
    /** Adds a refresh listener*/
    fun addRefreshListener(listener: () -> Unit): String {
        val id = UUID.randomUUID().toString()
        listeners[id] = listener
        Log.d("data-refresh", "Added id: $id")
        return id
    }
    /** Removes a listener */
    fun removeRefreshListener(id: String? = null) {
        if (listeners.containsKey(id)) listeners.remove(id)
        Log.d("data-refresh", "Destroyed id: $id")
    }
    /** Function that will go through each listener and refresh*/
    fun refresh() {
        listeners.forEach {
            it.value.invoke()
            Log.d("data-refresh", "refreshed: ${it.key}")
        }
    }
    /** Remove all listeners*/
    fun removeAllListeners() = listeners.clear()
    /** Specifically refreshes one listener*/
    fun refresh(id: String) = listeners[id]?.let { it() }
}

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}
