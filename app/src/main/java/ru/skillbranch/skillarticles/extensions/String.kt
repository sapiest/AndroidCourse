package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(
    subStr: String,
    ignoreCase: Boolean = true
): List<Int> {
    val list = arrayListOf<Int>()
    var lastIndex = 0
    while (lastIndex >= 0 && lastIndex < this?.length ?: 0) {
        lastIndex = this?.indexOf(subStr, lastIndex, ignoreCase) ?: -1
        if(lastIndex >= 0){
            list.add(lastIndex)
            lastIndex += subStr.length
        }
    }
    return list
}