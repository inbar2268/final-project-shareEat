package com.example.shareeat
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.example.shareeat.model.ImageSelector
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class UploadRecipeFragment : Fragment() {

    private lateinit var recipeImagePreview: ImageView
    private lateinit var addPhotoText: TextView
    private lateinit var cameraIcon: ImageView
    private lateinit var postButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var questionTextInput: TextInputEditText
    private lateinit var questionTextInputLayout: TextInputLayout
    private lateinit var mealDescriptionInput: TextInputEditText
    private lateinit var mealDescriptionLayout: TextInputLayout
    private lateinit var instructionsInput: TextInputEditText
    private lateinit var instructionsLayout: TextInputLayout

    private lateinit var imageHandler: ImageSelector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_upload_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        recipeImagePreview = view.findViewById(R.id.recipeImagePreview)
        addPhotoText = view.findViewById(R.id.addPhotoText)
        cameraIcon = view.findViewById(R.id.cameraIcon)
        postButton = view.findViewById(R.id.postButton)
        progressBar = view.findViewById(R.id.progressBar)

        questionTextInput = view.findViewById(R.id.questionText)
        questionTextInputLayout = view.findViewById(R.id.questionTextInputLayout)
        mealDescriptionInput = view.findViewById(R.id.mealDescriptionEditText)
        mealDescriptionLayout = view.findViewById(R.id.mealDescriptionLayout)
        instructionsInput = view.findViewById(R.id.instructionsEditText)
        instructionsLayout = view.findViewById(R.id.instructionsLayout)

        progressBar.visibility = View.GONE

        imageHandler = ImageSelector(this)
        imageHandler.initialize(recipeImagePreview, addPhotoText, "recipe_images")

        cameraIcon.setOnClickListener {
            imageHandler.showImagePickerDialog()
        }

        postButton.setOnClickListener {
            val recipe = createRecipeFromInput()
            if (recipe != null) {
                progressBar.visibility = View.VISIBLE

                if (imageHandler.getSelectedImageBitmap() != null) {
                    uploadRecipeWithImage(recipe) // Upload image before saving recipe
                } else {
                    saveRecipe(recipe) // Save recipe directly if no image is selected
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageHandler.handleActivityResult(requestCode, resultCode, data)
    }

    private fun uploadRecipeWithImage(recipe: Recipe) {
        val imageName = "${UUID.randomUUID()}.jpg"
        imageHandler.uploadImageToCloudinary(imageName) { imageUrl ->
            if (imageUrl != null) {
                val updatedRecipe = recipe.copy(imageUrl = imageUrl)
                saveRecipe(updatedRecipe)
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentUser(): Pair<String, String>? {
        val sharedPreferences = requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("uid", "defaultUserId") ?: "defaultUserId"
        val userName = sharedPreferences.getString("displayName", "defaultUserName") ?: "defaultUserName"

        return Pair(userId, userName)
    }

    private fun createRecipeFromInput(): Recipe? {
        val title = questionTextInput.text.toString().trim()
        val description = mealDescriptionInput.text.toString().trim()
        val instructions = instructionsInput.text.toString().trim()

        val user = getCurrentUser()
        val userId = user?.first ?: return null
        val userName = user.second

        Log.d("UploadRecipeFragment", "Creating recipe...")

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
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            instructions = instructions,
            userId = userId,
            userName = userName,
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        return recipe
    }

    private fun saveRecipe(recipe: Recipe) {
        progressBar.visibility = View.VISIBLE

        Model.shared.addRecipe(recipe) {
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Recipe Uploaded!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}