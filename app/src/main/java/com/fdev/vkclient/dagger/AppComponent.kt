package com.fdev.vkclient.dagger

import com.fdev.vkclient.accounts.fragments.AccountsFragment
import com.fdev.vkclient.activities.ExceptionActivity
import com.fdev.vkclient.activities.LoginActivity
import com.fdev.vkclient.analyzer.dialog.AnalyzeDialogFragment
import com.fdev.vkclient.background.DownloadFileService
import com.fdev.vkclient.background.longpoll.LongPollCore
import com.fdev.vkclient.background.longpoll.receivers.MarkAsReadBroadcastReceiver
import com.fdev.vkclient.background.longpoll.services.NotificationJobIntentService
import com.fdev.vkclient.background.longpoll.services.NotificationService
import com.fdev.vkclient.chatowner.ChatOwnerViewModel
import com.fdev.vkclient.chats.attachments.audios.AudioAttachmentsFragment
import com.fdev.vkclient.chats.attachments.docs.DocAttachFragment
import com.fdev.vkclient.chats.attachments.docs.DocAttachmentsFragment
import com.fdev.vkclient.chats.attachments.gallery.GalleryFragment
import com.fdev.vkclient.chats.attachments.links.LinkAttachmentsFragment
import com.fdev.vkclient.chats.attachments.photos.PhotoAttachFragment
import com.fdev.vkclient.chats.attachments.photos.PhotoAttachmentsFragment
import com.fdev.vkclient.chats.attachments.stickers.StickersWindow
import com.fdev.vkclient.chats.attachments.stickersemoji.StickersEmojiRepository
import com.fdev.vkclient.chats.attachments.videos.VideoAttachFragment
import com.fdev.vkclient.chats.attachments.videos.VideoAttachmentsFragment
import com.fdev.vkclient.chats.messages.chat.secret.SecretChatMessagesFragment
import com.fdev.vkclient.chats.messages.chat.usual.ChatMessagesFragment
import com.fdev.vkclient.chats.messages.deepforwarded.DeepForwardedFragment
import com.fdev.vkclient.chats.messages.starred.StarredMessagesFragment
import com.fdev.vkclient.dagger.modules.ContextModule
import com.fdev.vkclient.dagger.modules.NetworkModule
import com.fdev.vkclient.dagger.modules.PresenterModule
import com.fdev.vkclient.dialogs.fragments.DialogsForwardFragment
import com.fdev.vkclient.dialogs.fragments.DialogsFragment
import com.fdev.vkclient.features.FeaturesFragment
import com.fdev.vkclient.features.general.GeneralViewModel
import com.fdev.vkclient.friends.fragments.FriendsFragment
import com.fdev.vkclient.main.MainActivity
import com.fdev.vkclient.photoviewer.ImageViewerActivity
import com.fdev.vkclient.pin.PinActivity
import com.fdev.vkclient.poll.PollFragment
import com.fdev.vkclient.profile.fragments.ProfileFragment
import com.fdev.vkclient.search.SearchFragment
import com.fdev.vkclient.utils.AppLifecycleTracker
import com.fdev.vkclient.wallpost.WallPostFragment
import com.fdev.vkclient.web.GifViewerActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, NetworkModule::class, PresenterModule::class])
interface AppComponent {

    //activities
    fun inject(loginActivity: LoginActivity)
    fun inject(mainActivity: MainActivity)
    fun inject(exceptionActivity: ExceptionActivity)
    fun inject(imageViewerActivity: ImageViewerActivity)
    fun inject(gifViewerActivity: GifViewerActivity)
    fun inject(pinActivity: PinActivity)

    // fragments
    fun inject(profileFragment: ProfileFragment)
    fun inject(wallPostFragment: WallPostFragment)
    fun inject(accountsFragment: AccountsFragment)
    fun inject(friendsFragment: FriendsFragment)
    fun inject(dialogsFragment: DialogsFragment)
    fun inject(dialogsForwardFragment: DialogsForwardFragment)
    fun inject(docAttachmentsFragment: DocAttachmentsFragment)
    fun inject(linkAttachmentsFragment: LinkAttachmentsFragment)
    fun inject(videoAttachmentsFragment: VideoAttachmentsFragment)
    fun inject(photoAttachmentsFragment: PhotoAttachmentsFragment)
    fun inject(audioAttachmentsFragment: AudioAttachmentsFragment)
    fun inject(photoAttachFragment: PhotoAttachFragment)
    fun inject(galleryFragment: GalleryFragment)
    fun inject(docAttachFragment: DocAttachFragment)
    fun inject(videoAttachFragment: VideoAttachFragment)
    fun inject(starredMessagesFragment: StarredMessagesFragment)
    fun inject(chatMessagesFragment: ChatMessagesFragment)
    fun inject(secretChatMessagesFragment: SecretChatMessagesFragment)
    fun inject(deepForwardedFragment: DeepForwardedFragment)
    fun inject(featuresFragment: FeaturesFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(analyzeDialogFragment: AnalyzeDialogFragment)
    fun inject(pollFragment: PollFragment)

    //other
    fun inject(notificationService: NotificationService)
    fun inject(downloadFileService: DownloadFileService)
    fun inject(notfJobIntentService: NotificationJobIntentService)
    fun inject(longPollCore: LongPollCore)
    fun inject(markAsReadBroadcastReceiver: MarkAsReadBroadcastReceiver)
    fun inject(stickersWindow: StickersWindow)
    fun inject(appLifecycleTracker: AppLifecycleTracker)
    fun inject(stickersEmojiRepository: StickersEmojiRepository)

    fun inject(chatOwnerViewModel: ChatOwnerViewModel)
    fun inject(generalViewModel: GeneralViewModel)

}