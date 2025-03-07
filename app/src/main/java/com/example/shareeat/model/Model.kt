package com.example.shareeat.model

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.shareeat.base.EmptyCallback
import com.example.shareeat.model.dao.AppLocalDb
import com.example.shareeat.model.dao.AppLocalDbRepository
import com.example.shareeat.model.firebase.FirebaseModel
import java.util.concurrent.Executors
import com.example.shareeat.model.firebase.FirebaseUser

class Model private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }

    enum class Storage {
        FIREBASE,
        CLOUDINARY
    }

    private val database: AppLocalDbRepository = AppLocalDb.database
    private var executor = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    val users: LiveData<List<User>> = database.userDao().getAllUser()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()

    private val firebaseModel = FirebaseModel()
    private val firebaseUser = FirebaseUser(firebaseModel)

    companion object {
        val shared = Model()
    }

    fun refreshAllUsers() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = User.lastUpdated
        firebaseUser.getAllUsers(lastUpdated) { users ->
            executor.execute {
                var currentTime = lastUpdated
                for (user in users) {
                    database.userDao().insertAll(user)
                    user.lastUpdated?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }

                User.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun add(user: User, storage: Storage, callback: EmptyCallback) {
        firebaseUser.add(user) {
            callback()
        }
    }

    private fun uploadTo(storage: Storage, image: Bitmap, name: String, callback: (String?) -> Unit) {
        when (storage) {
            Storage.FIREBASE -> {
                uploadImageToFirebase(image, name, callback)
            }
            Storage.CLOUDINARY -> {
            }
        }
    }

    fun delete(user: User, callback: EmptyCallback) {
        firebaseUser.delete(user, callback)
    }

    private fun uploadImageToFirebase(
        image: Bitmap,
        name: String,
        callback: (String?) -> Unit
    ) {
        firebaseModel.uploadImage(image, name, callback)
    }



}