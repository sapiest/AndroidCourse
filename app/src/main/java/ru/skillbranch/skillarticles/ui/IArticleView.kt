package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.BottomBarData
import ru.skillbranch.skillarticles.viewmodels.SubmenuData


interface IArticleView {
    fun setupSubmenu()
    fun setupBottombar()
    fun renderUi(data: ArticleState)
    fun renderBottombar(data: BottomBarData)
    fun renderSubmenu(data: SubmenuData)
    fun setupToolbar()
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)
    fun renderSearchPosition(searchPosition: Int)
    fun clearSearchResult()
    fun showSearchBar(resultsCount: Int, searchPosition: Int)
    fun hideSearchBar()

}