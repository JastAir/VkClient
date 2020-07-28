package com.fdev.vkclient.network.response

import com.fdev.vkclient.model.Group
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.WallPost

/**
 * Created by root on 1/27/17.
 */

class WallPostResponse {
    val items: MutableList<WallPost> = mutableListOf()
    val groups: MutableList<Group> = mutableListOf()
    val profiles: MutableList<User> = mutableListOf()
}
