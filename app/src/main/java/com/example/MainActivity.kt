package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.ProductRepository

data class ProductSize(
    val name: String,
    val price: Int
)

data class Product(
    val id: String,
    val name: String,
    val imageUrl: String,
    val sizes: List<ProductSize>,
    val category: String
)

val sampleProducts = listOf(
    Product("1", "اسبريسو", "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=500&q=80", listOf(ProductSize("سنجل", 2000), ProductSize("دبل", 3000)), "قهوة حارة"),
    Product("2", "كابتشينو", "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=500&q=80", listOf(ProductSize("وسط", 3500), ProductSize("كبير", 4500)), "قهوة حارة"),
    Product("3", "لاتيه", "https://images.unsplash.com/photo-1561882468-9110e03e0f78?w=500&q=80", listOf(ProductSize("وسط", 3500), ProductSize("كبير", 4500)), "قهوة حارة"),
    Product("4", "امريكانو", "https://images.unsplash.com/photo-1551030173-122aabc4489c?w=500&q=80", listOf(ProductSize("وسط", 2500), ProductSize("كبير", 3500)), "قهوة حارة"),
    Product("5", "موكا", "https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=500&q=80", listOf(ProductSize("وسط", 4000), ProductSize("كبير", 5000)), "قهوة حارة"),
    Product("6", "آيس لاتيه", "https://images.unsplash.com/photo-1461023058943-0708e5604d06?w=500&q=80", listOf(ProductSize("وسط", 4000), ProductSize("كبير", 5000)), "قهوة باردة"),
    Product("7", "آيس امريكانو", "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=500&q=80", listOf(ProductSize("وسط", 3000), ProductSize("كبير", 4000)), "قهوة باردة"),
    Product("8", "ماتشا لاتيه", "https://images.unsplash.com/photo-1536514498073-50e69d39c6cf?w=500&q=80", listOf(ProductSize("وسط", 4500), ProductSize("كبير", 5500)), "شاي"),
    Product("9", "كرواسون", "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=500&q=80", listOf(ProductSize("عادي", 3000)), "معجنات"),
    Product("10", "مافن توت الأرزق", "https://images.unsplash.com/photo-1607958996333-41aef7caefaa?w=500&q=80", listOf(ProductSize("عادي", 2500)), "معجنات")
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val repository = ProductRepository(db.productDao())
            val viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))
            
            LaunchedEffect(Unit) {
                viewModel.seedData()
            }
            
            AppNavigation(viewModel)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeMenuScreen(viewModel: ProductViewModel, navController: NavController) {
    val products by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }

    val filteredProducts = remember(searchQuery, selectedCategory, products) {
        products.filter {
            (selectedCategory == "الكل" || it.category == selectedCategory) &&
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قائمة القهوة", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("admin_login") }) {
                        Icon(Icons.Default.Settings, contentDescription = "الإدارة")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("البحث عن منتج...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Categories
            val dynamicCategories = remember(products) {
                listOf("الكل") + products.map { it.category }.distinct()
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(dynamicCategories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(product)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductCard(product: Product) {
    if (product.sizes.isEmpty()) return

    var selectedSizeIndex by remember { mutableIntStateOf(0) }
    // Add safeguard in case sizes change in DB while looking at ui
    val safeIndex = if (selectedSizeIndex < product.sizes.size) selectedSizeIndex else 0
    val selectedSize = product.sizes[safeIndex]

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Sizes
                if (product.sizes.size > 1) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        product.sizes.forEachIndexed { index, size ->
                            FilterChip(
                                selected = index == safeIndex,
                                onClick = { selectedSizeIndex = index },
                                label = { Text(size.name) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = product.sizes[0].name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(selectedSize.price)
                    Text(
                        text = "$formattedPrice د.ع",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
