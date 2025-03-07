package com.example.shareeat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.shareeat.databinding.FragmentHomePageBinding
import com.google.firebase.auth.FirebaseAuth

class HomePageFragment : Fragment() {

    private var binding: FragmentHomePageBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePageBinding.inflate(layoutInflater, container, false)

        // Display user information
        displayUserInfo()

        // Set up logout button
        binding?.logoutButton?.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(it).navigate(R.id.action_homePageFragment_to_signInFragment)
        }

        return binding?.root
    }

    private fun displayUserInfo() {
        val currentUser = auth.currentUser
        currentUser?.let {
            binding?.userEmailText?.text = "Email: ${it.email}"
            binding?.userIdText?.text = "User ID: ${it.uid}"
            binding?.userNameText?.text = "Display Name: ${it.displayName ?: "Not set"}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
