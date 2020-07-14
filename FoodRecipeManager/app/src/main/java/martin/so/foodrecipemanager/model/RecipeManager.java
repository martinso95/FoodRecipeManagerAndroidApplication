package martin.so.foodrecipemanager.model;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class for handling the recipes and provide global access to the recipe objects.
 * Provides data saving/loading by using Firebase realtime database.
 */
public class RecipeManager {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference fireBaseDatabaseReference;

    private ArrayList<Recipe> allRecipesList;
    private ArrayList<Recipe> breakfastRecipesList;
    private ArrayList<Recipe> lightMealRecipesList;
    private ArrayList<Recipe> heavyMealRecipesList;
    private ArrayList<Recipe> dessertRecipesList;

    private static RecipeManager recipeManagerInstance = new RecipeManager();

    public static RecipeManager getInstance() {
        return recipeManagerInstance;
    }

    private RecipeManager() {
    }

    /**
     * Initialized the RecipeManager with a new FirebaseAuth and FirebaseDatabase Reference instances.
     * Needs to be re-initialized whenever a new user signs in so that the data gets refreshed.
     */
    public void initializeRecipeManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        fireBaseDatabaseReference = FirebaseDatabase.getInstance().getReference(Utils.FIREBASE_RECIPES_PATH).child(firebaseAuth.getUid());
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
    public void addRecipe(Recipe recipe) {
        List<Recipe> typeRecipesList = getRecipeTypeList(recipe.getType());

        int index1 = Collections.binarySearch(typeRecipesList, recipe,
                (recipe1, recipe2) -> recipe1.getName().compareToIgnoreCase(recipe2.getName()));

        if (index1 < 0) {
            index1 = (index1 * -1) - 1;
        }
        typeRecipesList.add(index1, recipe);

        int index2 = Collections.binarySearch(allRecipesList, recipe,
                (recipe1, recipe2) -> recipe1.getName().compareToIgnoreCase(recipe2.getName()));

        if (index2 < 0) {
            index2 = (index2 * -1) - 1;
        }

        allRecipesList.add(index2, recipe);

        saveChanges();
    }

    /**
     * Edit the recipe based on parameters.
     */
    public void editRecipe(Recipe recipe, String photoPath, String name, String category, String type, int timeHours, int timeMinutes, List<Ingredient> ingredients, String instructions) {
        String oldName = recipe.getName();
        String oldType = recipe.getType();

        // Remove old recipe from the main list.
        for (Recipe oldRecipe : allRecipesList) {
            if (oldRecipe.getName().equals(oldName)) {
                allRecipesList.remove(oldRecipe);
                break;
            }
        }

        // Remove old recipe from the sub list.
        List<Recipe> subList = getRecipeTypeList(oldType);
        for (Recipe oldRecipe : subList) {
            if (oldRecipe.getName().equals(oldName)) {
                subList.remove(oldRecipe);
                break;
            }
        }

        recipe.setPhotoPath(photoPath);
        recipe.setName(name);
        recipe.setCategory(category);
        recipe.setType(type);
        recipe.setTimeHours(timeHours);
        recipe.setTimeMinutes(timeMinutes);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);

        // Add newly edited recipe to the main list and sub list.
        addRecipe(recipe);
    }

    public void removeRecipe(Recipe recipe) {
        // Remove photo if it exists.
        if (recipe.getPhotoPath() != null) {
            removeRecipePhoto(recipe.getPhotoPath());
        }

        // Remove old recipe from the main list.
        for (Recipe r : allRecipesList) {
            if (r.getName().equals(recipe.getName())) {
                allRecipesList.remove(r);
                break;
            }
        }

        // Remove old recipe from the sub list.
        List<Recipe> subList = getRecipeTypeList(recipe.getType());
        for (Recipe r : subList) {
            if (r.getName().equals(recipe.getName())) {
                subList.remove(r);
                break;
            }
        }
        saveChanges();
    }

    /**
     * If the file to be removed already exists locally, it means that it has not been uploaded to Firebase yet.
     * Therefore, the local file should be deleted, instead of trying to delete non existing file in Firebase.
     * If the local file does not exist, then delete in Firebase, because the file was already uploaded.
     *
     * @param recipePhotoPath the photo path/name of the recipe.
     */
    public void removeRecipePhoto(String recipePhotoPath) {
        if (FirebaseStorageOfflineHandler.getInstance().fileExists(recipePhotoPath)) {
            FirebaseStorageOfflineHandler.getInstance().removeFileForUploadInFirebaseStorage(recipePhotoPath);
        } else {
            FirebaseStorageOfflineHandler.getInstance().addFileForDeletionInFirebaseStorage(recipePhotoPath);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Utils.FIREBASE_IMAGES_PATH).child(firebaseAuth.getUid()).child(recipePhotoPath);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseStorageOfflineHandler.getInstance().removeFileForDeletionInFirebaseStorage(recipePhotoPath);
                }
            });
        }
    }

    /**
     * Save changes that has been made to the recipe list.
     * Saves the entire list in Firebase realtime database.
     */
    public void saveChanges() {
        fireBaseDatabaseReference.setValue(allRecipesList);
    }

    /**
     * Returns the recipe list based on the recipe's type.
     *
     * @param recipeType The recipe object.
     */
    private List<Recipe> getRecipeTypeList(String recipeType) {
        switch (recipeType) {
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
