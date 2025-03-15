package com.example.shareeat.adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.R
import com.example.shareeat.databinding.ItemSearchUserBinding
import com.example.shareeat.model.User
import com.squareup.picasso.Picasso

class SearchUserViewHolder(
    private val binding: ItemSearchUserBinding,
    private val onUserClick: (User) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.userName.text = user.displayName

        // Load profile image or placeholder
        if (user.photoUrl.isNotBlank()) {
            Picasso.get()
                .load(user.photoUrl)
                .placeholder(R.drawable.avatar)
                .into(binding.userImage)
        } else {
            binding.userImage.setImageResource(R.drawable.avatar)
        }

        // Set click listener
        itemView.setOnClickListener {
            onUserClick(user)
        }
    }
}
