package com.example.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.Product
import com.example.ProductSize
import com.example.ProductViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(navController: NavController) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("تسجيل دخول الإدارة") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ادخل الرمز السري للمتابعة", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it; showError = false },
                label = { Text("الرمز السري") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = showError,
                singleLine = true
            )
            if (showError) {
                Text("الرمز غير صحيح", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (pin == "xfb_") { // Hardcoded pin for demo 
                        navController.navigate("admin_dashboard") {
                            popUpTo("admin_login") { inclusive = true }
                        }
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("دخول")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, viewModel: ProductViewModel) {
    val products by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("إدارة المنتجات") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("admin_edit_product/new") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text(product.category, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = { navController.navigate("admin_edit_product/${product.id}") }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditProductScreen(navController: NavController, viewModel: ProductViewModel, productId: String?) {
    val products by viewModel.uiState.collectAsState()
    val isEditing = productId != null && productId != "new"
    
    val initialProduct = if (isEditing) products.find { it.id == productId } else null
    
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var imageUrl by remember { mutableStateOf(initialProduct?.imageUrl ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "العصائر الطبيعية") }
    
    // We'll manage sizes as a list of mutable pairs for the UI
    var sizes by remember { mutableStateOf(
        initialProduct?.sizes?.map { Pair(it.name, it.price.toString()) } ?: listOf(Pair("عادي", "0"))
    ) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val fileName = "product_img_${System.currentTimeMillis()}.jpg"
                    val file = File(context.filesDir, fileName)
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    imageUrl = android.net.Uri.fromFile(file).toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEditing) "تعديل منتج" else "إضافة منتج") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("رابط الصورة أو مسارها") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = "اختر صورة")
                }
            }
            
            if (imageUrl.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("التصنيف") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            val categoriesList = listOf(
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
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoriesList) { cat ->
                    SuggestionChip(
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }
            
            Text("الأحجام والأسعار", style = MaterialTheme.typography.titleSmall)
            sizes.forEachIndexed { index, sizePair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sizePair.first,
                        onValueChange = { newName ->
                            val newList = sizes.toMutableList()
                            newList[index] = newList[index].copy(first = newName)
                            sizes = newList
                        },
                        label = { Text("الحجم") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sizePair.second,
                        onValueChange = { newPrice ->
                            val newList = sizes.toMutableList()
                            newList[index] = newList[index].copy(second = newPrice)
                            sizes = newList
                        },
                        label = { Text("السعر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { 
                        if (sizes.size > 1) {
                            val newList = sizes.toMutableList()
                            newList.removeAt(index)
                            sizes = newList
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                    }
                }
            }
            
            TextButton(onClick = { 
                sizes = sizes + Pair("", "0") 
            }) {
                Text("إضافة حجم")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val finalSizes = sizes.mapNotNull { 
                        val priceInt = it.second.toIntOrNull()
                        if (it.first.isNotBlank() && priceInt != null) {
                            ProductSize(it.first, priceInt)
                        } else null
                    }
                    if (name.isNotBlank() && finalSizes.isNotEmpty()) {
                        val product = Product(
                            id = if (isEditing) productId!! else UUID.randomUUID().toString(),
                            name = name,
                            imageUrl = imageUrl,
                            sizes = finalSizes,
                            category = category
                        )
                        viewModel.addOrUpdateProduct(product)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ")
            }
        }
    }
}
