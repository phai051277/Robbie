package com.example.firebasesample

import java.util.*

data class Checkin(
    var employeeId: String = "",
    val eventId: Int = 0,
    val eventName: String = "",
    val sobaJoin: Int = 0,
    val eventMonth: Int = 0,
    val registerTime: Date = Date()
)