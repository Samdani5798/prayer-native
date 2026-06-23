package com.realislamic.prayertimes.data.remote

import com.realislamic.prayertimes.data.model.AlAdhanResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AlAdhanApiService {

    @GET("v1/timings")
    suspend fun getTimingsByCoordinates(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("timestamp") timestamp: Long? = null
    ): Response<AlAdhanResponse>

    companion object {
        const val BASE_URL = "https://api.aladhan.com/"
    }
}
