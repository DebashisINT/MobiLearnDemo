package com.breezemobilearndemo
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.os.Handler
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezefieldsalesdemo.features.logout.presentation.api.LogoutRepositoryProvider
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.ActivityDashboardBinding
import com.breezemobilearndemo.databinding.MenuItemLayoutBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.Locale
import java.util.Stack


class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

        var binding: ActivityDashboardBinding? = null
    val dashView get() = binding!!
    lateinit var toolbarTitle: TextView
    private lateinit var toggle: ActionBarDrawerToggle
    private var backPressedTime: Long = 0
    private val fragmentStack = Stack<String>()
    private var backpressed: Long = 0

    companion object {
        @JvmStatic
        val compositeDisposable: CompositeDisposable = CompositeDisposable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(dashView.root)

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_lms))
        dashView.dashToolbar.customToolbar.visibility = View.VISIBLE
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initView()

        if (intent != null && intent.extras != null) {
            if (intent.getStringExtra("TYPE")
                    .equals("lms_content_assign", ignoreCase = true)
            ) {
                Handler().postDelayed(Runnable {
                                                loadFrag(NotificationFragment(), NotificationFragment::class.java.name , false)
                                               }, 500)
            }
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(fcmReceiver, IntentFilter("FCM_ACTION_RECEIVER"))

        }


    private fun initView() {
        toggle = ActionBarDrawerToggle(
            this,
            dashView.drawer,
            findViewById(R.id.dashToolbar),
            R.string.openDrawer,
            R.string.closeDrawer
        )
        dashView.drawer.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
        dashView.navView.getHeaderView(0).findViewById<TextView>(R.id.tv_menu_header_name).text = Pref.user_name
        dashView.navView.getHeaderView(0).findViewById<TextView>(R.id.tv_menu_adv_login_time).text = "Last login time : " + Pref.login_time
        dashView.navView.getHeaderView(0).findViewById<TextView>(R.id.tv_menu_adv_version).text = AppUtils.getVersionName(this@DashboardActivity)
        dashView.navView.setNavigationItemSelectedListener(this)

        dashView.navView.itemIconTintList = null
        //dashView.navView.menu.findItem(R.id.menu_privacy_policy).setIcon(R.drawable.ic_privacy_policy)

        toolbarTitle = findViewById(R.id.toolbarText)
        dashView.dashToolbar.ivHomeIcon.setOnClickListener(this)
        dashView.dashToolbar.logo.setOnClickListener(this)
        dashView.navView.getHeaderView(0).findViewById<LinearLayout>(R.id.ll_menu_adv_logout).setOnClickListener(this)
        dashView.navView.getHeaderView(0).findViewById<CardView>(R.id.cv_menu_adv_voice).setOnClickListener(this)
        dashView.dashToolbar.addBookmark.setOnClickListener(this)

        loadFrag(MyLearningFragment(), MyLearningFragment::class.java.name)

        toolbarTitle.text = "Home"

        supportFragmentManager.addOnBackStackChangedListener {
            handleToolbarVisibility()
        }

        dashView.navView.getHeaderView(0).findViewById<EditText>(R.id.et_search_menu)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString()
                    if (query.isEmpty()) {
                        restoreMenuItems()
                    } else {
                        filterMenuItems(query)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })


        dashView.navView.getHeaderView(0).findViewById<CardView>(R.id.cv_menu_adv_voice).setOnClickListener {
            val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            Handler().postDelayed(Runnable {
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
            }, 1000)
            try {
                startActivityForResult(intent, 7009)
                Handler().postDelayed(Runnable {
                }, 3000)

            } catch (a: ActivityNotFoundException) {
                a.printStackTrace()
            }
        }

        dashView.navView.getHeaderView(0).findViewById<LinearLayout>(R.id.ll_menu_adv_logout).setOnClickListener {
            CommonDialog.getInstance(
                AppUtils.hiFirstNameText() + "!",
                getString(R.string.confirm_logout),
                getString(R.string.cancel),
                getString(R.string.ok),
                object : CommonDialogClickListener {
                    override fun onLeftClick() {

                    }

                    override fun onRightClick(editableData: String) {
                        logoutAPICalling()
                    }

                }).show(supportFragmentManager, "")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu) // Inflate your menu resource

        for (i in 0 until menu!!.size()) {
            val menuItem = menu.getItem(i)
            val actionView = menuItem.actionView

            // Check if actionView is not null
            if (actionView != null) {
                // Use ViewBinding to access the views
                val binding = MenuItemLayoutBinding.bind(actionView) // Replace with your actual binding class

                // Now you can access the views directly
                val icon = binding.menuIcon // Access the ImageView
                val title = binding.menuTitle // Access the TextView

                // Set the icon and title as needed
                icon.setImageDrawable(menuItem.icon) // Set the icon directly from the MenuItem
                title.text = menuItem.title // Set the title directly from the MenuItem
            }
        }

        return true
    }
    private fun logoutAPICalling() {

        val repository = LogoutRepositoryProvider.provideLogoutRepository()
        DashboardActivity.compositeDisposable.add(
            repository.logout(Pref.user_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    val logoutResponse = result as BaseResponse

                    if (logoutResponse.status == NetworkConstant.SUCCESS) {

                        clearData()
                        //finish()

                    }
                    else {
                        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    }

                },
                    { error ->
                        error.printStackTrace()
                    })
        )
    }

    fun restoreMenuItems() {

        for (i in 0 until dashView.navView.menu.size()) {
            val menuItem = dashView.navView.menu.getItem(i)
            menuItem.isVisible = true

            if (menuItem.hasSubMenu()) {
                val subMenu = menuItem.subMenu
                for (j in 0 until subMenu!!.size()) {
                    subMenu.getItem(j).isVisible = true
                }
            }
        }
    }

    fun filterMenuItems(query: String) {

        for (i in 0 until dashView.navView.menu.size()) {
            val menuItem = dashView.navView.menu.getItem(i)
            val isVisible = menuItem.title.toString().contains(query, ignoreCase = true)
            menuItem.isVisible = isVisible

            if (menuItem.hasSubMenu()) {
                val subMenu = menuItem.subMenu
                for (j in 0 until subMenu!!.size()) {
                    val subMenuItem = subMenu!!.getItem(j)
                    val isSubItemVisible = subMenuItem.title.toString().contains(query, ignoreCase = true)
                    subMenuItem.isVisible = isSubItemVisible
                }
            }
        }
    }

    private fun handleToolbarVisibility() {
        val currentFragment = supportFragmentManager.findFragmentById(dashView.fragContainerView.id)
        when (currentFragment) {
            is MyLearningFragment -> {
                dashView.dashToolbar.ivHomeIcon.visibility = View.GONE
                dashView.dashToolbar.addBookmark.visibility = View.VISIBLE
                dashView.dashToolbar.logo.visibility = View.VISIBLE
                dashView.dashToolbar.tvNotiCount.visibility = View.VISIBLE
                dashView.dashToolbar.downLogoSpc.visibility = View.GONE
                toolbarTitle.text = "Home"
            }
            is VideoPlayLMS -> {
                dashView.dashToolbar.ivHomeIcon.visibility = View.GONE
                dashView.dashToolbar.addBookmark.visibility = View.VISIBLE
                dashView.dashToolbar.logo.visibility = View.GONE
                dashView.dashToolbar.tvNotiCount.visibility = View.GONE
            }
            else -> {
                dashView.dashToolbar.ivHomeIcon.visibility = View.VISIBLE
                dashView.dashToolbar.addBookmark.visibility = View.VISIBLE
                dashView.dashToolbar.logo.visibility = View.GONE
                dashView.dashToolbar.upLogoSpc.visibility = View.GONE
                dashView.dashToolbar.tvNotiCount.visibility = View.GONE
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                loadFrag(MyLearningFragment(), MyLearningFragment::class.java.name,isAdd = false)
                dashView.dashToolbar.ivHomeIcon.visibility = View.GONE
                toolbarTitle.text = "Home"
            }

            R.id.menu_my_learning -> {
                loadFrag(MyLearningTopicList(), MyLearningTopicList::class.java.name,isAdd = false)
            }

            R.id.menu_all_topics -> {
                loadFrag(SearchLmsKnowledgeFrag(), SearchLmsKnowledgeFrag::class.java.name,isAdd = false)
            }
            R.id.menu_my_topics -> {
                loadFrag(SearchLmsFrag(), SearchLmsFrag::class.java.name,isAdd = false)
            }
            R.id.menu_my_performance -> {
                loadFrag(MyPerformanceFrag(), MyPerformanceFrag::class.java.name,isAdd = false)
            }
            R.id.menu_leaderboard -> {
                loadFrag(LeaderboardLmsFrag(), LeaderboardLmsFrag::class.java.name,isAdd = false)
            }

            /*R.id.menu_privacy_policy -> {
                loadFrag(PrivacypolicyWebviewFrag(), PrivacypolicyWebviewFrag::class.java.name, false)
            }*/
        }
        dashView.drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun clearData() {
                    Pref.user_id = ""
                    Pref.session_token = ""
                    Pref.login_date = ""
                    Pref.login_time = ""

                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finishAffinity()
    }

    fun loadFrag(fragment: Fragment, tag: String, isAdd: Boolean = false) {
        fragmentStack.push(tag)
        dashView.drawer.closeDrawer(GravityCompat.START)

        val transaction = supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if (isAdd) {
            transaction.add(dashView.fragContainerView.id, fragment, tag)
                .addToBackStack(tag)
        } else {
            transaction.replace(dashView.fragContainerView.id, fragment, tag)
                .addToBackStack(tag)
        }

        if(fragment is MyLearningFragment){
            showHamburgerIcon()
        }else{
            showBackArrow()
        }

        transaction.commitAllowingStateLoss()

        // Initialize Firebase if not already initialized
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Retrieve the FCM token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Log.d("token_firebase", "FCM Token: $token")

            }
        CustomStatic.IsBackClick = false
        println("load_frag " + fragment.toString() + " " + Pref.user_id.toString())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            dashView.dashToolbar.ivHomeIcon.id -> {
                loadFrag(MyLearningFragment(), MyLearningFragment::class.java.name)

                toolbarTitle.text = "Home"
            }
            dashView.dashToolbar.logo.id -> {
                loadFrag(NotificationFragment(), NotificationFragment::class.java.name)
            }
            dashView.dashToolbar.addBookmark.id -> {
                loadFrag(BookmarkFrag(), BookmarkFrag::class.java.name)
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = getFragment()
        dashView.drawer.closeDrawer(GravityCompat.START)
        if (currentFragment is MyLearningFragment) {
            if (backpressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.alert_exit), Toast.LENGTH_SHORT).show()
            }
            backpressed = System.currentTimeMillis()
        }
        else if (currentFragment is VideoPlayLMS || currentFragment is RetryIncorrectQuizFrag) {
            CustomStatic.IsBackClick = true
            super.onBackPressed()
            /*if (currentFragment is LmsQuestionAnswerSet) {
                super.onBackPressed()
                super.onBackPressed()
            }*/
            val updatedFragment = getFragment()
            if (updatedFragment is MyLearningFragment) {
                (getFragment() as MyLearningFragment).callLastPlayedVideo()
            }
            else if (updatedFragment is AllTopicsWiseContents) {
                (updatedFragment as AllTopicsWiseContents).getMyLarningInfoAPI()
            }
            else if (updatedFragment is MyTopicsWiseContents) {
                (updatedFragment as MyTopicsWiseContents).getMyLarningInfoAPI()
            }
            else if (updatedFragment is SearchLmsLearningFrag) {
                (updatedFragment as SearchLmsLearningFrag).getMyLarningInfoAPI()
            }
        }
        else if (currentFragment is MyTopicsWiseContents || currentFragment is AllTopicsWiseContents || currentFragment is SearchLmsLearningFrag ){
            super.onBackPressed()
            val updatedFragment = getFragment()
            if (updatedFragment is SearchLmsFrag) {
                (getFragment() as SearchLmsFrag).getTopicL()
            }
            else if (updatedFragment is SearchLmsKnowledgeFrag) {
                (getFragment() as SearchLmsKnowledgeFrag).getTopicL()
            }
            else if (updatedFragment is MyLearningTopicList) {
                (getFragment() as MyLearningTopicList).getTopicL()
            }
        }
        else if (currentFragment is SearchLmsFrag || currentFragment is SearchLmsKnowledgeFrag ||currentFragment is PerformanceInsightPage) {
            super.onBackPressed()
            val updatedFragment = getFragment()
            if (updatedFragment is MyLearningFragment) {
                (getFragment() as MyLearningFragment).callLastPlayedVideo()
            }
        }
        else
         {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                super.onBackPressed()
            }
        }
    }

    fun getFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(dashView.fragContainerView.id)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        binding = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent == null || intent.extras == null)
            return

        if (intent.getStringExtra("TYPE")
                .equals("lms_content_assign", ignoreCase = true)
        ) {
            loadFrag(NotificationFragment(), NotificationFragment::class.java.name , false)
        }
    }

    fun updateBookmarkCnt() {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getBookmarkedApiCall(Pref.user_id.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        var response = result as BookmarkFetchResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            Pref.CurrentBookmarkCount =
                                response.bookmark_list.distinctBy { it.content_id.toString() }.size
                            dashView.dashToolbar.tvSavedCount.visibility = View.VISIBLE
                            dashView.dashToolbar.tvSavedCount.text = Pref.CurrentBookmarkCount.toString()
                        } else {
                            Pref.CurrentBookmarkCount = 0
                            dashView.dashToolbar.tvSavedCount.visibility = View.GONE
                        }
                    }, { error ->
                        error.printStackTrace()
                        Pref.CurrentBookmarkCount = 0
                        dashView.dashToolbar.tvSavedCount.visibility = View.GONE
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            Pref.CurrentBookmarkCount = 0
            dashView.dashToolbar.tvSavedCount.visibility = View.GONE
        }
    }

    fun hideToolbar(){
        try {
            Handler().postDelayed(Runnable {
                dashView.dashToolbar.customToolbar.visibility = View.GONE
            }, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 7009) {
                try {
                    val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    dashView.navView.getHeaderView(0).findViewById<TextView>(R.id.et_search_menu)
                        .setText(result!![0].toString())
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            else{

                if (intent.getStringExtra("TYPE")
                        .equals("lms_content_assign", ignoreCase = true)
                ) {
                    (getFragment() as VideoPlayLMS).onPause()
                    (getFragment() as VideoPlayLMS).onStop()
                    loadFrag(NotificationFragment(), NotificationFragment::class.java.name , false)
                }
            }
        }
    }

    fun showToolbar(){
        try {
            Handler().postDelayed(Runnable {
                dashView.dashToolbar.customToolbar.visibility = View.VISIBLE
            }, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        updateNotificationCount() // Refresh the notification count when the activity is resumed
        toolbarTitle.text = "Home"
    }

    public fun showBackArrow() {
        toggle.syncState()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_back)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 35, 40, false)
        val scaledDrawable = BitmapDrawable(resources, scaledBitmap)
        val colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.color_white), PorterDuff.Mode.SRC_IN)
        scaledDrawable.colorFilter = colorFilter

        dashView.dashToolbar.customToolbar.setNavigationIcon(scaledDrawable)
        dashView.dashToolbar.customToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        dashView.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    public fun showHamburgerIcon() {
        toggle = ActionBarDrawerToggle(
            this,
            dashView.drawer,
            findViewById(R.id.dashToolbar),
            R.string.openDrawer,
            R.string.closeDrawer
        )
        toggle.syncState()
        dashView.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        dashView.drawer.addDrawerListener(toggle)
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.color_white)

    }

    private val fcmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            dashView.dashToolbar.logo.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
            updateNotificationCount()
        }
    }

    fun updateNotificationCount() {
        val notificationCount = fetchNotificationCount() // Get the count from the database
        if (notificationCount >0) {
            //dashView.dashToolbar.tvNotiCount.visibility = View.VISIBLE
            dashView.dashToolbar.tvNotiCount.text =
                notificationCount.toString() // Update the TextView
        }
    }
    private fun fetchNotificationCount(): Int {
        return AppDatabase.getDBInstance()!!.lmsNotiDao().getUnreadNotificationCount()
    }
}



