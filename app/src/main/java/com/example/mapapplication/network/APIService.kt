package com.example.mapapplication.network

import com.example.mapapplication.data.request.UserRequest
import com.example.mapapplication.data.response.LocationDetailResponse
import com.example.mapapplication.data.response.LocationSearchResponse
import com.example.mapapplication.data.response.RouteResponse
import com.example.mapapplication.data.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {

    @POST("/v4/auth/login-with-password")
    suspend fun login(
        @Body userRequest: UserRequest
    ): Response<UserResponse>

    @GET("/v5/api/routing/search-route")
    suspend fun searchRoute(
        @Query("bearings") bearings: Int,
        @Query("dstLat") dstLat: Double,
        @Query("dstLon") dstLng: Double,
        @Query("srcLat") srcLat: Double,
        @Query("srcLon") srcLng: Double,
    ) : Response<RouteResponse>

    @GET("/v5/Place/AutoComplete")
    suspend fun searchLocation(
        @Query("input") input: String,
    ): Response<LocationSearchResponse>

    @GET("/v5/Place/Detail")
    suspend fun detailLocation(
        @Query("place_id") placeId: String,
    ): Response<LocationDetailResponse>
}