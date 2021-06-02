package ru.skillbranch.skillarticles.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.databinding.LayoutSubmenuBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.custom.*
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    private val viewmodel: ArticleViewModel by viewModels<ArticleViewModel> { ViewModelFactory(this, "0") }
    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)

    private val vbBottomBar: Bottombar
        get() = vb.bottombar
    private val vbSubmenu: LayoutSubmenuBinding
        get() = vb.submenu.binding

    private val toolbar: Toolbar by lazy { vb.toolbar }
    private val btnLike: CheckableImageView by lazy { vbBottomBar.binding.btnLike }
    private val btnBookmark: CheckableImageView by lazy { vbBottomBar.binding.btnBookmark }
    private val btnShare: ImageView by lazy { vbBottomBar.binding.btnShare }
    private val btnSettings: CheckableImageView by lazy { vbBottomBar.binding.btnSettings }
    private val switchMode: SwitchMaterial by lazy { vbSubmenu.switchMode }
    private val root: CoordinatorLayout by lazy { vb.coordinatorContainer }
    private val bottombar: Bottombar by lazy { vb.bottombar }
    private val submenu: ArticleSubmenu by lazy { vb.submenu }
    private val btnTextUp: CheckableImageView by lazy { vbSubmenu.btnTextUp }
    private val btnTextDown: CheckableImageView by lazy { vbSubmenu.btnTextDown }
    private val tvTextContent: TextView by lazy { vb.tvTextContent }

    private lateinit var searchView: SearchView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val bgColor by AttrValue(R.attr.colorSecondary)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val fgColor by AttrValue(R.attr.colorOnSecondary)

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewmodel.observeState(this, ::renderUi)
        viewmodel.observeSubState(this, ArticleState::toBottomBarData, ::renderBottombar)
        viewmodel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)

        viewmodel.observeNotifications(this) {
            renderNotifications(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        searchView = (menuItem?.actionView as SearchView)
        searchView.queryHint = "Search"

        if (viewmodel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewmodel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewmodel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewmodel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewmodel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewmodel.handleSearch(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewmodel.saveState()
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("ShowToast")
    private fun renderNotifications(notify: Notify) {
        val snackbar = Snackbar.make(root, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))

        when (notify) {
            is Notify.TextMessage -> {
            }

            is Notify.ActionMessage -> {
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }

            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }

    override fun setupSubmenu() {
        btnTextUp.setOnClickListener { viewmodel.handleUpText() }
        btnTextDown.setOnClickListener { viewmodel.handleDownText() }
        switchMode.setOnClickListener { viewmodel.handleNightMode() }
    }

    override fun setupBottombar() {
        btnLike.setOnClickListener { viewmodel.handleLike() }
        btnBookmark.setOnClickListener { viewmodel.handleBookmark() }
        btnShare.setOnClickListener { viewmodel.handleShare() }
        btnSettings.setOnClickListener { viewmodel.handleToggleMenu() }

        bottombar.binding.btnResultUp.setOnClickListener {
            searchView.clearFocus()
            viewmodel.handleUpResult()
        }

        bottombar.binding.btnResultDown.setOnClickListener {
            searchView.clearFocus()
            viewmodel.handleDownResult()
        }

        bottombar.binding.btnSearchClose.setOnClickListener {
            viewmodel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
    }

    override fun renderUi(data: ArticleState) {
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with(tvTextContent){
            textSize = if (data.isBigText) 18f else 14f
            movementMethod = ScrollingMovementMethod()
            val content = if (data.isLoadingContent) "Loading" else data.content.first()
            if(text.toString() == content) {
                setText(content, TextView.BufferType.SPANNABLE)
            }
        }

        //bind content
        tvTextContent.text =
            if (data.isLoadingContent) "Loading" else data.content.first() as String

        //bind toolbar

        with(toolbar){
            title = data.title ?: "Loading"
            subtitle = data.category ?: "Loading"
            if (data.categoryIcon != null) logo =
                ContextCompat.getDrawable(this@RootActivity, data.categoryIcon as Int)

        }


        if(data.isSearch){
            renderSearchResult(data.searchResults)
            renderSearchPosition(data.searchPosition)
        }else clearSearchResult()
    }

    override fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }


    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = tvTextContent.text as Spannable

        clearSearchResult()

        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(bgColor, fgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun renderSearchPosition(searchPosition: Int) {
        val content = tvTextContent.text as Spannable

        val spans = content.getSpans<SearchSpan>()

        //remove old search focus span
        content.getSpans<SearchFocusSpan>().forEach {
            content.removeSpan(it)
        }

        if(spans.isNotEmpty()){
            val result = spans[searchPosition]
            Selection.setSelection(content, content.getSpanStart(result))
            content.setSpan(
                SearchFocusSpan(bgColor, fgColor),
                content.getSpanStart(result),
                content.getSpanEnd(result),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun clearSearchResult() {
        val content = tvTextContent.text as Spannable
        content.getSpans<SearchSpan>()
            .forEach { content.removeSpan(it) }
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vbBottomBar){
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        //vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        //TODO("Not yet implemented")
        //vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun renderBottombar(data: BottomBarData) {
        btnSettings.isChecked = data.isShowMenu
        btnLike.isChecked = data.isLike
        btnBookmark.isChecked = data.isBookmark

        if(data.isSearch) showSearchBar(data.resultsCount, data.searchPosition)
    }

    override fun renderSubmenu(data: SubmenuData) {
        switchMode.isChecked = data.idDarkMode
        btnTextDown.isChecked = !data.isBigText
        btnTextUp.isChecked = data.isBigText

        if (data.isShowMenu) submenu.open() else submenu.close()
    }
}