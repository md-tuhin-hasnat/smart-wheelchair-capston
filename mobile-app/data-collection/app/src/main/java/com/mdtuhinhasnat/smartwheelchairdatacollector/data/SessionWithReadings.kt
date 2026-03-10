package com.mdtuhinhasnat.smartwheelchairdatacollector.data

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithReadings(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val readings: List<SensorReadingEntity>
)
