package martin.so.foodrecipemanager.ui.recipes;

import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.InformationDialog;
import martin.so.foodrecipemanager.model.Ingredient;
import martin.so.foodrecipemanager.model.IngredientsAdapter;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.Utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity containing the "Add recipe"-view.
 */
public class AddRecipeActivity extends AppCompatActivity {

    private ImageButton recipePhoto;
    private boolean recipePhotoAdded = false;
    private Uri recipePhotoLocalFilePath;
    private Bitmap recipePhotoBitmap = null;
    private TextInputEditText recipeName;
    private TextInputEditText recipeDescription;
    private Spinner recipeCategory;
    private String selectedRecipeCategory = Utils.RECIPE_CATEGORY;
    private Spinner recipeType;
    private String selectedRecipeType = Utils.RECIPE_TYPE;
    private TextInputEditText recipeIngredientInput;
    private ImageButton recipeAddIngredient;
    private ListView recipeIngredientsList;
    private List<Ingredient> recipeIngredients;
    IngredientsAdapter ingredientsAdapter;
    private TextInputEditText recipeInstructions;
    private RequestOptions glideRequestOptions;

    final String[] recipeCategories = {Utils.RECIPE_CATEGORY, Utils.RECIPE_CATEGORY_MEAT, Utils.RECIPE_CATEGORY_VEGETARIAN, Utils.RECIPE_CATEGORY_VEGAN};
    final String[] recipeTypes = {Utils.RECIPE_TYPE, Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_TYPE_DESSERT};

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_add);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recipePhoto = findViewById(R.id.imageButtonRecipePhotoAddRecipe);
        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameAddRecipe);
        recipeDescription = findViewById(R.id.textInputLayoutEditRecipeDescriptionAddRecipe);
        recipeCategory = findViewById(R.id.spinnerRecipeCategoryAddRecipe);
        recipeType = findViewById(R.id.spinnerRecipeTypeAddRecipe);
        recipeIngredientInput = findViewById(R.id.textInputLayoutEditRecipeAddIngredientAddRecipe);
        recipeAddIngredient = findViewById(R.id.imageButtonAddIngredientButtonAddRecipe);
        recipeIngredientsList = findViewById(R.id.listViewIngredientsAddRecipe);
        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsAddRecipe);

        recipePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            // Sets the type as image/*. This ensures only components of type image are selected.
            intent.setType("image/*");
            // Pass an extra array with the accepted mime types.
            // This will ensure that only components with these MIME types are targeted.
            String[] mimeTypes = {"image/jpeg", "image/png"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, PICK_IMAGE);
        });

        ArrayAdapter<String> recipeCategoryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, recipeCategories);
        recipeCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipeCategory.setAdapter(recipeCategoryAdapter);

        recipeCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                selectedRecipeCategory = recipeCategories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<String> recipeTypeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, recipeTypes);
        recipeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipeType.setAdapter(recipeTypeAdapter);

        recipeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                selectedRecipeType = recipeTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        recipeIngredients = new ArrayList<>();
        ingredientsAdapter = new IngredientsAdapter(this, recipeIngredients, true);
        recipeIngredientsList.setAdapter(ingredientsAdapter);

        recipeAddIngredient.setOnClickListener(v -> {
            String input = recipeIngredientInput.getText().toString();
            if (!input.isEmpty()) {
                recipeIngredients.add(new Ingredient(input));
                recipeIngredientInput.getText().clear();
                recipeIngredientInput.clearFocus();
                ingredientsAdapter.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
            }
        });

        glideRequestOptions = new RequestOptions();
        glideRequestOptions.centerCrop();
    }

    /**
     * Creates the menu item: "Add the recipe"-button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_add, menu);
        return true;
    }

    /**
     * Handling the click of the menu item: "Confirm adding the recipe".
     * If the recipe name that is wanted to be added already exists, the click will not go through.
     * If any fields are empty and not configured, the click will not go through,
     * and hints will be given based on what needs to be done.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_added_recipe_button) {
            if (checkFieldsAreNotEmpty()) {
                boolean duplicateFound = false;
                for (Recipe r : RecipeManager.getInstance().getAllRecipes()) {
                    if (r.getName().equals(recipeName.getText().toString())) {
                        recipeName.setError("Recipe name already exists");
                        duplicateFound = true;
                    }
                }
                if (!duplicateFound) {
                    if (recipePhotoAdded) {
                        String recipePhotoPath = UUID.randomUUID().toString();
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipePhotoPath);
                        storageReference.putFile(recipePhotoLocalFilePath);
                        Recipe recipe = new Recipe(recipePhotoPath, recipeName.getText().toString(), recipeDescription.getText().toString(), selectedRecipeType, selectedRecipeCategory, recipeIngredients, recipeInstructions.getText().toString());
                        recipe.setTemporaryLocalPhoto(recipePhotoBitmap);
                        RecipeManager.getInstance().addRecipe(recipe);

                        finish();
                    } else {
                        Recipe recipe = new Recipe(null, recipeName.getText().toString(), recipeDescription.getText().toString(), selectedRecipeType, selectedRecipeCategory, recipeIngredients, recipeInstructions.getText().toString());
                        RecipeManager.getInstance().addRecipe(recipe);
                        finish();
                    }
                }
            }
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if the recipe property input fields are not empty.
     * The reason is that none of these fields are allowed to be empty.
     */
    private boolean checkFieldsAreNotEmpty() {
        boolean noEmpty = true;
        if (recipeName.getText().toString().isEmpty()) {
            recipeName.setError("Please enter Recipe name");
            noEmpty = false;
        }
        if (recipeDescription.getText().toString().isEmpty()) {
            recipeDescription.setError("Please enter Description");
            noEmpty = false;
        }

        if (selectedRecipeCategory.equals(Utils.RECIPE_CATEGORY)) {
            Toast.makeText(getApplicationContext(), "Select Recipe Category and Type",
                    Toast.LENGTH_LONG).show();
            noEmpty = false;
        }
        if (selectedRecipeType.equals(Utils.RECIPE_TYPE)) {
            Toast.makeText(getApplicationContext(), "Select Recipe Category and Type",
                    Toast.LENGTH_LONG).show();
            noEmpty = false;
        }

        if (recipeIngredients.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Add ingredients",
                    Toast.LENGTH_LONG).show();
            noEmpty = false;
        }

        if (recipeInstructions.getText().toString().isEmpty()) {
            recipeInstructions.setError("Please enter Instructions");
            noEmpty = false;
        }
        return noEmpty;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            recipePhotoLocalFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), recipePhotoLocalFilePath);
                Glide.with(getApplicationContext()).load(bitmap).apply(glideRequestOptions).into(recipePhoto);
                recipePhotoBitmap = bitmap;
                recipePhotoAdded = true;
            } catch (IOException e) {
                Log.d("Test", "Failed to read image file path.");
                recipePhotoAdded = false;
                recipePhotoBitmap = null;
                InformationDialog informationDialog = new InformationDialog();
                informationDialog.showDialog(AddRecipeActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
                e.printStackTrace();
            }
        }
    }
}