package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(
    subStr: String,
    ignoreCase: Boolean = true
): List<Int> {
//    val list = arrayListOf<Int>()
//    var lastIndex = 0

//    if (lastIndex == -1) break
//    list.add(lastIndex)
//    lastIndex += subStr.length


//    val lastIndex = this?.indexOf(subStr, 0, ignoreCase) ?: -1
//    return if(lastIndex != -1){
//        listOf(lastIndex) + this?.substring(lastIndex + subStr.length, lastIndex).indexesOf(subStr, ignoreCase)
//    }else{
//        emptyList()
//    }
    val list = arrayListOf<Int>()
    var index = 0
    //var lastIndex = this?.indexOf(subStr, 0, ignoreCase) ?: -1
    while (index != -1){
        index = this?.indexOf(subStr, 0, ignoreCase) ?: -1
        if(index == -1){
            return list
        }else{
            list.add(index)
        }
    }

    return list
}