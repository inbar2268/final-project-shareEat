package com.example.shareeat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
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

    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null
    private var currentPhotoPath: String? = null

    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 1002
    private val CAMERA_PERMISSION_REQUEST_CODE = 1003

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

        cameraIcon.setOnClickListener {
            showImagePickerDialog()
        }

        postButton.setOnClickListener {
            val recipe = createRecipeFromInput()
            if (recipe != null) {
                progressBar.visibility = View.VISIBLE

                if (selectedImageBitmap != null) {
                    uploadImageToCloudinary(recipe) // Upload image before saving recipe
                } else {
                    saveRecipe(recipe) // Save recipe directly if no image is selected
                }
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take a Photo", "Choose from Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("UploadRecipeFragment", "Error creating file: ${ex.message}")
                null
            }

            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    photoFile
                )
                currentPhotoPath = photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(requireContext(), "Failed to create image file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No Camera App Available!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    Log.d("UploadRecipeFragment", "📸 Camera image selected")
                    if (currentPhotoPath != null) {
                        val file = File(currentPhotoPath!!)
                        selectedImageUri = Uri.fromFile(file)

                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver,
                                selectedImageUri
                            )
                        } catch (e: Exception) {
                        }

                        recipeImagePreview.setImageURI(selectedImageUri)
                        updateImageViewVisibility()
                    } else {
                        Log.e("UploadRecipeFragment", "Camera image path is null")
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        selectedImageUri = imageUri

                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver,
                                selectedImageUri
                            )
                        } catch (e: Exception) {
                            Log.e("UploadRecipeFragment", "Failed to convert gallery image to Bitmap: ${e.message}")
                        }

                        recipeImagePreview.setImageURI(imageUri)
                        updateImageViewVisibility()
                    } else {
                        Log.e("UploadRecipeFragment", "Gallery image URI is null")
                    }
                }
            }
        } else {
            Log.e("UploadRecipeFragment", "Image selection failed or cancelled")
        }
    }


    private fun createImageFile(): File {
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "recipe_image_${UUID.randomUUID()}",
            ".jpg",
            storageDir
        )
    }

    private fun updateImageViewVisibility() {
        recipeImagePreview.visibility = View.VISIBLE
        addPhotoText.visibility = View.GONE
    }

    private fun uploadImageToCloudinary(recipe: Recipe) {
        val imageName = "${UUID.randomUUID()}.jpg"

        Model.shared.uploadTo(Model.Storage.CLOUDINARY, selectedImageBitmap!!, imageName) { imageUrl ->
            requireActivity().runOnUiThread {
                if (imageUrl != null) {
                    val updatedRecipe = recipe.copy(imageUrl = imageUrl)
                    saveRecipe(updatedRecipe) // Save recipe with uploaded image URL
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
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
