package com.example.water_me

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WaterMeApp()
                }
            }
        }
    }
}

@Composable
fun WaterMeApp(waterViewModel: WaterViewModel = viewModel()) {
    // Tự động xin quyền thông báo trên Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var selectedPlant by remember { mutableStateOf<Plant?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(waterViewModel.plants) { plant ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPlant = plant }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = plant.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "${plant.type} • Tưới: ${plant.schedule}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = plant.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // Hộp thoại chọn thời gian nhắc nhở
    selectedPlant?.let { plant ->
        AlertDialog(
            onDismissRequest = { selectedPlant = null },
            title = { Text("Nhắc tưới cây ${plant.name}") },
            text = {
                Column {
                    val options = listOf(
                        "5 giây" to (5L to TimeUnit.SECONDS),
                        "1 ngày" to (1L to TimeUnit.DAYS),
                        "7 ngày" to (7L to TimeUnit.DAYS),
                        "30 ngày" to (30L to TimeUnit.DAYS)
                    )
                    options.forEach { (label, timePair) ->
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    waterViewModel.scheduleReminder(timePair.first, timePair.second, plant.name)
                                    selectedPlant = null
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}