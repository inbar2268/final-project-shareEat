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
import com.example.shareeat.model.Model
import com.example.shareeat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        auth = FirebaseAuth.getInstance()
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
        binding?.signUpButton?.setOnClickListener(::onSignUpClicked)
        binding?.loginLink?.setOnClickListener(::onLoginClicked)

        return binding?.root
    }

    private fun onSignUpClicked(view: View) {
        val name = binding?.firstNameInput?.text.toString().trim()
        val email = binding?.emailInput?.text.toString().trim()
        val password = binding?.passwordInput?.text.toString().trim()
        val confirmPassword = binding?.confirmPasswordInput?.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        binding?.progressBar?.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        val user = User(
                            id = userId,
                            displayName = name,
                            email = email,
                            photoUrl = "",
                            password = password
                        )

                        val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        auth.currentUser?.updateProfile(userProfileChangeRequest)
                            ?.addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Model.shared.addUser(user) {
                                        binding?.progressBar?.visibility = View.GONE
                                        Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                                        auth.signOut()
                                        Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment)
                                    }
                                } else {
                                    binding?.progressBar?.visibility = View.GONE
                                    Toast.makeText(requireContext(), "Error setting display name", Toast.LENGTH_SHORT).show()
                                }
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
        Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
