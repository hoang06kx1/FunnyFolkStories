package app.truyencuoidangian

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import app.truyencuoidangian.fragment.FilterDialog
import app.truyencuoidangian.repository.Story
import app.truyencuoidangian.repository.StoryDB
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val tabAdapter = TabAdapter()
    private val storiesAdapter = StoryAdapter(R.layout.item_story, ArrayList())
    private val favoritedStoriesAdapter = StoryAdapter(R.layout.item_story, ArrayList())
    val filterReadStories = { t: Story -> t.lastView != null && t.id != -1 }
    val filterUnReadStories = { t: Story -> t.lastView == null && t.id != -1 }
    val filterAllStories = { t: Story -> t.id != -1 }
    val filterObsceneStories = { t: Story -> t.category == 1 && t.id != -1 }
    val filterFolkStories = { t: Story -> t.category == 2 && t.id != -1 }
    var searchKey: String = ""
    var currentFilter = filterAllStories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vp.adapter = tabAdapter
        tabs.setupWithViewPager(vp)
        // create fake story using for trigger reload data
        StoryDB.getInstance(this)!!.StoryDao().insertStory(Story(-1, "Faked", "Faked story", null, 1, null, null, "faked"))
        initStories()

        storiesAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            val story = storiesAdapter.data[position]
            if (story.lastView == null) {
                story.lastView = System.currentTimeMillis()
                StoryDB.getInstance(this)!!.StoryDao().updateStory(story)
            }
            val i = Intent(this, ContentActivity::class.java)
            i.putExtra("STORY_ID", story.id)
            startActivity(i)
        }

        storiesAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            if (view.id == R.id.ic_favorite) {
                val story = storiesAdapter.data[position]
                story.favorited = if (story.favorited == 1) 0 else 1
                StoryDB.getInstance(this)!!.StoryDao().updateStory(story)
            }
        }

        favoritedStoriesAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            val story = favoritedStoriesAdapter.data[position]
            if (story.lastView == null) {
                story.lastView = System.currentTimeMillis()
                StoryDB.getInstance(this)!!.StoryDao().updateStory(story)
            }
            val i = Intent(this, ContentActivity::class.java)
            i.putExtra("STORY_ID", story.id)
            startActivity(i)
        }

        favoritedStoriesAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            if (view.id == R.id.ic_favorite) {
                val story = favoritedStoriesAdapter.data[position]
                story.favorited = if (story.favorited == 1) 0 else 1
                StoryDB.getInstance(this)!!.StoryDao().updateStory(story)
            }
        }

        edt_search.textChanges()
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    searchKey = it.toString()
                    StoryDB.getInstance(this)!!.StoryDao().triggerReload()
                }

        ic_filter.setOnClickListener {
            val dialog = FilterDialog()
            dialog.show(supportFragmentManager, "filter_dialog")
        }
    }

    private fun initStories() {
        StoryDB.getInstance(this)!!.StoryDao().apply {
            getAll().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { it.filter(currentFilter).filter(searchFilter(searchKey)) }
                    .subscribe({
                        storiesAdapter.setNewData(it)
                    }, Throwable::printStackTrace)

            getFavoriteStories().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { it.filter(currentFilter).filter(searchFilter(searchKey)) }
                    .subscribe({
                        favoritedStoriesAdapter.setNewData(it)
                    }, Throwable::printStackTrace)
        }
    }

    private fun searchFilter(s: String): (Story) -> Boolean {
        return { t: Story -> t.title.removeAccent().removeWhiteSpaces().contains(s.removeAccent().removeWhiteSpaces(), true) && t.id != -1 }
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
        override fun convert(helper: BaseViewHolder?, item: Story?) {
            helper?.apply {
                setText(R.id.tv_name, item?.title)
                if (item?.favorited == 1) {
                    setImageResource(R.id.ic_favorite, R.drawable.ic_favorite)
                } else {
                    setImageResource(R.id.ic_favorite, R.drawable.ic_favorite_grey)
                }
                addOnClickListener(R.id.ic_favorite)
            }
        }
    }
}
