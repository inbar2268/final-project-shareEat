package com.example.shareeat

import com.example.shareeat.databinding.FragmentHomeBinding
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.shareeat.adapters.OnItemClickListener
import com.example.shareeat.adapters.RecipeRecyclerAdapter
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import java.util.UUID


class homeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null

    private val viewModel: RecipesViewModel by viewModels()
    private var adapter: RecipeRecyclerAdapter? = null
    private var recipesFromTastyApi: List<Recipe> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Model.shared.startListeningForRecipeChanges()
        apiRecipesToRecipes()
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding?.recipesRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recipesRecyclerView?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = RecipeRecyclerAdapter(viewModel.recipes.value ?: listOf())

        viewModel.recipes.observe(viewLifecycleOwner) {
            adapter?.update(it+recipesFromTastyApi)
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }

        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshAllRecipes()
            viewModel.fechRecipes()
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        adapter?.listener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("TAG", "On click Activity listener on position $position")
            }

            override fun onItemClick(recipe: Recipe?) {
                Log.d("TAG", "On student clicked name: ${recipe?.id}")
                recipe?.let {
                    val action =
                        homeFragmentDirections.actionRecipesFragmentToRecipesDetailsFragment(it.id)
                    binding?.root?.let {
                        Navigation.findNavController(it).navigate(action)
                    }
                }
            }
        }
        binding?.recipesRecyclerView?.adapter = adapter
        return binding?.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        getAllRecipes()
    }

    private fun getAllRecipes() {
        binding?.progressBar?.visibility = View.VISIBLE
        viewModel.refreshAllRecipes()

    }

    private fun apiRecipesToRecipes() {
        viewModel.fechRecipes()
        viewModel.apiRecipes.observe(viewLifecycleOwner) { recipes ->
            recipesFromTastyApi = recipes

        }

    }
}