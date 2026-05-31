import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun main() {
    val jsonFile = File("app/src/main/java/com/example/macrotrainingtracker/FoodData_Central_foundation_food_json_2026-04-30.json")
    val outputFile = File("app/src/main/java/com/example/macrotrainingtracker/extracted_foods.txt")
    
    println("Reading JSON file...")
    val jsonContent = jsonFile.readText()
    
    println("Parsing JSON...")
    val jsonRoot = JSONObject(jsonContent)
    val foods = jsonRoot.optJSONArray("FoundationFoods") ?: jsonRoot.optJSONArray("foods") ?: return
    
    val output = StringBuilder()
    output.appendLine("Food Name,Protein (g/100g),Carbohydrates (g/100g),Fat (g/100g),Calories (kcal/100g)")
    
    var processedCount = 0
    
    for (i in 0 until foods.length()) {
        val food = foods.getJSONObject(i)
        val description = food.optString("description", "Unknown")
        
        val foodNutrients = food.optJSONArray("foodNutrients") ?: continue
        
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0
        var calories = 0.0
        
        for (j in 0 until foodNutrients.length()) {
            val nutrient = foodNutrients.getJSONObject(j)
            val nutrientObj = nutrient.optJSONObject("nutrient") ?: continue
            val name = nutrientObj.optString("name", "").lowercase()
            val amount = nutrient.optDouble("amount", 0.0)
            
            when {
                name.contains("protein") -> protein = amount
                name.contains("carbohydrate") -> carbs = amount
                name.contains("total lipid") || name.contains("fat, total") -> fat = amount
                name.contains("energy") && name.contains("kcal") -> calories = amount
            }
        }
        
        if (protein > 0 || carbs > 0 || fat > 0) {
            output.appendLine("\"$description\",$protein,$carbs,$fat,$calories")
            processedCount++
        }
    }
    
    outputFile.writeText(output.toString())
    println("Extracted $processedCount foods to ${outputFile.absolutePath}")
}

main()

