package com.example.shareeat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shareeat.model.Model
import com.example.shareeat.model.Recipe
import com.example.shareeat.model.ImageSelector
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class UploadRecipeFragment : Fragment() {

    private lateinit var recipeImagePreview: ImageView
    private lateinit var addPhotoText: TextView
    private lateinit var cameraIcon: ImageView
    private lateinit var postButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var questionTextInput: TextInputEditText
    private lateinit var mealDescriptionInput: TextInputEditText
    private lateinit var instructionsInput: TextInputEditText

    private lateinit var imageHandler: ImageSelector
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var geoHash: String? = null
    private var imageSelected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_upload_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeImagePreview = view.findViewById(R.id.recipeImagePreview)
        addPhotoText = view.findViewById(R.id.addPhotoText)
        cameraIcon = view.findViewById(R.id.cameraIcon)
        postButton = view.findViewById(R.id.postButton)
        progressBar = view.findViewById(R.id.progressBar)

        questionTextInput = view.findViewById(R.id.questionText)
        mealDescriptionInput = view.findViewById(R.id.mealDescriptionEditText)
        instructionsInput = view.findViewById(R.id.instructionsEditText)

        progressBar.visibility = View.GONE

        imageHandler = ImageSelector(this)
        imageHandler.initialize(recipeImagePreview, addPhotoText, "recipe_images")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getUserLocation()

        cameraIcon.setOnClickListener {
            imageHandler.showImagePickerDialog()
        }

        postButton.setOnClickListener {
            val recipe = createRecipeFromInput()
            if (recipe != null) {
                progressBar.visibility = View.VISIBLE

                if (imageSelected && imageHandler.getSelectedImageBitmap() != null) {
                    uploadRecipeWithImage(recipe)
                } else {
                    saveRecipe(recipe)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imageHandler.handleActivityResult(requestCode, resultCode, data)

        if (imageHandler.getSelectedImageBitmap() != null) {
            imageSelected = true
            addPhotoText.visibility = View.GONE
            recipeImagePreview.visibility = View.VISIBLE
        }
    }


    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.e("UploadRecipeFragment", "Location permission NOT granted, requesting now...")
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updateLocation(location)
                } else {
                    Log.e("UploadRecipeFragment", "Location is null, requesting fresh location...")
                    requestNewLocationData()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UploadRecipeFragment", "Error getting location: ${exception.message}")
                requestNewLocationData()
            }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // 5 seconds
            fastestInterval = 2000 // 2 seconds
            numUpdates = 1
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.e("UploadRecipeFragment", "Permissions not granted, cannot request location updates")
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    updateLocation(location)
                } else {
                    Log.e("UploadRecipeFragment", "Failed to get new location")
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }, null)
    }

    private fun updateLocation(location: Location) {
        userLatitude = location.latitude
        userLongitude = location.longitude
        geoHash = GeoFireUtils.getGeoHashForLocation(GeoLocation(userLatitude!!, userLongitude!!))

        Log.d("UploadRecipeFragment", "Location updated: lat=$userLatitude, long=$userLongitude, geoHash=$geoHash")
    }


    private fun createRecipeFromInput(): Recipe? {
        val title = questionTextInput.text.toString().trim()
        val description = mealDescriptionInput.text.toString().trim()
        val instructions = instructionsInput.text.toString().trim()

        val user = getCurrentUser()
        val userId = user?.first ?: return null
        val userName = user.second

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            return null
        }

        if (instructions.isEmpty()) {
            Toast.makeText(requireContext(), "Instructions are required", Toast.LENGTH_SHORT).show()
            return null
        }

        return Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            instructions = instructions,
            userId = userId,
            userName = userName,
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            latitude = userLatitude,
            longitude = userLongitude,
            geohash = geoHash
        )
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

    private fun getCurrentUser(): Pair<String, String>? {
        val sharedPreferences = requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("uid", "defaultUserId") ?: "defaultUserId"
        val userName = sharedPreferences.getString("displayName", "defaultUserName") ?: "defaultUserName"

        return Pair(userId, userName)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }
}
