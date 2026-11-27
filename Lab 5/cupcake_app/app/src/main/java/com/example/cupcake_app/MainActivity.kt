package com.example.cupcake_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class CupcakeScreen(val title: String) {
    Start("Đặt bánh Cupcake"),
    Flavor("Chọn hương vị"),
    Pickup("Chọn ngày nhận"),
    Summary("Tóm tắt đơn hàng")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CupcakeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupcakeApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = CupcakeScreen.valueOf(
        backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = CupcakeScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Màn 1: Chọn số lượng
            composable(route = CupcakeScreen.Start.name) {
                StartOrderScreen(
                    onNextButtonClicked = { quantity ->
                        viewModel.setQuantity(quantity)
                        navController.navigate(CupcakeScreen.Flavor.name)
                    }
                )
            }
            // Màn 2: Chọn hương vị
            composable(route = CupcakeScreen.Flavor.name) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    options = listOf("Vani", "Sô-cô-la", "Red Velvet", "Caramel muối", "Cà phê"),
                    onSelectionChanged = { viewModel.setFlavor(it) },
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) }
                )
            }
            // Màn 3: Chọn ngày nhận
            composable(route = CupcakeScreen.Pickup.name) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) },
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.name) }
                )
            }
            // Màn 4: Tóm tắt & Chia sẻ đơn
            composable(route = CupcakeScreen.Summary.name) {
                val context = LocalContext.current
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    onSendButtonClicked = { subject, summary -> shareOrder(context, subject, summary) }
                )
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(viewModel: OrderViewModel, navController: NavHostController) {
    viewModel.resetOrder()
    navController.popBackStack(CupcakeScreen.Start.name, inclusive = false)
}

private fun shareOrder(context: Context, subject: String, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(Intent.createChooser(intent, "Gửi đơn hàng Cupcake"))
}

@Composable
fun StartOrderScreen(onNextButtonClicked: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text("Chọn số lượng bánh", style = MaterialTheme.typography.headlineSmall)
        listOf(1, 6, 12).forEach { quantity ->
            Button(
                onClick = { onNextButtonClicked(quantity) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("$quantity bánh Cupcake")
            }
        }
    }
}

@Composable
fun SelectOptionScreen(
    subtotal: String,
    options: List<String>,
    onSelectionChanged: (String) -> Unit,
    onCancelButtonClicked: () -> Unit,
    onNextButtonClicked: () -> Unit
) {
    var selectedValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            options.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedValue == item,
                            onClick = {
                                selectedValue = item
                                onSelectionChanged(item)
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedValue == item, onClick = null)
                    Text(text = item, modifier = Modifier.padding(start = 8.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Tạm tính: $subtotal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onCancelButtonClicked, modifier = Modifier.weight(1f)) {
                Text("Hủy")
            }
            Button(
                onClick = onNextButtonClicked,
                enabled = selectedValue.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Tiếp tục")
            }
        }
    }
}

@Composable
fun OrderSummaryScreen(
    orderUiState: OrderUiState,
    onCancelButtonClicked: () -> Unit,
    onSendButtonClicked: (String, String) -> Unit
) {
    val summaryText = "Số lượng: ${orderUiState.quantity} bánh\nHương vị: ${orderUiState.flavor}\nNgày nhận: ${orderUiState.date}\nTổng tiền: ${orderUiState.price}"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SỐ LƯỢNG", style = MaterialTheme.typography.labelMedium)
            Text("${orderUiState.quantity} bánh cupcake", fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Text("HƯƠNG VỊ", style = MaterialTheme.typography.labelMedium)
            Text(orderUiState.flavor, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Text("NGÀY NHẬN", style = MaterialTheme.typography.labelMedium)
            Text(orderUiState.date, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Text("Tổng cộng: ${orderUiState.price}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onSendButtonClicked("Đơn hàng Cupcake mới", summaryText) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gửi đơn hàng đến ứng dụng khác")
            }
            OutlinedButton(onClick = onCancelButtonClicked, modifier = Modifier.fillMaxWidth()) {
                Text("Hủy đơn")
            }
        }
    }
}