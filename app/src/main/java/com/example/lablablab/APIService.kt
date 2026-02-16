package com.example.lablablab

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface APIService {
    @GET("house.json")
    suspend fun getHouse(): House
    @PUT("house.json")
    suspend fun setHouse(@Body house: House): House

}

private val json = Json { ignoreUnknownKeys = true }

//hardcoded url for school upload
private const val URL = "https://softwareengineering-1f92a-default-rtdb.firebaseio.com/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(URL)
    .build()

object HouseApi {
    val retrofitService: APIService by lazy {
        retrofit.create(APIService::class.java)
    }
}
