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
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val lastUpdated: Long? = null
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
        const val FIRST_NAME_KEY = "firstName"
        const val LAST_NAME_KEY = "lastName"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val LAST_UPDATED = "lastUpdated"
        const val LOCAL_LAST_UPDATED = "locaStudentLastUpdated"

        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: ""
            val firstName = json[FIRST_NAME_KEY] as? String ?: ""
            val lastName = json[LAST_NAME_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val password = json[PASSWORD_KEY] as? String ?: ""
            val timeStamp = json[LAST_UPDATED] as? Timestamp
            val lastUpdatedLongTimestamp = timeStamp?.toDate()?.time
            return User(
                id = id,
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
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

        fun saveUserToLocalStorage(context: Context, user: FirebaseUser?) {
            val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            editor.putString("uid", user?.uid)
            editor.putString("email", user?.email)
            editor.putString("displayName", user?.displayName)
            editor.apply()
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            FIRST_NAME_KEY to firstName,
            LAST_NAME_KEY to lastName,
            EMAIL_KEY to email,
            PASSWORD_KEY to password,
            LAST_UPDATED to FieldValue.serverTimestamp()
        )


}


