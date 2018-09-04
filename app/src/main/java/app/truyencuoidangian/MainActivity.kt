package app.truyencuoidangian

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import app.truyencuoidangian.repository.Story
import app.truyencuoidangian.repository.StoryDB
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private val tabAdapter = TabAdapter()
    private val storiesAdapter = StoryAdapter(R.layout.item_story, ArrayList())
    private val favoritedStoriesAdapter = StoryAdapter(R.layout.item_story, ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vp.adapter = tabAdapter
        tabs.setupWithViewPager(vp)
        initStories()

        storiesAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            val storyId = storiesAdapter.data[position].id
            val i = Intent(this, ContentActivity::class.java)
            i.putExtra("STORY_ID", storyId)
            startActivity(i)
        }
        favoritedStoriesAdapter.onItemClickListener = storiesAdapter.onItemClickListener

        storiesAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            if (view.id == R.id.ic_favorite) {
                val story = storiesAdapter.data[position]
                story.favorited = if (story.favorited == 1) 0 else 1
                StoryDB.getInstance(this)!!.StoryDao().updateStory(story)
                initStories()
            }
        }
        favoritedStoriesAdapter.onItemChildClickListener = storiesAdapter.onItemChildClickListener
    }

    private fun initStories() {
        StoryDB.getInstance(this)!!.StoryDao().apply {
            getAll().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        storiesAdapter.setNewData(it)
                    }, Throwable::printStackTrace)

            getFavoriteStories().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        favoritedStoriesAdapter.setNewData(it)
                    }, Throwable::printStackTrace)
        }
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
