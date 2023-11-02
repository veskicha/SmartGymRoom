package com.example.smartgymroom

import android.util.Log

class Room () {
    data class Point(val lon: Double, val lat: Double)  //lon --> x, lat --> y
    data class Vector(val lon: Double, val lat: Double)

    //TODO: updates point to coordinates of corners of room (clockwise)
    val roomCorners1 = listOf(
        Point(6.8516624, 52.2434713),
        Point(6.8516949, 52.2435097),
        Point(6.8517593, 52.2434828),
        Point(6.8517284, 52.2434469)
    )
    val roomCorners2 = listOf(
        Point(6.8516540, 52.2434651),
        Point(6.8517157, 52.2434397),
        Point(6.8516848, 52.2434089),
        Point(6.8516198, 52.243402)
    )
    val roomCorners3 = listOf(
        Point(1.0, 1.0),
        Point(4.0, 1.0),
        Point(4.0, 3.0),
        Point(1.0, 3.0)
    )

    val rooms = listOf(roomCorners1, roomCorners2)

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
        var n = 1
        rooms.forEach { room ->
            if (isPointInsideRoom(
                    room,
                    position
                )
            ) {  // This will print true because the point is inside the rotated rectangle.
                Log.d("Found room", n.toString())
                return n
            } else {
                n = n + 1
            }
        }
        return -1
    }

    //getRoom(
}