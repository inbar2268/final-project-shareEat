package com.example.shareeat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.shareeat.adapters.OnItemClickListener
import com.example.shareeat.adapters.RecipeRecyclerAdapter
import com.example.shareeat.databinding.FragmentUserProfileBinding
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.example.shareeat.model.User

class UserProfileFragment : Fragment() {
    private var binding: FragmentUserProfileBinding? = null
    private var userId: String? = null
    private val viewModel: RecipesViewModel by viewModels()
    private var adapter: RecipeRecyclerAdapter? = null
    var userRecipes: List<Recipe> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            userId = it.getString("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_LONG).show()
        } else {
            fetchUserProfile(userId!!)
        }

        binding?.recipesRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recipesRecyclerView?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = RecipeRecyclerAdapter(userRecipes )

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            val currentUserId = userId
            userRecipes = recipes?.filter { it.userId == currentUserId } ?: emptyList()
            adapter?.update(userRecipes)
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }

        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshAllRecipes()
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
                        UserProfileFragmentDirections.actionUserProfileFragmentToRecipeDetailsFragment(it.id)
                    binding?.root?.let {
                        Navigation.findNavController(it).navigate(action)
                    }
                }
            }
        }
        binding?.recipesRecyclerView?.adapter = adapter
    }

    private fun fetchUserProfile(userId: String) {
        binding?.progressBar?.visibility = View.VISIBLE

        Model.shared.getUserById(userId) { user ->
            binding?.progressBar?.visibility = View.GONE

            if (user != null) {
                binding?.displayNameText?.text = user.displayName
                binding?.emailText?.text = user.email

                if (!user.photoUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(user.photoUrl)
                        .placeholder(R.drawable.avatar)
                        .circleCrop()
                        .into(binding?.profileImage!!)
                } else {
                    binding?.profileImage?.setImageResource(R.drawable.avatar)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
        }
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
        val currentUserId =userId;
        userRecipes = viewModel.recipes.value?.filter { it.userId == currentUserId } ?: emptyList()

    }
}
