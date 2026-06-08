package com.example.data

import com.example.Product
import com.example.ProductSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProductRepository(private val productDao: ProductDao) {
    private val converters = Converters()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { entity ->
            Product(
                id = entity.id,
                name = entity.name,
                imageUrl = entity.imageUrl,
                sizes = converters.toSizesList(entity.sizesJson),
                category = entity.category
            )
        }
    }

    suspend fun getProductById(id: String): Product? {
        val entity = productDao.getProductById(id) ?: return null
        return Product(
            id = entity.id,
            name = entity.name,
            imageUrl = entity.imageUrl,
            sizes = converters.toSizesList(entity.sizesJson),
            category = entity.category
        )
    }

    suspend fun insertOrUpdateProduct(product: Product) {
        val entity = ProductEntity(
            id = product.id.ifEmpty { UUID.randomUUID().toString() },
            name = product.name,
            imageUrl = product.imageUrl,
            sizesJson = converters.fromSizesList(product.sizes),
            category = product.category
        )
        productDao.insertProduct(entity)
    }

    suspend fun deleteProductById(id: String) {
        productDao.deleteProductById(id)
    }
}
