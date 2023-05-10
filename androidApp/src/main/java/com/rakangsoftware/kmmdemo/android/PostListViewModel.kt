package com.rakangsoftware.kmmdemo.android

import androidx.lifecycle.ViewModel
import com.rakangsoftware.kmmdemo.domain.Post
import com.rakangsoftware.kmmdemo.domain.PostRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostListViewModel(
    private val repo: PostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(listOf<Post>())
    val uiState: StateFlow<List<Post>> = _uiState

    init {
        CoroutineScope(Dispatchers.Main).launch {
            _uiState.emit(repo.getAll())
        }
    }
}
