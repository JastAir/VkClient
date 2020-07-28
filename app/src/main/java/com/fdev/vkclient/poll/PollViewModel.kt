package com.fdev.vkclient.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.model.attachments.Poll
import com.fdev.vkclient.model.attachments.PollAnswer
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

class PollViewModel(private val api: ApiService) : ViewModel() {

    private val votedLiveData = WrappedMutableLiveData<Boolean>()

    private val pollLiveData = WrappedMutableLiveData<Poll>()

    val voted: WrappedLiveData<Boolean>
        get() = votedLiveData

    val poll: WrappedLiveData<Poll>
        get() = pollLiveData

    fun loadPoll(pollId: Int, ownerId: Int) {
        api.getPoll(ownerId, pollId)
                .subscribeSmart({ poll ->
                    pollLiveData.value = Wrapper(poll)
                }, { error ->
                    pollLiveData.value = Wrapper(error = error)
                })
    }

    fun clearVotes() {
        val poll = pollLiveData.value?.data ?: return

        api.clearVote(poll.ownerId, poll.id)
                .subscribeSmart({ response ->
                    if (response == 1) {
                        loadPoll(poll.id, poll.ownerId)
                    } else {
                        pollLiveData.value = pollLiveData.value
                    }
                }, {})
    }

    fun vote(answers: List<PollAnswer>) {
        val poll = pollLiveData.value?.data ?: return

        api.addVote(poll.ownerId, poll.id, answers.map { it.id }.joinToString(separator = ","))
                .subscribeSmart({ response ->
                    val success = response == 1
                    votedLiveData.value = Wrapper(success)
                    if (success) {
                        loadPoll(poll.id, poll.ownerId)
                    }
                }, { error ->
                    votedLiveData.value = Wrapper(error = error)
                })
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = PollViewModel(api) as T
    }

}