package com.example.shareeat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe

class RecipesViewModel : ViewModel() {

    var recipes: LiveData<List<Recipe>> = Model.shared.recipes

    fun refreshAllRecipes() {
        Model.shared.refreshAllRecipes()
    }

}