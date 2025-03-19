package com.example.shareeat.utils

import ch.hsr.geohash.GeoHash
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object GeohashUtils {

    fun getGeohashPrecision(zoomLevel: Float): Int {
        return when {
            zoomLevel >= 17 -> 8
            zoomLevel >= 14 -> 7
            zoomLevel >= 11 -> 6
            zoomLevel >= 9 -> 5
            zoomLevel >= 7 -> 4
            zoomLevel >= 5 -> 3
            zoomLevel >= 3 -> 2
            else -> 1
        }
    }

    fun getGeohashRanges(bounds: LatLngBounds, precision: Int): List<Pair<String, String>> {
        if (precision <= 1) {
            return listOf(Pair("0", "z"))
        }

        val northEast = bounds.northeast
        val southWest = bounds.southwest

        val center = LatLng(
            (northEast.latitude + southWest.latitude) / 2,
            (northEast.longitude + southWest.longitude) / 2
        )

        val northMid = LatLng(northEast.latitude, center.longitude)
        val southMid = LatLng(southWest.latitude, center.longitude)
        val eastMid = LatLng(center.latitude, northEast.longitude)
        val westMid = LatLng(center.latitude, southWest.longitude)
        val southEast = LatLng(southWest.latitude, northEast.longitude)
        val northWest = LatLng(northEast.latitude, southWest.longitude)

        val points = listOf(
            northEast, southWest, center,
            southEast, northWest,
            northMid, southMid, eastMid, westMid
        )

        val geohashes = points.map {
            val fullGeohash = GeoHash.withCharacterPrecision(it.latitude, it.longitude, precision).toBase32()
            fullGeohash.substring(0, minOf(precision, 10)) // Ensure we don't exceed 10 characters
        }

        val prefixLength = when {
            precision <= 2 -> 1
            precision <= 5 -> precision - 1
            else -> precision - 2 // More aggressive prefix reduction for higher precision
        }

        val prefixes = geohashes.map {
            it.substring(0, minOf(prefixLength, it.length))
        }.toSet()

        return prefixes.map { prefix ->
            Pair(prefix + "0", prefix + "z")
        }
    }

    fun isPointInBounds(point: LatLng, bounds: LatLngBounds, usePadding: Boolean = true): Boolean {
        if (!usePadding) return bounds.contains(point)

        val latPadding = (bounds.northeast.latitude - bounds.southwest.latitude) * 0.1
        val lngPadding = (bounds.northeast.longitude - bounds.southwest.longitude) * 0.1

        val paddedBounds = LatLngBounds.Builder()
            .include(LatLng(bounds.northeast.latitude + latPadding, bounds.northeast.longitude + lngPadding))
            .include(LatLng(bounds.southwest.latitude - latPadding, bounds.southwest.longitude - lngPadding))
            .build()

        return paddedBounds.contains(point)
    }

}