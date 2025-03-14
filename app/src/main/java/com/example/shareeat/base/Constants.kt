package com.example.shareeat.base

import com.example.shareeat.model.Recipe
import com.example.shareeat.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias UserCallback = (User) -> Unit
typealias RecipesCallback = (List<Recipe>) -> Unit
typealias RecipeCallback = (Recipe) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object Collections {
        const val USERS = "users"
        const val RECIPES = "recipes"
    }
}