package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    val newList = mutableListOf<T>()
    this.forEach { elem ->
        if (predicate(elem)) {
            return newList
        }
        newList.add(elem)
    }
    return newList
}