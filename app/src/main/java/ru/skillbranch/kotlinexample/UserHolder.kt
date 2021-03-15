package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.util.*

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also { user ->
                if (map[user.login] != null) {
                    throw IllegalArgumentException()
                }
                map[user.login] = user
            }
    }

    fun registerUserByPhone(
        fullName: String, rawPhone: String
    ): User {
        return User.makeUser(fullName, phone = rawPhone)
            .also { user ->
                if (map[rawPhone] != null) {
                    throw IllegalArgumentException()
                }
                map[rawPhone] = user
            }
    }

    fun requestAccessCode(login: String) {
        map[login]?.let { user ->
           user.requestAccessCode()
        }
    }

    fun loginUser(login: String, password: String): String? {
        return map[login.trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }
}
