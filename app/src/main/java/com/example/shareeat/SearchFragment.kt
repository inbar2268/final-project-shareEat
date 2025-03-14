package com.example.shareeat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.adapters.SearchRecipeAdapter
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var tabPeople: TextView
    private lateinit var tabRecipes: TextView
    private lateinit var tabMap: TextView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var adapter: SearchRecipeAdapter
    private lateinit var progressBar: View

    private var allRecipes: List<Recipe> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        searchIcon = view.findViewById(R.id.searchIcon)
        tabPeople = view.findViewById(R.id.tabPeople)
        tabRecipes = view.findViewById(R.id.tabRecipes)
        tabMap = view.findViewById(R.id.tabMap)
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        adapter = SearchRecipeAdapter(emptyList()) { selectedRecipe ->
            onRecipeClick(selectedRecipe)
        }

        searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = this@SearchFragment.adapter
        }

        fetchRecipes()

        restoreSelectedTab()

        tabPeople.setOnClickListener { selectTab(tabPeople, 0) }
        tabRecipes.setOnClickListener { selectTab(tabRecipes, 1) }
        tabMap.setOnClickListener { selectTab(tabMap, 2) }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString())
            }

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

    private fun fetchRecipes() {
        progressBar.visibility = View.VISIBLE
        Model.shared.recipes.observe(viewLifecycleOwner) { recipes ->
            allRecipes = recipes
            progressBar.visibility = View.GONE
        }
    }

    private fun selectTab(selectedTab: TextView, tabIndex: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.dark_brown)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.black)

        tabPeople.setTextColor(defaultColor)
        tabRecipes.setTextColor(defaultColor)
        tabMap.setTextColor(defaultColor)

        selectedTab.setTextColor(selectedColor)

        saveSelectedTab(tabIndex)

        if (tabIndex == 1) {
            adapter.setRecipes(allRecipes)
        } else {
            adapter.setRecipes(emptyList())
        }
    }

    private fun performSearch(query: String) {
        if (tabRecipes.currentTextColor == ContextCompat.getColor(requireContext(), R.color.dark_brown)) {
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
            adapter.setRecipes(filteredRecipes)
        }
    }

    private fun onRecipeClick(recipe: Recipe) {
        Log.d("SearchFragment", "Recipe clicked: ${recipe.title}")

        val action = SearchFragmentDirections.actionSearchFragmentToRecipeDetailsFragment(recipe.id)
        view?.let {
            Navigation.findNavController(it).navigate(action)
        }
    }

    private fun saveSelectedTab(tabIndex: Int) {
        val prefs = requireActivity().getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("selected_tab", tabIndex).apply()
    }

    private fun restoreSelectedTab() {
        val prefs = requireActivity().getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)
        val savedTabIndex = prefs.getInt("selected_tab", 0)

        when (savedTabIndex) {
            0 -> selectTab(tabPeople, 0)
            1 -> selectTab(tabRecipes, 1)
            2 -> selectTab(tabMap, 2)
        }
    }
}
