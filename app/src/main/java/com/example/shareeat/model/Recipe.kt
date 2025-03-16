package com.example.shareeat.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.example.shareeat.base.MyApplication

@Entity
data class Recipe(
    @PrimaryKey val id: String = "",
    var title: String = "",
    var description: String = "",
    var imageUrl: String? = null,
    var instructions: String = "",
    val userId: String = "",
    val userName: String = "",
    var timestamp: Long? = null,
    var lastUpdated: Long? = null,

    var latitude: Double? = null,
    var longitude: Double? = null,
    var geohash: String? = null
) {

    companion object {
        var lastUpdated: Long
            get() = MyApplication.Globals.context?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                ?.getLong(LOCAL_LAST_UPDATED, 0) ?: 0
            set(value) {
                MyApplication.Globals.context
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)?.apply {
                        edit().putLong(LOCAL_LAST_UPDATED, value).apply()
                    }
            }

        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val DESCRIPTION_KEY = "description"
        const val IMAGE_URL_KEY = "imageUrl"
        const val INSTRUCTIONS_KEY = "instructions"
        const val USER_ID_KEY = "userId"
        const val USER_NAME_KEY = "userName"
        const val TIMESTAMP = "timestamp"
        const val LAST_UPDATED = "lastUpdated"
        const val LOCAL_LAST_UPDATED = "localRecipeLastUpdated"

        const val LATITUDE_KEY = "latitude"
        const val LONGITUDE_KEY = "longitude"
        const val GEOHASH_KEY = "geohash"

        fun fromJSON(json: Map<String, Any>): Recipe {
            val id = json[ID_KEY] as? String ?: ""
            val title = json[TITLE_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val imageUrl = json[IMAGE_URL_KEY] as? String
            val instructions = json[INSTRUCTIONS_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val userName = json[USER_NAME_KEY] as? String ?: ""

            val timestampValue = json[TIMESTAMP] as? Timestamp
            val lastUpdatedValue = json[LAST_UPDATED] as? Timestamp

            val latitude = json[LATITUDE_KEY] as? Double
            val longitude = json[LONGITUDE_KEY] as? Double
            val geohash = json[GEOHASH_KEY] as? String

            return Recipe(
                id = id,
                title = title,
                description = description,
                imageUrl = imageUrl,
                instructions = instructions,
                userId = userId,
                userName = userName,
                timestamp = timestampValue?.toDate()?.time,
                lastUpdated = lastUpdatedValue?.toDate()?.time,
                latitude = latitude,
                longitude = longitude,
                geohash = geohash
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            TITLE_KEY to title,
            DESCRIPTION_KEY to description,
            IMAGE_URL_KEY to (imageUrl ?: ""),
            INSTRUCTIONS_KEY to instructions,
            USER_ID_KEY to userId,
            USER_NAME_KEY to userName,
            TIMESTAMP to FieldValue.serverTimestamp(),
            LAST_UPDATED to FieldValue.serverTimestamp(),

            LATITUDE_KEY to (latitude ?: 0.0),
            LONGITUDE_KEY to (longitude ?: 0.0),
            GEOHASH_KEY to (geohash ?: "")
        )
}
