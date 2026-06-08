package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
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

data class CartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val product: Product,
    val size: ProductSize
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
    
    // Settle dynamic checkout card lists
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var isCartExpanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

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
                    // Badge details for active listings
                    val totalQuantity = cartItems.size
                    IconButton(onClick = { 
                        if (cartItems.isNotEmpty()) {
                            isCartExpanded = !isCartExpanded
                        }
                    }) {
                        if (totalQuantity > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(totalQuantity.toString())
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "السلة")
                            }
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "السلة")
                        }
                    }
                    IconButton(onClick = { navController.navigate("admin_login") }) {
                        Icon(Icons.Default.Settings, contentDescription = "الإدارة")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
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

                // Calculate dynamic bottom padding so grid item elements aren't obscured by bottom bar card
                val bottomGridPadding = if (cartItems.isNotEmpty()) {
                    if (isCartExpanded) 340.dp else 115.dp
                } else {
                    16.dp
                }

                // Product Grid
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 180.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomGridPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalItemSpacing = 16.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = { selectedProd, selectedSize ->
                                cartItems.add(CartItem(product = selectedProd, size = selectedSize))
                                // Auto-expand when a product is clicked/added so the user knows it's added!
                                isCartExpanded = true
                            }
                        )
                    }
                }
            }

            // Collapsible/Expanding persistent bottom sheet overlay at the bottom
            if (cartItems.isNotEmpty()) {
                val totalSum = cartItems.sumOf { it.size.price }
                val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalSum)

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Slider / Drag Handle Indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "السلة",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تفاصيل السلة/الفاتورة (${cartItems.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { cartItems.clear() }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "تفريغ السلة",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                IconButton(onClick = { isCartExpanded = !isCartExpanded }) {
                                    Icon(
                                        imageVector = if (isCartExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = if (isCartExpanded) "تصغير" else "توسيع",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Expanded item lists
                        AnimatedVisibility(visible = isCartExpanded) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 160.dp)
                                ) {
                                    items(cartItems, key = { it.id }) { item ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.product.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "الحجم: ${item.size.name}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    val formattedItemPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(item.size.price)
                                                    Text(
                                                        text = "$formattedItemPrice د.ع",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { cartItems.remove(item) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "إزالة الحجم",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        // Bottom summary details action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "المجموع الكلي:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$formattedTotal د.ع",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { showConfirmationDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إتمام الطلب")
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Order Complete Notification / Success Dialog
    if (showConfirmationDialog) {
        val totalSum = cartItems.sumOf { it.size.price }
        val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalSum)

        AlertDialog(
            onDismissRequest = { 
                cartItems.clear()
                showConfirmationDialog = false 
            },
            confirmButton = {
                Button(
                    onClick = {
                        cartItems.clear()
                        showConfirmationDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حسناً")
                }
            },
            title = {
                Text(
                    text = "تم إرسال طلبك بنجاح!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "شكراً لطلبك من كافيه قائمة القهوة! تفاصيل طلبك جاهزة الآن:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "عدد المنتجات: ${cartItems.size} عنصر",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المجموع الكلي: $formattedTotal د.ع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "سيتم تحضير القهوة الخاصة بك وتجهيزها في غضون دقائق معدودة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductCard(product: Product, onAddToCart: (Product, ProductSize) -> Unit) {
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
            if (product.imageUrl.isNotBlank()) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(selectedSize.price)
                    Text(
                        text = "$formattedPrice د.ع",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onAddToCart(product, selectedSize) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة إلى السلة",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
