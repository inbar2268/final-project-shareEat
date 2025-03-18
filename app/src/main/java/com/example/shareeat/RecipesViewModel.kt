package com.example.shareeat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.example.shareeat.model.Recipes

class RecipesViewModel : ViewModel() {

    var recipes: LiveData<List<Recipe>> = Model.shared.recipes
    val apiRecipes: LiveData<List<Recipe>> = Model.shared.apiRecipes
    fun refreshAllRecipes() {
        Model.shared.refreshAllRecipes()
    }

    fun fechRecipes(){
        Model.shared.getAllApiRecipes()
    }

}