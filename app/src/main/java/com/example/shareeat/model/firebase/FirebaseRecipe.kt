package com.example.shareeat.model.firebase


import android.util.Log
import com.example.shareeat.base.Constants
import com.example.shareeat.base.EmptyCallback
import com.example.shareeat.base.RecipesCallback
import com.example.shareeat.extensions.toFirebaseTimestamp
import com.example.shareeat.model.Recipe


class FirebaseRecipe(private val firebaseModel: FirebaseModel) {


    fun getAllRecipes(sinceLastUpdated: Long, callback: RecipesCallback) {
        firebaseModel.database.collection(Constants.Collections.RECIPES)
            .whereGreaterThanOrEqualTo(Recipe.LAST_UPDATED, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val recipes: MutableList<Recipe> = mutableListOf()
                    task.result?.forEach { document ->
                        recipes.add(Recipe.fromJSON(document.data))
                    }
                    Log.d("FirebaseRecipe", "Recipes fetched: ${recipes.size}")
                    callback(recipes)
                } else {
                    Log.e("FirebaseRecipe", "Error fetching recipes", task.exception)
                    callback(emptyList())
                }
            }
    }



    fun add(recipe: Recipe, callback: EmptyCallback) {
        firebaseModel.database.collection(Constants.Collections.RECIPES)
            .document(recipe.id)
            .set(recipe.json)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseRecipe", "Recipe added: ${recipe.id}")
                } else {
                    Log.e("FirebaseRecipe", "Error adding recipe", it.exception)
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseRecipe", "Error adding recipe", exception)
                callback()
            }
    }

    fun updateRecipe(recipe: Recipe, callback: EmptyCallback) {
        firebaseModel.database.collection(Constants.Collections.RECIPES)
            .document(recipe.id)
            .update(recipe.json)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseRecipe", "Recipe updated: ${recipe.id}")
                } else {
                    Log.e("FirebaseRecipe", "Error updating recipe", it.exception)
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseRecipe", "Error updating recipe", exception)
                callback()
            }
    }


    fun delete(recipe: Recipe, callback: EmptyCallback) {
        firebaseModel.database.collection(Constants.Collections.RECIPES)
            .document(recipe.id)
            .delete()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseRecipe", "Recipe deleted: ${recipe.id}")
                } else {
                    Log.e("FirebaseRecipe", "Error deleting recipe", it.exception)
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseRecipe", "Error deleting recipe", exception)
                callback()
            }
    }
}
