package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.ProductSize
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val sizesJson: String, // Stored as JSON string
    val category: String
)

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ProductSize::class.java)
    private val adapter = moshi.adapter<List<ProductSize>>(listType)

    @TypeConverter
    fun fromSizesList(value: List<ProductSize>): String {
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toSizesList(value: String): List<ProductSize> {
        return adapter.fromJson(value) ?: emptyList()
    }
}
