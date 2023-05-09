//
//  PostListScreen.swift
//  iosApp
//
//  Created by Per-Erik Bergman on 5/5/2566 BE.
//  Copyright © 2566 BE orgName. All rights reserved.
//

import SwiftUI
import shared

struct PostListScreen: View {
    @State var posts = [Post]()
    let onPostClicked: (Int) -> Void
    
    var body: some View {
        PostListView(posts: posts, onPostClicked: onPostClicked)
            .onAppear {
                let repository : PostRepository = PostRepositoryKtor()
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
