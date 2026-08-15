package com.nahunp.todoapp.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.nahunp.todoapp.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope

/**
 * Wraps ConnectivityManager's callback API as a Flow<Boolean> — "is there
 * a network with validated internet access right now". SyncManager is the
 * one consumer; it collects this to know when to (re)try draining the
 * offline queue. Requires ACCESS_NETWORK_STATE (see AndroidManifest.xml).
 */
interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
}

@Singleton
class ConnectivityObserverImpl @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope applicationScope: CoroutineScope,
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Shared + replayed (stateIn), not a cold callbackFlow re-registered
    // per collector — SyncManager and any future UI collector should all
    // see the same debounced-by-the-OS connectivity state, not each open
    // their own NetworkCallback registration.
    override val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val validatedNetworks = mutableSetOf<Network>()

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (validated) validatedNetworks.add(network) else validatedNetworks.remove(network)
                trySend(validatedNetworks.isNotEmpty())
            }

            override fun onLost(network: Network) {
                validatedNetworks.remove(network)
                trySend(validatedNetworks.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        trySend(false) // safe initial value; real state follows almost immediately

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()
        .stateIn(applicationScope, SharingStarted.Eagerly, false)
}
