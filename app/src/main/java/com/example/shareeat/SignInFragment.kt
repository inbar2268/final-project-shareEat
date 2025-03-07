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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class SignInFragment : Fragment() {

    private var binding: FragmentSignInBinding? = null
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
        binding = FragmentSignInBinding.inflate(layoutInflater, container, false)

        binding?.loginButton?.setOnClickListener(::onLoginClicked)
        binding?.registerLink?.setOnClickListener(::onRegisterClicked)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, navigate to main screen
            /*
                        view?.let { Navigation.findNavController(it).navigate(R.id.action_signInFragment_to_mainFragment) }
            */
        }

        return binding?.root
    }

    private fun onLoginClicked(view: View) {

        binding?.progressBar?.visibility = View.VISIBLE

        val email = binding?.emailInput?.text.toString().trim()
        val password = binding?.passwordInput?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            binding?.progressBar?.visibility = View.GONE
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding?.progressBar?.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
//                    Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_mainFragment)
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(requireContext(), "Email not found. Please register.", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(requireContext(), "Invalid password. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun onRegisterClicked(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_signUpFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
