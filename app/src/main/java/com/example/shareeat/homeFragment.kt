package com.example.shareeat

import com.example.shareeat.databinding.FragmentHomeBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.shareeat.adapters.RecipeRecyclerAdapter
import com.example.shareeat.model.Recipe


class homeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
//    private var recipes: List<Recipe> = emptyList()

    val recipes: List<Recipe> = listOf(
        Recipe(
            title = "פסטה ברוטב עגבניות",
            description = "פסטה טעימה עם רוטב עגבניות עשיר",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS7QR8UsG6r86SxTTlPdUxhStBSiYzzsr0V7g&s",
            instructions = "לבשל את הפסטה, להוסיף רוטב ולבשל עוד 5 דקות.",
            userId = "123",
            userName = "שף ישראל",
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Recipe(
            title = "פיצה ביתית",
            description = "פיצה טעימה עם גבינה מותכת",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS9W8ICl6_sK-d_2Se_cYQuJ6sGCyB4LapIpw&s",
            instructions = "ללוש בצק, למרוח רוטב, להוסיף גבינה ולאפות 15 דקות.",
            userId = "456",
            userName = "שף דנה",
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Recipe(
            title = "עוגת שוקולד",
            description = "עוגת שוקולד עסיסית וקלה להכנה",
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSwZ-GpTn9xTtYg8AC20UZwI3Qkj8E3UCqvSQ&s",
            instructions = "לערבב חומרים, לאפות 30 דקות ולקרר.",
            userId = "789",
            userName = "שף תומר",
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    )
            ;
    private var adapter: RecipeRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding?.recipesRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recipesRecyclerView?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        adapter = RecipeRecyclerAdapter(recipes)
        binding?.recipesRecyclerView?.adapter = adapter
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}