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
            //right room
            KnownBeacon( "14e67c9b-28c3-4c38-a144-75b62cb7e968", Pair(6.8553846,52.2395310)), //iris old one  rechts voorin
            KnownBeacon("fe2b2084-72d5-4a8d-9e70-e41688af4db5", Pair(6.8553692,52.2394706)), //iris phone
            KnownBeacon("a6b17f61-62c7-4c98-b9fc-0f4cead64a29", Pair(6.855228,52.2394722)), //moto (wel hoesje)
            KnownBeacon("5f6be5f9-39a8-4ec6-8d9d-2e1b8f79d92d", Pair(6.8552438, 52.2395355)))//pixel 4


        //testing room
//        KnownBeacon("14e67c9b-28c3-4c38-a144-75b62cb7e968",Pair(6.8553390,52.2392620)),
//        KnownBeacon("fe2b2084-72d5-4a8d-9e70-e41688af4db5",Pair(6.8552786, 52.2392675)),
//        KnownBeacon("a6b17f61-62c7-4c98-b9fc-0f4cead64a29",Pair(6.8552843, 52.2393123)),
//        KnownBeacon("5f6be5f9-39a8-4ec6-8d9d-2e1b8f79d92d",Pair(6.8553413, 52.2393103)))




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