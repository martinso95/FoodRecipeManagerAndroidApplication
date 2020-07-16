package martin.so.foodrecipemanager.ui.recipes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
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

    // ======================================== Recipe photo ========================================
    private ImageButton recipePhoto;
    private File takePhotoFile = null;
    private boolean recipePhotoAdded = false;
    private Uri recipePhotoLocalFilePath = null;
    private Uri temporaryRecipePhotoLocalFilePath = null;
    private Bitmap recipePhotoBitmap = null;

    // ======================================== Recipe name ========================================
    private TextInputEditText recipeName;

    // ======================================== Recipe category ========================================
    private Spinner recipeCategory;
    private String selectedRecipeCategory = Utils.RECIPE_CATEGORY;
    // TODO: Change the initial value back to normal (CATEGORY), once testing is done.
    final String[] recipeCategories = {Utils.RECIPE_CATEGORY_MEAT, Utils.RECIPE_CATEGORY, Utils.RECIPE_CATEGORY_VEGETARIAN, Utils.RECIPE_CATEGORY_VEGAN};

    // ======================================== Recipe type ========================================
    private Spinner recipeType;
    private String selectedRecipeType = Utils.RECIPE_TYPE;
    // TODO: Change the initial value back to normal (TYPE), once testing is done.
    final String[] recipeTypes = {Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_TYPE, Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_TYPE_DESSERT};

    // ======================================== Recipe time ========================================
    private TextView recipeTime;
    private int recipeTimeHours = 0;
    private int recipeTimeMinutes = 0;

    // ======================================== Recipe ingredients ========================================
    private TextInputEditText recipeIngredientAmountInput;
    private Spinner recipeIngredientUnitInput;
    private String selectedRecipeIngredientUnitInput = Utils.RECIPE_INGREDIENT_UNITS[2]; // Default is grams.
    private TextInputEditText recipeIngredientNameInput;
    private ImageButton recipeAddIngredient;
    private ListView recipeIngredientsList;
    private List<Ingredient> recipeIngredients;
    private IngredientsAdapter ingredientsAdapter;

    // ======================================== Recipe portions ========================================
    private int recipePortions = 2;
    private TextView recipePortionsTextView;
    private ImageButton decrementRecipePortions;
    private ImageButton addRecipePortions;

    // ======================================== Recipe instructions ========================================
    private TextInputEditText recipeInstructions;

    // ======================================== Glide options ========================================
    private RequestOptions glideRequestOptions;

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

        recipeTime = findViewById(R.id.textViewRecipeTimeAddRecipe);

        recipeIngredientAmountInput = findViewById(R.id.textInputLayoutEditAddIngredientAmountAddRecipe);
        recipeIngredientUnitInput = findViewById(R.id.spinnerAddIngredientUnitAddRecipe);
        recipeIngredientNameInput = findViewById(R.id.textInputLayoutEditAddIngredientNameAddRecipe);
        recipeAddIngredient = findViewById(R.id.imageButtonAddIngredientButtonAddRecipe);
        recipeIngredientsList = findViewById(R.id.listViewIngredientsAddRecipe);

        recipePortionsTextView = findViewById(R.id.textViewPortionsAddRecipe);
        decrementRecipePortions = findViewById(R.id.imageButtonDecrementPortionsButtonAddRecipe);
        addRecipePortions = findViewById(R.id.imageButtonAddPortionsButtonAddRecipe);

        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsAddRecipe);

        // ======================================== Recipe photo ========================================
        recipePhoto.setOnClickListener(v -> {
            showPhotoPickerDialog(recipePhotoAdded);
        });

        // ======================================== Recipe category ========================================
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

        // ======================================== Recipe type ========================================
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

        // ======================================== Recipe time ========================================
        recipeTime.setText(Utils.NA);
        recipeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        // ======================================== Recipe name ========================================
        recipeName.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        // ======================================== Recipe ingredients ========================================
        recipeIngredients = new ArrayList<>();
        ingredientsAdapter = new IngredientsAdapter(this, recipeIngredients, true, 0);
        recipeIngredientsList.setAdapter(ingredientsAdapter);
        ingredientsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
            }
        });

        recipeIngredientAmountInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        ArrayAdapter<String> recipeIngredientUnitInputAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Utils.RECIPE_INGREDIENT_UNITS);
        recipeIngredientUnitInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipeIngredientUnitInput.setAdapter(recipeIngredientUnitInputAdapter);

        recipeIngredientUnitInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                selectedRecipeIngredientUnitInput = Utils.RECIPE_INGREDIENT_UNITS[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        recipeIngredientUnitInput.setSelection(Utils.getSpinnerPosition(Utils.RECIPE_INGREDIENT_UNITS, selectedRecipeIngredientUnitInput));

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

        // TODO: Remove once done with testing.
        // placeholder for quicker testing...
        IngredientManager.addIngredient(recipeIngredients, new Ingredient(2, Utils.RECIPE_INGREDIENT_UNITS[8], "Milk"));
        ingredientsAdapter.notifyDataSetChanged();
        Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);

        // ======================================== Recipe portions ========================================
        decrementRecipePortions.setOnClickListener(v -> {
            if (recipePortions > 0) {
                recipePortions--;
                recipePortionsTextView.setText(getString(R.string.recipe_portions, recipePortions));
            }
        });

        addRecipePortions.setOnClickListener(v -> {
            if (recipePortions < 100) {
                recipePortions++;
                recipePortionsTextView.setText(getString(R.string.recipe_portions, recipePortions));
            }
        });

        recipePortionsTextView.setText(getString(R.string.recipe_portions, recipePortions));

        // ======================================== Glide options ========================================
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

                        Recipe recipe = new Recipe(recipePhotoPath, recipeName.getText().toString(), selectedRecipeCategory, selectedRecipeType, recipeTimeHours, recipeTimeMinutes, recipeIngredients, recipePortions, recipeInstructions.getText().toString());
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
                        Recipe recipe = new Recipe(null, recipeName.getText().toString(), selectedRecipeCategory, selectedRecipeType, recipeTimeHours, recipeTimeMinutes, recipeIngredients, recipePortions, recipeInstructions.getText().toString());
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
     * <p>
     * Time can be unset.
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
        String amountInputValue = recipeIngredientAmountInput.getText().toString();
        double amountInput = amountInputValue.isEmpty() ? 0 : Double.parseDouble(amountInputValue);
        String nameInput = recipeIngredientNameInput.getText().toString();

        if (!nameInput.isEmpty()) {
            IngredientManager.addIngredient(recipeIngredients, new Ingredient(amountInput, selectedRecipeIngredientUnitInput, nameInput));
            ingredientsAdapter.notifyDataSetChanged();
            Utils.setListViewHeightBasedOnChildren(recipeIngredientsList);
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
     * Dialog for picking a time for the recipe.
     */
    private void showTimePickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_time_picker);
        dialog.show();

        NumberPicker recipeNumberPickerHour = dialog.findViewById(R.id.numberPickerHoursTimePickerDialog);
        recipeNumberPickerHour.setMaxValue(200);
        recipeNumberPickerHour.setMinValue(0);
        recipeNumberPickerHour.setWrapSelectorWheel(false);
        recipeNumberPickerHour.setValue(recipeTimeHours);
        NumberPicker recipeNumberPickerMinutes = dialog.findViewById(R.id.numberPickerMinutesTimePickerDialog);
        recipeNumberPickerMinutes.setMaxValue(59);
        recipeNumberPickerMinutes.setMinValue(0);
        recipeNumberPickerMinutes.setWrapSelectorWheel(false);
        recipeNumberPickerMinutes.setValue(recipeTimeMinutes);

        Button okButton = dialog.findViewById(R.id.buttonOkTimePickerDialog);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            recipeTimeHours = recipeNumberPickerHour.getValue();
            recipeTimeMinutes = recipeNumberPickerMinutes.getValue();
            recipeTime.setText(new StringBuilder().append(recipeNumberPickerHour.getValue()).append("h : ").append(recipeNumberPickerMinutes.getValue()).append("m"));
        });

        Button cancelButton = dialog.findViewById(R.id.buttonCancelTimePickerDialog);
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        Button removeTimeButton = dialog.findViewById(R.id.buttonRemoveTimePickerDialog);
        removeTimeButton.setOnClickListener(v -> {
            dialog.dismiss();
            recipeTimeHours = -1;
            recipeTimeMinutes = -1;
            recipeTime.setText(Utils.NA);
        });
    }

    /**
     * Dialog for adding a photo either by taking a photo or picking one from the gallery.
     *
     * @param photoAdded flag for knowing whether to provide option to remove an added photo or not.
     */
    private void showPhotoPickerDialog(boolean photoAdded) {
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