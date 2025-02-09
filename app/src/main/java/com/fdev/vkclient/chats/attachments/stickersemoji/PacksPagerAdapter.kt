package com.fdev.vkclient.chats.attachments.stickersemoji

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.attachments.stickersemoji.model.Emoji
import com.fdev.vkclient.chats.attachments.stickersemoji.model.EmojiPack
import com.fdev.vkclient.chats.attachments.stickersemoji.model.Sticker
import com.fdev.vkclient.chats.attachments.stickersemoji.model.StickerPack
import com.fdev.vkclient.utils.load
import kotlinx.android.synthetic.main.item_sticker_tab.view.*
import kotlinx.android.synthetic.main.view_sticker_pack.view.*

class PacksPagerAdapter(
        private val context: Context,
        stickers: List<StickerPack>,
        emojis: List<EmojiPack>,
        private val callback: Callback
) : PagerAdapter() {

    private val recentTitle = context.getString(R.string.recent)
    private val unions = arrayListOf<Union>()

    val recentStickersPosition = emojis.size

    init {
        emojis.forEach { pack ->
            val view = getView(pack)
            val title = if (pack.name == null) {
                recentTitle
            } else {
                pack.name.toLowerCase()
            }
            val union = Union(
                    view, title,
                    emojis = pack
            )
            unions.add(union)
        }
        stickers.forEach { pack ->
            val view = getView(pack)
            val title = if (pack.name == null) {
                recentTitle
            } else {
                pack.name.toLowerCase()
            }
            val union = Union(
                    view, title,
                    stickers = pack
            )
            unions.add(union)
        }
        unions.sort()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = unions[position].view
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, obj: Any) = view == obj

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getPageTitle(position: Int): String = unions[position].title

    private fun getPreviewUrl(position: Int): String? {
        val union = unions[position]
        return when {
            union.title != recentTitle && union.stickers != null -> {
                union.stickers.stickers.getOrNull(0)?.photo128
            }
            union.title != recentTitle && union.emojis != null -> {
                union.emojis.emojis.getOrNull(0)?.fullPath
            }
            else -> null
        }
    }

    fun getTabView(position: Int): View? =
            View.inflate(context, R.layout.item_sticker_tab, null)?.apply {
                val url = getPreviewUrl(position)
                if (url != null) {
                    ivStickerTab.load(url)
                } else {
                    ivStickerTab.setImageResource(R.drawable.ic_clock_recent)
                }
            }

    override fun getCount() = unions.size

    private fun getView(pack: EmojiPack): View =
            View.inflate(context, R.layout.view_sticker_pack, null).apply {
                rvStickers.layoutManager =
                        GridLayoutManager(this@PacksPagerAdapter.context, 7)
                rvStickers.adapter = EmojisAdapter(context, callback::onEmojiClicked) {}.apply {
                    addAll(pack.emojis.toMutableList())
                }
            }

    private fun getView(pack: StickerPack): View =
            View.inflate(context, R.layout.view_sticker_pack, null).apply {
                rvStickers.layoutManager =
                        GridLayoutManager(this@PacksPagerAdapter.context, 5)
                rvStickers.adapter = StickersAdapter(context, callback::onStickerClicked) {}.apply {
                    addAll(pack.stickers.toMutableList())
                }
            }

    interface Callback {

        fun onStickerClicked(sticker: Sticker)

        fun onEmojiClicked(emoji: Emoji)
    }

    private inner class Union(
            val view: View,
            val title: String,
            val stickers: StickerPack? = null,
            val emojis: EmojiPack? = null
    ) : Comparable<Union> {

        override fun compareTo(other: Union): Int {
            val thisOrder = getOrder()
            val otherOrder = other.getOrder()

            return thisOrder - otherOrder
        }

        private fun getOrder() = when {
            stickers != null -> getStickersOrder(title)
            emojis != null -> getEmojiOrder(title)
            else -> 0
        }

        private fun getEmojiOrder(title: String?) = when (title) {
            "other" -> 0
            "food" -> 1
            "nature" -> 2
            "symbols" -> 3
            "animals" -> 4
            "people" -> 5
            "faces" -> 6
            else -> 7
        }

        private fun getStickersOrder(title: String?) =
                if (title == recentTitle) 8 else 9
    }
}