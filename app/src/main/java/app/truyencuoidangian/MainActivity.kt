package app.truyencuoidangian

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.util.StringUtil
import android.content.Intent
import android.content.pm.PathPermission
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import app.truyencuoidangian.fragment.FilterDialog
import app.truyencuoidangian.fragment.RewardDialog
import app.truyencuoidangian.fragment.SettingDialog
import app.truyencuoidangian.repository.AppSetting
import app.truyencuoidangian.repository.Story
import app.truyencuoidangian.repository.StoryDB
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.jakewharton.rxbinding2.widget.textChanges
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import io.paperdb.Paper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), RewardedVideoAdListener, ColorPickerDialogListener {
    companion object {
        var sInstance: MainActivity? = null
    }

    private val tabAdapter = TabAdapter()
    private val storiesAdapter = StoryAdapter(R.layout.item_story, ArrayList())
    private val favoritedStoriesAdapter = StoryAdapter(R.layout.item_story, ArrayList())
    private var mAdView: AdView? = null
    private var mRewardedVideoAd: RewardedVideoAd? = null
    val filterReadStories = { t: Story -> t.lastView != null }
    val filterUnReadStories = { t: Story -> t.lastView == null }
    val filterAllStories = { t: Story -> true }
    val filterObsceneStories = { t: Story -> t.category == 1 }
    val filterFolkStories = { t: Story -> t.category == 2 }
    val filterFavoriteStories = { t: Story -> t.favorited == 1 }
    var searchKey: String = ""
    var currentFilter = filterAllStories
    val watchedTimes = MutableLiveData<Int>()
    val stories = MutableLiveData<HashMap<Int, Story>>()
    val WATCH_STRING = "WATCH_TIMES"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sInstance = this
        setContentView(R.layout.activity_main)
        // ad init
        mAdView = findViewById(R.id.adView)
        val adRequest = if (BuildConfig.DEBUG) AdRequest.Builder().addTestDevice("A335A7A192255371F76D62FA9B9B66B6").build() else AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)
        mAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mAdView?.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                mAdView?.visibility = View.GONE
            }
        }
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd!!.rewardedVideoAdListener = this

        watchedTimes.observe(this, Observer { times ->
            tv_times?.text = times.toString()
            Paper.book().write(WATCH_STRING, times)
        })

//        watchedTimes.value = Paper.book().read(WATCH_STRING, if (BuildConfig.DEBUG) 10000 else 5)
        watchedTimes.value = Paper.book().read(WATCH_STRING, 5)
        tv_times.text = watchedTimes.toString()
        vp.adapter = tabAdapter
        tabs.setupWithViewPager(vp)
        storiesAdapter.setting = Paper.book().read("SETTING")
        favoritedStoriesAdapter.setting = Paper.book().read("SETTING")
        initStories()

        storiesAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            if (watchedTimes.value!! > 0) {
                watchedTimes.value = watchedTimes.value!! - 1
                val story = storiesAdapter.data[position]
                if (story.lastView == null) {
                    story.lastView = System.currentTimeMillis()
                    updateStory(story)
                }
                val i = Intent(this, ContentActivity::class.java)
                i.putExtra("STORY_ID", story.id)
                startActivity(i)
            } else {
                showRewardDialog()
            }
        }

        storiesAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            if (view.id == R.id.ic_favorite) {
                val story = storiesAdapter.data[position]
                story.favorited = if (story.favorited == 1) 0 else 1
                updateStory(story)
            }
        }

        favoritedStoriesAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            if (watchedTimes.value!! > 0) {
                watchedTimes.value = watchedTimes.value!! - 1
                val story = favoritedStoriesAdapter.data[position]
                if (story.lastView == null) {
                    story.lastView = System.currentTimeMillis()
                    updateStory(story)
                }
                val i = Intent(this, ContentActivity::class.java)
                i.putExtra("STORY_ID", story.id)
                startActivity(i)
            } else {
                showRewardDialog()
            }
        }

        favoritedStoriesAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            if (view.id == R.id.ic_favorite) {
                val story = favoritedStoriesAdapter.data[position]
                story.favorited = if (story.favorited == 1) 0 else 1
                updateStory(story)
            }
        }

        edt_search.textChanges()
                .skipInitialValue()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    searchKey = it.toString()
                    triggerReload()
                }

        ic_filter.setOnClickListener {
            val dialog = FilterDialog()
            dialog.show(supportFragmentManager, "filter_dialog")
        }
        ic_key.setOnClickListener {
            showRewardDialog()
        }
        ic_setting.setOnClickListener {
            showSettingDialog()
        }

        loadRewardedVideoAd()
    }

    fun triggerReload() {
        storiesAdapter.setting = Paper.book().read("SETTING")
        favoritedStoriesAdapter.setting = Paper.book().read("SETTING")
        stories.value = stories.value // re-assign to trigger observable emit items
    }

    fun updateStory(story: Story) {
        StoryDB.getInstance(this)!!.StoryDao().updateStoryToDB(story)
        stories.value?.put(story.id, story)
        stories.value = stories.value
    }

    private fun showRewardDialog() {
        val dialog = RewardDialog()
        dialog.show(supportFragmentManager, "reward_dialog")
    }

    private fun showSettingDialog() {
        val dialog = SettingDialog()
        dialog.show(supportFragmentManager, "setting_dialog")
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd!!.loadAd(rewardAdId,
                AdRequest.Builder().build())
    }

    fun showRewardAd() {
        if (mRewardedVideoAd!!.isLoaded) {
            mRewardedVideoAd!!.show()
        } else {
            Toast.makeText(this, "Hiện tại chưa có quảng cáo. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show()
        }
    }

    private fun initStories() {
        stories.observe(this, Observer { stories ->
            if (stories != null) {
                val listStories = stories.values
                storiesAdapter.updateData(listStories.filter(currentFilter).filter(searchFilter(searchKey)).sortedBy { it.id })
                favoritedStoriesAdapter.updateData(listStories.filter(filterFavoriteStories).filter(currentFilter).filter(searchFilter(searchKey)).sortedBy { it.id })
            }
        })

        StoryDB.getInstance(this)!!.StoryDao().apply {
            getAll().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // create fake story using for trigger reload data
                        val listStories = ArrayList(it)
                        stories.value = HashMap(listStories.associateBy({ it.id }, { it }))
                    }, Throwable::printStackTrace)
        }
    }

    private fun searchFilter(s: String): (Story) -> Boolean {
        return { t: Story -> t.slug!!.contains(s, true) }
    }

    private inner class TabAdapter : PagerAdapter() {
        val views = HashMap<Int, WeakReference<RecyclerView>>()

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }

        override fun getCount(): Int {
            return 2
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val recyclerView = layoutInflater.inflate(R.layout.view_recyclerview, container, false) as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = if (position == 0) storiesAdapter else favoritedStoriesAdapter
            views[position] = WeakReference(recyclerView)
            container.addView(recyclerView)
            return recyclerView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            views.remove(position)
            container.removeView(`object` as View)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "TẤT CẢ"
                else -> "YÊU THÍCH"
            }
        }
    }

    private inner class StoryAdapter(layoutId: Int, stories: List<Story>) : BaseQuickAdapter<Story, BaseViewHolder>(layoutId, stories) {
        var setting: AppSetting? = null

        override fun convert(helper: BaseViewHolder?, item: Story?) {
            helper?.apply {
                setText(R.id.tv_name, item?.title)
                if (getTimeString(item?.lastView) == "") {
                    getView<View>(R.id.tv_time).visibility = View.GONE
                } else {
                    getView<View>(R.id.tv_time).visibility = View.VISIBLE
                    setText(R.id.tv_time, getTimeString(item?.lastView))
                }
                if (item?.favorited == 1) {
                    setImageResource(R.id.ic_favorite, R.drawable.ic_favorite)
                } else {
                    setImageResource(R.id.ic_favorite, R.drawable.ic_favorite_grey)
                }
                setting?.let {
                    getView<TextView>(R.id.tv_name).setTextColor(it.textColor)
                    getView<View>(R.id.item_bg).setBackgroundColor(it.backgroundColor)
                }
                addOnClickListener(R.id.ic_favorite)
            }
        }

        fun updateData(newList: List<Story>) {
//            val diffResult = DiffUtil.calculateDiff(StoryDiff(this.mData, newList))
//            this.mData.clear()
//            this.mData = newList
//            diffResult.dispatchUpdatesTo(this)
            this.setNewData(newList)
        }
    }

    override fun onRewarded(reward: RewardItem) {
        // Reward the user.
    }

    override fun onRewardedVideoAdLeftApplication() {
    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
    }

    override fun onRewardedVideoAdLoaded() {
    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoCompleted() {
        watchedTimes.value = watchedTimes.value!! + 1
    }

    override fun onPause() {
        super.onPause()
        mRewardedVideoAd!!.pause(this)
    }

    override fun onResume() {
        super.onResume()
        mRewardedVideoAd!!.resume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd!!.destroy(this)
    }

    class StoryDiff(val oldList: List<Story>, val newList: List<Story>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }
    }

    fun showColorDialog(dialogId: Int) {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setShowAlphaSlider(false)
                .setAllowPresets(false)
                .setPresetsButtonText(R.string.preset)
                .setShowColorShades(false)
                .setAllowCustom(true)
                .setCustomButtonText(R.string.custom)
                .setDialogTitle(R.string.choose_color)
                .setSelectedButtonText(R.string.ok)
                .setDialogId(dialogId)
                .setColor(Color.BLACK)
                .setShowAlphaSlider(false)
                .show(this)
    }

    override fun onDialogDismissed(dialogId: Int) {
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        when (dialogId) {
            123 -> { // bg_color
                SettingDialog.sInstance?.get()?.setBgColor(color)
            }
            234 -> { // text_color
                SettingDialog.sInstance?.get()?.setTextColor(color)
            }
        }

    }
}
