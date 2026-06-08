package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    val uiState: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun seedData() {
        viewModelScope.launch {
            // Delete all products to guarantee clean slate and precise updates of categories & items
            repository.deleteAllProducts()
            
            // Register complete revised cafe catalog
            sampleProducts.forEach { product ->
                repository.insertOrUpdateProduct(product)
            }
        }
    }

    fun addOrUpdateProduct(product: Product) {
        viewModelScope.launch {
            repository.insertOrUpdateProduct(product)
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProductById(id)
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
