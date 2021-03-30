package ru.skillbranch.skillarticles.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import ru.skillbranch.skillarticles.ui.custom.CheckableImageView
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private val toolbar: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val btnLike: CheckableImageView by lazy { findViewById<CheckableImageView>(R.id.btn_like) }
    private val btnBookmark: CheckableImageView by lazy { findViewById<CheckableImageView>(R.id.btn_bookmark) }
    private val btnShare: ImageView by lazy { findViewById<ImageView>(R.id.btn_share) }
    private val btnSettings: CheckableImageView by lazy { findViewById<CheckableImageView>(R.id.btn_settings) }
    private val switchMode: SwitchMaterial by lazy { findViewById<SwitchMaterial>(R.id.switch_mode) }
    private val root: CoordinatorLayout by lazy { findViewById<CoordinatorLayout>(R.id.coordinator_container) }
    private val bottombar: Bottombar by lazy { findViewById<Bottombar>(R.id.bottombar) }
    private val submenu: ArticleSubmenu by lazy { findViewById<ArticleSubmenu>(R.id.submenu) }
    private val btnTextUp: CheckableImageView by lazy { findViewById<CheckableImageView>(R.id.btn_text_up) }
    private val btnTextDown: CheckableImageView by lazy { findViewById<CheckableImageView>(R.id.btn_text_down) }
    private val tvTextContent: TextView by lazy { findViewById<TextView>(R.id.tv_text_content) }


    private lateinit var viewmodel: ArticleViewModel

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        val vmFactory = ViewModelFactory("0")
        viewmodel = ViewModelProvider(viewModelStore, vmFactory).get(ArticleViewModel::class.java)
        viewmodel.observeState(this) {
            renderUi(it)
        }
        viewmodel.observeNotifications(this) {
            renderNotifications(it)
        }
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
                    setAction(notify.errorLabel) {
                        notify.errorHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }

    private fun setupSubmenu() {
        btnTextUp.setOnClickListener { viewmodel.handleUpText() }
        btnTextDown.setOnClickListener { viewmodel.handleDownText() }
        switchMode.setOnClickListener { viewmodel.handleNightMode() }
    }

    private fun setupBottombar() {
        btnLike.setOnClickListener { viewmodel.handleLike() }
        btnBookmark.setOnClickListener { viewmodel.handleBookmark() }
        btnShare.setOnClickListener { viewmodel.handleShare() }
        btnSettings.setOnClickListener { viewmodel.handleToggleMenu() }
    }

    private fun renderUi(data: ArticleState) {
        //bind submenu
        btnSettings.isChecked = data.isShowMenu
        if (data.isShowMenu) submenu.open() else submenu.close()

        //bind article person data
        btnLike.isChecked = data.isLike
        btnBookmark.isChecked = data.isBookmark

        //bind submenu views
        switchMode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (data.isBigText) {
            tvTextContent.textSize = 18f
            btnTextUp.isChecked = true
            btnTextDown.isChecked = false
        } else {
            tvTextContent.textSize = 14f
            btnTextUp.isChecked = false
            btnTextDown.isChecked = false
        }

        //bind content
        tvTextContent.text =
            if (data.isLoadingContent) "Loading" else data.content.first() as String

        //bind toolbar
        toolbar.title = data.title ?: "Loading"
        toolbar.subtitle = data.category ?: "Loading"
        if (data.categoryIcon != null) toolbar.logo =
            ContextCompat.getDrawable(this, data.categoryIcon as Int)
    }

    private fun setupToolbar() {
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
}