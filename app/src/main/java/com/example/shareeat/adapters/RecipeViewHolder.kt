package com.example.shareeat.adapters
import android.content.Context
import android.widget.AdapterView
import com.squareup.picasso.Picasso
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.R
import com.example.shareeat.databinding.ItemRecipeBinding
import com.example.shareeat.model.Recipe

interface OnItemClickListener {
    fun onItemClick(position: Int)
    fun onItemClick(recipe: Recipe?)
}
class RecipeViewHolder(
    private val binding: ItemRecipeBinding,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(binding.root){

    private var recipe: Recipe? = null
    private var titleTextView: TextView? = null
    private var recipeImageView: ImageView? = null

    init {
        titleTextView = binding.recipeTitle
        recipeImageView = binding.recipeImage
        itemView.setOnClickListener {
            listener?.onItemClick(adapterPosition)
            listener?.onItemClick(recipe)
        }

    }

        fun bind(recipe: Recipe?, position: Int) {
            this.recipe = recipe
            binding.recipeTitle.text = recipe?.title

            val height = when {
                position % 3 == 0 -> 220
                position % 2 == 0 -> 180
                else -> 150
            }
            val params = binding.recipeImage.layoutParams
            params.height = dpToPx(binding.root.context, height)
            binding.recipeImage.layoutParams = params

            recipe?.imageUrl?.let {
                if (it.isNotBlank()) {
                    Picasso.get()
                        .load(it)
                        .placeholder(R.drawable.food_placeholder)
                        .into(binding.recipeImage)
                }
            }
        }
    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    }


