package com.example.shareeat.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shareeat.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAllUser(): LiveData<List<User>>

    @Query("SELECT * FROM User WHERE id =:id")
    fun getUserById(id: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg student: User)

    @Delete
    fun delete(student: User)

}