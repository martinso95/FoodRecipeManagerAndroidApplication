package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A class for handling the recipes and provide global access to the recipe objects.
 * Provides data saving by using SharedPreferences.
 */
public class RecipeManager {

    private ArrayList<Recipe> recipeList;

    private static final RecipeManager recipeManagerInstance = new RecipeManager();

    public static RecipeManager getInstance() {
        return recipeManagerInstance;
    }

    private RecipeManager() {
        recipeList = new ArrayList<>();
    }

    public Recipe getRecipe(int index) {
        return recipeList.get(index);
    }

    /**
     * Adds a recipe into the recipe list in alphabetical order.
     *
     * @param recipe The recipe object.
     */
    public void addRecipe(Recipe recipe) {
        int index = Collections.binarySearch(recipeList, recipe,
                (recipe1, recipe2) -> recipe1.getName().compareToIgnoreCase(recipe2.getName()));

        if (index < 0) {
            index = (index * -1) - 1;
        }

        recipeList.add(index, recipe);
    }

    public ArrayList<Recipe> getAllRecipes() {
        return recipeList;
    }

    public void removeRecipe(Recipe recipe) {
        recipeList.remove(recipe);
    }

    /**
     * Save changes that has been made to the recipe list.
     * Uses SharedPreferences and gson json for conversions.
     * The recipe list is saved as a String, in the form of a json.
     *
     * @param context The application  context.
     */
    public void saveChanges(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("FoodRecipeManager", MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String json = gson.toJson(recipeList);

        editor.putString("allRecipes", json);
        editor.apply();
    }

    /**
     * Load the current saved recipe list from SharedPreferences.
     * Loads it into the active recipe list.
     * Uses SharedPreferences and gson json for conversions.
     *
     * @param context The application  context.
     */
    public void loadRecipes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("FoodRecipeManager", MODE_PRIVATE);
        Gson gson = new Gson();

        String string = prefs.getString("allRecipes", null);

        Type type = new TypeToken<List<Recipe>>() {
        }.getType();
        List<Recipe> recipes = gson.fromJson(string, type);
        if (recipes != null) recipeList.addAll(recipes);
    }

}
