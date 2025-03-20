package com.example.shareeat.model.networking

import com.example.shareeat.model.Recipes
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface RecipesApi {
    @GET("recipes/list")
    fun getRecipes(
        @Query("from") from: Int = 0,
        @Query("size") size: Int = 3,
        @Query("tags") tags: String? = "under_30_minutes"
    ): Call<Recipes>
}