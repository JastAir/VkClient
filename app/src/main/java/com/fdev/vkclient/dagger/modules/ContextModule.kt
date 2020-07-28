package com.fdev.vkclient.dagger.modules

import android.app.Application
import android.content.Context
import com.fdev.vkclient.chats.tools.ChatStorage
import com.fdev.vkclient.db.AppDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideAppDb(context: Context): AppDb = AppDb.buildDatabase(context)

    @Provides
    @Singleton
    fun provideChatStorage(context: Context): ChatStorage = ChatStorage(context)

}