package com.example.shareeat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.example.shareeat.model.ImageSelector
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import java.util.*

class EditRecipeFragment : Fragment() {
    var recipeId: String? = ""
    private lateinit var recipeImagePreview: ImageView
    private lateinit var addPhotoText: TextView
    private lateinit var cameraIcon: ImageView
    private lateinit var updateButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var titleTextInput: TextInputEditText
    private lateinit var titleTextInputLayout: TextInputLayout
    private lateinit var mealDescriptionInput: TextInputEditText
    private lateinit var mealDescriptionLayout: TextInputLayout
    private lateinit var instructionsInput: TextInputEditText
    private lateinit var instructionsLayout: TextInputLayout

    private lateinit var imageHandler: ImageSelector
    private lateinit var recipe: Recipe
    private var imageChanged = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            recipeId = it.getString("recipeId")
        }
        recipeId = arguments?.let {
            EditRecipeFragmentArgs.fromBundle(it).recipeId
        }
        return inflater.inflate(R.layout.fragment_edit_recipe, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeImagePreview = view.findViewById(R.id.recipeImagePreview)
        addPhotoText = view.findViewById(R.id.addPhotoText)
        cameraIcon = view.findViewById(R.id.cameraIcon)
        updateButton = view.findViewById(R.id.updateButton)
        progressBar = view.findViewById(R.id.progressBar)

        titleTextInput = view.findViewById(R.id.questionText)
        titleTextInputLayout = view.findViewById(R.id.questionTextInputLayout)
        mealDescriptionInput = view.findViewById(R.id.mealDescriptionEditText)
        mealDescriptionLayout = view.findViewById(R.id.mealDescriptionLayout)
        instructionsInput = view.findViewById(R.id.instructionsEditText)
        instructionsLayout = view.findViewById(R.id.instructionsLayout)

        progressBar.visibility = View.GONE

        imageHandler = ImageSelector(this)
        imageHandler.initialize(recipeImagePreview, addPhotoText, "recipe_images")

        loadExistingRecipe()

        cameraIcon.setOnClickListener {
            imageHandler.showImagePickerDialog()
            imageChanged = true
        }

        updateButton.setOnClickListener {
            val updatedRecipe = createUpdatedRecipeFromInput(recipe)
            if (updatedRecipe != null) {
                progressBar.visibility = View.VISIBLE

                if (imageChanged && imageHandler.getSelectedImageBitmap() != null) {
                    uploadRecipeWithNewImage(updatedRecipe)
                } else {
                    updateRecipe(updatedRecipe)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageHandler.handleActivityResult(requestCode, resultCode, data)
    }

    private fun loadExistingRecipe() {
        Log.d("EditRecipeFragment", "Loading recipe with ID: $recipeId")

        recipeId?.let {
            Model.shared.getRecipeById(it) { loadedRecipe ->
                recipe = loadedRecipe
                updateUIWithRecipeData()
            }
        }
    }

    private fun updateUIWithRecipeData() {
        requireActivity().runOnUiThread {
            titleTextInput.setText(recipe.title)
            mealDescriptionInput.setText(recipe.description)
            instructionsInput.setText(recipe.instructions)

            // טעינת התמונה אם קיימת
            recipe.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    addPhotoText.visibility = View.GONE
                    recipeImagePreview.visibility = View.VISIBLE
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.food_placeholder)
                        .into(recipeImagePreview)
                }
            }
        }
    }

    private fun uploadRecipeWithNewImage(updatedRecipe: Recipe) {
        val imageName = "${UUID.randomUUID()}.jpg"
        imageHandler.uploadImageToCloudinary(imageName) { imageUrl ->
            if (imageUrl != null) {
                val recipeWithNewImage = updatedRecipe.copy(imageUrl = imageUrl)
                updateRecipe(recipeWithNewImage)
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUpdatedRecipeFromInput(recipe: Recipe): Recipe? {
        val title = titleTextInput.text.toString().trim()
        val description = mealDescriptionInput.text.toString().trim()
        val instructions = instructionsInput.text.toString().trim()

        if (title.isEmpty()) {
            Log.e("UploadRecipeFragment", "Validation failed: Title is empty")
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            return null
        }

        if (instructions.isEmpty()) {
            Log.e("UploadRecipeFragment", "Validation failed: Instructions are empty")
            Toast.makeText(requireContext(), "Instructions are required", Toast.LENGTH_SHORT).show()
            return null
        }

        val recipe = Recipe(
            id = recipe.id,
            title = title,
            description = description,
            instructions = instructions,
            imageUrl = recipe.imageUrl,
            userId = recipe.userId,
            userName = recipe.userName,
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        return recipe
    }

    private fun updateRecipe(updatedRecipe: Recipe) {
        progressBar.visibility = View.VISIBLE

        Model.shared.editRecipe(updatedRecipe) {
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Recipe Uploaded!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}