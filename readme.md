# KMM Demo
## .gitignore
File: `.gitignore`
```sh
# Gradle files
.gradle/
build/

content-creator-old

# IntelliJ IDEA files
.idea/
*.iml
*.ipr
*.iws
.idea_modules/

# Android files
/local.properties
*.log
*.jks

# iOS files
*.xcodeproj/
*.xcworkspace/
*.pbxproj
*.mode1v3
*.mode2v3
*.perspectivev3
*.xcuserstate

# draw.io
*.bkp

# Compiled files
/bin/
/lib/
/*.klib

# Other
.DS_Store
Thumbs.db

# PDF
*.pdf

```
## Business Logic Layer (Domain Layer).
### Dependencies
File: `shared/build.gradle.kts`
```kotlin
plugins {
    ...
    kotlin("plugin.serialization") version "1.8.21"
}

kotlin {
    ...

    val ktorVersion = "2.3.0"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
            }
        }
        ...
    }
}
...
```
### Add domain object
File: `shared/src/commonMain/kotlin/com/rakangsoftware/kmmdemo/domain/Post.kt`
```kotlin
package com.rakangsoftware.kmmdemo.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    @SerialName("userId")
    val userId: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
)
```
### Define the repository
File: `shared/src/commonMain/kotlin/com/rakangsoftware/kmmdemo/domain/PostRepository.kt`
```kotlin
package com.rakangsoftware.kmmdemo.domain

interface PostRepository {
    suspend fun getById(id: Int): Post

    suspend fun getAll(): List<Post>
}
```

## Data Access Layer (networking, ktor)
### Dependencies
File: `shared/build.gradle.kts`
```kotlin
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.8.21"
}

kotlin {
    ...

    val ktorVersion = "2.3.0"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")

                // These 3 dependencies are added.
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        ...
         val androidMain by getting {
            dependencies {
                // This dependency is added.
                implementation("io.ktor:ktor-client-android:$ktorVersion")
            }
        }
        ...
        val iosMain by creating {
            ...
            dependencies {
                // This dependency is added.
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        ...
    }
}
...
```
### Implement the repository
File: `shared/src/commonMain/kotlin/com/rakangsoftware/kmmdemo/data/network/PostRepositoryKtor.kt`
```kotlin
class PostRepositoryKtor : PostRepository {
    private val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun getById(id: Int): Post =
        client.get("https://jsonplaceholder.typicode.com/posts/$id").body()

    override suspend fun getAll(): List<Post> =
        client.get("https://jsonplaceholder.typicode.com/posts/").body()
}
```
### Update the AndroidManifest
File: `androidApp/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET"/>

    <application>
        ...
    </application>
</manifest> 
```
## Presentation Layer Android
### Create the list of posts.
File: `androidApp/src/main/java/com/rakangsoftware/kmmdemo/android/PostListScreen.kt`
```kotlin
@Composable
fun PostListScreen(modifier: Modifier = Modifier, onPostClicked: (id: Int) -> Unit) {
    val list = remember { mutableStateListOf<Post>() }
    LaunchedEffect(true) {
        list.apply {
            clear()
            addAll(
                PostRepositoryKtor()
                    .getAll()
            )
        }
    }
    PostListView(
        posts = list,
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
```
### Update the MainActivity
File: `androidApp/src/main/java/com/rakangsoftware/kmmdemo/android/MainActivity.kt`
```kotlin
package com.rakangsoftware.kmmdemo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rakangsoftware.kmmdemo.Greeting
import com.rakangsoftware.kmmdemo.data.network.PostRepositoryKtor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PostListScreen(modifier = Modifier.fillMaxSize()) { postId ->
                        println("Post $postId clicked.")
                    }
                }
            }
        }
    }
}
```
## Create Presentation iOS
### Create the list of posts.
File: `iosApp/iosApp/PostListScreen.swift`
```swift
import SwiftUI
import shared

struct PostListScreen: View {
    @State var posts = [Post]()

    let onPostClicked: (Int) -> Void
    
    var body: some View {
        PostListView(posts: posts, onPostClicked: onPostClicked)
            .onAppear {
                let repository : PostRepository = PostRepositorySQLDelight(databaseDriverFactory: DatabaseDriverFactory())
                repository.getAll { fetchedPosts, error in
                    if let error = error {
                        print("Error: \(error)")
                    } else if let posts = fetchedPosts {
                        self.posts = posts
                        posts.forEach { post in
                            print(post)
                        }
                    }
                }
            }
    }
}

struct PostListView: View {
    let posts: [Post]
    let onPostClicked: (Int) -> Void
    
    var body: some View {
        List(posts, id: \.id) { post in
            PostView(post: post, onPostClicked: onPostClicked)
        }
    }
}

struct PostView: View {
    let post: Post
    let onPostClicked: (Int) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(post.title)
                .font(.headline)
            Text(post.body)
            Text("User ID: \(post.userId), Post ID: \(post.id)")
                .font(.caption)
        }
        .padding(8)
        .background(Color.white)
        .onTapGesture {
            onPostClicked(Int(post.id))
        }
        .cornerRadius(8)
        .shadow(radius: 4)
    }
}
```
### Update the ContentView
File: `iosApp/iosApp/ContentView.swift`
```swift
import SwiftUI
import shared

struct ContentView: View {
	var body: some View {
        PostListScreen(onPostClicked: { postId in
            print("Post \(postId) clicked!")
        })
	}    
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
```
## Data Access Layer (database, SQLDelight)
### Dependency
File: `build.gradle.kts`
```kotlin
plugins {
    ...
}

...
// Added.
buildscript {
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.5")
    }
}
```

File: `shared/build.gradle.kts`
```kotlin
plugins {
    ...
    kotlin("plugin.serialization") version "1.8.21"
    // Added.
    id("com.squareup.sqldelight")
}

kotlin {
    ...

    val ktorVersion = "2.3.0"
    // Added.
    val sqlDelightVersion = "1.5.5"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                // This one added.
                implementation("com.squareup.sqldelight:runtime:$sqlDelightVersion")
            }
        }
        ...
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:$ktorVersion")
                // Added.
                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
            }
        }
        ...
        val iosMain by creating {
            ...
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
                // Added.
                implementation("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
            }
        }
        ...
    }
}
...
```
### Define the database schema
File: `shared/src/commonMain/sqldelight/com/rakangsoftware/kmmdemo/database/AppDatabase.sq`
```sql
CREATE TABLE PostDto (
    userId INTEGER NOT NULL,
    id INTEGER NOT NULL,
    title TEXT,
    body TEXT
);

insertPostDto:
INSERT INTO PostDto(userId, id, title, body)
VALUES(?, ?, ?, ?);

removeAllPostDtos:
DELETE FROM PostDto;

selectAllPostDto:
SELECT PostDto.*
FROM PostDto;

selectPostDtoById:
SELECT PostDto.*
FROM PostDto
WHERE id =?;
```
### Prefill the database.
`shared/src/commonMain/sqldelight/com/rakangsoftware/kmmdemo/database/prefill.sq`
```sql
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 1, 'sunt aut facere repellat provident occaecati excepturi optio reprehenderit', 'quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 2, 'qui est esse', 'est rerum tempore vitae\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\nqui aperiam non debitis possimus qui neque nisi nulla');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 3, 'ea molestias quasi exercitationem repellat qui ipsa sit aut', 'et iusto sed quo iure\nvoluptatem occaecati omnis eligendi aut ad\nvoluptatem doloribus vel accusantium quis pariatur\nmolestiae porro eius odio et labore et velit aut');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 4, 'eum et est occaecati', 'ullam et saepe reiciendis voluptatem adipisci\nsit amet autem assumenda provident rerum culpa\nquis hic commodi nesciunt rem tenetur doloremque ipsam iure\nquis sunt voluptatem rerum illo velit');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 5, 'nesciunt quas odio', 'repudiandae veniam quaerat sunt sed\nalias aut fugiat sit autem sed est\nvoluptatem omnis possimus esse voluptatibus quis\nest aut tenetur dolor neque');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 6, 'dolorem eum magni eos aperiam quia', 'ut aspernatur corporis harum nihil quis provident sequi\nmollitia nobis aliquid molestiae\nperspiciatis et ea nemo ab reprehenderit accusantium quas\nvoluptate dolores velit et doloremque molestiae');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 7, 'magnam facilis autem', 'dolore placeat quibusdam ea quo vitae\nmagni quis enim qui quis quo nemo aut saepe\nquidem repellat excepturi ut quia\nsunt ut sequi eos ea sed quas');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 8, 'dolorem dolore est ipsam', 'dignissimos aperiam dolorem qui eum\nfacilis quibusdam animi sint suscipit qui sint possimus cum\nquaerat magni maiores excepturi\nipsam ut commodi dolor voluptatum modi aut vitae');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 9, 'nesciunt iure omnis dolorem tempora et accusantium', 'consectetur animi nesciunt iure dolore\nenim quia ad\nveniam autem ut quam aut nobis\net est aut quod aut provident voluptas autem voluptas');
INSERT INTO PostDto(userId, id, title, body) VALUES (1, 10, 'optio molestias id quiaeum', 'quo et expedita modi cum officia vel magni\ndoloribus qui repudiandae\nvero nisi sit\nquos veniam quod sed accusamus veritatis error');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 11, 'et ea vero quia laudantium autem', 'delectus reiciendis molestiae occaecati non minima eveniet qui voluptatibus\naccusamus in eum beatae sit\nvel qui neque voluptates ut commodi qui incidunt\nut animi commodi');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 12, 'in quibusdam tempore odit est dolorem', 'itaque id aut magnam\npraesentium quia et ea odit et ea voluptas et\nsapiente quia nihil amet occaecati quia id voluptatem\nincidunt ea est distinctio odio');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 13, 'dolorum ut in voluptas mollitia et saepe quo animi', 'aut dicta possimus sint mollitia voluptas commodi quo doloremque\niste corrupti reiciendis voluptatem eius rerum\nsit cumque quod eligendi laborum minima\nperferendis recusandae assumenda consectetur porro architecto ipsum ipsam');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 14, 'voluptatem eligendi optio', 'fuga et accusamus dolorum perferendis illo voluptas\nnon doloremque neque facere\nad qui dolorum molestiae beatae\nsed aut voluptas totam sit illum');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 15, 'eveniet quod temporibus', 'reprehenderit quos placeat\nvelit minima officia dolores impedit repudiandae molestiae nam\nvoluptas recusandae quis delectus\nofficiis harum fugiat vitae');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 16, 'sint suscipit perspiciatis velit dolorum rerum ipsa laboriosam odio', 'suscipit nam nisi quo aperiam aut\nasperiores eos fugit maiores voluptatibus quia\nvoluptatem quis ullam qui in alias quia est\nconsequatur magni mollitia accusamus ea nisi voluptate dicta');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 17, 'fugit voluptas sed molestias voluptatem provident', 'eos voluptas et aut odit natus earum\naspernatur fuga molestiae ullam\ndeserunt ratione qui eos\nqui nihil ratione nemo velit ut aut id quo');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 18, 'voluptate et itaque vero tempora molestiae', 'eveniet quo quis\nlaborum totam consequatur non dolor\nut et est repudiandae\nest voluptatem vel debitis et magnam');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 19, 'adipisci placeat illum aut reiciendis qui', 'illum quis cupiditate provident sit magnam\nea sed aut omnis\nveniam maiores ullam consequatur atque\nadipisci quo iste expedita sit quos voluptas');
INSERT INTO PostDto(userId, id, title, body) VALUES (2, 20, 'doloribus ad provident suscipit at', 'qui consequuntur ducimus possimus quisquam amet similique\nsuscipit porro ipsam amet\neos veritatis officiis exercitationem vel fugit aut necessitatibus totam\nomnis rerum consequatur expedita quidem cumque explicabo');
INSERT INTO PostDto(userId, id, title, body) VALUES (3, 21, 'asperiores ea ipsam voluptatibus modi minima quia sint', 'repellat aliquid praesentium dolorem quo\nsed totam minus non itaque\nnihil labore molestiae sunt dolor eveniet hic recusandae veniam\ntempora et tenetur expedita sunt');
INSERT INTO PostDto(userId, id, title, body) VALUES (3, 22, 'dolor sint quo a velit explicabo quia nam', 'eos qui et ipsum ipsam suscipit aut\nsed omnis non odio\nexpedita earum mollitia molestiae aut atque rem suscipit\nnam impedit esse');
INSERT INTO PostDto(userId, id, title, body) VALUES (3, 23, 'maxime id vitae nihil numquam', 'veritatis unde neque eligendi\nquae quod architecto quo neque vitae\nest illo sit tempora doloremque fugit quod\net et vel beatae sequi ullam sed tenetur perspiciatis');
INSERT INTO PostDto(userId, id, title, body) VALUES (3, 24, 'autem hic labore sunt dolores incidunt', 'enim et ex nulla\nomnis voluptas quia qui\nvoluptatem consequatur numquam aliquam sunt\ntotam recusandae id dignissimos aut sed asperiores deserunt');
INSERT INTO PostDto(userId, id, title, body) VALUES (3, 25, 'rem alias distinctio quo quis', 'ullam consequatur ut\nomnis quis sit vel consequuntur\nipsa eligendi ipsum molestiae et omnis error nostrum\nmolestiae illo tempore quia et distinctio');
```
### Configure the database.
File: `shared/build.gradle.kts`
```kotlin
...
sqldelight {
    database("AppDatabase") {
        packageName = "com.rakangsoftware.kmmdemo.database"
    }
}
```

### Update Data layer (database)

File: `shared/src/commonMain/kotlin/com/rakangsoftware/kmmdemo/database/DatabaseDriverFactory.kt`

```kotlin
package com.rakangsoftware.demo.database

import com.squareup.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```
### Add actual for Android

`shared/src/androidMain/kotlin/com/rakangsoftware/kmmdemo/database/DatabaseDriverFactory.kt`
```kotlin
package com.rakangsoftware.kmmdemo.data.database

import android.content.Context
import com.rakangsoftware.kmmdemo.database.AppDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "database.db")
    }
}

```
### Add actual for iOS
File: `shared/src/iosMain/kotlin/com/rakangsoftware/kmmdemo/database/DatabaseDriverFactory.kt
```kotlin
package com.rakangsoftware.demo.database

import com.rakangsoftware.kmmdemo.database.AppDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AppDatabase.Schema, "database.db")
    }
}
```
### Implement the repository
File: `shared/src/commonMain/kotlin/com/rakangsoftware/kmmdemo/database/PostRepositorySQLDelight.kt`
```kotlin
package com.rakangsoftware.demo.database

import com.rakangsoftware.demo.PostRepository
import com.rakangsoftware.demo.domain.Post

class PostRepositorySQLDelight(databaseDriverFactory: DatabaseDriverFactory) : PostRepository {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllPostDtos()
        }
    }

    override suspend fun getById(id: Int): Post =
        dbQuery.selectPostDtoById(id.toLong(), ::mapPostSelecting)
            .executeAsOne()

    override suspend fun getAll(): List<Post> {
        return dbQuery.selectAllPostDto()
            .executeAsList()
            .map { it.toPost() }
    }

    private fun mapPostSelecting(
        userId: Long,
        id: Long,
        title: String?,
        body: String?,
    ): Post {
        return Post(
            userId = userId.toInt(),
            id = id.toInt(),
            title = title ?: "",
            body = body ?: ""
        )
    }

    private fun PostDto.toPost(): Post = Post(
        userId = userId.toInt(),
        id = id.toInt(),
        title = title ?: "",
        body = body ?: ""
    )
}
```
### Update presentation layer Android
```kotlin
@Composable
fun PostListScreen(modifier: Modifier = Modifier, onPostClicked: (id: Int) -> Unit) {
    val context = LocalContext.current
    val list = remember { mutableStateListOf<Post>() }
    LaunchedEffect(true) {
        list.apply {
            clear()
            addAll(
                PostRepositorySQLDelight(DatabaseDriverFactory(context))
                    .getAll()
            )
        }
    }
    ...
}
```
### Update presentation layer iOS
```swift
struct PostListScreen: View {
    @State var posts = [Post]()
    let onPostClicked: (Int) -> Void
    
    var body: some View {
        PostListView(posts: posts, onPostClicked: onPostClicked)
            .onAppear {
                let repository = PostRepositorySQLDelight(databaseDriverFactory: DatabaseDriverFactory())
                repository.getAll { fetchedPosts, error in
                    if let error = error {
                        print("Error: \(error)")
                    } else if let posts = fetchedPosts {
                        self.posts = posts
                        posts.forEach { post in
                            print(post)
                        }
                    }
                }
            }
    }
}
```