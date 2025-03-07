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
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE id =:id")
    fun getUserById(id: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg users: User)

    @Query("DELETE FROM User WHERE id= :id ")
    fun deleteUser(id: String)

    @Query("UPDATE User SET firstName= :firstName, lastName= :lastName, email= :email , password= :password ,id= :id  WHERE id= :oldStudentId ")
    fun updateUser(oldStudentId:String,firstName:String, id:String, lastName:String, email: String, password: String)
}