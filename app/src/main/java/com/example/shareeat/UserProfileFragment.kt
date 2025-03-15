package com.example.shareeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.shareeat.databinding.FragmentUserProfileBinding
import com.example.shareeat.model.Model
import com.example.shareeat.model.User

class UserProfileFragment : Fragment() {
    private var binding: FragmentUserProfileBinding? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            userId = it.getString("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_LONG).show()
        } else {
            fetchUserProfile(userId!!)
        }
    }

    private fun fetchUserProfile(userId: String) {
        binding?.progressBar?.visibility = View.VISIBLE

        Model.shared.getUserById(userId) { user ->
            binding?.progressBar?.visibility = View.GONE

            if (user != null) {
                binding?.displayNameText?.text = user.displayName
                binding?.emailText?.text = user.email

                if (!user.photoUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(user.photoUrl)
                        .placeholder(R.drawable.avatar)
                        .circleCrop()
                        .into(binding?.profileImage!!)
                } else {
                    binding?.profileImage?.setImageResource(R.drawable.avatar)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
