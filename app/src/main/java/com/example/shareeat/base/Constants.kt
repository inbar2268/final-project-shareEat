package com.example.shareeat.base

import com.example.shareeat.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias UserCallback = (User) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object Collections {
        const val USERS = "students"
    }
}