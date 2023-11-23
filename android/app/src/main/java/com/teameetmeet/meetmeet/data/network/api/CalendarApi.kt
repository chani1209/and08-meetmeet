package com.teameetmeet.meetmeet.data.network.api

import com.teameetmeet.meetmeet.data.network.entity.EventResponse
import com.teameetmeet.meetmeet.data.network.entity.Events
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarApi {
    @GET("event")
    suspend fun getEvents(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Events

    @GET("event/search")
    suspend fun searchEvents(
        @Query("keyword") keyword: String?,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): List<EventResponse>
}