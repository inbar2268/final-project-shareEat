package com.example.shareeat.model

import com.google.gson.annotations.SerializedName

data class Instruction(
    @SerializedName("display_text")
    val displayText: String = "",
    @SerializedName("position")
    val position: Int = 0,
    @SerializedName("start_time")
    val startTime: Int? = null,
    @SerializedName("end_time")
    val endTime: Int? = null,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("temperature")
    val temperature: String? = null,
    @SerializedName("appliance")
    val appliance: String? = null
)