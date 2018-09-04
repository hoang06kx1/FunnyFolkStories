package app.truyencuoidangian

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import app.truyencuoidangian.repository.Story
import app.truyencuoidangian.repository.StoryDB
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    var stories: List<Story> = ArrayList()
    var favoritedStories: List<Story> = ArrayList()
    private val tabAdatper = TabAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initStories()
        vp.adapter = tabAdatper
        tabs.setupWithViewPager(vp)
    }

    private fun initStories() {
        StoryDB.getInstance(this)!!.StoryDao().apply {
            getAll().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        stories = it
                        tabAdatper.views[0]?.get()?.adapter?.notifyDataSetChanged()
                    }, Throwable::printStackTrace)

            getFavoriteStories().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        favoritedStories = it
                        tabAdatper.views[1]?.get()?.adapter?.notifyDataSetChanged()
                    }, Throwable::printStackTrace)
        }
    }

    private inner class TabAdapter : PagerAdapter() {
        val views = HashMap<Int, WeakReference<RecyclerView>>()

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 === p1
        }

        override fun getCount(): Int {
            return 2
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val recyclerView = layoutInflater.inflate(R.layout.view_recyclerview, container, false) as RecyclerView
            recyclerView.adapter = StoryAdapter(R.layout.view_recyclerview, if (position == 0) stories else favoritedStories)
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
}
