package app.truyencuoidangian

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private inner class TabAdapter(): PagerAdapter() {
        val views = HashMap<Int, WeakReference<View>>()

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 === p1
        }

        override fun getCount(): Int {
            return 2
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val recyclerView = layoutInflater.inflate(R.layout.view_recyclerview, container, false)
            views[position] = WeakReference(recyclerView)
            container.addView(recyclerView)
            return super.instantiateItem(container, position)
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
}
