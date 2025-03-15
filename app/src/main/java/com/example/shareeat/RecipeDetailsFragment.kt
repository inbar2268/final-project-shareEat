package com.example.shareeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.shareeat.databinding.FragmentRecipeDetailsBinding
import com.example.shareeat.extensions.getCurrentUser
import com.example.shareeat.model.Model
import com.squareup.picasso.Picasso

class RecipeDetailsFragment : Fragment() {
    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    var recipeId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getString("recipeId")
        }
        recipeId = arguments?.let {
            RecipeDetailsFragmentArgs.fromBundle(it).recipeId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        recipeId?.let {
            displayRecipe(it)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun displayRecipe(recipeId: String) {
        Model.shared.getRecipeById(recipeId) { recipe ->
            binding.recipeDetailTitle.text = recipe?.title
            binding.recipeDetailDescription.text = recipe?.description
            binding.recipeDetailInstructions.text = recipe?.instructions
            binding.recipeDetailAuthor.text = recipe?.userName

            recipe?.imageUrl?.let {
                if (it.isNotBlank()) {
                    Picasso.get()
                        .load(it)
                        .placeholder(R.drawable.food_placeholder)
                        .into(binding.recipeDetailImage)
                }
            }
            val user = getCurrentUser(requireContext())
            val userId = user?.first
            if (userId == recipe.userId) {
                binding.editButton.visibility = View.VISIBLE
            }
            val action =
                RecipeDetailsFragmentDirections.actionRecipesDetailsFragmentToEditRecipeFragment(
                    recipeId
                )
            binding.editButton.setOnClickListener(Navigation.createNavigateOnClickListener(action))

            binding.appBarLayout.visibility = View.VISIBLE
        }
    }
}