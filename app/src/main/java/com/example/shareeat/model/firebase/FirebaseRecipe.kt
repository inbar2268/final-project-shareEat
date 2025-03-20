package com.example.shareeat.model.firebase


import android.util.Log
import com.example.shareeat.base.Constants
import com.example.shareeat.base.EmptyCallback
import com.example.shareeat.base.RecipesCallback
import com.example.shareeat.extensions.toFirebaseTimestamp
import com.example.shareeat.model.Recipe
import com.google.firebase.firestore.DocumentChange


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

    fun addRecipeChangeListener(callback: (Recipe, DocumentChange.Type) -> Unit) {
        firebaseModel.database.collection(Constants.Collections.RECIPES)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("RecipeListener", "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d("RecipeListener", "New recipe: ${dc.document.data}")
                            val addedRecipe = Recipe.fromJSON(dc.document.data)
                            callback(addedRecipe, DocumentChange.Type.ADDED)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("RecipeListener", "Modified recipe: ${dc.document.data}")
                            val modifiedRecipe = Recipe.fromJSON(dc.document.data)
                            callback(modifiedRecipe, DocumentChange.Type.MODIFIED)
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d("RecipeListener", "Removed recipe: ${dc.document.data}")
                            val removedRecipe = Recipe.fromJSON(dc.document.data)
                            callback(removedRecipe, DocumentChange.Type.REMOVED)
                        }
                    }
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
        if (recipe.id.isNullOrEmpty()) {
            Log.e("FirebaseRecipe", "Cannot update recipe with empty ID")
            callback()
            return
        }
        Log.d("FirebaseRecipe", "Updating recipe with ID: ${recipe.id}")
        firebaseModel.database.collection(Constants.Collections.RECIPES)
            .document(recipe.id)
            .update(recipe.json)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseRecipe", "Recipe updated: ${recipe.id}")
                } else {
                    Log.e("FirebaseUser", "Error updating recipe", it.exception)
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseRecipe", "Error updating recipe", exception)
                callback() // Call the callback even on failure, but handle it within the callback
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
