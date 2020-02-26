package martin.so.foodrecipemanager.ui.recipes;

import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.ImageHandler;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.Utils;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity containing the "Recipe details"-view.
 */
public class RecipeDetailsActivity extends AppCompatActivity {

    private static final String TAG = RecipeDetailsActivity.class.getName();

    private Recipe currentRecipe;
    private Menu actionbarMenu;
    boolean editActive = false;

    private ImageButton recipePhoto;
    private String recipePhotoFilePath;
    private boolean recipePhotoChanged = false;
    private TextInputEditText recipeName;
    private TextInputEditText recipeDescription;
    private TextView recipeCategory;
    private Spinner recipeCategorySpinner;
    private String selectedRecipeCategory;
    private TextView recipeType;
    private Spinner recipeTypeSpinner;
    private String selectedRecipeType;
    private TextInputEditText recipeInstructions;

    final String[] recipeCategories = {Utils.RECIPE_CATEGORY_MEAT, Utils.RECIPE_CATEGORY_VEGETARIAN, Utils.RECIPE_CATEGORY_VEGAN};
    final String[] recipeTypes = {Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_TYPE_DESSERT};

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        currentRecipe = (Recipe) getIntent().getSerializableExtra("recipeObject");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentRecipe.getName());
        }

        recipePhoto = findViewById(R.id.imageButtonRecipePhotoRecipeDetails);
        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameRecipeDetails);
        recipeDescription = findViewById(R.id.textInputLayoutEditDescriptionRecipeDetails);
        recipeCategory = findViewById(R.id.textViewRecipeCategoryRecipeDetails);
        recipeCategorySpinner = findViewById(R.id.spinnerRecipeCategoryRecipeDetails);
        recipeType = findViewById(R.id.textViewRecipeTypeRecipeDetails);
        recipeTypeSpinner = findViewById(R.id.spinnerRecipeTypeRecipeDetails);
        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsRecipeDetails);

        recipePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            // Sets the type as image/*. This ensures only components of type image are selected
            intent.setType("image/*");
            // Pass an extra array with the accepted mime types.
            // This will ensure that only components with these MIME types are targeted.
            String[] mimeTypes = {"image/jpeg", "image/png"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, PICK_IMAGE);
        });

        recipePhoto.setFocusable(false);
        recipePhoto.setEnabled(false);

        recipeName.setFocusable(false);
        recipeName.setEnabled(false);
        recipeDescription.setFocusable(false);
        recipeDescription.setEnabled(false);
        recipeInstructions.setFocusable(false);
        recipeInstructions.setEnabled(false);

        // Load the recipe photo from  internal storage.
        Bitmap bitmapRecipePhoto = new ImageHandler(this).loadImageFile(currentRecipe.getName());
        if (bitmapRecipePhoto != null) recipePhoto.setImageBitmap(bitmapRecipePhoto);

        recipeName.setText(currentRecipe.getName());
        recipeDescription.setText(currentRecipe.getDescription());
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

                        // If recipe name is change, the image file name needs to be changed too.
                        // If there was no previous image set, then a new image file will be created,
                        // otherwise, the image file name will be changed.
                        ImageHandler imageHandler = new ImageHandler(this);
                        if (!imageHandler.editImageFileName(currentRecipe.getName(), newRecipeName)) {
                            imageHandler.createImageFile(newRecipeName, recipePhotoFilePath);
                        }

                        RecipeManager.getInstance().editRecipe(this, currentRecipe, newRecipeName, recipeDescription.getText().toString(), selectedRecipeCategory, selectedRecipeType, recipeInstructions.getText().toString());

                        getSupportActionBar().setTitle(recipeName.getText().toString());
                        Toast.makeText(getApplicationContext(), "Recipe edited",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "No changes made",
                                Toast.LENGTH_LONG).show();
                    }
                    editActive = false;
                    actionbarMenu.findItem(R.id.edit_recipe_button).setIcon(R.drawable.ic_edit_black_24dp);
                    recipePhoto.setFocusable(false);
                    recipePhoto.setEnabled(false);
                    recipeName.setFocusable(false);
                    recipeName.setEnabled(false);
                    recipeDescription.setFocusable(false);
                    recipeDescription.setEnabled(false);
                    recipeCategorySpinner.setVisibility(View.INVISIBLE);
                    recipeCategory.setVisibility(View.VISIBLE);
                    recipeCategory.setText(selectedRecipeCategory);
                    recipeTypeSpinner.setVisibility(View.INVISIBLE);
                    recipeType.setVisibility(View.VISIBLE);
                    recipeType.setText(selectedRecipeType);
                    recipeInstructions.setFocusable(false);
                    recipeInstructions.setEnabled(false);
                }
            }
        } else {
            editActive = true;
            actionbarMenu.findItem(R.id.edit_recipe_button).setIcon(R.drawable.ic_save_black_24dp);
            recipePhoto.setFocusable(true);
            recipePhoto.setEnabled(true);
            recipeName.setFocusable(true);
            recipeName.setEnabled(true);
            recipeName.setFocusableInTouchMode(true);
            recipeDescription.setFocusable(true);
            recipeDescription.setEnabled(true);
            recipeDescription.setFocusableInTouchMode(true);
            recipeCategorySpinner.setVisibility(View.VISIBLE);
            recipeCategory.setVisibility(View.INVISIBLE);
            recipeTypeSpinner.setVisibility(View.VISIBLE);
            recipeType.setVisibility(View.INVISIBLE);
            recipeInstructions.setFocusable(true);
            recipeInstructions.setEnabled(true);
            recipeInstructions.setFocusableInTouchMode(true);
        }
    }

    /**
     * Creates the menu items: "Edit recipe"-button, "Delete recipe"-button, "Save changes"-button
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_details, menu);
        actionbarMenu = menu;
        return true;
    }

    /**
     * Handling the click of the menu items:
     * "Edit recipe"-button, "Delete recipe"-button, "Save changes"-button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.edit_recipe_button) {
            editRecipe();
            return true;

        } else if (id == R.id.delete_recipe_button) {
            Toast.makeText(getApplicationContext(), "Recipe " + recipeName.getText().toString() + " deleted",
                    Toast.LENGTH_LONG).show();
            RecipeManager.getInstance().removeRecipe(this, currentRecipe);
            finish();
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
        if (!(recipeDescription.getText().toString()).equals(currentRecipe.getDescription())) {
            haveChanged = true;
        }
        if (!selectedRecipeCategory.equals(currentRecipe.getCategory())) {
            haveChanged = true;
        }
        if (!selectedRecipeType.equals(currentRecipe.getType())) {
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
        if (recipeDescription.getText().toString().isEmpty()) {
            recipeDescription.setError("Please enter Description");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            recipePhoto.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            recipePhotoChanged = true;
            recipePhotoFilePath = picturePath;
        }
    }
}
