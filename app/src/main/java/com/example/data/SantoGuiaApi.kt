package com.example.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SantoGuiaApi {

    @GET("api/clerigos/")
    suspend fun getClerigos(
        @Query("search") search: String? = null
    ): List<NetworkClerigo>

    @GET("api/clerigos/{slug}/")
    suspend fun getClerigoDetail(
        @Path("slug") slug: String
    ): NetworkClerigo

    @GET("api/igrejas/")
    suspend fun getIgrejas(
        @Query("bairro") bairro: String? = null
    ): List<NetworkIgreja>

    @GET("api/igrejas/{slug}/")
    suspend fun getIgrejaDetail(
        @Path("slug") slug: String
    ): NetworkIgreja

    @GET("api/celebracoes/")
    suspend fun getCelebracoes(
        @Query("categoria") categoria: String? = null,
        @Query("dia") dia: String? = null
    ): List<NetworkTipoCelebracao>
}
