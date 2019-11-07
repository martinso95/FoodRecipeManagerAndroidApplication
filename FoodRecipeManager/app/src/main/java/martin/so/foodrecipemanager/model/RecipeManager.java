package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

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

    public void addRecipe(Recipe recipe) {
        recipeList.add(recipe);
    }

    public ArrayList<Recipe> getAllRecipes() {
        return recipeList;
    }

    public void removeRecipe(Recipe recipe) {
        recipeList.remove(recipe);
    }

    public void saveChanges(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("FoodRecipeManager", MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String json = gson.toJson(recipeList);

        editor.putString("allRecipes", json);
        editor.apply();
    }

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
