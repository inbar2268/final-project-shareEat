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
import com.example.shareeat.databinding.FragmentSignInBinding
import com.example.shareeat.model.Model
import com.example.shareeat.model.User

class SignInFragment : Fragment() {

    private var binding: FragmentSignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(layoutInflater, container, false)

        // Set up click listeners
        binding?.loginButton?.setOnClickListener(::onLoginClicked)
        binding?.forgotPasswordLink?.setOnClickListener(::onForgotPasswordClicked)
        binding?.registerLink?.setOnClickListener(::onRegisterClicked)

        return binding?.root
    }

    private fun onLoginClicked(view: View) {
        // Show progress bar while attempting login
        binding?.progressBar?.visibility = View.VISIBLE

        val email = binding?.emailInput?.text.toString().trim()
        val password = binding?.passwordInput?.text.toString().trim()

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            binding?.progressBar?.visibility = View.GONE
            return
        }

//        // Attempt login using Model class
//        Model.shared.signInUser(email, password) { success, error ->
//            // Hide progress bar
//            binding?.progressBar?.visibility = View.GONE
//
//            if (success) {
//                // Navigate to main screen after successful login
//                Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_mainFragment)
//            } else {
//                // Show error message
//                Toast.makeText(requireContext(), error ?: "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    private fun onForgotPasswordClicked(view: View) {
        // Navigate to password reset screen
//        Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_resetPasswordFragment)
    }

    private fun onRegisterClicked(view: View) {
        // Navigate to registration screen
//        Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_registerFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}