package com.example.shareeat.model.networking
import com.example.shareeat.model.Instruction
import com.google.gson.annotations.SerializedName

class apiRecipe(
    @SerializedName("name")
    var title: String = "",
    @SerializedName("thumbnail_url")
    var imageUrl: String? = null,
    @SerializedName("description")
    var description: String = "",
    @SerializedName("instructions")
    var instructions: List<Instruction> = listOf()

)
