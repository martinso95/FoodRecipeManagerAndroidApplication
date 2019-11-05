package martin.so.foodrecipemanager.model;

import java.util.ArrayList;

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


}
