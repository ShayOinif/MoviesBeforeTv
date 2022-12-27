package com.shayo.moviespoint

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.common.snackbar.SnackBarMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var conMgr: ConnectivityManager

    // TODO: Move all network handling to a different class
    private val callback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            SnackBarManager.messages.value = SnackBarMessage.NetworkMessage.HasNetwork
        }

        override fun onLost(network: Network) {
            super.onLost(network)

            if (
                conMgr.getActiveNetworkInfo() == null
                || conMgr.getActiveNetworkInfo()?.isAvailable() != true
                || conMgr.getActiveNetworkInfo()?.isConnected() != true
            ) {
                SnackBarManager.messages.value =
                    SnackBarMessage.NetworkMessage.NoNetwork("You're Offline, Some Features Might Not Be Available")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setContent { MoviesPointApp() }
    }

    override fun onResume() {
        super.onResume()

        if (
            conMgr.getActiveNetworkInfo() == null
            || conMgr.getActiveNetworkInfo()?.isAvailable() != true
            || conMgr.getActiveNetworkInfo()?.isConnected() != true
        ) {
            SnackBarManager.messages.value =
                SnackBarMessage.NetworkMessage.NoNetwork("You're Offline, Some Features Might Not Be Available")
        }

        conMgr.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ).build(),
            callback
        )
    }

    override fun onPause() {
        super.onPause()

        conMgr.unregisterNetworkCallback(callback)
    }
}