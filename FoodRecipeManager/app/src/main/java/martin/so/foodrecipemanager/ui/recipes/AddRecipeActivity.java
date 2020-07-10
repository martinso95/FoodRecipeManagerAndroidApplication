package martin.so.foodrecipemanager.ui.recipes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.FirebaseStorageOfflineHandler;

import martin.so.foodrecipemanager.model.InformationDialog;
import martin.so.foodrecipemanager.model.Ingredient;
import martin.so.foodrecipemanager.model.IngredientsAdapter;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.Utils;

import android.app.Dialog;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity containing the "Add recipe"-view.
 */
public class AddRecipeActivity extends AppCompatActivity {

    private ImageButton recipePhoto;
    private File takePhotoFile = null;
    private boolean recipePhotoAdded = false;
    private Uri recipePhotoLocalFilePath = null;
    private Uri temporaryRecipePhotoLocalFilePath = null;
    private Bitmap recipePhotoBitmap = null;
    private TextInputEditText recipeName;
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

    final String[] recipeCategories = {Utils.RECIPE_CATEGORY_MEAT, Utils.RECIPE_CATEGORY, Utils.RECIPE_CATEGORY_VEGETARIAN, Utils.RECIPE_CATEGORY_VEGAN};
    final String[] recipeTypes = {Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_TYPE, Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_TYPE_DESSERT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_add);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recipePhoto = findViewById(R.id.imageButtonRecipePhotoAddRecipe);
        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameAddRecipe);
        recipeCategory = findViewById(R.id.spinnerRecipeCategoryAddRecipe);
        recipeType = findViewById(R.id.spinnerRecipeTypeAddRecipe);
        recipeIngredientInput = findViewById(R.id.textInputLayoutEditRecipeAddIngredientAddRecipe);
        recipeAddIngredient = findViewById(R.id.imageButtonAddIngredientButtonAddRecipe);
        recipeIngredientsList = findViewById(R.id.listViewIngredientsAddRecipe);
        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsAddRecipe);

        recipePhoto.setOnClickListener(v -> {
            showDialog(recipePhotoAdded);
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
        ingredientsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
            }
        });


        // placeholder for quicker testing...
        recipeIngredients.add(new Ingredient("abc"));
        recipeIngredientInput.getText().clear();
        recipeIngredientInput.clearFocus();
        ingredientsAdapter.notifyDataSetChanged();
        Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);

        recipeName.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                recipeName.clearFocus();
                handled = true;
            }
            return handled;
        });

        recipeIngredientInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addIngredient();
                handled = true;
            }
            return handled;
        });

        recipeAddIngredient.setOnClickListener(v -> {
            addIngredient();
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

                        FirebaseStorageOfflineHandler.getInstance().addFileForUploadInFirebaseStorage(recipePhotoPath, recipePhotoBitmap);

                        Recipe recipe = new Recipe(recipePhotoPath, recipeName.getText().toString(), selectedRecipeType, selectedRecipeCategory, recipeIngredients, recipeInstructions.getText().toString());
                        recipe.setTemporaryLocalPhoto(recipePhotoBitmap);
                        RecipeManager.getInstance().addRecipe(recipe);

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipePhotoPath);
                        storageReference.putFile(recipePhotoLocalFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (takePhotoFile != null)
                                    takePhotoFile.delete();
                                FirebaseStorageOfflineHandler.getInstance().removeFileForUploadInFirebaseStorage(recipePhotoPath);

                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipe.getPhotoPath());
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        recipe.setPhotoDownloadUri(uri.toString());
                                        RecipeManager.getInstance().saveChanges();
                                    }

                                });
                            }
                        });
                    } else {
                        Recipe recipe = new Recipe(null, recipeName.getText().toString(), selectedRecipeType, selectedRecipeCategory, recipeIngredients, recipeInstructions.getText().toString());
                        RecipeManager.getInstance().addRecipe(recipe);
                    }
                    finish();
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

    /**
     * Add the ingredient to the recipe, based on what the user has input in the ingredient textInput.
     */
    private void addIngredient() {
        String input = recipeIngredientInput.getText().toString();
        if (!input.isEmpty()) {
            recipeIngredients.add(new Ingredient(input));
            recipeIngredientInput.getText().clear();
            ingredientsAdapter.notifyDataSetChanged();
            Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
        }
        Utils.hideKeyboard(this);
        recipeIngredientInput.clearFocus();
    }

    /**
     * Shows a dialog in the activity in order to inform the user.
     * With possibility to redirect the user to a new activity.
     *
     * @param photoAdded flag for knowing whether to provide option to remove an added photo or not.
     */
    private void showDialog(boolean photoAdded) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_handle_photo);

        Button takePhotoButton = dialog.findViewById(R.id.buttonCaptureHandlePhotoDialog);
        takePhotoButton.setOnClickListener(v -> {
            dialog.dismiss();
            ActivityResultLauncher<Uri> takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), temporaryRecipePhotoLocalFilePath);
                        Glide.with(getApplicationContext()).load(bitmap).apply(glideRequestOptions).into(recipePhoto);
                        recipePhotoBitmap = bitmap;
                        recipePhotoAdded = true;
                        recipePhotoLocalFilePath = temporaryRecipePhotoLocalFilePath;
                        temporaryRecipePhotoLocalFilePath = null;
                    } catch (IOException e) {
                        recipePhotoAdded = false;
                        recipePhotoBitmap = null;
                        temporaryRecipePhotoLocalFilePath = null;
                        if (takePhotoFile != null)
                            takePhotoFile.delete();
                        InformationDialog informationDialog = new InformationDialog();
                        informationDialog.showDialog(AddRecipeActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
                        e.printStackTrace();
                    }
                } else {
                    if (takePhotoFile != null)
                        takePhotoFile.delete();
                    recipePhotoAdded = false;
                    recipePhotoBitmap = null;
                    temporaryRecipePhotoLocalFilePath = null;
                    InformationDialog informationDialog = new InformationDialog();
                    informationDialog.showDialog(AddRecipeActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
                }
            });
            try {
                if (takePhotoFile != null)
                    takePhotoFile.delete();
                takePhotoFile = Utils.createTemporaryPhoto(this);
                temporaryRecipePhotoLocalFilePath = FileProvider.getUriForFile(this, Utils.FILE_PROVIDER_AUTHORITY, takePhotoFile);
                takePhoto.launch(temporaryRecipePhotoLocalFilePath);
            } catch (IOException e) {
                temporaryRecipePhotoLocalFilePath = null;
                e.printStackTrace();
            }
        });

        Button pickPhotoButton = dialog.findViewById(R.id.buttonGalleryHandlePhotoDialog);
        pickPhotoButton.setOnClickListener(v -> {
            dialog.dismiss();
            ActivityResultLauncher<String> pickPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (recipePhotoAdded && takePhotoFile != null) {
                            takePhotoFile.delete();
                        }
                        recipePhotoLocalFilePath = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), recipePhotoLocalFilePath);
                            Glide.with(getApplicationContext()).load(bitmap).apply(glideRequestOptions).into(recipePhoto);
                            recipePhotoBitmap = bitmap;
                            recipePhotoAdded = true;
                        } catch (IOException e) {
                            recipePhotoAdded = false;
                            recipePhotoBitmap = null;
                            InformationDialog informationDialog = new InformationDialog();
                            informationDialog.showDialog(AddRecipeActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
                            e.printStackTrace();
                        }

                    });
            pickPhoto.launch("image/*");
        });

        Button removePhotoButton = dialog.findViewById(R.id.buttonRemoveHandlePhotoDialog);
        removePhotoButton.setOnClickListener(v -> {
            dialog.dismiss();
            // Remove current photo.
            if (recipePhotoAdded && takePhotoFile != null)
                takePhotoFile.delete();
            recipePhotoLocalFilePath = null;
            recipePhotoBitmap = null;
            recipePhotoAdded = false;
            recipePhoto.setImageResource(R.drawable.ic_add_photo_200dp);
        });

        Button cancelPhotoButton = dialog.findViewById(R.id.buttonCancelHandlePhotoDialog);
        cancelPhotoButton.setOnClickListener(v -> dialog.dismiss());

        // If a photo has been added, it should be possible to remove it.
        if (photoAdded) {
            dialog.findViewById(R.id.relativeLayoutRemoveHandlePhotoDialog).setVisibility(View.VISIBLE);
        }
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            if (takePhotoFile != null)
                takePhotoFile.delete();
        }
    }
}