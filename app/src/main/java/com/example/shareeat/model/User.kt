package com.example.shareeat.model

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.example.shareeat.base.MyApplication
import com.google.firebase.auth.FirebaseUser

@Entity
data class User(
    @PrimaryKey val id: String,
    var displayName: String,
    var email: String,
    val password: String,
    var photoUrl: String,
    var lastUpdated: Long? = null
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
        const val DISPLAY_NAME_KEY = "displayName"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val PHOTO_URL_KEY = "photoUrl"
        const val LAST_UPDATED = "lastUpdated"
        const val LOCAL_LAST_UPDATED = "locaStudentLastUpdated"

        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val displayName = json[DISPLAY_NAME_KEY] as? String ?: ""
            val photoUrl = json[PHOTO_URL_KEY]  as? String ?: ""
            val password = json[PASSWORD_KEY] as? String ?: ""
            val timeStamp = json[LAST_UPDATED] as? Timestamp
            val lastUpdatedLongTimestamp = timeStamp?.toDate()?.time
            return User(
                id = id,
                displayName = displayName,
                email = email,
                password = password,
                photoUrl = photoUrl,
                lastUpdated = lastUpdatedLongTimestamp
            )
        }

        fun getUserFromLocalStorage(context: Context): Map<String, String?> {
            val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            return mapOf(
                "uid" to sharedPref.getString("uid", null),
                "email" to sharedPref.getString("email", null),
                "displayName" to sharedPref.getString("displayName", null),
                )
        }

        fun clearAllUserData(context: Context) {
            val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()
        }

        fun saveUserToLocalStorage(context: Context, uid: String, email: String, displayName: String) {
            val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            editor.putString("uid", uid)
            editor.putString("email", email)
            editor.putString("displayName", displayName)
            editor.apply()
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            DISPLAY_NAME_KEY to displayName,
            EMAIL_KEY to email,
            PASSWORD_KEY to password,
            PHOTO_URL_KEY to photoUrl,
            LAST_UPDATED to FieldValue.serverTimestamp()
        )


}


