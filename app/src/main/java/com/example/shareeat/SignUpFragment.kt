package com.example.shareeat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.shareeat.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        // Set up click listeners
        binding?.signUpButton?.setOnClickListener(::onSignUpClicked)
        binding?.loginLink?.setOnClickListener(::onLoginClicked)

        return binding?.root
    }

    private fun onSignUpClicked(view: View) {
        // Get user inputs
        val firstName = binding?.firstNameInput?.text.toString().trim()
        val lastName = binding?.lastNameInput?.text.toString().trim()
        val email = binding?.emailInput?.text.toString().trim()
        val password = binding?.passwordInput?.text.toString().trim()
        val confirmPassword = binding?.confirmPasswordInput?.text.toString().trim()

        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        binding?.progressBar?.visibility = View.VISIBLE

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Get current user ID
                    val userId = auth.currentUser?.uid

                    // Create user profile in Firestore
                    if (userId != null) {
                        val user = hashMapOf(
                            "userId" to userId,
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        // Add user to Firestore
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                binding?.progressBar?.visibility = View.GONE
                                Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                                // Navigate to main screen or sign in page
//                                Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_mainFragment)
                            }
                            .addOnFailureListener { e ->
                                binding?.progressBar?.visibility = View.GONE
                                Toast.makeText(requireContext(), "Error creating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error creating account", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding?.progressBar?.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun onLoginClicked(view: View) {
        // Navigate back to sign in fragment
        Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}