package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Helper to fetch GPS coordinates and reverse geocode addresses in Chinese.
 */
class LocationHelper(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val address: String?
    )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? = withContext(Dispatchers.IO) {
        try {
            val cts = CancellationTokenSource()
            val task = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            )
            val location: Location? = com.google.android.gms.tasks.Tasks.await(task)
            if (location != null) {
                val address = resolveAddress(location.latitude, location.longitude)
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else 0.0,
                    address = address
                )
            } else {
                // Try last known location
                val lastTask = fusedClient.lastLocation
                val lastLoc: Location? = com.google.android.gms.tasks.Tasks.await(lastTask)
                if (lastLoc != null) {
                    val address = resolveAddress(lastLoc.latitude, lastLoc.longitude)
                    LocationResult(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        altitude = if (lastLoc.hasAltitude()) lastLoc.altitude else 0.0,
                        address = address
                    )
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun resolveAddress(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.CHINA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use synchronous fallback for reliable result waiting in IO coroutine
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lng, 1)
                if (!list.isNullOrEmpty()) {
                    val addr = list[0]
                    formatAddress(addr)
                } else null
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lng, 1)
                if (!list.isNullOrEmpty()) {
                    val addr = list[0]
                    formatAddress(addr)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatAddress(addr: android.location.Address): String {
        val full = addr.getAddressLine(0)
        if (!full.isNullOrBlank()) {
            return full.replace("中国", "")
        }
        val sb = StringBuilder()
        addr.adminArea?.let { sb.append(it) }
        addr.locality?.let { if (!sb.contains(it)) sb.append(it) }
        addr.subLocality?.let { sb.append(it) }
        addr.thoroughfare?.let { sb.append(it) }
        addr.subThoroughfare?.let { sb.append(it) }
        addr.featureName?.let { if (!sb.contains(it)) sb.append(it) }
        return if (sb.isNotEmpty()) sb.toString() else "位置获取成功"
    }
}
