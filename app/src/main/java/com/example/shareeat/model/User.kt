package com.example.shareeat.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class User(
    @PrimaryKey var id: String,
    var name: String,
    var phone: String,
    var address: String,
    var isChecked: Boolean
)