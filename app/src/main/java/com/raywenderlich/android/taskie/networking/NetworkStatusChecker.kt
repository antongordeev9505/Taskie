package com.raywenderlich.android.taskie.networking

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

//connectivityManager - check connection and capability, if it is ok - do something
class NetworkStatusChecker(private val connectivityManager: ConnectivityManager?) {

    inline fun performIfConnectedToInternet(action: () -> Unit) {
        //check the internet connection
        if (hasInternetConnection()) {
            action()
        }
    }

    fun hasInternetConnection(): Boolean {
        //check internet
        val network = connectivityManager?.activeNetwork ?: return false
        //check is there mobile, wifi or VPN connection
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        //check which connection we have
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

}