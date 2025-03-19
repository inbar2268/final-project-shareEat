package com.example.shareeat.model

import com.example.shareeat.model.networking.apiRecipe
import com.google.gson.annotations.SerializedName

class Recipes(
    @SerializedName("results")
    val result: List<apiRecipe>,
    @SerializedName("count")
    val totalResult: Int
)