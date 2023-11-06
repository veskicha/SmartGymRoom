package com.example.smartgymroom

import android.util.Log

class Room () {
    data class Point(val lon: Double, val lat: Double)  //lon --> x, lat --> y
    data class Vector(val lon: Double, val lat: Double)

    //TODO: updates point to coordinates of corners of room (clockwise)
    val roomCorners0 = listOf(
        Point(6.8553735,52.2394911),
        Point(6.8552203,52.2394963),
        Point(6.8552237, 52.2395702),
        Point(6.8553859, 52.2395593)
    )
    val roomCorners1 = listOf(
        Point(6.8553390,52.2392620),
        Point(6.8552786,52.2392675),
        Point(6.8552843, 52.2393123),
        Point(6.8553413,52.2393103)
    )
    val roomCorners2 = listOf(
        Point(1.0, 1.0),
        Point(4.0, 1.0),
        Point(4.0, 3.0),
        Point(1.0, 3.0)
    )

    val rooms = listOf(roomCorners0, roomCorners1)

    fun isPointInsideRoom(roomCorners: List<Point>, point: Point): Boolean {
        if (roomCorners.size != 4) {
            throw IllegalArgumentException("The room must have 4 corners.")
        }

        // Calculate vectors representing the edges of the room
        val AB = Vector(roomCorners[1].lon - roomCorners[0].lon, roomCorners[1].lat - roomCorners[0].lat)
        val BC = Vector(roomCorners[2].lon - roomCorners[1].lon, roomCorners[2].lat - roomCorners[1].lat)
        val CD = Vector(roomCorners[3].lon - roomCorners[2].lon, roomCorners[3].lat - roomCorners[2].lat)
        val DA = Vector(roomCorners[0].lon - roomCorners[3].lon, roomCorners[0].lat - roomCorners[3].lat)

        // Calculate vectors from each corner to the point
        val AP = Vector(point.lon - roomCorners[0].lon, point.lat - roomCorners[0].lat)
        val BP = Vector(point.lon - roomCorners[1].lon, point.lat - roomCorners[1].lat)
        val CP = Vector(point.lon - roomCorners[2].lon, point.lat - roomCorners[2].lat)
        val DP = Vector(point.lon - roomCorners[3].lon, point.lat - roomCorners[3].lat)

        // Calculate dot products
        val dotProductABAP = AB.lon * AP.lon + AB.lat * AP.lat
        val dotProductBCBP = BC.lon * BP.lon + BC.lat * BP.lat
        val dotProductCDCP = CD.lon * CP.lon + CD.lat * CP.lat
        val dotProductDADP = DA.lon * DP.lon + DA.lat * DP.lat

        // Check if all dot products are non-negative
        return dotProductABAP >= 0 && dotProductBCBP >= 0 && dotProductCDCP >= 0 && dotProductDADP >= 0
    }

    fun getRoom(location : Location.Point): Int {
        var position = Point(location.y, location.x)
        var n = 0
        rooms.forEach { room ->
            if (isPointInsideRoom(
                    room,
                    position
                )
            ) {  // This will print true because the point is inside the rotated rectangle.
                Log.d("Found room", n.toString())
                return n
            } else {
                n = n+1
            }
        }
        Log.d("Found no room", n.toString())

        return 0
    }

    //getRoom(
}