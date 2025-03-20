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
    fun getRecipeById(id: String): Recipe

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg recipes: Recipe)

    @Update
    fun update(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe)

    @Query("SELECT id FROM Recipe")
    fun getAllRecipeIds(): List<String>

    @Query("DELETE FROM Recipe WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)
}



