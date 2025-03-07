package com.example.shareeat.model.firebase

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.example.shareeat.base.Constants
import com.example.shareeat.base.EmptyCallback
import com.example.shareeat.base.UsersCallback
import com.example.shareeat.extensions.toFirebaseTimestamp
import com.example.shareeat.model.User
import java.io.ByteArrayOutputStream

class FirebaseUser {

    private val database = Firebase.firestore
    private val storage = Firebase.storage

    init {

        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun getAllUsers(sinceLastUpdated: Long, callback: UsersCallback) {

        database.collection(Constants.Collections.USERS)
            .whereGreaterThanOrEqualTo(User.LAST_UPDATED, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val students: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            students.add(User.fromJSON(json.data))
                        }
                        Log.d("TAG", students.size.toString())
                        callback(students)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun add(user: User, callback: EmptyCallback) {
        database.collection(Constants.Collections.USERS).document(user.id).set(user.json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun delete(user: User, callback: EmptyCallback) {

    }

    fun uploadImage(image: Bitmap, name: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/$name.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            callback(null)
        }.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }
    }
}