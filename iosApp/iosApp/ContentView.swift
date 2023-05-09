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
