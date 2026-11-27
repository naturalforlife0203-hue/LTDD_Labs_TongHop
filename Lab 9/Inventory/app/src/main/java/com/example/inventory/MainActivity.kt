package com.example.inventory // Đổi đúng theo tên package của bạn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                InventoryApp()
            }
        }
    }
}

@Composable
fun InventoryApp(viewModel: InventoryViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // 1. Màn hình danh sách kho
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("entry") },
                onItemClick = { itemId -> navController.navigate("detail/$itemId") }
            )
        }
        // 2. Màn hình thêm sản phẩm mới
        composable("entry") {
            ItemEntryScreen(
                onSaveClick = { name, price, qty ->
                    viewModel.addItem(name, price, qty)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        // 3. Màn hình chi tiết sản phẩm (Bán / Xóa)
        composable(
            route = "detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            ItemDetailsScreen(
                itemId = itemId,
                viewModel = viewModel,
                onEditClick = { navController.navigate("edit/$itemId") },
                onBackClick = { navController.popBackStack() }
            )
        }
        // 4. Màn hình sửa sản phẩm
        composable(
            route = "edit/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            ItemEditScreen(
                itemId = itemId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: InventoryViewModel,
    onAddClick: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    val itemList by viewModel.allItems.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kho hàng (Inventory)") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Thêm hàng")
            }
        }
    ) { padding ->
        if (itemList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Kho trống. Bấm + để thêm sản phẩm.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(itemList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onItemClick(item.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(NumberFormat.getCurrencyInstance().format(item.price))
                            }
                            Text("Tồn kho: ${item.quantity}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryScreen(
    onSaveClick: (String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Giá tiền") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Số lượng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Button(
                onClick = { onSaveClick(name, price, quantity) },
                enabled = name.isNotBlank() && price.toDoubleOrNull() != null && quantity.toIntOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu vào kho")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: Int,
    viewModel: InventoryViewModel,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val item by viewModel.getItemById(itemId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Sửa") }
        }
    ) { padding ->
        item?.let { currentItem ->
            Column(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tên: ${currentItem.name}", fontWeight = FontWeight.Bold)
                        Text("Giá: ${NumberFormat.getCurrencyInstance().format(currentItem.price)}")
                        Text("Số lượng còn: ${currentItem.quantity}")
                    }
                }
                Button(
                    onClick = { viewModel.sellItem(currentItem) },
                    enabled = currentItem.quantity > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bán 1 sản phẩm (-1)")
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xóa sản phẩm")
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn chắc chắn muốn xóa ${currentItem.name}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteItem(currentItem)
                            showDeleteDialog = false
                            onBackClick()
                        }) { Text("Xóa") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    itemId: Int,
    viewModel: InventoryViewModel,
    onBackClick: () -> Unit
) {
    val item by viewModel.getItemById(itemId).collectAsState(initial = null)
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(item) {
        if (item != null && !isInitialized) {
            name = item!!.name
            price = item!!.price.toString()
            quantity = item!!.quantity.toString()
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sửa sản phẩm") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Giá tiền") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Số lượng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    val p = price.toDoubleOrNull()
                    val q = quantity.toIntOrNull()
                    if (item != null && name.isNotBlank() && p != null && q != null) {
                        viewModel.updateItem(item!!.copy(name = name, price = p, quantity = q))
                        onBackClick()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cập nhật")
            }
        }
    }
}