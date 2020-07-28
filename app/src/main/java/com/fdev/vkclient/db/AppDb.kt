package com.fdev.vkclient.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fdev.vkclient.accounts.db.AccountsDao
import com.fdev.vkclient.accounts.models.Account
import com.fdev.vkclient.chats.attachments.stickersemoji.db.EmojisDao
import com.fdev.vkclient.chats.attachments.stickersemoji.db.StickersDao
import com.fdev.vkclient.chats.attachments.stickersemoji.model.Emoji
import com.fdev.vkclient.chats.attachments.stickersemoji.model.EmojiUsage
import com.fdev.vkclient.chats.attachments.stickersemoji.model.Sticker
import com.fdev.vkclient.chats.attachments.stickersemoji.model.StickerUsage
import com.fdev.vkclient.dialogs.db.DialogsDao
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.utils.applyCompletableSchedulers
import io.reactivex.Completable
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


@Database(entities = [
    Dialog::class, Account::class,
    Sticker::class, Emoji::class,
    StickerUsage::class, EmojiUsage::class], version = 6)
abstract class AppDb : RoomDatabase() {

    abstract fun dialogsDao(): DialogsDao

    abstract fun accountsDao(): AccountsDao

    abstract fun stickersDao(): StickersDao

    abstract fun emojisDao(): EmojisDao

    @SuppressLint("CheckResult")
    fun clearAsync() {
        Completable.fromCallable {
            clearAllTables()
        }
                .compose(applyCompletableSchedulers())
                .subscribe({}) {
                    it.printStackTrace()
                    Lg.wtf("[app db] error clearing: ${it.message}")
                }
    }

    companion object {

        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE dialogs ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE dialogs ADD COLUMN alias TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE stickers (" +
                        "id INTEGER NOT NULL," +
                        "key_words TEXT NOT NULL," +
                        "key_words_custom TEXT NOT NULL," +
                        "pack_name TEXT NOT NULL," +
                        "PRIMARY KEY(id)" +
                        ")")
                database.execSQL("CREATE TABLE emojis (" +
                        "code TEXT NOT NULL," +
                        "file_name TEXT NOT NULL," +
                        "pack_name TEXT NOT NULL," +
                        "PRIMARY KEY(code)" +
                        ")")
                database.execSQL("CREATE TABLE sticker_usages (" +
                        "sticker_id INTEGER NOT NULL," +
                        "last_used INTEGER NOT NULL," +
                        "PRIMARY KEY(sticker_id)" +
                        ")")
                database.execSQL("CREATE TABLE emoji_usages (" +
                        "emoji_code TEXT NOT NULL," +
                        "last_used INTEGER NOT NULL," +
                        "PRIMARY KEY(emoji_code)" +
                        ")")
            }
        }

        fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDb::class.java, "vkclient_room.db")
                        .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
//                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                fillWithEmojisIfEmpty(context, db)
                            }
                        })
                        .build()

        private fun fillWithEmojisIfEmpty(context: Context, db: SupportSQLiteDatabase) {
            var cursor: Cursor? = null
            var count = 0
            try {
                cursor = db.query("SELECT * FROM emojis")
                count = cursor.count
            } catch (e: Exception) {
                e.printStackTrace()
                Lg.wtf("error getting emojis count: ${e.message}")
            } finally {
                cursor?.close()
            }

            if (count == 0) {
                var br: BufferedReader? = null
                try {
                    val inputStream: InputStream = context.assets.open("emojis.sql")
                    br = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

                    var str: String? = br.readLine()
                    while (str != null) {
                        db.execSQL(str)
                        str = br.readLine()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Lg.wtf("error inserting emojis: ${e.message}")
                } finally {
                    br?.close()
                }
            }
        }
    }
}