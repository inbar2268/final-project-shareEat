package com.example.shareeat.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.adapters.RecipeViewHolder
import com.example.shareeat.databinding.ItemRecipeBinding
import com.example.shareeat.model.Recipe

class RecipeRecyclerAdapter (private var recipes: List<Recipe>) :
    RecyclerView.Adapter<RecipeViewHolder>() {

    var listener: OnItemClickListener? = null

    fun set(recipes: List<Recipe>) {
        this.recipes = recipes
        notifyDataSetChanged() // נעדכן את הרשימה
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = ItemRecipeBinding.inflate(inflator, parent, false)
        return RecipeViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(
            recipe = recipes?.get(position),
            position = position
        )
    }

    fun update(recipes: List<Recipe>) {
        this.recipes = recipes
    }
    override fun getItemCount(): Int = recipes.size
}
