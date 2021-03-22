package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.util.*

object UserHolder {
    private val map = mutableMapOf<String, User>()

    private const val NAME_INDEX = 0
    private const val EMAIL_INDEX = 1
    private const val HASH_INDEX = 2
    private const val PHONE_INDEX = 3

    fun importUsers(list: List<String>): List<User> {
        val newList = mutableListOf<User>()
        list.forEach { userString ->
            val userData = userString.split(";")
            newList.add(registerUserFromCsv(userData))
        }
        return newList
    }

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also { user ->
                if (map[user.login] != null) {
                    throw IllegalArgumentException("A user with this email already exists")
                }
                map[user.login] = user
            }
    }

    fun registerUserFromCsv(
        list: List<String>
    ): User {
        val fullName = list[NAME_INDEX]
        val email = list[EMAIL_INDEX]
        val rawPhone = list[PHONE_INDEX]
        val hash = list[HASH_INDEX].replace(":", "")

        return User.makeUser(fullName, email = email, phone = rawPhone, hash = hash)
            .also { user ->
                if (map[user.login] != null) {
                    throw IllegalArgumentException("A user with this email already exists")
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
                    throw IllegalArgumentException("A user with this phone already exists")
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
