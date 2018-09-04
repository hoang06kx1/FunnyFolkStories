package app.truyencuoidangian.repository

import android.arch.persistence.room.*
import android.content.Context

@Entity(tableName = "stories")
data class Story(@PrimaryKey var id: Int,
                 @ColumnInfo(name = "title") var title: String,
                 @ColumnInfo(name = "content") var content: String,
                 @ColumnInfo(name = "favorited") var favorited: Int?,
                 @ColumnInfo(name = "category") var category: Int?,
                 @ColumnInfo(name = "lastView") var lastView: Long?,
                 @ColumnInfo(name = "read") var read: Long?,
                 @ColumnInfo(name = "slug") var slug: String?)

@Entity(tableName = "category")
data class Category(@PrimaryKey var id: Int,
                    @ColumnInfo(name = "name") var name: String,
                    @ColumnInfo(name = "order") var oder: Int)

@Dao
interface StoryDao {
    @Query("SELECT * from stories")
    fun getAll(): List<Story>

    @Query("SELECT * from stories where favorited = 1")
    fun getFavoriteStories(): List<Story>

    @Query("SELECT * from stories where read = 1")
    fun getReadStories(): List<Story>

    @Query("SELECT * from stories where read = 0")
    fun getUnreadStories(): List<Story>

    @Query("SELECT * from stories where category = 1")
    fun getObsceneStories(): List<Story>

    @Query("SELECT * from stories where category = 2")
    fun getFolkStories(): List<Story>
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