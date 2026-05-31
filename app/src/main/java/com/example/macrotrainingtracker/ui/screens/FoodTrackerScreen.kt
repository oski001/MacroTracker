package com.example.macrotrainingtracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macrotrainingtracker.data.barcode.BarcodeLookupService
import com.example.macrotrainingtracker.data.barcode.ScannedProduct
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.ui.model.FoodTrackerUiState
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val FOOD_CATEGORIES = listOf("Protein", "Dairy", "Carbs", "Fats", "Fruits", "Vegetables", "Others")
private const val CAT_ALL       = "Wszystkie"
private const val CAT_FAVORITES = "Ulubione"
private const val CAT_CUSTOM    = "Moje produkty"

private enum class WeightUnit(val label: String, val toGrams: Double) {
    G    ("g",    1.0),
    CUP  ("cup",  236.588236),
    TBSP ("tbsp", 14.786765),
    TSP  ("tsp",  4.928922)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodTrackerScreen(
    state: FoodTrackerUiState,
    onAddFromProduct: (FoodProductEntity, Double) -> Unit,
    onDeleteFood: (Long) -> Unit,
    onQuickAdd: (String, Int, Int, Int) -> Unit,
    onAddCustomProduct: (FoodProductEntity) -> Unit,
    onDeleteCustomProduct: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onSnoozeRecalcReminder: () -> Unit
) {
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CAT_ALL) }
    var selectedProduct  by remember { mutableStateOf<FoodProductEntity?>(null) }
    var showQuickAdd     by remember { mutableStateOf(false) }
    var showAddCustom    by remember { mutableStateOf(false) }
    val lastUsedGrams    = remember { mutableStateMapOf<Long, Double>() }

    // Barcode scanning state
    var barcodeLoading   by remember { mutableStateOf(false) }
    var scannedProduct   by remember { mutableStateOf<ScannedProduct?>(null) }
    var barcodeNotFound  by remember { mutableStateOf(false) }
    var barcodeError     by remember { mutableStateOf<String?>(null) }
    val coroutineScope   = rememberCoroutineScope()

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val barcode = result.contents ?: return@rememberLauncherForActivityResult
        barcodeLoading = true
        coroutineScope.launch {
            val res = BarcodeLookupService.lookup(barcode)
            barcodeLoading = false
            res.fold(
                onSuccess = { scannedProduct = it },
                onFailure = {
                    if (it.message?.contains("not found", ignoreCase = true) == true)
                        barcodeNotFound = true
                    else
                        barcodeError = it.message ?: "Błąd sieci"
                }
            )
        }
    }

    val hasFavorites = remember(state.products) { state.products.any { it.isFavorite } }
    val hasCustom    = remember(state.products) { state.products.any { it.isCustom } }

    val allChips = remember(hasFavorites, hasCustom) {
        buildList {
            add(CAT_ALL)
            if (hasFavorites) add(CAT_FAVORITES)
            if (hasCustom)    add(CAT_CUSTOM)
            addAll(FOOD_CATEGORIES)
        }
    }

    val filteredProducts = remember(searchQuery, selectedCategory, state.products) {
        val q = searchQuery.trim()
        var list = state.products
        list = when (selectedCategory) {
            CAT_ALL       -> list
            CAT_FAVORITES -> list.filter { it.isFavorite }
            CAT_CUSTOM    -> list.filter { it.isCustom }
            else          -> list.filter { !it.isCustom && it.category == selectedCategory }
        }
        if (q.length >= 2) list = list.filter { it.name.contains(q, ignoreCase = true) || it.category.contains(q, ignoreCase = true) }
        list.sortedWith(compareByDescending<FoodProductEntity> { it.isFavorite }.thenByDescending { it.isCustom }.thenBy { it.name })
    }

    val grouped = remember(filteredProducts, selectedCategory) {
        if (selectedCategory != CAT_ALL) {
            if (filteredProducts.isEmpty()) emptyMap() else mapOf(selectedCategory to filteredProducts)
        } else {
            val favs   = filteredProducts.filter { it.isFavorite }
            val custom = filteredProducts.filter { !it.isFavorite && it.isCustom }
            val rest   = filteredProducts.filter { !it.isFavorite && !it.isCustom }.groupBy { it.category }
            val result = linkedMapOf<String, List<FoodProductEntity>>()
            if (favs.isNotEmpty())    result[CAT_FAVORITES] = favs
            if (custom.isNotEmpty())  result[CAT_CUSTOM]    = custom
            FOOD_CATEGORIES.forEach { cat -> rest[cat]?.let { result[cat] = it } }
            rest.entries.filter { it.key !in FOOD_CATEGORIES }.sortedBy { it.key }.forEach { result[it.key] = it.value }
            result
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                Text("BIBLIOTEKA", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)
                Text("Żywienie", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        item(span = { GridItemSpan(3) }) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { showQuickAdd = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A), contentColor = AccentPrimary)
                ) { Text("+ QUICK ADD", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                Button(onClick = { showAddCustom = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A), contentColor = Color.White)
                ) { Text("+ NOWY PRODUKT", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
        item(span = { GridItemSpan(3) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Szukaj produktu…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, 
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline, 
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                // Barcode scan button
                IconButton(
                    onClick = {
                        barcodeLauncher.launch(ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES)
                            setPrompt("Skanuj kod kreskowy produktu")
                            setBeepEnabled(true)
                            setBarcodeImageEnabled(false)
                            setOrientationLocked(false)
                        })
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    if (barcodeLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Skanuj kod", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allChips.forEach { chip ->
                    val sel = chip == selectedCategory
                    Surface(shape = RoundedCornerShape(50), color = if (sel) AccentPrimary else Color(0xFF1A1A1A),
                        modifier = Modifier.border(1.dp, if (sel) AccentPrimary else Color(0xFF2A2A2A), RoundedCornerShape(50)).clickable { selectedCategory = chip }) {
                        Text(chip, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp), fontSize = 12.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (state.products.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = AccentPrimary, modifier = Modifier.size(32.dp))
                        Text("Ładowanie biblioteki…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else if (grouped.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🔍", fontSize = 28.sp)
                        Text("Brak wyników", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Spróbuj po angielsku lub skróć frazę", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }

        grouped.entries.forEach { (category, foods) ->
            item(span = { GridItemSpan(3) }) {
                Text(category.uppercase(), modifier = Modifier.padding(top = 8.dp, start = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = when (category) { CAT_FAVORITES -> Color(0xFFFFD700); CAT_CUSTOM -> AccentPrimary; else -> Color(0xFF9E9E9E) },
                    letterSpacing = 1.5.sp)
            }
            items(foods, key = { it.id }) { product ->
                FoodProductGridCell(
                    product = product,
                    onClick = { selectedProduct = product },
                    onToggleFavorite = { onToggleFavorite(product.id, product.isFavorite) },
                    onDelete = if (product.isCustom) { { onDeleteCustomProduct(product.id) } } else null
                )
            }
        }
    }

    selectedProduct?.let { product ->
        FoodLogDialog(product = product, initialGrams = lastUsedGrams[product.id] ?: 100.0,
            onDismiss = { selectedProduct = null },
            onLog = { grams -> lastUsedGrams[product.id] = grams; onAddFromProduct(product, grams); selectedProduct = null }
        )
    }

    if (showQuickAdd) QuickAddDialog(onDismiss = { showQuickAdd = false }, onAdd = { name, p, f, c -> onQuickAdd(name, p, f, c); showQuickAdd = false })
    if (showAddCustom) AddCustomProductDialog(onDismiss = { showAddCustom = false }, onSave = { onAddCustomProduct(it); showAddCustom = false })

    // Barcode scan result dialogs
    scannedProduct?.let { sp ->
        BarcodeProductDialog(
            product = sp,
            onDismiss = { scannedProduct = null },
            onSave = { entity ->
                onAddCustomProduct(entity)
                scannedProduct = null
            }
        )
    }

    if (barcodeNotFound) {
        AlertDialog(
            onDismissRequest = { barcodeNotFound = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
            title = { Text("Nie znaleziono", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Produkt nie istnieje w bazie Open Food Facts.\nMożesz dodać go ręcznie.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = { barcodeNotFound = false; showAddCustom = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("DODAJ RĘCZNIE", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { barcodeNotFound = false }) { Text("ZAMKNIJ", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }

    barcodeError?.let { err ->
        AlertDialog(
            onDismissRequest = { barcodeError = null },
            containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
            title = { Text("Błąd", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Nie udało się pobrać danych:\n$err", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { barcodeError = null }) { Text("OK", color = MaterialTheme.colorScheme.primary) } }
        )
    }
}

// ── Grid cell ────────────────────────────────────────────────────────────────

@Composable
private fun FoodProductGridCell(product: FoodProductEntity, onClick: () -> Unit, onToggleFavorite: () -> Unit, onDelete: (() -> Unit)?) {
    Box(modifier = Modifier.aspectRatio(1f)
        .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
        .border(1.dp, if (product.isFavorite) Color(0xFFFFD700).copy(alpha = 0.4f) else Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
        .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(product.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp).align(Alignment.TopStart).padding(2.dp)) {
            Icon(if (product.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder, contentDescription = null,
                tint = if (product.isFavorite) Color(0xFFFFD700) else Color(0xFF444444), modifier = Modifier.size(16.dp))
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).align(Alignment.TopEnd).padding(2.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF616161), modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ── FoodLogDialog with unit selector ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodLogDialog(product: FoodProductEntity, initialGrams: Double = 100.0, onDismiss: () -> Unit, onLog: (Double) -> Unit) {
    val initStr = if (initialGrams % 1.0 == 0.0) initialGrams.toInt().toString() else "%.2f".format(initialGrams)
    var amount       by remember(product.id) { mutableStateOf(initStr) }
    var portions     by remember(product.id) { mutableStateOf("1") }
    var unit         by remember { mutableStateOf(WeightUnit.G) }
    var unitExpanded by remember { mutableStateOf(false) }

    val amountVal   = amount.toDoubleOrNull()   ?: 0.0
    val portionsVal = portions.toIntOrNull()    ?: 1
    val grams       = amountVal * unit.toGrams  * portionsVal

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
        title = { Text(product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${(product.caloriesPerGram * grams).roundToInt()} kcal", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("B: ${"%.2f".format(product.proteinPerGram*grams)}g  T: ${"%.2f".format(product.fatsPerGram*grams)}g  W: ${"%.2f".format(product.carbsPerGram*grams)}g", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("≈ ${"%.2f".format(grams)}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = amount, onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                        label = { Text("ILOŚĆ") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline, 
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }, modifier = Modifier.width(100.dp)) {
                        OutlinedTextField(value = unit.label, onValueChange = {}, readOnly = true, label = { Text("JED.", fontSize = 9.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }, singleLine = true,
                            modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, 
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline, 
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            WeightUnit.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.label}  (${if (u == WeightUnit.G) "1 g" else "≈ ${"%.3f".format(u.toGrams)}g"})", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) },
                                    onClick = { unit = u; unitExpanded = false }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = portions, onValueChange = { if (it.matches(Regex("^\\d*$"))) portions = it },
                    label = { Text("PORCJE") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, 
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline, 
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = { Button(onClick = { onLog(grams) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(10.dp)) { Text("DODAJ", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

// ── QuickAddDialog ────────────────────────────────────────────────────────────

@Composable
fun QuickAddDialog(date: String = "", onDismiss: () -> Unit, onAdd: (String, Int, Int, Int) -> Unit) {
    var name    by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fats    by remember { mutableStateOf("") }
    var carbs   by remember { mutableStateOf("") }
    val p = protein.toIntOrNull() ?: 0; val f = fats.toIntOrNull() ?: 0; val c = carbs.toIntOrNull() ?: 0
    val calories = p * 4 + f * 9 + c * 4

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
        title = { Text("Quick Add", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("$calories kcal", fontSize = 28.sp, fontWeight = FontWeight.Black, color = AccentPrimary)
                }
                QAField("NAZWA (opcjonalna)", name, KeyboardType.Text) { name = it }
                QAField("BIAŁKO (g)", protein, KeyboardType.Number) { if (it.matches(Regex("^\\d*$"))) protein = it }
                QAField("TŁUSZCZE (g)", fats, KeyboardType.Number) { if (it.matches(Regex("^\\d*$"))) fats = it }
                QAField("WĘGLOWODANY (g)", carbs, KeyboardType.Number) { if (it.matches(Regex("^\\d*$"))) carbs = it }
            }
        },
        confirmButton = { Button(onClick = { onAdd(name, p, f, c) }, colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black), shape = RoundedCornerShape(10.dp)) { Text("DODAJ", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

// ── AddCustomProductDialog ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomProductDialog(onDismiss: () -> Unit, onSave: (FoodProductEntity) -> Unit) {
    var name             by remember { mutableStateOf("") }
    var category         by remember { mutableStateOf(FOOD_CATEGORIES.last()) }
    var expandedCategory by remember { mutableStateOf(false) }
    var kcal             by remember { mutableStateOf("") }
    var protein          by remember { mutableStateOf("") }
    var fats             by remember { mutableStateOf("") }
    var carbs            by remember { mutableStateOf("") }
    var autoCalc         by remember { mutableStateOf(true) }

    val pv = protein.toDoubleOrNull() ?: 0.0
    val fv = fats.toDoubleOrNull()    ?: 0.0
    val cv = carbs.toDoubleOrNull()   ?: 0.0
    val autoKcal = pv * 4 + fv * 9 + cv * 4

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
        title = { Text("Nowy produkt", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QAField("NAZWA PRODUKTU", name, KeyboardType.Text) { name = it }
                ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("KATEGORIA", fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline, 
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        FOOD_CATEGORIES.forEach { cat -> DropdownMenuItem(text = { Text(cat, color = MaterialTheme.colorScheme.onSurface) }, onClick = { category = cat; expandedCategory = false }) }
                    }
                }
                QAField("BIAŁKO / 100g (g)", protein, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) protein = it }
                QAField("TŁUSZCZE / 100g (g)", fats, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) fats = it }
                QAField("WĘGLOWODANY / 100g (g)", carbs, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) carbs = it }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Licz kalorie automatycznie", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("P×4 + T×9 + W×4", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoCalc, onCheckedChange = { autoCalc = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary))
                }
                if (autoCalc) {
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                        Text("${autoKcal.roundToInt()} kcal / 100g", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    QAField("KALORIE / 100g", kcal, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) kcal = it }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalKcal = if (autoCalc) autoKcal else (kcal.toDoubleOrNull() ?: return@Button)
                val pp = protein.toDoubleOrNull() ?: return@Button
                val ff = fats.toDoubleOrNull()    ?: return@Button
                val cc = carbs.toDoubleOrNull()   ?: return@Button
                if (name.isBlank()) return@Button
                onSave(FoodProductEntity(name = name, category = category, caloriesPerGram = finalKcal / 100.0, proteinPerGram = pp / 100.0, fatsPerGram = ff / 100.0, carbsPerGram = cc / 100.0, isCustom = true))
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(10.dp)) { Text("ZAPISZ", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

// ── Barcode result dialog ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeProductDialog(
    product: ScannedProduct,
    onDismiss: () -> Unit,
    onSave: (FoodProductEntity) -> Unit
) {
    val displayName = if (product.brand.isNotBlank()) "${product.name} (${product.brand})" else product.name
    var name             by remember { mutableStateOf(product.name.take(60)) }
    var category         by remember { mutableStateOf("Others") }
    var expandedCategory by remember { mutableStateOf(false) }
    var protein          by remember { mutableStateOf("%.2f".format(product.proteinPer100g)) }
    var fats             by remember { mutableStateOf("%.2f".format(product.fatPer100g)) }
    var carbs            by remember { mutableStateOf("%.2f".format(product.carbsPer100g)) }
    var autoCalc         by remember { mutableStateOf(product.kcalPer100g <= 0.0) }
    var kcal             by remember { mutableStateOf(if (product.kcalPer100g > 0) "%.1f".format(product.kcalPer100g) else "") }

    val pv = protein.toDoubleOrNull() ?: 0.0
    val fv = fats.toDoubleOrNull()    ?: 0.0
    val cv = carbs.toDoubleOrNull()   ?: 0.0
    val autoKcal = pv * 4 + fv * 9 + cv * 4
    val displayKcal = if (autoCalc) autoKcal else (kcal.toDoubleOrNull() ?: autoKcal)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A), shape = RoundedCornerShape(20.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Znaleziony produkt", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                Text(displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nutritional summary box
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MacroSummaryItem("KCAL", "${displayKcal.roundToInt()}", AccentPrimary)
                        MacroSummaryItem("B", "${protein}g", Color(0xFF66BB6A))
                        MacroSummaryItem("T", "${fats}g", Color(0xFFFF9800))
                        MacroSummaryItem("W", "${carbs}g", Color(0xFF4FC3F7))
                    }
                }
                QAField("NAZWA PRODUKTU", name, KeyboardType.Text) { name = it }
                ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true,
                        label = { Text("KATEGORIA", fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        singleLine = true, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        FOOD_CATEGORIES.forEach { cat -> DropdownMenuItem(text = { Text(cat, color = MaterialTheme.colorScheme.onSurface) }, onClick = { category = cat; expandedCategory = false }) }
                    }
                }
                QAField("BIAŁKO / 100g (g)", protein, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) protein = it }
                QAField("TŁUSZCZE / 100g (g)", fats, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) fats = it }
                QAField("WĘGLOWODANY / 100g (g)", carbs, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) carbs = it }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Licz kalorie automatycznie", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("P×4 + T×9 + W×4", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoCalc, onCheckedChange = { autoCalc = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentPrimary))
                }
                if (!autoCalc) {
                    QAField("KALORIE / 100g", kcal, KeyboardType.Decimal) { if (it.matches(Regex("^\\d*\\.?\\d*$"))) kcal = it }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalKcal = if (autoCalc) autoKcal else (kcal.toDoubleOrNull() ?: autoKcal)
                if (name.isBlank()) return@Button
                onSave(FoodProductEntity(
                    name = name, category = category,
                    caloriesPerGram = finalKcal / 100.0,
                    proteinPerGram  = pv / 100.0,
                    fatsPerGram     = fv / 100.0,
                    carbsPerGram    = cv / 100.0,
                    isCustom = true
                ))
            }, colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black), shape = RoundedCornerShape(10.dp)) {
                Text("DODAJ DO BIBLIOTEKI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
private fun MacroSummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun QAField(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label, fontSize = 10.sp) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
    )
}
