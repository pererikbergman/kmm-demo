package com.rakangsoftware.kmmdemo.android
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.rakangsoftware.kmmdemo.data.database.DatabaseDriverFactory
import com.rakangsoftware.kmmdemo.data.database.PostRepositorySQLDelight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // TODO: Fix proper DI.
                    val viewModel = PostListViewModel(
                        PostRepositorySQLDelight(
                            DatabaseDriverFactory(LocalContext.current)
                        )
                    )
                    PostListScreen(viewModel, modifier = Modifier.fillMaxSize()) { postId ->
                        println("Post $postId clicked.")
                    }
                }
            }
        }
    }
}
