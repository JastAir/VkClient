package com.fdev.vkclient.chats.messages.chat.base

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.chatowner.ChatOwnerActivity
import com.fdev.vkclient.chats.attachments.attach.AttachActivity
import com.fdev.vkclient.chats.attachments.attach.AttachFragment
import com.fdev.vkclient.chats.attachments.attached.AttachedAdapter
import com.fdev.vkclient.chats.attachments.gallery.model.DeviceItem
import com.fdev.vkclient.chats.messages.base.BaseMessagesFragment
import com.fdev.vkclient.chats.messages.base.MessagesAdapter
import com.fdev.vkclient.chats.messages.base.MessagesReplyItemCallback
import com.fdev.vkclient.chats.messages.chat.MentionedMembersAdapter
import com.fdev.vkclient.chats.messages.chat.StickersSuggestionAdapter
import com.fdev.vkclient.chats.tools.ChatInputController
import com.fdev.vkclient.chats.tools.ChatToolbarController
import com.fdev.vkclient.dialogs.activities.DialogsForwardActivity
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.model.CanWrite
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.attachments.*
import com.fdev.vkclient.model.messages.Message
import com.fdev.vkclient.photoviewer.ImageViewerActivity
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.utils.contextpopup.ContextPopupItem
import com.fdev.vkclient.utils.contextpopup.createContextPopup
import com.fdev.vkclient.views.TextInputAlertDialog
import com.fdev.vkclient.web.VideoViewerActivity
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*
import kotlinx.android.synthetic.main.view_chat_multiselect.*
import java.util.concurrent.TimeUnit

abstract class BaseChatMessagesFragment<VM : BaseChatMessagesViewModel> : BaseMessagesFragment<VM>() {

    protected val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }
    protected val title by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    protected val photo by lazy { arguments?.getString(ARG_PHOTO) ?: "" }
    private val forwardedMessages by lazy { arguments?.getString(ARG_FORWARDED) }
    private val shareText by lazy { arguments?.getString(ARG_SHARE_TEXT) }
    private val shareImage by lazy { arguments?.getString(ARG_SHARE_IMAGE) }

    private val permissionHelper by lazy { PermissionHelper(this) }
    private val attachedAdapter by lazy {
        AttachedAdapter(contextOrThrow, ::onAttachClicked) {
            inputController?.setAttachedCount(it)
        }
    }
    private val membersAdapter by lazy {
        MentionedMembersAdapter(contextOrThrow) {
            inputController?.mentionUser(it)
        }
    }
    private val chatToolbarController by lazy {
        ChatToolbarController(toolbar)
    }
    protected val stickersAdapter by lazy {
        StickersSuggestionAdapter(contextOrThrow, ::onSuggestedStickerClicked)
    }

    private val handler = Handler()
    private var inputController: ChatInputController? = null

    abstract fun onEncryptedDocClicked(doc: Doc)

    override fun prepareViewModel() {
        viewModel.peerId = peerId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        inputController = ChatInputController(contextOrThrow, view, InputCallback())
        swipeContainer.setOnRefreshListener { viewModel.loadMessages() }

        rvChatList.addOnScrollListener(RecyclerDateScroller())
        rvAttached.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        rvAttached.adapter = attachedAdapter

        rvMentionedMembers.layoutManager = LinearLayoutManager(context)
        rvMentionedMembers.adapter = membersAdapter

        val swipeToReply = ItemTouchHelper(MessagesReplyItemCallback(context, ::onSwipedToReply))
        swipeToReply.attachToRecyclerView(rvChatList)
        stylize()
        initContent()
        initMultiSelectMenu()

        rvStickersSuggestion.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvStickersSuggestion.adapter = stickersAdapter

        // TODO move to onActivityCreated with viewLifeCycleOwner
        viewModel.getLastSeen().observe(this, Observer { onOnlineChanged(it) })
        viewModel.getCanWrite().observe(this, Observer { onCanWriteChanged(it) })
        viewModel.getActivity().observe(this, Observer { onActivityChanged(it) })
        viewModel.mentionedMembers.observe(this, Observer(::showMentionedMembers))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chatToolbarController.setTitle(title)
//        chatToolbarController.setTitle(FakeData.name)
        if (peerId != -App.GROUP) {
            chatToolbarController.setAvatar(photo)
//            chatToolbarController.setAvatar(FakeData.avatar)
        }
        if (!peerId.matchesUserId()) {
            onOnlineChanged(Triple(false, 0, 0))
        }
        if (peerId.matchesChatId()) {
            viewModel.loadMembers()
        }
        toolbar?.setOnClickListener {
            activity?.let { hideKeyboard(it) }
            ChatOwnerActivity.launch(context, peerId)
        }

        ViewCompat.setOnApplyWindowInsetsListener(rlInputBack) { view, insets ->
            view.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(fabHasMore) { view, insets ->
            (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
                val margin = context?.resources?.getDimensionPixelSize(R.dimen.chat_fab_more_bottom_margin) ?: 0
                bottomMargin = margin + insets.systemWindowInsetBottom
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(rvStickersSuggestion) { view, insets ->
            (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
                val margin = context?.resources?.getDimensionPixelSize(R.dimen.chat_sticker_suggestions_bottom_margin) ?: 0
                bottomMargin = margin + insets.systemWindowInsetBottom
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(rvMentionedMembers) { view, insets ->
            (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
                val margin = context?.resources?.getDimensionPixelSize(R.dimen.chat_mentioned_members_bottom_margin) ?: 0
                bottomMargin = margin + insets.systemWindowInsetBottom
            }
            insets
        }
    }

    private fun initMultiSelectMenu() {
        ivReplyMulti.setOnClickListener {
            attachedAdapter.fwdMessages = getSelectedMessageIds()
            attachedAdapter.isReply = true
            adapter.multiSelectMode = false
        }
        ivDeleteMulti.setOnClickListener {
            val selectedMessages = adapter.multiSelect
            val callback = { forAll: Boolean ->
                viewModel.deleteMessages(getSelectedMessageIds(), forAll)
                adapter.multiSelectMode = false
            }
            val edgeDate = time() - 3600 * 24
            val isOut = selectedMessages.none { !it.isOut() }
            val isRecent = selectedMessages.none { it.date < edgeDate }
            if (isOut && isRecent) {
                showDeleteMessagesDialog(callback)
            } else {
                context?.let {
                    showDeleteDialog(it) { callback.invoke(false) }
                }
            }
        }
        ivMarkMulti.setOnClickListener {
            viewModel.markAsImportant(getSelectedMessageIds())
            adapter.multiSelectMode = false
        }
    }

    private fun stylize() {
        rlMultiAction.stylizeAll()
        rlMultiAction.stylizeColor()
        fabHasMore.stylize()
        rlBack.stylizeAll()
        progressBar?.stylize()

        if (Prefs.chatBack.isNotEmpty()) {
            try {
                flContainer.backgroundImage = Drawable.createFromPath(Prefs.chatBack)
            } catch (e: Exception) {
                Prefs.chatBack = ""
                showError(activity, e.message ?: "background not found")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isShown = true
        adapter.items.lastOrNull()?.also { viewModel.invalidateMessages(it) }
    }

    override fun onPause() {
        super.onPause()
        viewModel.isShown = false
    }

    override fun onScrolled(isAtBottom: Boolean) {
        viewModel.isShown = isAtBottom
    }

    private fun initContent() {
        forwardedMessages?.also {
            handler.postDelayed({
                attachedAdapter.fwdMessages = it
            }, 500L)
        }
        shareText?.also {
            handler.postDelayed({
                etInput.setText(it)
            }, 500L)
        }
        shareImage?.also {
            handler.postDelayed({
                onImageSelected(it)
            }, 500L)
        }
    }

    private fun showDeleteMessagesDialog(callback: (Boolean) -> Unit) {
        val context = context ?: return

        val dialog = AlertDialog.Builder(context)
                .setMessage(R.string.wanna_delete_messages)
                .setNeutralButton(R.string.delete_for_all) { _, _ -> callback.invoke(true) }
                .setPositiveButton(R.string.delete_only_for_me) { _, _ -> callback.invoke(false) }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        dialog.stylize()
    }

    private fun showEditMessageDialog(message: Message) {
        val context = context ?: return

        TextInputAlertDialog(context, "", message.text) {
            viewModel.editMessage(message.id, it)
        }.show()
    }

    /**
     * handles click on attached media
     */
    private fun onAttachClicked(attachment: Attachment) {
        when (attachment.type) {
            Attachment.TYPE_PHOTO -> attachment.photo?.let {
                ImageViewerActivity.viewImages(context, arrayListOf(it))
            }
            Attachment.TYPE_VIDEO -> attachment.video?.let {
                viewModel.loadVideo(context ?: return, it, { playerUrl ->
                    VideoViewerActivity.launch(context, playerUrl)
                }, { error ->
                    showError(context, error)
                })
            }
        }
    }

    /**
     * handle setting subtitle and changing user's status
     * triple represents:
     *  - online flag
     *  - last seen time
     *  - device code
     */
    private fun onOnlineChanged(value: Triple<Boolean, Int, Int>) {
        chatToolbarController.setSubtitle(when {
            peerId.matchesChatId() -> getString(R.string.conversation)
            peerId.matchesGroupId() -> getString(R.string.community)
            else -> getLastSeenText(context?.resources, value.first, value.second, value.third)
        })
    }

    /**
     * handles setting peer's activity: one of [BaseChatMessagesViewModel.ACTIVITY_TYPING],
     * [BaseChatMessagesViewModel.ACTIVITY_VOICE], [BaseChatMessagesViewModel.ACTIVITY_NONE]
     */
    private fun onActivityChanged(activity: String) {
        when (activity) {
            BaseChatMessagesViewModel.ACTIVITY_NONE -> chatToolbarController.hideActions()
            else -> chatToolbarController.showActivity()
        }
    }

    private fun onSuggestedStickerClicked(sticker: Sticker) {
        viewModel.sendSticker(sticker)
        etInput.clear()
    }

    private fun onCanWriteChanged(canWrite: CanWrite) {
        rlCantWrite.setVisible(!canWrite.allowed)
    }

    private fun onSwipedToReply(position: Int) {
        val message = adapter.items.getOrNull(position) ?: return

        attachedAdapter.fwdMessages = "${message.id}"
        attachedAdapter.isReply = true
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        attachedAdapter.addAll(attachments.toMutableList())
    }

    private fun onSelectedFromGallery(paths: List<DeviceItem>) {
        val maxOrder = attachedAdapter.maxOrder
        paths.forEachIndexed { index, deviceItem ->

            inputController?.addItemAsBeingLoaded(deviceItem.path)
            when (deviceItem.type) {
                DeviceItem.Type.PHOTO -> viewModel.attachPhoto(deviceItem.path) { path, attachment ->
                    inputController?.removeItemAsLoaded(path)
                    attachedAdapter.addWithOrder(attachment, index + maxOrder)
                }
                DeviceItem.Type.VIDEO -> viewModel.attachVideo(deviceItem.path) { path, attachment ->
                    inputController?.removeItemAsLoaded(path)
                    attachedAdapter.addWithOrder(attachment, index + maxOrder)
                }
                DeviceItem.Type.DOC -> viewModel.attachDoc(deviceItem.path) { path, attachment ->
                    inputController?.removeItemAsLoaded(path)
                    attachedAdapter.addWithOrder(attachment, index + maxOrder)
                }
            }
        }
    }

    private fun onImageSelected(path: String) {
        viewModel.attachPhoto(path) { pathAttached, attachment ->
            inputController?.removeItemAsLoaded(pathAttached)
            attachedAdapter.addWithOrder(attachment, attachedAdapter.maxOrder)
        }
        inputController?.addItemAsBeingLoaded(path)
    }

    private fun showMentionedMembers(members: List<User>) {
        if (members.isEmpty() && rvMentionedMembers.isShown) {
            rvMentionedMembers.fadeOut(200L) {
                rvMentionedMembers?.hide()
            }
        } else if (members.isNotEmpty() && !rvMentionedMembers.isShown) {
            rvMentionedMembers.show()
            rvMentionedMembers.fadeIn(200L)
        }
        if (members.size > MEMBERS_MAX) {
            membersAdapter.update(members.take(MEMBERS_MAX))
        } else {
            membersAdapter.update(members)
        }
    }

    protected fun onDocSelected(path: String) {
        viewModel.attachDoc(path) { pathAttached, attachment ->
            inputController?.removeItemAsLoaded(pathAttached)
            attachedAdapter.addWithOrder(attachment, attachedAdapter.maxOrder)
        }
        inputController?.addItemAsBeingLoaded(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ATTACH -> {
                data?.extras?.apply {
                    getParcelableArrayList<Attachment>(AttachFragment.ARG_ATTACHMENTS)
                            ?.let(::onAttachmentsSelected)
                    getParcelableArrayList<DeviceItem>(AttachFragment.ARG_PATHS)
                            ?.let(::onSelectedFromGallery)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        chatToolbarController.hideActions()
    }

    override fun getAdapterSettings() = MessagesAdapter.Settings(
            isImportant = false
    )

    override fun getAdapterCallback() = AdapterCallback()

    companion object {

        const val MEMBERS_MAX = 5

        const val ARG_PEER_ID = "peerId"
        const val ARG_TITLE = "title"
        const val ARG_FORWARDED = "forwarded"
        const val ARG_PHOTO = "photo"
        const val ARG_SHARE_TEXT = "shareText"
        const val ARG_SHARE_IMAGE = "shareImage"

        const val REQUEST_ATTACH = 2653
    }

    inner class AdapterCallback : MessagesAdapter.Callback {
        override fun onClicked(message: Message) {
            val items = arrayListOf(
                    ContextPopupItem(R.drawable.ic_copy_popup, R.string.copy) {
                        copyToClip(message.text)
                    },
                    ContextPopupItem(R.drawable.ic_reply_popup, R.string.reply) {
                        attachedAdapter.fwdMessages = "${message.id}"
                        attachedAdapter.isReply = true
                    },
                    ContextPopupItem(R.drawable.ic_transfer_popup, R.string.forward) {
                        DialogsForwardActivity.launch(context, forwarded = "${message.id}")
                    },
                    ContextPopupItem(R.drawable.ic_delete_popup, R.string.delete) {
                        val callback = { forAll: Boolean ->
                            viewModel.deleteMessages(message.id.toString(), forAll)
                        }
                        if (message.isDeletableForAll()) {
                            showDeleteMessagesDialog(callback)
                        } else {
                            context?.let {
                                showDeleteDialog(it) { callback.invoke(false) }
                            }
                        }
                    }
            )

            if (!Prefs.markAsRead) {
                items.add(ContextPopupItem(R.drawable.ic_eye, R.string.mark_as_read) {
                    viewModel.markAsRead(listOf(message.id))
                })
            }

            items.add(if (message.important) {
                ContextPopupItem(R.drawable.ic_star_crossed, R.string.unmark) {
                    viewModel.unmarkAsImportant(message.id.toString())
                }
            } else {
                ContextPopupItem(R.drawable.ic_star_popup, R.string.markasimportant) {
                    viewModel.markAsImportant(message.id.toString())
                }
            })
            if (message.isEditable()) {
                items.add(ContextPopupItem(R.drawable.ic_edit_popup, R.string.edit) {
                    showEditMessageDialog(message)
                })
            }
            createContextPopup(context ?: return, items).show()
        }

        override fun onUserClicked(userId: Int) {
            ChatOwnerActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {
            onEncryptedDocClicked(doc)
        }

        override fun onPhotoClicked(position: Int, photos: ArrayList<Photo>) {
            ImageViewerActivity.viewImages(context, photos, position)
        }

        override fun onVideoClicked(video: Video) {
            viewModel.loadVideo(context ?: return, video, { playerUrl ->
                VideoViewerActivity.launch(context, playerUrl)
            }, { error ->
                showError(context, error)
            })
        }
    }

    private inner class RecyclerDateScroller : RecyclerView.OnScrollListener() {

        private var lastHandledTopPosition = -1
        private var lastHandledBottomPosition = -1
        private var disposable: Disposable? = null

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val adapterTopPosition = (recyclerView.layoutManager as? LinearLayoutManager)
                    ?.findFirstVisibleItemPosition() ?: -1
            val adapterBottomPosition = (recyclerView.layoutManager as? LinearLayoutManager)
                    ?.findLastVisibleItemPosition() ?: -1
            if (adapterTopPosition != lastHandledTopPosition
                    && adapterTopPosition != -1
                    && adapterBottomPosition != lastHandledBottomPosition) {
                val message = adapter.items.getOrNull(adapterTopPosition) ?: return
                if (message.date == 0) return

                val uiDate = getDate(message.date)
                showDate(uiDate)
                lastHandledTopPosition = adapterTopPosition
                lastHandledBottomPosition = adapterBottomPosition

                disposable?.dispose()
                disposable = Completable.timer(2L, TimeUnit.SECONDS)
                        .compose(applyCompletableSchedulers())
                        .subscribe {
                            hideDate()
                        }

            }
        }

        private fun showDate(date: String) {
            if (!rlDate.isShown) {
                rlDate.fadeIn(200L)
                rlDate.show()
            }
            tvDatePopup.text = date
        }

        private fun hideDate() {
            rlDate?.fadeOut(200L) {
                rlDate?.hide()
            }
        }
    }

    private inner class InputCallback : ChatInputController.ChatInputCallback {

        override fun onRichContentAdded(filePath: String) {
            if (filePath.endsWith("gif", ignoreCase = true)) {
                onDocSelected(filePath)
            } else {
                onImageSelected(filePath)
            }
        }

        override fun onStickerClicked(sticker: Sticker) {
            viewModel.sendSticker(sticker, attachedAdapter.replyTo)
            attachedAdapter.clear()
        }

        override fun onSendClick() {
            val replyTo = attachedAdapter.replyTo
            val forwarded = if (replyTo == null) {
                attachedAdapter.fwdMessages
            } else {
                null
            }
            viewModel.sendMessage(
                    text = etInput.asText(),
                    attachments = attachedAdapter.asString(),
                    forwardedMessages = forwarded,
                    replyTo = replyTo
            )
            etInput.clear()
            attachedAdapter.clear()
        }

        override fun hasMicPermissions(): Boolean {
            val hasPermissions = permissionHelper.hasRecordAudioPermissions()
            if (!hasPermissions) {
                permissionHelper.request(arrayOf(PermissionHelper.RECORD_AUDIO)) {}
            }
            return hasPermissions
        }

        override fun onAttachClick() {
            AttachActivity.launch(this@BaseChatMessagesFragment, REQUEST_ATTACH)
        }

        override fun onVoiceRecordingInvoke() {
            viewModel.setActivity(type = BaseChatMessagesViewModel.ACTIVITY_VOICE)
        }

        override fun onTypingInvoke() {
            viewModel.setActivity(type = BaseChatMessagesViewModel.ACTIVITY_TYPING)
        }

        override fun onVoiceRecorded(fileName: String) {
            inputController

                    ?.addItemAsBeingLoaded(fileName)
            viewModel.attachVoice(fileName) {
                inputController?.removeItemAsLoaded(it)
            }
        }

        override fun onStickersSuggested(stickers: List<Sticker>) {
            stickersAdapter.update(stickers)
            if (stickers.isEmpty() && rvStickersSuggestion.isShown) {
                rvStickersSuggestion.fadeOut(200L) {
                    rvStickersSuggestion?.hide()
                }
            } else if (stickers.isNotEmpty() && !rvStickersSuggestion.isShown) {
                rvStickersSuggestion.show()
                rvStickersSuggestion.fadeIn(200L)
            }
        }

        override fun onMention(query: String?) {
            if (peerId.matchesChatId() && query != null) {
                viewModel.getMatchingMembers(query)
            } else {
                this@BaseChatMessagesFragment.showMentionedMembers(listOf())
            }
        }
    }
}