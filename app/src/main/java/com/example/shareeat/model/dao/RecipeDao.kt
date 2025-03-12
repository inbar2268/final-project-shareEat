package com.example.shareeat.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shareeat.model.Recipe

@Dao
interface RecipeDao {
    @Query("SELECT * FROM Recipe")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getRecipeById(id: String): LiveData<Recipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg recipes: Recipe)

    @Update
    fun update(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe)

}



