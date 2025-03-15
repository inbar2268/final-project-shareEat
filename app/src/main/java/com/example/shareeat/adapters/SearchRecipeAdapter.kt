package com.example.shareeat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.databinding.ItemSearchRecipeBinding
import com.example.shareeat.model.Recipe

class SearchRecipeAdapter(
    private var recipes: List<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit
) : RecyclerView.Adapter<SearchRecipeViewHolder>() {

    fun setRecipes(newRecipes: List<Recipe>) {
        this.recipes = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSearchRecipeBinding.inflate(inflater, parent, false)
        return SearchRecipeViewHolder(binding, onRecipeClick)
    }

    override fun onBindViewHolder(holder: SearchRecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size
}
