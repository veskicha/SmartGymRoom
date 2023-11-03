package com.example.smartgymroom

class Location {
    data class Circle(val center: Point, val radius: Double)

    data class Point(val x: Double, val y: Double)

    data class Rectangle(val center: Point, val width: Double, val height: Double)


    private fun getCircumcenter(circle1: Circle, circle2: Circle, circle3: Circle): Point? {
        val x1 = -circle1.center.x
        val y1 = -circle1.center.y
        val r1 = circle1.radius

        val x2 = -circle2.center.x
        val y2 = -circle2.center.y
        val r2 = circle2.radius

        val x3 = -circle3.center.x
        val y3 = -circle3.center.y
        val r3 = circle3.radius

        val A = x2 - x1
        val B = y2 - y1
        val C = x3 - x1
        val D = y3 - y1

        val E = (r2 * r2 - r1 * r1 - x2 * x2 + x1 * x1 - y2 * y2 + y1 * y1)
        val F = (r3 * r3 - r1 * r1 - x3 * x3 + x1 * x1 - y3 * y3 + y1 * y1)

        val denominator = 2 * (A * D - B * C)

        if (denominator == 0.0) {
            return null // Circles do not intersect at a single point
        }

        val centerX = (D * E - B * F) / denominator
        val centerY = (-C * E + A * F) / denominator

        return Point(centerX, centerY)
    }

    fun getIntersection(
        beacon1: KnownBeacon,
        beacon2: KnownBeacon,
        beacon3: KnownBeacon,
        closeBeacons: MutableMap<KnownBeacon, Double>
    ): Point? {
        val circle1 = Circle(getLocationBeacon(beacon1), convertDistance(beacon1, closeBeacons)!!)
        val circle2 = Circle(getLocationBeacon(beacon2), convertDistance(beacon2, closeBeacons)!!)
        val circle3 = Circle(getLocationBeacon(beacon3), convertDistance(beacon3, closeBeacons)!!)

        val circumcenter = getCircumcenter(circle1, circle2, circle3)

        if (circumcenter != null) {
            return Point(circumcenter.x, circumcenter.y)
        } else {
            return null
        }
    }

    private fun getRectangleIntersection(rect1: Rectangle, rect2: Rectangle): Rectangle? {
        val point1 = rect1.center
        val point2 = rect2.center
        val x1 = java.lang.Double.max(
            point1.x - (rect1.width / 2),
            point2.x - (rect2.width / 2)
        ) //max(2,1) 2
        val y1 = java.lang.Double.max(
            point1.y - (rect1.height / 2),
            point2.y - (rect2.height / 2)
        ) // max(0,3)3
        val x2 = java.lang.Double.min(
            point1.x + (rect1.width / 2),
            point2.x + (rect2.width / 2)
        ) // min(6,3)3
        val y2 = java.lang.Double.min(
            point1.y + (rect1.height / 2),
            point2.y + (rect2.height / 2)
        )//min (4,5)4

        if (x1 < x2 && y1 < y2) {
            val width = x2 - x1
            val height = y2 - y1
            return Rectangle(Point(x1 + (width / 2), y1 + (height / 2)), width, height)
        }

        return null
    }

    fun getRectanglesIntersection(
        beacon1: KnownBeacon,
        beacon2: KnownBeacon,
        beacon3: KnownBeacon,
        closeBeacons: MutableMap<KnownBeacon, Double>
    ): Point {
//        val square1 = Rectangle(0.0, 4.0, 4.0, 4.0) //TODO: use convertDistance for the width and height
//        val square2 = Rectangle(1.0, 1.0, 2.0,2.0)
//        val square3 = Rectangle(4.0, 2.0, 2.0,2.0)

        val dist1 = convertDistance(beacon1, closeBeacons)
        val dist2 = convertDistance(beacon2, closeBeacons)
        val dist3 = convertDistance(beacon3, closeBeacons)

        val square1 = Rectangle(getLocationBeacon(beacon1), dist1!!, dist1)
        val square2 = Rectangle(getLocationBeacon(beacon2), dist2!!, dist2)
        val square3 = Rectangle(getLocationBeacon(beacon2), dist3!!, dist3)

        // Find the intersection rectangle between the first two squares
        val intersect12 = getRectangleIntersection(square1, square2)

        // Calculate the intersection between the third square and the intersection rectangle from the previous step
        if (intersect12 != null) {
            val intersect3 = getRectangleIntersection(intersect12, square3)

            if (intersect3 != null) {
                println("Intersection Rectangle:")
                return intersect3.center
            } else {
//                    return("No intersection between the three squares.")
                val squareU1 = scaleUp(square1)
                val squareU2 = scaleUp(square2)
                val squareU3 = scaleUp(square3)

                return getRectangleIntersectFromSquares(squareU1, squareU2, squareU3)
            }
        } else {
            val squareU1 = scaleUp(square1)
            val squareU2 = scaleUp(square2)
            val squareU3 = scaleUp(square3)

            return getRectangleIntersectFromSquares(squareU1, squareU2, squareU3)
        }
    }

    private fun getRectangleIntersectFromSquares(
        square1: Rectangle,
        square2: Rectangle,
        square3: Rectangle
    ): Point {
        val intersect12 = getRectangleIntersection(square1, square2)

        // Calculate the intersection between the third square and the intersection rectangle from the previous step
        if (intersect12 != null) {
            val intersect3 = getRectangleIntersection(intersect12, square3)

            if (intersect3 != null) {
                println("Intersection Rectangle:")
                return intersect3.center
            } else {
                val squareU1 = scaleUp(square1)
                val squareU2 = scaleUp(square2)
                val squareU3 = scaleUp(square3)
                return getRectangleIntersectFromSquares(squareU1, squareU2, squareU3)

            }
        } else {
            val squareU1 = scaleUp(square1)
            val squareU2 = scaleUp(square2)
            val squareU3 = scaleUp(square3)
            return getRectangleIntersectFromSquares(squareU1, squareU2, squareU3)

        }
    }


    fun getLocationBeacon(beacon: KnownBeacon): Point {
        val (lat, lang) = beacon.location
        return Point(lat, lang)
    }

    private fun scaleUp(rect: Rectangle): Rectangle {
        return Rectangle(rect.center, (rect.width * 1.05), (rect.height * 1.05))
    }

    fun getOurLocation(closeBeacons: MutableMap<KnownBeacon, Double>): Point? {
        if (closeBeacons.size < 3) {
            return null
        }

        val (beacon1, beacon2, beacon3) = getThreeClosest(closeBeacons)

        /**if (atBeacon(beacon1,closeBeacons) ){
        return getLocationBeacon(beacon1)
        }
        else if(atBeacon(beacon2,closeBeacons)){
        return getLocationBeacon(beacon2)
        }
        else if(atBeacon(beacon3,closeBeacons)){
        return getLocationBeacon(beacon3)
        }*/
        var ourLocation: Point?

        ourLocation = getIntersection(beacon1, beacon2, beacon3, closeBeacons)

        if (ourLocation == null) {
            ourLocation = getRectanglesIntersection(beacon1, beacon2, beacon3, closeBeacons)
        }

        return ourLocation
    }

    private fun getThreeClosest(closeBeacons: MutableMap<KnownBeacon, Double>): Triple<KnownBeacon, KnownBeacon, KnownBeacon> {
        val three = closeBeacons.entries.sortedBy { it.value }.take(3)
        val first = three[0].key
        val second = three[1].key
        val third = three[2].key

        val closestThree = Triple(first, second, third)
        return closestThree
    }


    private fun convertDistance(
        beacon: KnownBeacon,
        closeBeacons: MutableMap<KnownBeacon, Double>
    ): Double? {
        return ((closeBeacons.get(beacon))?.div(111139))
    }
    //private fun atBeacon(beacon: KnownBeacon, closeBeacons: MutableMap<KnownBeacon, Double>): Boolean{
    //   return ((closeBeacons.get(beacon))!! < 0.3)
    //}


}