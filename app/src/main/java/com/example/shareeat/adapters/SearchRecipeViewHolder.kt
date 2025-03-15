package com.example.shareeat.adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.R
import com.example.shareeat.databinding.ItemSearchRecipeBinding
import com.example.shareeat.model.Recipe
import com.squareup.picasso.Picasso

class SearchRecipeViewHolder(
    private val binding: ItemSearchRecipeBinding,
    private val onRecipeClick: (Recipe) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(recipe: Recipe) {
        binding.recipeTitle.text = recipe.title
        binding.recipeDescription.text = recipe.description

        binding.recipeImage.setImageResource(R.drawable.food_placeholder)

        recipe.imageUrl?.let {
            if (it.isNotBlank()) {
                Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.food_placeholder)
                    .into(binding.recipeImage)
            }
        }

        itemView.setOnClickListener {
            onRecipeClick(recipe)
        }
    }
}
