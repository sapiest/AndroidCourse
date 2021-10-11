package ru.skillbranch.skillarticles.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.databinding.LayoutSubmenuBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.*
import ru.skillbranch.skillarticles.ui.custom.spans.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")
    private val viewmodel: ArticleViewModel by viewModels<ArticleViewModel> { viewModelFactory }
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
        setupCopyListener()

        viewmodel.observeState(this, ::renderUi)
        viewmodel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)
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
            is Notify.ActionMessage -> {
                val (_, label, handler) = notify
                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(label) { handler.invoke() }
                }
            }

            is Notify.ErrorMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    handler ?: return@with
                    setAction(label) { handler.invoke() }
                }
            }

            else -> {

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
            if (!tvTextContent.hasFocus()) tvTextContent.requestFocus()
            searchView.clearFocus()
            viewmodel.handleUpResult()
        }

        bottombar.binding.btnResultDown.setOnClickListener {
            if (!tvTextContent.hasFocus()) tvTextContent.requestFocus()
            searchView.clearFocus()
            viewmodel.handleDownResult()
        }

        bottombar.binding.btnSearchClose.setOnClickListener {
            viewmodel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
    }

    override fun renderBotombar(data: BottombarData) {
        btnSettings.isChecked = data.isShowMenu
        btnLike.isChecked = data.isLike
        btnBookmark.isChecked = data.isBookmark

        if (data.isSearch) {
            showSearchBar(data.resultsCount, data.searchPosition)
            with(vb.toolbar) {
                (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
            }
        } else {
            hideSearchBar()

        }
    }

    override fun renderSubmenu(data: SubmenuData) {
        switchMode.isChecked = data.idDarkMode
        btnTextDown.isChecked = !data.isBigText
        btnTextUp.isChecked = data.isBigText

        if (data.isShowMenu) submenu.open() else submenu.close()
    }


    override fun renderUi(data: ArticleState) {
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with(tvTextContent) {
            textSize = if (data.isBigText) 18f else 14f
            isLoading = data.content.isEmpty()
            setContent(data.content)

//            movementMethod = LinkMovementMethod()
//
//            MarkdownBuilder(context).markdownToSpan(data.content)
//                .run { setText(this, TextView.BufferType.SPANNABLE) }
////            val content = if (data.isLoadingContent) "Loading" else data.content.first()
////            if (text.toString() == content) {
////                setText(content, TextView.BufferType.SPANNABLE)
////            }
        }

//        //bind content
//        tvTextContent.text =
//            if (data.isLoadingContent) "Loading" else data.content

        //bind toolbar

        with(toolbar) {
            title = data.title ?: "Loading"
            subtitle = data.category ?: "Loading"
            if (data.categoryIcon != null) logo =
                ContextCompat.getDrawable(this@RootActivity, data.categoryIcon as Int)
        }

        if (data.isLoadingContent) return

        if (data.isSearch) {
            renderSearchResult(data.searchResults)
            renderSearchPosition(data.searchPosition, data.searchResults)
        } else clearSearchResult()
    }

    override fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val logo = toolbar.children.find { it is AppCompatImageView } as? ImageView
        logo ?: return
        logo.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }


    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        tvTextContent.renderSearchResult(searchResult)
//        val content = tvTextContent.text as Spannable
//
//        clearSearchResult()
//
//        searchResult.forEach { (start, end) ->
//            content.setSpan(
//                SearchSpan(),
//                start,
//                end,
//                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
    }

    override fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>) {
        tvTextContent.renderSearchPosition(searchResult.getOrNull(searchPosition))
//        val content = tvTextContent.text as Spannable
//
//        val spans = content.getSpans<SearchSpan>()
//
//        //remove old search focus span
//        content.getSpans<SearchFocusSpan>().forEach {
//            content.removeSpan(it)
//        }
//
//        if (spans.isNotEmpty()) {
//            val result = spans[searchPosition]
//            Selection.setSelection(content, content.getSpanStart(result))
//            content.setSpan(
//                SearchFocusSpan(),
//                content.getSpanStart(result),
//                content.getSpanEnd(result),
//                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
    }

    override fun clearSearchResult() {
        tvTextContent.clearSearchResult()
//        val content = tvTextContent.text as Spannable
//        content.getSpans<SearchSpan>()
//            .forEach { content.removeSpan(it) }
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vbBottomBar) {
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        with(vbBottomBar) {
            setSearchState(false)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }


    override fun setupCopyListener() {
        tvTextContent.setCopyListener{ copy ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied code", copy)
            clipboard.setPrimaryClip(clip)
            viewmodel.handleCopyCode()
        }
    }

}