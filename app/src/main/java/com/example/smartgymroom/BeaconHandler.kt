package com.example.smartgymroom

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter

class BeaconHandler(val context: Context, private val viewModel: BeaconViewModel) {

    private val beaconManager = BeaconManager.getInstanceForApplication(context)
    val beaconParser = BeaconParser()
        .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
//    val beaconParser2 =

    private val TAG = "BeaconInfo"
    val region = Region("all-beacons-region", null, null, null)

    fun startBeaconMonitoring() {
        beaconManager.foregroundScanPeriod = 500L
        beaconManager.foregroundBetweenScanPeriod = 200L
        beaconManager.beaconParsers.add(beaconParser)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        beaconManager.getRegionViewModel(region).regionState.observe(
            context as LifecycleOwner,
            monitoringObserver
        )
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(
            context as LifecycleOwner,
            rangingObserver
        )
        beaconManager.startMonitoring(region)
        Log.d(TAG, "Started monitoring beacons")
        beaconManager.startRangingBeacons(region)
        Log.d(TAG, "Started ranging beacons")
    }

    fun stopBeaconMonitoring() {
        beaconManager.stopMonitoring(region)
        Log.d(TAG, "Stopped monitoring beacons")
        beaconManager.stopRangingBeacons(region)
        Log.d(TAG, "Stopped ranging beacons")

    }

    private val monitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.INSIDE) {
            Log.d(TAG, "Detected beacons(s)")
        } else {
            Log.d(TAG, "Stopped detecting beacons")
        }
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(TAG, "Ranged: ${beacons.count()} beacons")
        if (!beacons.isEmpty()) {
            viewModel.updateRangedBeacons(beacons)
        }
        for (beacon: Beacon in beacons) {
            Log.d(TAG, "$beacon about ${beacon.distance} meters away")
        }
    }


    private val beaconTransmitter: BeaconTransmitter = BeaconTransmitter(context, beaconParser)
    fun createBeacon() {
        Log.d(TAG, "Creating beacon...")
        val beacon = Beacon.Builder()
            .setId1("5f6be5f9-39a8-4ec6-8d9d-2e1b8f79d92d")
            .setId2("1")
            .setId3("2")
            .setManufacturer(0x0118)
            .setTxPower(-59)
            .setDataFields(listOf(0L))
            .build()

        beaconTransmitter.startAdvertising(beacon)
        Log.d(TAG, "Beacon created, advertising presence now.")
    }

//    14e67c9b-28c3-4c38-a144-75b62cb7e968 //iris old one
//    fe2b2084-72d5-4a8d-9e70-e41688af4db5 //iris phone
//    a6b17f61-62c7-4c98-b9fc-0f4cead64a29 //moto (wel hoesje)
//    5f6be5f9-39a8-4ec6-8d9d-2e1b8f79d92d //no hoesje pixel 4
}
