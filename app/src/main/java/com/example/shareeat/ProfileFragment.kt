package com.example.shareeat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.shareeat.databinding.FragmentProfileBinding
import com.example.shareeat.model.User.Companion.clearAllUserData
import com.example.shareeat.model.User.Companion.getUserFromLocalStorage
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        // Display user information
        displayUserInfo()

        // Set up logout button
        binding?.logoutButton?.setOnClickListener {
            auth.signOut()
            clearAllUserData(requireContext())
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(it).navigate(R.id.action_ProfileFragment_to_signInFragment)
        }

        return binding?.root
    }

    private fun displayUserInfo() {
//        val currentUser = auth.currentUser
//        currentUser?.let {
//            binding?.userEmailText?.text = "Email: ${it.email}"
//            binding?.userIdText?.text = "User ID: ${it.uid}"
//            binding?.userNameText?.text = "Display Name: ${it.displayName ?: "Not set"}"
//        }

        val userData = getUserFromLocalStorage(requireContext())
        binding?.userEmailText?.text = userData["uid"]
        binding?.userIdText?.text = userData["email"]
        binding?.userNameText?.text = userData["displayName"]

        }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}