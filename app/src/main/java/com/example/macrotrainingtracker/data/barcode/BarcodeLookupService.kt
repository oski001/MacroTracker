package com.example.macrotrainingtracker.data.barcode

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ScannedProduct(
    val name: String,
    val brand: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double
)

object BarcodeLookupService {

    /**
     * Looks up a barcode via Open Food Facts API.
     * Returns [ScannedProduct] on success, null if not found or on error.
     */
    suspend fun lookup(barcode: String): Result<ScannedProduct> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "MacroTrainingTracker/1.0 (Android)")
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            try {
                val text = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                val root = JSONObject(text)
                val status = root.optInt("status", 0)
                if (status != 1) error("Product not found (status=$status)")

                val product = root.getJSONObject("product")
                val name  = product.optString("product_name").trim().ifBlank { "Nieznany produkt" }
                val brand = product.optString("brands").trim()
                val nut   = product.optJSONObject("nutriments")
                    ?: error("No nutriment data")

                fun n(key: String) = maxOf(0.0, nut.optDouble(key, nut.optDouble("${key}_100g", 0.0)))

                ScannedProduct(
                    name          = name,
                    brand         = brand,
                    kcalPer100g   = n("energy-kcal"),
                    proteinPer100g = n("proteins"),
                    fatPer100g    = n("fat"),
                    carbsPer100g  = n("carbohydrates")
                )
            } finally {
                conn.disconnect()
            }
        }.also { result ->
            if (result.isFailure)
                Log.w("BarcodeLookup", "Lookup failed for $barcode", result.exceptionOrNull())
        }
    }
}

