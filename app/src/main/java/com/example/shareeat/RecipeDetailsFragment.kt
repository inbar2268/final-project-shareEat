package com.example.shareeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.shareeat.databinding.FragmentRecipeDetailsBinding
import com.example.shareeat.extensions.getCurrentUser
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
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
            getRecipe(it)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getRecipe(recipeId: String) {
        var recipe = Model.shared.getRecipeFromApiById(recipeId);
        if (recipe == null) {
            Model.shared.getRecipeById(recipeId) { fireBaseRecipe ->
                displayRecipe(fireBaseRecipe)
            }
        } else {
            displayRecipe(recipe)

        }
    }


    private fun displayRecipe(recipe: Recipe) {

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
            binding.deleteRecipeButton.visibility = View.VISIBLE
            binding.deleteRecipeButton.setOnClickListener {
                deleteRecipe(recipe)
            }
        }
        val action =
            RecipeDetailsFragmentDirections.actionRecipesDetailsFragmentToEditRecipeFragment(
                recipe.id
            )
        binding.editButton.setOnClickListener(Navigation.createNavigateOnClickListener(action))

        binding.appBarLayout.visibility = View.VISIBLE

    }

    private fun deleteRecipe(recipe: Recipe) {
        Model.shared.deleteRecipe(recipe) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Recipe Deleted!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}