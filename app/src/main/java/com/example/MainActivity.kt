package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.graphics.Bitmap
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.ProductRepository
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable

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
    val size: ProductSize,
    val quantity: Int = 1
)

val sampleProducts = listOf(
    // 1. العصائر الطبيعية
    Product("j1", "عصير خوخ", "", listOf(ProductSize("عادي", 3000)), "العصائر الطبيعية"),
    Product("j3", "عصير برتقال", "", listOf(ProductSize("عادي", 3000)), "العصائر الطبيعية"),
    Product("j2", "عصير بطيخ", "", listOf(ProductSize("عادي", 3000)), "العصائر الطبيعية"),
    Product("j4", "مكس ليمون نعناع", "", listOf(ProductSize("عادي", 3000)), "العصائر الطبيعية"),
    Product("j5", "عصير مانجا", "", listOf(ProductSize("عادي", 4000)), "العصائر الطبيعية"),
    Product("j6", "مكس فراوله موز", "", listOf(ProductSize("عادي", 4000)), "العصائر الطبيعية"),
    Product("j7", "مكس فراوله مانجا", "", listOf(ProductSize("عادي", 5000)), "العصائر الطبيعية"),

    // 2. ميلك شيك
    Product("j8", "ميلك شيك لوتس", "", listOf(ProductSize("عادي", 5000)), "ميلك شيك"),
    Product("j9", "ميلك شيك اوريو", "", listOf(ProductSize("عادي", 4500)), "ميلك شيك"),
    Product("j10", "ميلك شيك بستاشيو", "", listOf(ProductSize("عادي", 4500)), "ميلك شيك"),

    // 3. موهيتو
    Product("m1", "موهيتو توت", "", listOf(ProductSize("عادي", 2500)), "موهيتو"),
    Product("m2", "موهيتو مانجا", "", listOf(ProductSize("عادي", 2500)), "موهيتو"),
    Product("m3", "موهيتو بلوبيري", "", listOf(ProductSize("عادي", 2500)), "موهيتو"),
    Product("m4", "موهيتو فراوله", "", listOf(ProductSize("عادي", 2500)), "موهيتو"),
    Product("m5", "موهيتو خوخ", "", listOf(ProductSize("عادي", 2500)), "موهيتو"),
    Product("m6", "موهيتو موز", "", listOf(ProductSize("عادي", 2500)), "موهيتو"),

    // 4. المشروبات الباردة
    Product("c1", "ماتشا كلاسك", "", listOf(ProductSize("عادي", 4000)), "المشروبات الباردة"),
    Product("c2_1", "ماتشا فراوله", "", listOf(ProductSize("عادي", 4500)), "المشروبات الباردة"),
    Product("c2_2", "ماتشا جوز الهند", "", listOf(ProductSize("عادي", 4500)), "المشروبات الباردة"),
    Product("c2_3", "ماتشا مانجا", "", listOf(ProductSize("عادي", 4500)), "المشروبات الباردة"),
    Product("c3", "كركديه ليش", "", listOf(ProductSize("عادي", 3500)), "المشروبات الباردة"),
    Product("c4", "انرجي ليش", "", listOf(ProductSize("عادي", 4000)), "المشروبات الباردة"),
    Product("c5", "ايكودا", "", listOf(ProductSize("عادي", 3000)), "المشروبات الباردة"),
    Product("c6", "كولد برو ليش", "", listOf(ProductSize("عادي", 3500)), "المشروبات الباردة"),
    Product("c7", "كولد برو انرجي", "", listOf(ProductSize("عادي", 4000)), "المشروبات الباردة"),

    // 5. ايس تي
    Product("i1", "ايس تي خوخ", "", listOf(ProductSize("عادي", 2500)), "ايس تي"),
    Product("i2", "ايس تي أناناس", "", listOf(ProductSize("عادي", 2500)), "ايس تي"),
    Product("i4", "ايس تي ليمون", "", listOf(ProductSize("عادي", 2500)), "ايس تي"),
    Product("i3", "ايس تي باشن فروت", "", listOf(ProductSize("عادي", 2500)), "ايس تي"),

    // 6. ايس لاتيه
    Product("i6", "ايس لاتيه فانيلا", "", listOf(ProductSize("عادي", 4000)), "ايس لاتيه"),
    Product("i5", "ايس لاتيه بندق", "", listOf(ProductSize("عادي", 4000)), "ايس لاتيه"),
    Product("i7", "ايس لاتيه كرميل", "", listOf(ProductSize("عادي", 4000)), "ايس لاتيه"),
    Product("i8", "ايس لاتيه كلاسك", "", listOf(ProductSize("عادي", 3500)), "ايس لاتيه"),

    // 7. ايس امريكانو
    Product("i9", "ايس امريكانو برتقال", "", listOf(ProductSize("عادي", 4000)), "ايس امريكانو"),
    Product("i10", "ايس امريكانو كلاسك", "", listOf(ProductSize("عادي", 4000)), "ايس امريكانو"),

    // 8. المشروبات الساخنة
    Product("h1", "اسبريسو سنكل", "", listOf(ProductSize("عادي", 2000)), "المشروبات الساخنة"),
    Product("h2", "اسبريسو دبل", "", listOf(ProductSize("عادي", 3000)), "المشروبات الساخنة"),
    Product("h3", "كبتشينو", "", listOf(ProductSize("عادي", 3000)), "المشروبات الساخنة"),
    Product("h7", "هوت چوکلیت ارت", "", listOf(ProductSize("عادي", 3500)), "المشروبات الساخنة"),
    Product("h6", "بينك لاتيه", "", listOf(ProductSize("عادي", 3000)), "المشروبات الساخنة"),
    Product("h4", "لاتيه ساخن", "", listOf(ProductSize("عادي", 3000)), "المشروبات الساخنة"),
    Product("h9", "كورتيادو", "", listOf(ProductSize("عادي", 4000)), "المشروبات الساخنة"),
    Product("h5", "هوت چوکلیت", "", listOf(ProductSize("عادي", 3000)), "المشروبات الساخنة"),
    Product("s1", "شاي عراقي", "", listOf(ProductSize("عادي", 500)), "المشروبات الساخنة"),
    Product("s4", "شاي ليمون", "", listOf(ProductSize("عادي", 2000)), "المشروبات الساخنة"),
    Product("s5", "شاي كرك", "", listOf(ProductSize("عادي", 2000)), "المشروبات الساخنة"),
    Product("h8", "موكا", "", listOf(ProductSize("عادي", 3500)), "المشروبات الساخنة"),
    Product("s3", "شاي رمان", "", listOf(ProductSize("عادي", 2000)), "المشروبات الساخنة"),
    Product("s2", "شاي نعناع", "", listOf(ProductSize("عادي", 1000)), "المشروبات الساخنة"),
    Product("h10", "امريكانو", "", listOf(ProductSize("عادي", 4000)), "المشروبات الساخنة"),

    // 9. القهوة
    Product("t1", "القهوة التركية", "", listOf(ProductSize("عادي", 2500)), "القهوة"),
    Product("t2", "القهوة بندق", "", listOf(ProductSize("عادي", 2500)), "القهوة"),
    Product("t3", "القهوة سادة", "", listOf(ProductSize("عادي", 2500)), "القهوة"),
    Product("t4", "القهوة جكليتيه", "", listOf(ProductSize("عادي", 2500)), "القهوة"),
    Product("t5", "القهوة فستق", "", listOf(ProductSize("عادي", 2500)), "القهوة"),
    Product("t6", "القهوة وسط", "", listOf(ProductSize("عادي", 2500)), "القهوة"),
    Product("t7", "القهوة مكسرات", "", listOf(ProductSize("عادي", 2500)), "القهوة")
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
    val cartItems = viewModel.cartItems
    var isCartExpanded by rememberSaveable { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showPrintDialog by remember { mutableStateOf(false) }

    val filteredProducts = remember(searchQuery, selectedCategory, products) {
        products.filter {
            (selectedCategory == "الكل" || it.category == selectedCategory) &&
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val categoriesOrder = remember {
        listOf(
            "العصائر الطبيعية",
            "ميلك شيك",
            "موهيتو",
            "المشروبات الباردة",
            "ايس تي",
            "ايس لاتيه",
            "ايس امريكانو",
            "المشروبات الساخنة",
            "القهوة"
        )
    }

    val groupedProducts = remember(filteredProducts, categoriesOrder) {
        val defined = categoriesOrder.map { cat ->
            cat to filteredProducts.filter { it.category == cat }
        }.filter { it.second.isNotEmpty() }
        
        val otherCats = filteredProducts.map { it.category }.distinct() - categoriesOrder.toSet()
        val undefined = otherCats.map { cat ->
            cat to filteredProducts.filter { it.category == cat }
        }
        
        defined + undefined
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeshLogo()
                        Column {
                            Text("ليش كافيه", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Lesh Cafe", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    val totalQuantity = cartItems.sumOf { it.quantity }
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
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Products grid columns on the left (taking majority of the space)
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("البحث عن منتج...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Categories Filter Row
                    val dynamicCategories = remember(products, categoriesOrder) {
                        val existing = products.map { it.category }.distinct()
                        val sorted = categoriesOrder.filter { it in existing }
                        val remaining = existing - categoriesOrder.toSet()
                        listOf("الكل") + sorted + remaining
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
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Product Grid (No dynamic bottom padding needed in landscape!)
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = 180.dp),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 16.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        groupedProducts.forEach { (categoryName, productList) ->
                            item(span = StaggeredGridItemSpan.FullLine) {
                                CategoryHeader(title = categoryName)
                            }
                            items(productList, key = { it.id }) { product ->
                                ProductCard(
                                    product = product,
                                    onAddToCart = { selectedProd, selectedSize ->
                                        val existingIndex = cartItems.indexOfFirst { 
                                            it.product.id == selectedProd.id && it.size.name == selectedSize.name 
                                        }
                                        if (existingIndex != -1) {
                                            val existingItem = cartItems[existingIndex]
                                            cartItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
                                        } else {
                                            cartItems.add(CartItem(product = selectedProd, size = selectedSize, quantity = 1))
                                        }
                                        isCartExpanded = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Split pane Cart detail side bar on the right
                if (cartItems.isNotEmpty()) {
                    val totalSum = cartItems.sumOf { it.size.price * it.quantity }
                    val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalSum)

                    Card(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxHeight()
                            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp, start = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(width = 40.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(6.dp))

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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تفاصيل السلة (${cartItems.sumOf { it.quantity }})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = { cartItems.clear() }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "تفريغ السلة",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
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
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.product.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "×${item.quantity}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = if (item.size.name == "عادي") "" else "الحجم: ${item.size.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                val formattedItemPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(item.size.price * item.quantity)
                                                Text(
                                                    text = "$formattedItemPrice د.ع",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        val idx = cartItems.indexOf(item)
                                                        if (idx != -1) {
                                                            if (item.quantity > 1) {
                                                                cartItems[idx] = item.copy(quantity = item.quantity - 1)
                                                            } else {
                                                                cartItems.removeAt(idx)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = "تقليل الكمية",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Text(
                                                    text = item.quantity.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        val idx = cartItems.indexOf(item)
                                                        if (idx != -1) {
                                                            cartItems[idx] = item.copy(quantity = item.quantity + 1)
                                                        }
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "زيادة الكمية",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))

                                                IconButton(
                                                    onClick = { cartItems.remove(item) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "إزالة الحجم",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
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
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElevatedButton(
                                        onClick = { showPrintDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color(0xFF2E7D32), // high-visibility print green
                                            contentColor = Color.White
                                        ),
                                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🖨️ طباعة الفاتورة (بلوتوث)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Portrait mode view (overlays cart at the bottom)
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        placeholder = { Text("البحث عن منتج...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Categories Filter Row
                    val dynamicCategories = remember(products, categoriesOrder) {
                        val existing = products.map { it.category }.distinct()
                        val sorted = categoriesOrder.filter { it in existing }
                        val remaining = existing - categoriesOrder.toSet()
                        listOf("الكل") + sorted + remaining
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(dynamicCategories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
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
                        groupedProducts.forEach { (categoryName, productList) ->
                            item(span = StaggeredGridItemSpan.FullLine) {
                                CategoryHeader(title = categoryName)
                            }
                            items(productList, key = { it.id }) { product ->
                                ProductCard(
                                    product = product,
                                    onAddToCart = { selectedProd, selectedSize ->
                                        val existingIndex = cartItems.indexOfFirst { 
                                            it.product.id == selectedProd.id && it.size.name == selectedSize.name 
                                        }
                                        if (existingIndex != -1) {
                                            val existingItem = cartItems[existingIndex]
                                            cartItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
                                        } else {
                                            cartItems.add(CartItem(product = selectedProd, size = selectedSize, quantity = 1))
                                        }
                                        // Auto-expand when a product is clicked/added so the user knows it's added!
                                        isCartExpanded = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Collapsible/Expanding persistent bottom sheet overlay at the bottom
                if (cartItems.isNotEmpty()) {
                    val totalSum = cartItems.sumOf { it.size.price * it.quantity }
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
                                        text = "تفاصيل السلة/الفاتورة (${cartItems.sumOf { it.quantity }})",
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
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = item.product.name,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = "×${item.quantity}",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                        Text(
                                                            text = if (item.size.name == "عادي") "" else "الحجم: ${item.size.name}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        val formattedItemPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(item.size.price * item.quantity)
                                                        Text(
                                                            text = "$formattedItemPrice د.ع",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                val idx = cartItems.indexOf(item)
                                                                if (idx != -1) {
                                                                    if (item.quantity > 1) {
                                                                        cartItems[idx] = item.copy(quantity = item.quantity - 1)
                                                                    } else {
                                                                        cartItems.removeAt(idx)
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Remove,
                                                                contentDescription = "تقليل الكمية",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }

                                                        Text(
                                                            text = item.quantity.toString(),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp)
                                                        )

                                                        IconButton(
                                                            onClick = {
                                                                val idx = cartItems.indexOf(item)
                                                                if (idx != -1) {
                                                                    cartItems[idx] = item.copy(quantity = item.quantity + 1)
                                                                }
                                                            },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription = "زيادة الكمية",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(4.dp))

                                                        IconButton(
                                                            onClick = { cartItems.remove(item) },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "إزالة الحجم",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
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
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
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
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ElevatedButton(
                                        onClick = { showPrintDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color(0xFF2E7D32), // high-visibility print green
                                            contentColor = Color.White
                                        ),
                                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🖨️ طباعة الفاتورة (بلوتوث)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Order Complete Notification / Success Dialog
    if (showConfirmationDialog) {
        val totalSum = cartItems.sumOf { it.size.price * it.quantity }
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
                        text = "عدد المنتجات: ${cartItems.sumOf { it.quantity }} عنصر",
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
                    Spacer(modifier = Modifier.height(20.dp))
                    ElevatedButton(
                        onClick = {
                            showPrintDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF2E7D32), // High-visibility print green
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🖨️ طباعة الفاتورة الفورية (بلوتوث)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        )
    }

    if (showPrintDialog) {
        BluetoothPrintDialog(
            cartItems = cartItems,
            onDismissRequest = { showPrintDialog = false },
            onOrderCompleted = {
                cartItems.clear()
                showPrintDialog = false
            }
        )
    }
}

@Composable
fun BluetoothPrintDialog(
    cartItems: List<CartItem>,
    onDismissRequest: () -> Unit,
    onOrderCompleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Shared Preferences to persist selected Bluetooth printer details
    val sharedPrefs = remember { context.getSharedPreferences("LESH_CAFE_PRINT_PREFS", Context.MODE_PRIVATE) }
    var selectedDeviceAddress by remember { mutableStateOf(sharedPrefs.getString("selected_printer_mac", "") ?: "") }
    var selectedDeviceName by remember { mutableStateOf(sharedPrefs.getString("selected_printer_name", "") ?: "") }
    
    var statusMessage by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(BluetoothPrinterHelper.hasBluetoothPermissions(context)) }
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        hasPermission = granted
        if (granted) {
            devices = BluetoothPrinterHelper.getPairedDevices(context)
            statusMessage = "تم منح الصلاحيات بنجاح! جاري جلب الأجهزة المقترنة..."
        } else {
            statusMessage = "برجاء توفير صلاحيات بلوتوث لتمكين الطباعة الحرارية."
        }
    }
    
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            devices = BluetoothPrinterHelper.getPairedDevices(context)
            if (selectedDeviceAddress.isBlank() && devices.isNotEmpty()) {
                val firstDevice = devices.first()
                try {
                    selectedDeviceAddress = firstDevice.address
                    selectedDeviceName = firstDevice.name ?: "Unknown Printer"
                    sharedPrefs.edit()
                        .putString("selected_printer_mac", firstDevice.address)
                        .putString("selected_printer_name", selectedDeviceName)
                        .apply()
                } catch (e: SecurityException) {
                    // Fail gracefully
                }
            }
        }
    }
    
    LaunchedEffect(cartItems) {
        // Generate high-fidelity visual receipt preview
        try {
            val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val randNum = (1000..9999).random()
            val receiptNo = "LSH-$randNum"
            val now = Date()
            
            previewBitmap = BluetoothPrinterHelper.generate80mmReceiptBitmap(
                context, cartItems, receiptNo, sdfDate.format(now), sdfTime.format(now)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("إغلاق", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = "إصدار وطباعة الفاتورة (بلوتوث) 🖨️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "معاينة الفاتورة قبل الطباعة (80mm):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Receipt Visual Card Mock
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFAF9F6) // warm receipt white
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        previewBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "معاينة الفاتورة قبل الطباعة",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!hasPermission) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تطبيق ليش كافيه يتطلب صلاحيات البلوتوث للاتصال بالطابعة الحرارية وإصدار فواتير الزبائن فورا.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    requestPermissionLauncher.launch(BluetoothPrinterHelper.getRequiredPermissions())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("منح صلاحيات البلوتوث ومتابعة الطباعة 🔒", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        // If there is a saved device, show action print button
                        if (selectedDeviceAddress.isNotBlank()) {
                            Button(
                                onClick = {
                                    val device = devices.find { it.address == selectedDeviceAddress }
                                    if (device != null) {
                                        scope.launch {
                                            BluetoothPrinterHelper.printCustomReceipt(
                                                context,
                                                device,
                                                cartItems
                                            ) { status ->
                                                statusMessage = status
                                            }
                                        }
                                    } else {
                                        // Attempt with whatever device matches address
                                        try {
                                            val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                                            val remoteDevice = adapter?.getRemoteDevice(selectedDeviceAddress)
                                            if (remoteDevice != null) {
                                                scope.launch {
                                                    BluetoothPrinterHelper.printCustomReceipt(
                                                        context,
                                                        remoteDevice,
                                                        cartItems
                                                    ) { status ->
                                                        statusMessage = status
                                                    }
                                                }
                                            } else {
                                                statusMessage = "الطابعة المطلوبة غير متوفرة حالياً بالبلوتوث"
                                            }
                                        } catch (e: Exception) {
                                            statusMessage = "لا تتوفر طابعة مرتبطة أو مقترنة حالياً"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32) // Prominent print green
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🖨️ اضغط لطباعة الفاتورة فوراّ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Active printer address
                            Text(
                                text = "طابعة البلوتوث النشطة: $selectedDeviceName",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Text(
                                text = "برجاء اختيار الطابعة من قائمة الطابعات المقترنة بالأسفل لبدء الطباعة 🔌",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "اختر طابعة البلوتوث المقترنة بالهاتف:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (devices.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "لم يتم العثور على أجهزة بلوتوث مقترنة بالهاتف.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يرجى تشغيل طابعة الإيصالات، والاقتران بها أولاً من إعدادات بلوتوث النظام.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        statusMessage = "تعذر فتح الإعدادات تلقائياً"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("فتح إعدادات البلوتوث للتسجيل", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        devices.forEach { device ->
                            val isSelected = device.address == selectedDeviceAddress
                            val deviceName = try { device.name ?: "طابعة حرارية غير معروفة" } catch (e: SecurityException) { "طابعة حرارية" }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedDeviceAddress = device.address
                                        selectedDeviceName = deviceName
                                        sharedPrefs.edit()
                                            .putString("selected_printer_mac", device.address)
                                            .putString("selected_printer_name", deviceName)
                                            .apply()
                                        
                                        // Auto print upon selection for ultimate worker convenience
                                        scope.launch {
                                            BluetoothPrinterHelper.printCustomReceipt(
                                                context,
                                                device,
                                                cartItems
                                            ) { status ->
                                                statusMessage = status
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = deviceName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = device.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                                        )
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "نشطة وطباعة 🖨️✨",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = "اختر واطبع 🔌",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (statusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        onOrderCompleted()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إكمال الطلب وتفريغ السلة ✅", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
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
                } else if (product.sizes.isNotEmpty() && product.sizes[0].name != "عادي") {
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

@Composable
fun LeshLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_lesh_logo),
            contentDescription = "Lesh Cafe Logo",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CategoryHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FloatingCoffeeBean(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f,
    angle: Float = 0f
) {
    Canvas(modifier = modifier.graphicsLayer(rotationZ = angle)) {
        val w = size.width
        val h = size.height
        // Draw the coffee bean background oval
        drawOval(
            color = Color(0xFF3B402B).copy(alpha = alpha), // beautiful dark olive-brown bean shade
            topLeft = Offset(0f, 0f),
            size = Size(w, h)
        )
        // Draw the middle bean crease
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.3f, w * 0.85f, h * 0.5f)
        }
        drawPath(
            path = path,
            color = Color(0xFF1F2216).copy(alpha = alpha),
            style = Stroke(width = w * 0.1f)
        )
    }
}

@Composable
fun SplashScreen(navController: NavController, modifier: Modifier = Modifier) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.05f else 0.85f,
        animationSpec = tween(durationMillis = 1200),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        kotlinx.coroutines.delay(2200)
        navController.navigate("menu") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF5A6044)) // Absolute matching organic olive-green background
            .clickable { 
                navController.navigate("menu") {
                    popUpTo("splash") { inclusive = true }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // High-quality full-screen splash image background transitioning smoothly via fade-in
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = "Lesh Cafe Splash Background",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha
                ),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}
