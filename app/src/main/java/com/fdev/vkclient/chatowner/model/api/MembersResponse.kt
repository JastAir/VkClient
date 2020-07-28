package com.fdev.vkclient.chatowner.model.api

import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.User

data class MembersResponse(

        @SerializedName("profiles")
        val profiles: List<User>
)