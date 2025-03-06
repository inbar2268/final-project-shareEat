package com.example.shareeat.base

import com.example.shareeat.model.User

typealias StudentsCallback = (List<User>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object Collections {
        const val USERS = "students"
    }
}