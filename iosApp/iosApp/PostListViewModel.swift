//
//  PostListViewModel.swift
//  iosApp
//
//  Created by Per-Erik Bergman on 10/5/2566 BE.
//  Copyright © 2566 BE orgName. All rights reserved.
//

import Foundation
import shared
import SwiftUI

class PostListViewModel: ObservableObject {
    
    private var repository: PostRepository
    
    @Published var posts = [Post]()
    
    init(repo:PostRepository) {
        self.repository = repo
    }
    
    func onLaunched() {
        repository.getAll(completionHandler: {posts, error in
            self.posts = posts ?? []
        })
    }
    
    func setTaskRepository(postRepository:PostRepository) {
        self.repository = postRepository
    }
}

