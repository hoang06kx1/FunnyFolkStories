package app.truyencuoidangian

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import app.truyencuoidangian.repository.StoryDB
import kotlinx.android.synthetic.main.activity_content.*
import java.text.SimpleDateFormat
import java.util.*

class ContentActivity : AppCompatActivity() {
    var id: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
        id = intent.getIntExtra("STORY_ID", -1)
        val story = StoryDB.getInstance(this)!!.StoryDao().getStory(id)
        tv_title.text = story.title
        tv_content.text = story.content
        ic_favorite.setImageResource(if (story.favorited == 1) R.drawable.ic_favorite else R.drawable.ic_favorite_grey)
        tv_time.text = getTimeString(story.lastView)
    }

    fun getTimeString(timestamp: Long?): String {
        var time = System.currentTimeMillis()
        if (timestamp != null && timestamp > 0) {
            time = timestamp
        }
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val dateString = formatter.format(Date(time))
        val timeFormatter = SimpleDateFormat("HH:mm:ss")
        val hourString = timeFormatter.format(time)
        return "Đã xem vào lúc $hourString ngày $dateString"
    }
}
