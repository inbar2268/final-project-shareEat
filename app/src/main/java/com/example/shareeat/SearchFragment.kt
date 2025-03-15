package com.example.shareeat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.adapters.SearchRecipeAdapter
import com.example.shareeat.adapters.SearchUserAdapter
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.example.shareeat.model.User

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var tabPeople: TextView
    private lateinit var tabRecipes: TextView
    private lateinit var tabMap: TextView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var progressBar: View

    private lateinit var recipeAdapter: SearchRecipeAdapter
    private lateinit var userAdapter: SearchUserAdapter

    private var allRecipes: List<Recipe> = emptyList()
    private var allUsers: List<User> = emptyList()
    private var selectedTabIndex: Int = 0 // 0 = People, 1 = Recipes, 2 = Map

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupRecyclerView()
        setupObservers()
        restoreSelectedTab()
        setupSearchListeners()
        setupTabListeners()
    }

    private fun initializeViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        searchIcon = view.findViewById(R.id.searchIcon)
        tabPeople = view.findViewById(R.id.tabPeople)
        tabRecipes = view.findViewById(R.id.tabRecipes)
        tabMap = view.findViewById(R.id.tabMap)
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        recipeAdapter = SearchRecipeAdapter(emptyList(), ::onRecipeClick)
        userAdapter = SearchUserAdapter(emptyList(), ::onUserClick)
    }

    private fun setupRecyclerView() {
        searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
        }
    }

    private fun setupObservers() {
        progressBar.visibility = View.VISIBLE

        Model.shared.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isEmpty()) Model.shared.refreshAllRecipes()
            allRecipes = recipes
            updateDisplayedData()
        }

        Model.shared.users.observe(viewLifecycleOwner) { users ->
            if (users.isEmpty()) Model.shared.refreshAllUsers()
            allUsers = users
            progressBar.visibility = View.GONE
            updateDisplayedData()
        }
    }

    private fun setupSearchListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = performSearch(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchIcon.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
            } else {
                performSearch(query)
            }
        }
    }

    private fun setupTabListeners() {
        tabPeople.setOnClickListener { selectTab(tabPeople, 0) }
        tabRecipes.setOnClickListener { selectTab(tabRecipes, 1) }
        tabMap.setOnClickListener { selectTab(tabMap, 2) }
    }

    private fun selectTab(selectedTab: TextView, tabIndex: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.dark_brown)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.black)

        listOf(tabPeople, tabRecipes, tabMap).forEach { it.setTextColor(defaultColor) }
        selectedTab.setTextColor(selectedColor)

        selectedTabIndex = tabIndex
        saveSelectedTab(tabIndex)
        updateDisplayedData()
    }

    private fun updateDisplayedData() {
        searchRecyclerView.adapter = when (selectedTabIndex) {
            0 -> userAdapter.also { it.setUsers(allUsers) }
            1 -> recipeAdapter.also { it.setRecipes(allRecipes) }
            else -> null
        }
    }

    private fun performSearch(query: String) {
        when (selectedTabIndex) {
            0 -> {
                val filteredUsers = if (query.isEmpty()) {
                    allUsers
                } else {
                    allUsers.filter { it.displayName.contains(query, ignoreCase = true) }
                        .sortedWith(compareBy(
                            { it.displayName.startsWith(query, ignoreCase = true) }, // Prioritize users whose names start with the query
                            { it.displayName.indexOf(query, ignoreCase = true) } // Then prioritize by first occurrence of query
                        )).reversed() // Ensure that users whose name **starts** with the query come first
                }
                userAdapter.setUsers(filteredUsers)
            }
            1 -> {
                val filteredRecipes = if (query.isEmpty()) {
                    allRecipes
                } else {
                    allRecipes.filter { recipe ->
                        recipe.title.contains(query, ignoreCase = true) ||
                                recipe.description.contains(query, ignoreCase = true)
                    }.sortedWith(compareBy(
                        { it.title.indexOf(query, ignoreCase = true).takeIf { it >= 0 } ?: Int.MAX_VALUE },
                        { it.description.indexOf(query, ignoreCase = true).takeIf { it >= 0 } ?: Int.MAX_VALUE }
                    ))
                }
                recipeAdapter.setRecipes(filteredRecipes)
            }
        }
    }


    private fun onRecipeClick(recipe: Recipe) {
        val action = SearchFragmentDirections.actionSearchFragmentToRecipeDetailsFragment(recipe.id)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun onUserClick(user: User) {
        val action = SearchFragmentDirections.actionSearchFragmentToUserProfileFragment(user.id)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun saveSelectedTab(tabIndex: Int) {
        requireActivity().getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)
            .edit().putInt("selected_tab", tabIndex).apply()
    }

    private fun restoreSelectedTab() {
        val savedTabIndex = requireActivity()
            .getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)
            .getInt("selected_tab", 0)

        when (savedTabIndex) {
            0 -> selectTab(tabPeople, 0)
            1 -> selectTab(tabRecipes, 1)
            2 -> selectTab(tabMap, 2)
        }
    }
}
