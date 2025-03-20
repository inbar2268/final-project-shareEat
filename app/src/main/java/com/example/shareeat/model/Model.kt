package com.example.shareeat.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.shareeat.BuildConfig
import com.example.shareeat.base.EmptyCallback
import com.example.shareeat.base.RecipeCallback
import com.example.shareeat.model.dao.AppLocalDb
import com.example.shareeat.model.dao.AppLocalDbRepository
import com.example.shareeat.model.firebase.FirebaseModel
import com.example.shareeat.model.firebase.FirebaseRecipe
import com.example.shareeat.model.firebase.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.Executors

class Model private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }

    enum class Storage {
        FIREBASE,
        CLOUDINARY
    }

    private val database: AppLocalDbRepository = AppLocalDb.database
    private var executor = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    val users: LiveData<List<User>> = database.userDao().getAllUser()
    val recipes: LiveData<List<Recipe>> = database.recipeDao().getAllRecipes()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData()
    val apiRecipes: MutableLiveData<List<Recipe>> = MutableLiveData()
    private val firebaseModel = FirebaseModel()
    private val firebaseUser = FirebaseUser(firebaseModel)
    private val firebaseRecipe = FirebaseRecipe(firebaseModel)

    companion object {
        val shared = Model()
    }

    // user:

    fun refreshAllUsers() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = User.lastUpdated
        firebaseUser.getAllUsers(lastUpdated) { users ->
            executor.execute {
                var currentTime = lastUpdated
                for (user in users) {
                    database.userDao().insertAll(user)
                    user.lastUpdated?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }
                User.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun addUser(user: User, callback: EmptyCallback) {
        firebaseUser.add(user) {
            executor.execute {
                database.userDao().insertAll(user)
                mainHandler.post { callback() }
            }
        }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        executor.execute {
            val localUser = database.userDao().getUserById(userId)
            if (localUser != null) {
                mainHandler.post { callback(localUser) }
            } else {
                firebaseUser.getUserById(userId) { user ->
                    if (user != null) {
                        executor.execute {
                            database.userDao().insertAll(user)
                            mainHandler.post { callback(user) }
                        }
                    } else {
                        mainHandler.post { callback(null) }
                    }
                }
            }
        }
    }

    fun editUser(user: User, callback: EmptyCallback) {
        user.lastUpdated = System.currentTimeMillis()
        firebaseUser.editUser(user) {
            executor.execute {
                database.userDao().insertAll(user)
                mainHandler.post { callback() }
            }
        }
    }

    fun editRecipe(recipe: Recipe, callback: EmptyCallback) {
        recipe.lastUpdated = System.currentTimeMillis()
        firebaseRecipe.updateRecipe(recipe) {
            executor.execute {
                database.recipeDao().insertAll(recipe)
                mainHandler.post { callback() }
            }
        }
    }


    fun addRecipe(recipe: Recipe, callback: EmptyCallback) {
        firebaseRecipe.add(recipe) {
            executor.execute {
                database.recipeDao().insertAll(recipe)
                mainHandler.post { callback() }
            }
        }
    }

    fun refreshAllRecipes() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = Recipe.lastUpdated
        firebaseRecipe.getAllRecipes(lastUpdated) { recipes ->
            executor.execute {
                var currentTime = lastUpdated
                for (recipe in recipes) {
                    database.recipeDao().insertAll(recipe)
                    recipe.lastUpdated?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }
                Recipe.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun startListeningForRecipeChanges() {
        firebaseRecipe.addRecipeChangeListener { recipe, changeType ->
            executor.execute {
                try {
                    when (changeType) {
                        DocumentChange.Type.ADDED -> {
                            // Check if we already have this recipe locally
                            val existingRecipe = try {
                                database.recipeDao().getRecipeById(recipe.id)
                            } catch (e: Exception) {
                                null
                            }

                            if (existingRecipe == null) {
                                // It's truly new, add it to local DB
                                database.recipeDao().insertAll(recipe)
                                Log.d("Model", "New recipe ${recipe.id} added to local database")
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            // Update the recipe in local DB
                            database.recipeDao().update(recipe)
                            Log.d("Model", "Recipe ${recipe.id} updated in local database")
                        }
                        DocumentChange.Type.REMOVED -> {
                            // Remove from local DB
                            database.recipeDao().delete(recipe)
                            Log.d("Model", "Recipe ${recipe.id} removed from local database")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Model", "Error processing recipe change in local DB", e)
                }
            }
        }
    }

    /**
     * Uploads an image to Cloudinary and returns its URL.
     */
    private fun uploadImageToCloudinary(
        folder: String,
        image: Bitmap,
        callback: (String?) -> Unit
    ) {
        Log.d("Cloudinary Upload", "Starting upload process...")

        val stream = ByteArrayOutputStream()
        val compressed = image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        if (!compressed) {
            Log.e("Cloudinary Upload", "Image compression failed!")
            callback(null)
            return
        }

        val imageData = stream.toByteArray()
        Log.d("Cloudinary Upload", "Image compressed successfully. Size: ${imageData.size} bytes")

        val cloudinary = Cloudinary(
            mapOf(
                "cloud_name" to BuildConfig.CLOUD_NAME,
                "api_key" to BuildConfig.API_KEY,
                "api_secret" to BuildConfig.API_SECRET
            )
        )

        executor.execute {
            try {
                Log.d("Cloudinary Upload", "Uploading image to Cloudinary...")
                val uploadResult =
                    cloudinary.uploader().upload(imageData, ObjectUtils.asMap("folder", folder))

                Log.d("Cloudinary Upload", "Upload response received: $uploadResult")
                val imageUrl = uploadResult["secure_url"] as? String
                if (imageUrl != null) {
                    Log.d("Cloudinary Upload", "Image uploaded successfully. URL: $imageUrl")
                } else {
                    Log.e("Cloudinary Upload", "Upload failed! No URL returned.")
                }

                mainHandler.post { callback(imageUrl) }
            } catch (e: Exception) {
                Log.e("Cloudinary Upload", "Upload error: ${e.message}", e)
                mainHandler.post { callback(null) }
            }
        }
    }


    /**
     * Decides which storage to use for uploading an image.
     */
    fun uploadTo(
        storage: Storage,
        image: Bitmap,
        name: String,
        folder: String,
        callback: (String?) -> Unit
    ) {
        when (storage) {
            Storage.FIREBASE -> {
                uploadImageToFirebase(image, name, callback)
            }

            Storage.CLOUDINARY -> {
                uploadImageToCloudinary(folder, image, callback)
            }
        }
    }

    fun delete(user: User, callback: EmptyCallback) {
        firebaseUser.delete(user, callback)
    }

    fun deleteRecipe(recipe: Recipe, callback: EmptyCallback) {
        firebaseRecipe.delete(recipe) {
            executor.execute {
                database.recipeDao().delete(recipe)
                mainHandler.post { callback() }
            }
        }
    }

    private fun uploadImageToFirebase(
        image: Bitmap,
        name: String,
        callback: (String?) -> Unit
    ) {
        firebaseModel.uploadImage(image, name, callback)
    }

    fun getRecipeById(studentId: String, callback: RecipeCallback) {
        executor.execute {
            val recipe = database.recipeDao().getRecipeById(studentId)
            mainHandler.post {
                callback(recipe)
            }
        }
    }

    fun getRecipeFromApiById(id: String):
            Recipe? {
        return apiRecipes.value?.find { it.id == id }
    }

    fun getAllApiRecipes() {
        executor.execute {
            try {
                val request = RecipesClient.recipesApiClient.getRecipes()
                val response = request.execute()

                if (response.isSuccessful) {
                    val recipes = response.body()
                    Log.e(
                        "TAG",
                        "Fetched recipes!.. with total number of movies ${recipes?.result?.size ?: 0}"
                    )
                    val result = recipes?.result ?: emptyList()
                    val recipesFromTastyApi = result.map { item ->
                        val instructionTexts = item.instructions.map { it.displayText }
                        val allInstructions = instructionTexts.joinToString("\n")
                        Recipe(
                            id = UUID.randomUUID().toString(),
                            title = item.title,
                            description = item.description,
                            instructions = allInstructions,
                            imageUrl = item.imageUrl,
                            userId = "0",
                            userName = "SHAREEAT USER",
                            timestamp = System.currentTimeMillis(),
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    this.apiRecipes.postValue(recipesFromTastyApi)
                } else {
                    Log.e("TAG", "Failed to fetch recipe!")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Failed to fetch recipes! with excpetio ${e}")
            }
        }
    }

}

