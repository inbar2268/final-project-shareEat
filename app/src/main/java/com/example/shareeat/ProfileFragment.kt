package com.example.shareeat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.shareeat.databinding.FragmentProfileBinding
import com.example.shareeat.model.ImageSelector
import com.example.shareeat.model.Model
import com.example.shareeat.model.User
import com.example.shareeat.model.User.Companion.clearAllUserData
import com.example.shareeat.model.User.Companion.getUserFromLocalStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.shareeat.adapters.OnItemClickListener
import com.example.shareeat.adapters.RecipeRecyclerAdapter
import com.example.shareeat.model.Recipe

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private var isEditMode = false
    private var currentPhotoUrl: String? = null
    private lateinit var imageHandler: ImageSelector
    private val viewModel: RecipesViewModel by viewModels()
    private var adapter: RecipeRecyclerAdapter? = null
    var userRecipes: List<Recipe> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        imageHandler = ImageSelector(this)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.profileImage?.let { imageView ->
            val imageTextView = TextView(requireContext())
            // Explicitly set the folder to profile_pictures
            imageHandler.initialize(imageView, imageTextView, "profile_pictures")
        }

        displayUserInfo()

        binding?.logoutButton?.setOnClickListener {
            auth.signOut()
            clearAllUserData(requireContext())
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(it).navigate(R.id.action_ProfileFragment_to_signInFragment)
        }

        binding?.editProfileButton?.setOnClickListener {
            toggleEditMode()
        }

        binding?.saveChangesButton?.setOnClickListener {
            saveProfileChanges()
        }

        binding?.cancelButton?.setOnClickListener {
            toggleEditMode(false)
            displayUserInfo()
        }

        binding?.changePhotoButton?.setOnClickListener {
            imageHandler.showImagePickerDialog()
        }

        binding?.recipesRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recipesRecyclerView?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = RecipeRecyclerAdapter(userRecipes )

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            val currentUserId = getUserId()
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
                        ProfileFragmentDirections.actionProfileFragmentToRecipeDetailsFragment(it.id)
                    binding?.root?.let {
                        Navigation.findNavController(it).navigate(action)
                    }
                }
            }
        }
        binding?.recipesRecyclerView?.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageHandler.handleActivityResult(requestCode, resultCode, data)
    }

    private fun getUserId(): String? {
        val userData = getUserFromLocalStorage(requireContext())
        return userData["uid"]
    }

    private fun displayUserInfo() {
        val userId = getUserId()

        binding?.progressBar?.visibility = View.VISIBLE

        if (userId != null) {
            Model.shared.getUserById(userId) { user ->
                binding?.progressBar?.visibility = View.GONE

                if (user != null) {
                    binding?.displayNameText?.text = user.displayName
                    binding?.emailText?.text = user.email
                    binding?.displayNameInput?.setText(user.displayName)

                    currentPhotoUrl = user.photoUrl
                    if (!currentPhotoUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(currentPhotoUrl)
                            .placeholder(R.drawable.image_placeholder)
                            .circleCrop()
                            .into(binding?.profileImage!!)
                    } else {
                        binding?.profileImage?.setImageResource(R.drawable.image_placeholder)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load user profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            binding?.progressBar?.visibility = View.GONE
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleEditMode(edit: Boolean = true) {
        isEditMode = edit

        binding?.emailText?.visibility = if (edit) View.GONE else View.VISIBLE
        binding?.displayNameText?.visibility = if (edit) View.GONE else View.VISIBLE
        binding?.displayNameInputLayout?.visibility = if (edit) View.VISIBLE else View.GONE
        binding?.changePhotoButton?.visibility = if (edit) View.VISIBLE else View.GONE
        binding?.editProfileButton?.visibility = if (edit) View.GONE else View.VISIBLE
        binding?.saveChangesButton?.visibility = if (edit) View.VISIBLE else View.GONE
        binding?.cancelButton?.visibility = if (edit) View.VISIBLE else View.GONE
    }

    private fun saveProfileChanges() {
        val displayName = binding?.displayNameInput?.text.toString().trim()

        if (displayName.isEmpty()) {
            binding?.displayNameInput?.error = "Display name cannot be empty"
            return
        }

        binding?.progressBar?.visibility = View.VISIBLE
        binding?.saveChangesButton?.isEnabled = false
        binding?.cancelButton?.isEnabled = false

        if (imageHandler.getSelectedImageBitmap() != null) {
            uploadProfilePicture(displayName)
        } else {
            updateUserProfile(displayName, currentPhotoUrl)
        }
    }

    private fun uploadProfilePicture(displayName: String) {
        val userId = getUserId() ?: return

        binding?.progressBar?.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Uploading profile picture...", Toast.LENGTH_SHORT).show()

        val profileImageName = "profile_${userId}"
        imageHandler.uploadImageToCloudinary(profileImageName) { imageUrl ->
            if (imageUrl != null) {
                updateUserProfile(displayName, imageUrl)
            } else {
                handleError("Failed to upload profile picture. Please try again.")
            }
        }
    }

    private fun updateUserProfile(displayName: String, photoUrl: String?) {
        val currentUser = auth.currentUser ?: return
        val userId = getUserId() ?: return

        binding?.progressBar?.visibility = View.VISIBLE

        // Update display name in Firebase Authentication
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(if (photoUrl != null) android.net.Uri.parse(photoUrl) else null)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Model.shared.getUserById(userId) { existingUser ->
                        val user = existingUser ?: User(
                            id = currentUser.uid,
                            email = currentUser.email ?: "",
                            displayName = currentUser.displayName ?: "",
                            photoUrl = "",
                            password = "",
                            lastUpdated = System.currentTimeMillis()
                        )

                        if (user.displayName != displayName) {
                            user.displayName = displayName
                        }

                        if (photoUrl != null) {
                            user.photoUrl = photoUrl
                        }

                        Model.shared.editUser(user) {
                            binding?.progressBar?.visibility = View.GONE
                            binding?.saveChangesButton?.isEnabled = true
                            binding?.cancelButton?.isEnabled = true
                            Toast.makeText(
                                requireContext(),
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            User.saveUserToLocalStorage(
                                requireContext(),
                                currentUser.uid,
                                currentUser.email ?: "",
                                currentUser.displayName ?: ""
                            )
                            toggleEditMode(false)
                            displayUserInfo()
                        }

                    }
                } else {
                    handleError("Failed to update profile: ${task.exception?.message}")
                }
            }
    }

    private fun handleError(message: String) {
        binding?.progressBar?.visibility = View.GONE
        binding?.saveChangesButton?.isEnabled = true
        binding?.cancelButton?.isEnabled = true
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        Log.e("ProfileFragment", message)
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
        val currentUserId =getUserId();
        userRecipes = viewModel.recipes.value?.filter { it.userId == currentUserId } ?: emptyList()

    }
}