package com.example.shareeat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shareeat.databinding.ItemSearchUserBinding
import com.example.shareeat.model.User

class SearchUserAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<SearchUserViewHolder>() {

    fun setUsers(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSearchUserBinding.inflate(inflater, parent, false)
        return SearchUserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}
