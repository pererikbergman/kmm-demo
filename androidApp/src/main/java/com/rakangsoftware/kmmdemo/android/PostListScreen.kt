package com.rakangsoftware.kmmdemo.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rakangsoftware.kmmdemo.data.database.DatabaseDriverFactory
import com.rakangsoftware.kmmdemo.data.database.PostRepositorySQLDelight
import com.rakangsoftware.kmmdemo.domain.Post

@Composable
fun PostListScreen(
    viewModel: PostListViewModel,
    modifier: Modifier = Modifier,
    onPostClicked: (id: Int) -> Unit
) {
    val posts = viewModel.uiState.collectAsState()

    PostListView(
        posts = posts.value,
        modifier = modifier,
        onPostClicked = onPostClicked
    )
}

@Composable
fun PostListView(posts: List<Post>, modifier: Modifier = Modifier,onPostClicked: (id: Int) -> Unit) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        items(posts) { post ->
            PostView(post, onPostClicked)
        }
    }
}

@Composable
fun PostView(post: Post, onPostClicked: (id: Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClicked(post.id) }
            .padding(16.dp),
        elevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.body)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "User ID: ${post.userId}, Post ID: ${post.id}")
        }
    }
}
