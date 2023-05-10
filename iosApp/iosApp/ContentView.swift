import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        PostListScreen(
            viewModel : PostListViewModel(
                repo : PostRepositorySQLDelight(databaseDriverFactory:DatabaseDriverFactory())
            ),
            onPostClicked: { postId in
                print("Post \(postId) clicked!")
            }
        )
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
