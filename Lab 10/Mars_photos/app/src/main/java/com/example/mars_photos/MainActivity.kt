package com.example.mars_photos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MarsPhotosApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarsPhotosApp(marsViewModel: MarsViewModel = viewModel()) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mars Photos") }) }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = marsViewModel.marsUiState) {
                is MarsUiState.Loading -> LoadingScreen()
                is MarsUiState.Success -> PhotosGridScreen(photos = state.photos)
                is MarsUiState.Error -> ErrorScreen(retryAction = { marsViewModel.getMarsPhotos() })
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(retryAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không tải được ảnh. Kiểm tra kết nối mạng!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = retryAction) {
            Text("Thử lại")
        }
    }
}

@Composable
fun PhotosGridScreen(photos: List<MarsPhoto>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = photos, key = { photo -> photo.id }) { photo ->
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.imgSrc.replace("http://", "https://"))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Ảnh sao Hỏa ${photo.id}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}