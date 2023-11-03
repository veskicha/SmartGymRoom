package com.example.smartgymroom

import org.altbeacon.beacon.Beacon
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class KnownBeacon(
    val address: String,
    val location: Pair<Double, Double>
)

class BeaconData {
    fun getKnownBeacons(): List<KnownBeacon> {
        return listOf(
//            KnownBeacon ("CA:02:32:D2:A1:83", Pair(6.85626633407474, 52.2393690021734)),
//            KnownBeacon("CD:79:35:3F:C3:8B", Pair(6.85621997115836, 52.2393262674391)),
//            KnownBeacon( "D4:50:EF:62:52:77", Pair(6.85653661335243, 52.2392316486003)))
//            KnownBeacon( "dc:6a:e7:bb:7f:9f", Pair(6.85653661335243, 52.2392316486003)),//Iris redmi 11
            KnownBeacon( "14e67c9b-28c3-4c38-a144-75b62cb7e968", Pair(6.8525860, 52.2432192)), //iris old one  right corner
//            KnownBeacon("fe2b2084-72d5-4a8d-9e70-e41688af4db5", Pair(6.8526283, 52.2432687)), //iris phone left corner
            KnownBeacon("a6b17f61-62c7-4c98-b9fc-0f4cead64a29", Pair(6.8524583, 52.2432535)), //moto (wel hoesje)
            KnownBeacon("5f6be5f9-39a8-4ec6-8d9d-2e1b8f79d92d", Pair(6.85621997115836, 52.2393262674391)))//pixel 4 (no hoesje)




    }

    /**
     * Compare the beacons found by the app when pressing 'Locate'
     * with the beacons we know, and return the ones that match,
     * resulting a list of beacons that are in range and can be
     * used for squaring or circling.
     */
    fun compareBeacons(foundBeacons: Map<String, Double>): MutableMap<KnownBeacon, Double> {
        val knownBeacons: List<KnownBeacon> = getKnownBeacons()
        val matchingBeacons = mutableMapOf<KnownBeacon, Double>()

        for (beacon in knownBeacons) {
            if (beacon.address in foundBeacons.keys)
                matchingBeacons.put(beacon, foundBeacons[beacon.address]!!)
        }
        return matchingBeacons
    }
}