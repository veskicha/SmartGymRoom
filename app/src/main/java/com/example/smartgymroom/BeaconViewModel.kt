package com.example.smartgymroom

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.altbeacon.beacon.Beacon

class BeaconViewModel : ViewModel() {

    private val _rangedBeacons: MutableLiveData<Collection<Beacon>> = MutableLiveData(emptyList())
    private val _allSeenBeacons: MutableList<String> = mutableListOf()
    private val _currentRangedBeacons = mutableMapOf<String, Double>()
    private var _isBeaconSearchStarted = MutableStateFlow(false)
    val rangedBeacons: LiveData<Collection<Beacon>> get() = _rangedBeacons
    val allSeenBeacons: MutableList<String> get() = _allSeenBeacons
    val currentRangedBeacons = _currentRangedBeacons
    val isBeaconSearchStarted: StateFlow<Boolean> = _isBeaconSearchStarted
    val beaconData = BeaconData()
    val listOfKnownBeacons: MutableList<KnownBeacon> = mutableListOf()
    private var comparedBeaconsList: MutableMap<KnownBeacon, Double> = mutableMapOf<KnownBeacon, Double>()

    fun updateRangedBeacons(beacons: Collection<Beacon>) {
        _rangedBeacons.postValue(beacons)
        for (beacon in beacons) {
//            if (beacon.bluetoothAddress != "D4:50:EF:62:52:77"```) {
            if (_currentRangedBeacons[beacon.id1.toString()] != null) {
                _currentRangedBeacons[beacon.id1.toString()] =
                    (beacon.distance + _currentRangedBeacons[beacon.id1.toString()]!!) / 2
            } else {
                _currentRangedBeacons[beacon.id1.toString()] = beacon.distance
            }

        }
//        }
        comparedBeaconsList = beaconData.compareBeacons(_currentRangedBeacons)

        for (beacon in comparedBeaconsList) {
            if (!_allSeenBeacons.contains(beacon.key.address)) {
                _allSeenBeacons.add(beacon.key.address)
            }
        }

    }

    fun getComparedBeacons() {
        var comparedBeacons = beaconData.compareBeacons(_currentRangedBeacons)

        for (key in comparedBeacons.keys) {
            Log.d("beaconKnown", "$key")
            listOfKnownBeacons.add(key)
            Log.d("beaconKnown", "new list $listOfKnownBeacons")
        }
    }

    fun setBeaconsState(newState: Boolean) {
        _isBeaconSearchStarted.value = newState
    }

    fun deleteBeacons() {
        _currentRangedBeacons.clear()
        _allSeenBeacons.clear()
        listOfKnownBeacons.clear()
    }

    fun getIsBeaconSearchStarted():Boolean{
        return isBeaconSearchStarted.value;
    }

    fun getComparedBeaconsList(): MutableMap<KnownBeacon, Double> {
        return comparedBeaconsList

    }
}