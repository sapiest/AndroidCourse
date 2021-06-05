package ru.skillbranch.skillarticles.data.adapters

import ru.skillbranch.skillarticles.data.local.User

class UserJsonAdapter() : JsonAdapter<User> {
    override fun fromJson(json: String): User? {
        if (json.isEmpty()) return null
        val list = json.substring(1, json.lastIndex).split(",")
        val user =  User(
            id = list.getOrNull(0)?.trim() ?: "",
            name = list.getOrNull(1)?.trim() ?: "",
            avatar = if(list.getOrNull(2)?.trim()?.isEmpty() == true) null else list.getOrNull(2)?.trim(),
            rating = list.getOrNull(3)?.trim()?.toInt() ?: 0,
            respect = list.getOrNull(4)?.trim()?.toInt() ?: 0,
            about = if(list.getOrNull(5)?.trim()?.isEmpty() == true) null else list.getOrNull(5)?.trim(),
        )
        return user
    }

    override fun toJson(obj: User?): String {
        val list = arrayListOf<String>()
        list.add(obj?.id.toString())
        list.add(obj?.name.toString())
        list.add(obj?.avatar ?: "")
        list.add(obj?.rating.toString())
        list.add(obj?.respect.toString())
        list.add(obj?.about ?: "")
        return list.toString()
    }
}