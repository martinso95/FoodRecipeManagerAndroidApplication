package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class for handling the recipes and provide global access to the recipe objects.
 * Provides data saving/loading by using Firebase realtime databse.
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

        saveChanges();
    }

    /**
     * Edit the recipe based on parameters.
     */
    public void editRecipe(String photoPath, Recipe recipe, String name, String description, String category, String type, List<Ingredient> ingredients, String instructions) {
        recipe.setPhotoPath(photoPath);
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setCategory(category);
        recipe.setType(type);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        saveChanges();
    }

    public void removeRecipe(Recipe recipe) {
        if (recipe.getPhotoPath() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Utils.FIREBASE_IMAGES_PATH).child(firebaseAuth.getUid()).child(recipe.getPhotoPath());
            storageReference.delete();
        }

        List<Recipe> recipeListToBeEdited = getRecipeTypeList(recipe.getType());
        recipeListToBeEdited.remove(recipe);
        allRecipesList.remove(recipe);

        saveChanges();
    }

    /**
     * Save changes that has been made to the recipe list.
     * Saves the entire list in Firebase realtime database.
     */
    private void saveChanges() {
        fireBaseDatabaseReference.setValue(allRecipesList);
    }

    /**
     * Load the entire recipe list in Firebase realtime database, into the current recipe list.
     */
    public void loadRecipes(Context context) {
        fireBaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    allRecipesList.clear();
                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                        Recipe recipe = dss.getValue(Recipe.class);

                        // Load the images locally for preventing the need of constant Firebase fetching.
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipe.getPhotoPath());
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        recipe.setTemporaryLocalPhoto(resource);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                            }
                        });

                        allRecipesList.add(recipe);
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
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("Test", "Failed to read the recipe list from Firebase.");
                // TODO: Handle failure of loading the recipe list from Firebase.
                // Ex. show "Something went wrong, reload please..." in the Recipe list view.
            }
        });
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
