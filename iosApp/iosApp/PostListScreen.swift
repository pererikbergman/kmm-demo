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

    @ObservedObject var viewModel: PostListViewModel
    
    @State var posts = [Post]()
    var onPostClicked: (Int) -> Void = { (a: Int) -> Void in }
    
    init(viewModel:PostListViewModel, onPostClicked: @escaping (Int) -> Void) {
        self.viewModel = viewModel
        self.onPostClicked = onPostClicked
    }
    
    var body: some View {
        PostListView(posts: viewModel.posts, onPostClicked: onPostClicked)
            .onAppear {
                viewModel.onLaunched()
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
