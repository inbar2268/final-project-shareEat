package com.example.shareeat.model.firebase

import android.util.Log
import com.example.shareeat.base.Constants
import com.example.shareeat.base.UsersCallback
import com.example.shareeat.base.EmptyCallback
import com.example.shareeat.model.User
import com.example.shareeat.extensions.toFirebaseTimestamp

class FirebaseUser(private val firebaseModel: FirebaseModel) {

    fun getAllUsers(sinceLastUpdated: Long, callback: UsersCallback) {
        firebaseModel.database.collection(Constants.Collections.USERS)
            .whereGreaterThanOrEqualTo(User.LAST_UPDATED, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val users: MutableList<User> = mutableListOf()
                    it.result?.forEach { document ->
                        users.add(User.fromJSON(document.data))
                    }
                    Log.d("FirebaseUser", "Users fetched: ${users.size}")
                    callback(users)
                } else {
                    Log.e("FirebaseUser", "Error fetching users", it.exception)
                    callback(emptyList())
                }
            }
    }

    fun add(user: User, callback: EmptyCallback) {
        firebaseModel.database.collection(Constants.Collections.USERS)
            .document(user.id)
            .set(user.json)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseUser", "User added: ${user.id}")
                } else {
                    Log.e("FirebaseUser", "Error adding user", it.exception)
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUser", "Error adding user", exception)
                callback() // Call the callback even on failure, but handle it within the callback
            }
    }

    fun delete(user: User, callback: EmptyCallback) {
        firebaseModel.database.collection(Constants.Collections.USERS)
            .document(user.id)
            .delete()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseUser", "User deleted: ${user.id}")
                } else {
                    Log.e("FirebaseUser", "Error deleting user", it.exception)
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUser", "Error deleting user", exception)
                callback() // Call the callback even on failure, but handle it within the callback
            }
    }
}
