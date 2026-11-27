package com.example.inventory // Đổi đúng theo tên package của bạn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val itemDao = InventoryDatabase.getDatabase(application).itemDao()

    val allItems: StateFlow<List<Item>> = itemDao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun getItemById(id: Int) = itemDao.getItem(id)

    fun addItem(name: String, price: String, quantity: String) {
        val priceVal = price.toDoubleOrNull() ?: return
        val qtyVal = quantity.toIntOrNull() ?: return
        if (name.isBlank()) return

        viewModelScope.launch {
            itemDao.insert(Item(name = name, price = priceVal, quantity = qtyVal))
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    fun sellItem(item: Item) {
        if (item.quantity > 0) {
            updateItem(item.copy(quantity = item.quantity - 1))
        }
    }
}