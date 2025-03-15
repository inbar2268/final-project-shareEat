package com.example.shareeat.extensions

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext

fun getCurrentUser( context: android.content.Context ): Pair<String, String>? {
    val sharedPreferences = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getString("uid", "defaultUserId") ?: "defaultUserId"
    val userName = sharedPreferences.getString("displayName", "defaultUserName") ?: "defaultUserName"

    return Pair(userId, userName)
}