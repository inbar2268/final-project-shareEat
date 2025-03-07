package com.example.shareeat.model

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.shareeat.model.dao.AppLocalDbRepository
import com.example.shareeat.model.dao.AppLocalDb
import java.util.concurrent.Executors


typealias UsersCallback = (List<User>) -> Unit
typealias UserCallback = (User) -> Unit
typealias EmptyCallback = () -> Unit

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    private var executor = Executors.newSingleThreadExecutor()

    companion object {
        val shared = Model()

    }

    fun getAllUsers(callback: UsersCallback) {
        executor.execute {
            val users = database.userDao().getAllUsers()
            mainHandler.post {
                callback(users)
            }
        }
    }

    fun getUserById(userId: String, callback: UserCallback) {
        executor.execute {
            val user = database.userDao().getUserById(userId)
            mainHandler.post {
                callback(user)
            }
        }
    }

    fun addUser(user: User, callback: EmptyCallback) {
        executor.execute {
            database.userDao().insertUser(user)
            mainHandler.post {
                callback()
            }
        }
    }

    fun editUser(user: User ,oldUserId: String, callback: EmptyCallback) {
        executor.execute {
            database.userDao().updateUser(oldUserId,user.firstName,user.id,user.lastName,user.email,user.password)
            mainHandler.post {
                callback()
            }
        }
    }

    fun deleteUser(userId: String, callback: EmptyCallback) {
        executor.execute {
            database.userDao().deleteUser(userId)
            mainHandler.post {
                callback()
            }
        }
    }
}