package com.fdev.vkclient.chats.attachments.stickersemoji.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fdev.vkclient.chats.attachments.stickersemoji.model.Emoji
import com.fdev.vkclient.chats.attachments.stickersemoji.model.EmojiUsage
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface EmojisDao {

    @Query("SELECT * FROM emojis")
    fun getAllEmojis(): Single<List<Emoji>>

    @Query("SELECT emoji_code FROM emoji_usages WHERE last_used != 0 ORDER BY last_used DESC LIMIT 40")
    fun getRecentEmojis(): Single<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateStickerUsed(emojiUsage: EmojiUsage): Completable

}