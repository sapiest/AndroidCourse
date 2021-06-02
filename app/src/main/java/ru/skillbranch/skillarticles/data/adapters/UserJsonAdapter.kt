package ru.skillbranch.skillarticles.data.adapters

import ru.skillbranch.skillarticles.data.local.User

class UserJsonAdapter() : JsonAdapter<User>{
    override fun fromJson(json: String): User? {
        //TODO implement me
        return null
    }

    override fun toJson(obj: User?): String {
        //TODO implement me
        return ""
    }
}