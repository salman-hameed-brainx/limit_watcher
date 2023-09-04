package com.brainx.limitwatcher

data class RoadSpeedLimitResponse(
    val snappedPoints: List<SnappedPoint>,
    val speedLimits: List<SpeedLimit>,
    val warningMessage: String
)