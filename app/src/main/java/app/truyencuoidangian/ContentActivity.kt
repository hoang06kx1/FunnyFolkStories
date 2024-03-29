package app.truyencuoidangian

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import app.truyencuoidangian.repository.StoryDB
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.activity_content.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.util.TypedValue
import android.view.View
import app.truyencuoidangian.repository.AppSetting
import com.google.android.gms.ads.AdListener
import io.paperdb.Paper


class ContentActivity : AppCompatActivity() {
    var id: Int = -1
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        // ad ID
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
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val setting = Paper.book().read<AppSetting>("SETTING")
        tv_content.setTextColor(setting.textColor)
        tv_title.setTextColor(setting.textColor)
        scrollView.setBackgroundColor(setting.backgroundColor)
        tv_content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, setting.textSize.toFloat())

        id = intent.getIntExtra("STORY_ID", -1)
        val story = StoryDB.getInstance(this)!!.StoryDao().getStory(id)
        tv_title.text = story.title
        tv_content.text = story.content
        ic_favorite.setImageResource(if (story.favorited == 1) R.drawable.ic_favorite else R.drawable.ic_favorite_grey)
        tv_time.text = getTimeString(story.lastView)
        ic_favorite.setOnClickListener {
            story.favorited = if (story.favorited == 1) 0 else 1
            ic_favorite.setImageResource(if (story.favorited == 1) R.drawable.ic_favorite else R.drawable.ic_favorite_grey)
            MainActivity.sInstance?.updateStory(story)
        }
        ic_share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, story.content)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, story.title)
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ truyện..."))
        }
    }
}
