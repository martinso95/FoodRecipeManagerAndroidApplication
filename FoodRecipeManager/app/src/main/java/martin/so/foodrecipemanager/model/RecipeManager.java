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

    private ArrayList<Recipe> allRecipesList;
    private ArrayList<Recipe> breakfastRecipesList;
    private ArrayList<Recipe> lightMealRecipesList;
    private ArrayList<Recipe> heavyMealRecipesList;
    private ArrayList<Recipe> dessertRecipesList;

    private static final RecipeManager recipeManagerInstance = new RecipeManager();

    public static RecipeManager getInstance() {
        return recipeManagerInstance;
    }

    private RecipeManager() {
        allRecipesList = new ArrayList<>();
        breakfastRecipesList = new ArrayList<>();
        lightMealRecipesList = new ArrayList<>();
        heavyMealRecipesList = new ArrayList<>();
        dessertRecipesList = new ArrayList<>();
    }

    public ArrayList<Recipe> getAllRecipes() {
        return allRecipesList;
    }

    public ArrayList<Recipe> getBreakfastRecipes() {
        return breakfastRecipesList;
    }

    public ArrayList<Recipe> getLightMealRecipes() {
        return lightMealRecipesList;
    }

    public ArrayList<Recipe> getHeavyMealRecipes() {
        return heavyMealRecipesList;
    }

    public ArrayList<Recipe> getDessertRecipes() {
        return dessertRecipesList;
    }

    /**
     * Adds a recipe into the recipe list in alphabetical order.
     *
     * @param recipe The recipe object.
     */
    public void addRecipe(Context context, Recipe recipe) {
        List<Recipe> typeRecipesList = getRecipeTypeList(recipe.getType());

        if (!recipe.getType().equals(Utils.RECIPE_TYPE_ALL)) {
            int index1 = Collections.binarySearch(typeRecipesList, recipe,
                    (recipe1, recipe2) -> recipe1.getName().compareToIgnoreCase(recipe2.getName()));

            if (index1 < 0) {
                index1 = (index1 * -1) - 1;
            }
            typeRecipesList.add(index1, recipe);
        }

        int index2 = Collections.binarySearch(allRecipesList, recipe,
                (recipe1, recipe2) -> recipe1.getName().compareToIgnoreCase(recipe2.getName()));

        if (index2 < 0) {
            index2 = (index2 * -1) - 1;
        }

        allRecipesList.add(index2, recipe);

        saveChanges(context);
    }

    /**
     * Edit the recipe based on parameters.
     *
     * @param context   The application context.
     * @param newRecipe The recipe object to be edited.
     */
    public void editRecipe(Context context, Recipe newRecipe, String name, String description, String category, String type, String instructions) {
        String previousName = newRecipe.getName();
        String previousType = newRecipe.getType();

        newRecipe.setName(name);
        newRecipe.setDescription(description);
        newRecipe.setCategory(category);
        newRecipe.setType(type);
        newRecipe.setInstructions(instructions);

        List<Recipe> recipeListToBeEdited = getRecipeTypeList(previousType);

        for (Recipe recipe : recipeListToBeEdited) {
            if (recipe.getName().equals(previousName)) {
                recipeListToBeEdited.remove(recipe);
                allRecipesList.remove(recipe);

                addRecipe(context, newRecipe);
                break;
            }
        }
        saveChanges(context);
    }

    public void removeRecipe(Context context, Recipe recipe) {
        List<Recipe> recipeListToBeEdited = getRecipeTypeList(recipe.getType());
        for (Recipe r : recipeListToBeEdited) {
            if (r.getName().equals(recipe.getName())) {
                recipeListToBeEdited.remove(r);
                allRecipesList.remove(r);
                break;
            }
        }
        new ImageHandler(context).deleteImageFile(recipe.getName());

        saveChanges(context);
    }

    /**
     * Save changes that has been made to the recipe list.
     * Uses SharedPreferences and gson json for conversions.
     * The recipe list is saved as a String, in the form of a json.
     *
     * @param context The application  context.
     */
    private void saveChanges(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("FoodRecipeManager", MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String json = gson.toJson(allRecipesList);

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
        if (recipes != null) allRecipesList.addAll(recipes);

        for (Recipe recipe : allRecipesList) {
            switch (recipe.getType()) {
                case Utils.RECIPE_TYPE_BREAKFAST:
                    breakfastRecipesList.add(recipe);
                    break;
                case Utils.RECIPE_TYPE_LIGHT_MEAL:
                    lightMealRecipesList.add(recipe);
                    break;
                case Utils.RECIPE_TYPE_HEAVY_MEAL:
                    heavyMealRecipesList.add(recipe);
                    break;
                case Utils.RECIPE_TYPE_DESSERT:
                    dessertRecipesList.add(recipe);
                    break;
            }
        }
    }

    /**
     * Returns the recipe list based on the recipe's type.
     *
     * @param recipe The recipe object.
     */
    private List<Recipe> getRecipeTypeList(String recipe) {
        switch (recipe) {
            case Utils.RECIPE_TYPE_ALL:
                return allRecipesList;
            case Utils.RECIPE_TYPE_BREAKFAST:
                return breakfastRecipesList;
            case Utils.RECIPE_TYPE_LIGHT_MEAL:
                return lightMealRecipesList;
            case Utils.RECIPE_TYPE_HEAVY_MEAL:
                return heavyMealRecipesList;
            case Utils.RECIPE_TYPE_DESSERT:
                return dessertRecipesList;
            default:
                return null;
        }
    }

}
