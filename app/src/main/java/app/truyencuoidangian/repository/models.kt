package app.truyencuoidangian.repository

import android.arch.persistence.room.*
import android.content.Context
import com.fstyle.library.helper.AssetSQLiteOpenHelperFactory
import io.reactivex.Flowable

@Entity(tableName = "stories")
data class Story(@PrimaryKey var id: Int,
                 @ColumnInfo(name = "title") var title: String,
                 @ColumnInfo(name = "content") var content: String,
                 @ColumnInfo(name = "favorited") var favorited: Int?,
                 @ColumnInfo(name = "category") var category: Int?,
                 @ColumnInfo(name = "lastview") var lastView: Long?,
                 @ColumnInfo(name = "read") var read: Long?,
                 @ColumnInfo(name = "slug") var slug: String?)

@Entity(tableName = "category")
data class Category(@PrimaryKey var id: Int,
                    @ColumnInfo(name = "name") var name: String,
                    @ColumnInfo(name = "order") var oder: Int)

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories")
    fun getAll(): Flowable<List<Story>>

    @Query("SELECT * FROM stories WHERE id= :id")
    fun getStory(id: Int): Story

    @Query("SELECT * FROM stories WHERE favorited = 1 AND id != -1")
    fun getFavoriteStories(): Flowable<List<Story>>

    @Query("SELECT * from stories where read = 1")
    fun getReadStories(): Flowable<List<Story>>

    @Query("SELECT * from stories where read = 0")
    fun getUnreadStories(): Flowable<List<Story>>

    @Query("SELECT * from stories where category = 1")
    fun getObsceneStories(): Flowable<List<Story>>

    @Query("SELECT * from stories where category = 2")
    fun getFolkStories(): Flowable<List<Story>>

    @Query("UPDATE stories SET read = RANDOM() where id = -1")
    fun triggerReload()

    @Update
    fun updateStory(story: Story)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStory(story: Story)
}

@Database(entities = [Story::class], version = 1)
abstract class StoryDB : RoomDatabase() {

    abstract fun StoryDao(): StoryDao

    companion object {
        private var INSTANCE: StoryDB? = null

        fun getInstance(context: Context): StoryDB? {
            if (INSTANCE == null) {
                synchronized(StoryDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            StoryDB::class.java, "truyencuoi.db")
                            .allowMainThreadQueries()
                            .openHelperFactory(AssetSQLiteOpenHelperFactory())
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}