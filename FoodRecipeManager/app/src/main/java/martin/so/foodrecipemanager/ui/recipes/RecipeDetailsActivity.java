package martin.so.foodrecipemanager.ui.recipes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.FirebaseStorageOfflineHandler;
import martin.so.foodrecipemanager.model.InformationDialog;
import martin.so.foodrecipemanager.model.Ingredient;
import martin.so.foodrecipemanager.model.IngredientManager;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
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
 * Activity containing the "Recipe details"-view.
 */
public class RecipeDetailsActivity extends AppCompatActivity {

    private Recipe currentRecipe;
    boolean editActive = false;

    private ImageButton recipePhoto;
    private File takePhotoFile = null;
    private boolean recipePhotoAdded = false;
    private Uri recipePhotoLocalFilePath;
    private Uri temporaryRecipePhotoLocalFilePath = null;
    private boolean shouldRemovePhoto = false;
    private boolean recipePhotoChanged = false;
    private String recipePhotoPath = null;
    private Bitmap recipePhotoBitmap = null;
    private TextInputEditText recipeName;
    private TextView recipeCategory;
    private Spinner recipeCategorySpinner;
    private String selectedRecipeCategory;
    private TextView recipeType;
    private Spinner recipeTypeSpinner;
    private String selectedRecipeType;
    private boolean recipeIngredientsChanged = false;
    private ListView recipeIngredientsList;
    private RelativeLayout recipeIngredientEditModeLayout;
    private TextInputEditText recipeIngredientAmountInput;
    private TextInputEditText recipeIngredientNameInput;
    private ImageButton recipeAddIngredient;
    private ListView recipeIngredientsListEditMode;
    private List<Ingredient> recipeIngredients;
    IngredientsAdapter ingredientsAdapter;
    IngredientsAdapter ingredientsAdapterEditMode;
    private TextInputEditText recipeInstructions;
    private RequestOptions glideRequestOptions;

    final String[] recipeCategories = {Utils.RECIPE_CATEGORY_MEAT, Utils.RECIPE_CATEGORY_VEGETARIAN, Utils.RECIPE_CATEGORY_VEGAN};
    final String[] recipeTypes = {Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_TYPE_DESSERT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        int currentRecipeAdapterPosition = getIntent().getIntExtra("recipeAdapterPosition", 0);
        List<Recipe> currentRecipeTypeList = null;
        String currentRecipeType = getIntent().getStringExtra("recipeType");

        switch (currentRecipeType) {
            case Utils.RECIPE_TYPE_ALL:
                currentRecipeTypeList = RecipeManager.getInstance().getAllRecipes();
                break;
            case Utils.RECIPE_TYPE_BREAKFAST:
                currentRecipeTypeList = RecipeManager.getInstance().getBreakfastRecipes();
                break;
            case Utils.RECIPE_TYPE_LIGHT_MEAL:
                currentRecipeTypeList = RecipeManager.getInstance().getLightMealRecipes();
                break;
            case Utils.RECIPE_TYPE_HEAVY_MEAL:
                currentRecipeTypeList = RecipeManager.getInstance().getHeavyMealRecipes();
                break;
            case Utils.RECIPE_TYPE_DESSERT:
                currentRecipeTypeList = RecipeManager.getInstance().getDessertRecipes();
                break;
        }

        currentRecipe = currentRecipeTypeList.get(currentRecipeAdapterPosition);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentRecipe.getName());
        }

        recipePhotoPath = currentRecipe.getPhotoPath();

        recipePhoto = findViewById(R.id.imageButtonRecipePhotoRecipeDetails);
        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameRecipeDetails);
        recipeCategory = findViewById(R.id.textViewRecipeCategoryRecipeDetails);
        recipeCategorySpinner = findViewById(R.id.spinnerRecipeCategoryRecipeDetails);
        recipeType = findViewById(R.id.textViewRecipeTypeRecipeDetails);
        recipeTypeSpinner = findViewById(R.id.spinnerRecipeTypeRecipeDetails);
        recipeIngredientsList = findViewById(R.id.listViewIngredientsRecipeDetails);
        recipeIngredientEditModeLayout = findViewById(R.id.relativeLayoutIngredientsEditModeRecipeDetails);
        recipeIngredientAmountInput = findViewById(R.id.textInputLayoutEditAddIngredientAmountEditModeRecipeDetails);
        recipeIngredientNameInput = findViewById(R.id.textInputLayoutEditAddIngredientNameEditModeRecipeDetails);
        recipeAddIngredient = findViewById(R.id.imageButtonAddIngredientButtonEditModeRecipeDetails);
        recipeIngredientsListEditMode = findViewById(R.id.listViewIngredientsEditModeRecipeDetails);
        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsRecipeDetails);

        recipePhotoAdded = currentRecipe.getPhotoPath() != null;
        recipePhoto.setOnClickListener(v -> {
            showDialog(recipePhotoAdded);
        });

        recipePhoto.setFocusable(false);
        recipePhoto.setEnabled(false);
        recipeName.setFocusable(false);
        recipeName.setEnabled(false);
        recipeInstructions.setFocusable(false);
        recipeInstructions.setEnabled(false);

        glideRequestOptions = new RequestOptions();
        glideRequestOptions.centerCrop();

        // Load the recipe photo from Firebase Storage.
        // If not available, then load placeholder in the form of the temporary local photo (if it exists),
        // or a plain static drawable image.
        if (currentRecipe.getPhotoPath() != null) {
            if (currentRecipe.getPhotoDownloadUri() != null) {
                // Load photo with Glide, which supports caching.
                Glide.with(getApplicationContext()).load(currentRecipe.getPhotoDownloadUri()).placeholder(R.drawable.ic_food_placeholder_200dp).apply(glideRequestOptions).into(recipePhoto);
            } else if (currentRecipe.getTemporaryLocalPhoto() != null) {
                // If photo has changed, load the local photo copy, because uploading the new download uri can take time.
                recipePhoto.setImageBitmap(currentRecipe.getTemporaryLocalPhoto());
            } else {
                // Only comes to this code if the app is online, and no download uri exists (which might happen when editing recipe photos offline).
                // If it is offline, it will get the glide cache. If photo was changed, it will get the local copy.
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(currentRecipe.getPhotoPath());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext()).load(uri.toString()).apply(glideRequestOptions).into(recipePhoto);
                        currentRecipe.setPhotoDownloadUri(uri.toString());
                        RecipeManager.getInstance().saveChanges();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        recipePhoto.setImageResource(R.drawable.ic_food_placeholder_200dp);
                    }
                });
            }
        } else {
            recipePhoto.setImageResource(R.drawable.ic_add_photo_200dp);
        }

        recipeName.setText(currentRecipe.getName());
        recipeCategory.setText(currentRecipe.getCategory());
        recipeType.setText(currentRecipe.getType());

        ArrayAdapter<String> recipeCategoryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, recipeCategories);
        recipeCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipeCategorySpinner.setAdapter(recipeCategoryAdapter);

        recipeCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        recipeTypeSpinner.setAdapter(recipeTypeAdapter);

        recipeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                selectedRecipeType = recipeTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        recipeCategorySpinner.setSelection(getSpinnerPosition(recipeCategories, currentRecipe.getCategory()));
        recipeTypeSpinner.setSelection(getSpinnerPosition(recipeTypes, currentRecipe.getType()));
        selectedRecipeCategory = currentRecipe.getCategory();
        selectedRecipeType = currentRecipe.getType();

        recipeIngredients = new ArrayList<>(currentRecipe.getIngredients());
        ingredientsAdapter = new IngredientsAdapter(this, recipeIngredients, false);
        ingredientsAdapterEditMode = new IngredientsAdapter(this, recipeIngredients, true);
        recipeIngredientsList.setAdapter(ingredientsAdapter);
        recipeIngredientsListEditMode.setAdapter(ingredientsAdapterEditMode);
        Utils.setListViewHeightBasedOnChildren(recipeIngredientsListEditMode);
        Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
        ingredientsAdapterEditMode.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Utils.setListViewHeightBasedOnChildren(recipeIngredientsListEditMode);
                Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
                recipeIngredientsChanged = true;
            }
        });

        recipeName.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                recipeName.clearFocus();
                handled = true;
            }
            return handled;
        });

        recipeIngredientAmountInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        recipeIngredientNameInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        recipeAddIngredient.setOnClickListener(v -> {
            addIngredient();
        });

        recipeInstructions.setText(currentRecipe.getInstructions());
    }

    /**
     * Handling the click of editing the recipe.
     * This method toggles between two modes, display mode and edit mode.
     * When edit mode is activated, all the fields will be editable.
     * <p>
     * If the new recipe name already exists, the click will not go through.
     * If any fields are empty, the click will not go through,
     * and hints will be given based on what needs to be done.
     */
    private void editRecipe() {
        if (editActive) {
            if (checkFieldsAreNotEmpty()) {
                boolean duplicateFound = false;
                for (Recipe b : RecipeManager.getInstance().getAllRecipes()) {
                    if (!(b.getName().equals(currentRecipe.getName())) && b.getName().equals(recipeName.getText().toString())) {
                        recipeName.setError("Recipe name already exists");
                        duplicateFound = true;
                    }
                }
                if (!duplicateFound) {
                    if (haveFieldsChanged()) {
                        String newRecipeName = recipeName.getText().toString();
                        if (recipePhotoChanged && recipePhotoAdded) {
                            recipePhotoChanged = false;

                            // Upload the new photo. If the recipe had a photo before changing to a new photo, the old photo will be replaced.
                            if (recipePhotoPath == null) {
                                recipePhotoPath = UUID.randomUUID().toString();
                            }
                            // Update the local image of the edited recipe.
                            currentRecipe.setTemporaryLocalPhoto(recipePhotoBitmap);

                            // Set photo download uri to null, in order to prevent loading a photo from an old uri.
                            currentRecipe.setPhotoDownloadUri(null);

                            RecipeManager.getInstance().editRecipe(currentRecipe, recipePhotoPath, newRecipeName, selectedRecipeCategory, selectedRecipeType, recipeIngredients, recipeInstructions.getText().toString());
                            getSupportActionBar().setTitle(recipeName.getText().toString());

                            FirebaseStorageOfflineHandler.getInstance().addFileForUploadInFirebaseStorage(recipePhotoPath, recipePhotoBitmap);

                            StorageReference newPhotoRef = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipePhotoPath);
                            newPhotoRef.putFile(recipePhotoLocalFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (takePhotoFile != null)
                                        takePhotoFile.delete();
                                    FirebaseStorageOfflineHandler.getInstance().removeFileForUploadInFirebaseStorage(recipePhotoPath);

                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(currentRecipe.getPhotoPath());
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            currentRecipe.setPhotoDownloadUri(uri.toString());
                                            RecipeManager.getInstance().saveChanges();
                                        }

                                    });
                                }
                            });
                        } else {
                            RecipeManager.getInstance().editRecipe(currentRecipe, recipePhotoPath, newRecipeName, selectedRecipeCategory, selectedRecipeType, recipeIngredients, recipeInstructions.getText().toString());
                            getSupportActionBar().setTitle(recipeName.getText().toString());
                        }

                        if (shouldRemovePhoto) {
                            if (recipePhotoPath != null) {
                                RecipeManager.getInstance().removeRecipePhoto(recipePhotoPath);
                            }
                            currentRecipe.setPhotoDownloadUri(null);
                            currentRecipe.setTemporaryLocalPhoto(null);
                            currentRecipe.setPhotoPath(null);
                            RecipeManager.getInstance().saveChanges();
                            shouldRemovePhoto = false;
                        }
                    }
                    editActive = false;
                    recipePhoto.setFocusable(false);
                    recipePhoto.setEnabled(false);
                    recipeName.setFocusable(false);
                    recipeName.setEnabled(false);
                    recipeName.setBackgroundResource(R.color.backgroundDark);
                    recipeCategorySpinner.setVisibility(View.INVISIBLE);
                    recipeCategory.setVisibility(View.VISIBLE);
                    recipeCategory.setText(selectedRecipeCategory);
                    recipeTypeSpinner.setVisibility(View.INVISIBLE);
                    recipeType.setVisibility(View.VISIBLE);
                    recipeType.setText(selectedRecipeType);
                    recipeIngredientEditModeLayout.setVisibility(View.GONE);
                    recipeIngredientsList.setVisibility(View.VISIBLE);
                    recipeInstructions.setFocusable(false);
                    recipeInstructions.setEnabled(false);
                    recipeInstructions.setBackgroundResource(R.color.backgroundDark);
                }
            }
        } else {
            editActive = true;
            recipePhoto.setFocusable(true);
            recipePhoto.setEnabled(true);
            recipeName.setFocusable(true);
            recipeName.setEnabled(true);
            recipeName.setFocusableInTouchMode(true);
            recipeName.setBackgroundResource(R.color.backgroundLightDark);
            recipeCategorySpinner.setVisibility(View.VISIBLE);
            recipeCategory.setVisibility(View.INVISIBLE);
            recipeTypeSpinner.setVisibility(View.VISIBLE);
            recipeType.setVisibility(View.INVISIBLE);
            recipeIngredientEditModeLayout.setVisibility(View.VISIBLE);
            recipeIngredientsList.setVisibility(View.GONE);
            recipeInstructions.setFocusable(true);
            recipeInstructions.setEnabled(true);
            recipeInstructions.setFocusableInTouchMode(true);
            recipeInstructions.setBackgroundResource(R.color.backgroundLightDark);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if (editActive) {
            inflater.inflate(R.menu.menu_recipe_details_edit_mode, menu);
        } else {
            inflater.inflate(R.menu.menu_recipe_details, menu);
        }
        return true;
    }

    /**
     * Handling the click of the menu items:
     * "Edit recipe"-button, "Delete recipe"-button, "Save changes"-button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.recipe_details_menu_edit_recipe_option) {
            invalidateOptionsMenu();
            editRecipe();
            return true;

        } else if (id == R.id.recipe_details_menu_delete_recipe_option) {
            RecipeManager.getInstance().removeRecipe(currentRecipe);
            finish();
            return true;

        } else if (id == R.id.recipe_details_menu_edit_mode) {
            invalidateOptionsMenu();
            editRecipe();
            return true;

        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if the recipe property input fields have changed.
     */
    private boolean haveFieldsChanged() {
        boolean haveChanged = false;
        if (recipePhotoChanged) {
            haveChanged = true;
        }
        if (!(recipeName.getText().toString()).equals(currentRecipe.getName())) {
            haveChanged = true;
        }
        if (!selectedRecipeCategory.equals(currentRecipe.getCategory())) {
            haveChanged = true;
        }
        if (!selectedRecipeType.equals(currentRecipe.getType())) {
            haveChanged = true;
        }
        if (recipeIngredientsChanged) {
            haveChanged = true;
        }
        if (!(recipeInstructions.getText().toString()).equals(currentRecipe.getInstructions())) {
            haveChanged = true;
        }
        return haveChanged;
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
     * Returns the position of the specified target in the spinner.
     *
     * @param spinnerValues The spinner array to be searched in.
     * @param targetValue   The target to be searched for in the spinner array.
     */
    private int getSpinnerPosition(String[] spinnerValues, String targetValue) {
        for (int i = 0; i < spinnerValues.length; i++) {
            if (spinnerValues[i].equals(targetValue)) {
                return i;
            }
        }
        return 404;
    }

    /**
     * Add the ingredient to the recipe, based on what the user has input in the ingredient textInput.
     */
    private void addIngredient() {
        String amountInput = recipeIngredientAmountInput.getText().toString();
        String nameInput = recipeIngredientNameInput.getText().toString();
        if (!nameInput.isEmpty()) {
            IngredientManager.addIngredient(recipeIngredients, new Ingredient(amountInput, nameInput));
            ingredientsAdapterEditMode.notifyDataSetChanged();
            ingredientsAdapter.notifyDataSetChanged();
            recipeIngredientsChanged = true;
            Utils.hideKeyboard(this);

            if (recipeIngredientAmountInput.isFocused()) {
                recipeIngredientAmountInput.clearFocus();
            }

            if (recipeIngredientNameInput.isFocused()) {
                recipeIngredientNameInput.clearFocus();
            }

            recipeIngredientAmountInput.getText().clear();
            recipeIngredientNameInput.getText().clear();
        }
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
                    recipePhotoChanged = true;
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), temporaryRecipePhotoLocalFilePath);
                        Glide.with(getApplicationContext()).load(bitmap).apply(glideRequestOptions).into(recipePhoto);
                        recipePhotoBitmap = bitmap;
                        recipePhotoAdded = true;
                        recipePhotoLocalFilePath = temporaryRecipePhotoLocalFilePath;
                        temporaryRecipePhotoLocalFilePath = null;
                        shouldRemovePhoto = false;
                    } catch (IOException e) {
                        recipePhotoChanged = false;
                        recipePhotoAdded = false;
                        recipePhotoBitmap = null;
                        temporaryRecipePhotoLocalFilePath = null;
                        if (takePhotoFile != null)
                            takePhotoFile.delete();
                        InformationDialog informationDialog = new InformationDialog();
                        informationDialog.showDialog(RecipeDetailsActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
                        e.printStackTrace();
                    }
                } else {
                    if (takePhotoFile != null)
                        takePhotoFile.delete();
                    recipePhotoChanged = false;
                    recipePhotoAdded = false;
                    recipePhotoBitmap = null;
                    temporaryRecipePhotoLocalFilePath = null;
                    InformationDialog informationDialog = new InformationDialog();
                    informationDialog.showDialog(RecipeDetailsActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
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
                            recipePhotoChanged = true;
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), recipePhotoLocalFilePath);
                            Glide.with(getApplicationContext()).load(bitmap).apply(glideRequestOptions).into(recipePhoto);
                            recipePhotoBitmap = bitmap;
                            recipePhotoAdded = true;
                            shouldRemovePhoto = false;
                        } catch (IOException e) {
                            recipePhotoChanged = false;
                            recipePhotoAdded = false;
                            recipePhotoBitmap = null;
                            InformationDialog informationDialog = new InformationDialog();
                            informationDialog.showDialog(RecipeDetailsActivity.this, null, false, getString(R.string.recipe_photo_add_fail_dialog));
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
            recipePhotoChanged = true;
            recipePhotoLocalFilePath = null;
            recipePhotoBitmap = null;
            recipePhotoAdded = false;
            shouldRemovePhoto = true;
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
