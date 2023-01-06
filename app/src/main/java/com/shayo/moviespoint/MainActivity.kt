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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.shayo.moviespoint.common.snackbar.SnackBarManager
import com.shayo.moviespoint.common.snackbar.SnackBarMessage
import dagger.hilt.android.AndroidEntryPoint

// TODO: Move somewhere else
enum class NavOption { BOTTOM_BAR, NAV_RAIL, NAV_DRAWER }

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
                conMgr.activeNetworkInfo == null
                || conMgr.activeNetworkInfo?.isAvailable != true
                || conMgr.activeNetworkInfo?.isConnected != true
            ) {
                SnackBarManager.messages.value =
                    SnackBarMessage.NetworkMessage.NoNetwork("You're Offline, Some Features Might Not Be Available")
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setContent {

            val windowSizeClass = calculateWindowSizeClass(this)
            // Perform logic on the window size class to decide whether to use a nav rail.
            val navOption = when {
                    windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded -> NavOption.NAV_DRAWER
                    windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium -> NavOption.NAV_RAIL
                    else -> NavOption.BOTTOM_BAR
                }

            MoviesPointApp(navOption)
        }
    }

    override fun onResume() {
        super.onResume()

        if (
            conMgr.activeNetworkInfo == null
            || conMgr.activeNetworkInfo?.isAvailable != true
            || conMgr.activeNetworkInfo?.isConnected != true
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