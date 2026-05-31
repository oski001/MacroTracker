package com.example.macrotrainingtracker.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.macrotrainingtracker.data.local.dao.FoodLogDao
import com.example.macrotrainingtracker.data.local.dao.FoodProductDao
import com.example.macrotrainingtracker.data.local.dao.NutritionPlanDao
import com.example.macrotrainingtracker.data.local.dao.UserBodyDao
import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.math.max

@Database(
    entities = [
        FoodProductEntity::class,
        FoodLogEntity::class,
        UserBodyEntity::class,
        NutritionPlanEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodProductDao(): FoodProductDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun userBodyDao(): UserBodyDao
    abstract fun nutritionPlanDao(): NutritionPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val appContext = context.applicationContext
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "macro_tracker.db"
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch { seedIfEmpty(appContext) }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Also seed on open in case onCreate was interrupted
                        scope.launch { seedIfEmpty(appContext) }
                    }
                })
                .build()
        }

        private suspend fun seedIfEmpty(context: Context) {
            try {
                val dao = INSTANCE?.foodProductDao() ?: return
                if (dao.count() > 0) return
                val products = loadFromAssets(context)
                if (products.isNotEmpty()) {
                    dao.insertAll(products)
                    Log.d("AppDatabase", "Seeded ${products.size} food products from assets")
                } else {
                    Log.w("AppDatabase", "No products loaded from assets/foods.json")
                }
            } catch (e: Exception) {
                Log.e("AppDatabase", "Seeding failed", e)
            }
        }

        private fun loadFromAssets(context: Context): List<FoodProductEntity> {
            return try {
                val json = context.assets.open("foods.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                val array = JSONArray(json)
                val list = mutableListOf<FoodProductEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val kcalPer100 = max(0.0, obj.optDouble("kcal", 0.0))
                    val pro  = max(0.0, obj.optDouble("pro",  0.0))
                    val fat  = max(0.0, obj.optDouble("fat",  0.0))
                    val carb = max(0.0, obj.optDouble("carb", 0.0))
                    // Clean up encoding artifacts (e.g. "Â " from broken UTF-8)
                    val rawName = obj.getString("name")
                    val cleanName = rawName
                        .replace("Â\u00a0", " ")
                        .replace("Â ", " ")
                        .replace("\u00c2", "")
                        .trim()
                    val rawCat = obj.getString("cat")
                    val mappedCat = when (rawCat) {
                        "Warzywa" -> "Vegetables"
                        "Owoce" -> "Fruits"
                        "Nabiał i jaja" -> "Dairy"
                        "Zboża i makaron", "Wypieki",
                        "Rośliny strączkowe", "Słodycze" -> "Carbs"
                        "Drb", "Drób", "Wołowina", "Wieprzowina",
                        "Ryby i owoce morza", "Wędliny", "Inne mięso" -> "Protein"
                        "Tłuszcze i oleje", "Orzechy i nasiona" -> "Fats"
                        "Napoje", "Przyprawy", "Zupy i sosy",
                        "Fast food" -> "Others"
                        else -> "Others"
                    }
                    list += FoodProductEntity(
                        name            = cleanName,
                        category        = mappedCat,
                        caloriesPerGram = kcalPer100 / 100.0,
                        proteinPerGram  = pro  / 100.0,
                        fatsPerGram     = fat  / 100.0,
                        carbsPerGram    = carb / 100.0,
                        isCustom        = false
                    )
                }
                list
            } catch (e: Exception) {
                Log.e("AppDatabase", "loadFromAssets failed", e)
                emptyList()
            }
        }
    }
}
