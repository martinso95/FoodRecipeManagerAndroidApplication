package martin.so.foodrecipemanager.ui.recipes;

import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.Utils;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Activity containing the "Recipe details"-view.
 */
public class RecipeDetailsActivity extends AppCompatActivity {

    private Recipe currentRecipe;
    private Menu actionbarMenu;
    boolean editActive = false;

    private TextView temporaryPhotoPlaceholder;
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

    /**
     * Returns the recipe list based on the recipe's type.
     *
     * @param recipe The recipe type as a String.
     */
    private List<Recipe> getRecipeTypeList(String recipe) {
        switch (recipe) {
            case Utils.RECIPE_TYPE_ALL:
                return RecipeManager.getInstance().getAllRecipes();
            case Utils.RECIPE_TYPE_BREAKFAST:
                return RecipeManager.getInstance().getBreakfastRecipes();
            case Utils.RECIPE_TYPE_LIGHT_MEAL:
                return RecipeManager.getInstance().getLightMealRecipes();
            case Utils.RECIPE_TYPE_HEAVY_MEAL:
                return RecipeManager.getInstance().getHeavyMealRecipes();
            case Utils.RECIPE_TYPE_DESSERT:
                return RecipeManager.getInstance().getDessertRecipes();
            default:
                return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        Bundle data = getIntent().getExtras();
        String temporaryRecipeName = data.getString("recipeName");
        String temporaryRecipeType = data.getString("recipeType");


        for (Recipe recipe : getRecipeTypeList(temporaryRecipeType)) {
            if (recipe.getName().equals(temporaryRecipeName)) {
                currentRecipe = recipe;
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentRecipe.getName());
        }

//        temporaryPhotoPlaceholder...
        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameRecipeDetails);
        recipeDescription = findViewById(R.id.textInputLayoutEditDescriptionRecipeDetails);
        recipeCategory = findViewById(R.id.textViewRecipeCategoryRecipeDetails);
        recipeCategorySpinner = findViewById(R.id.spinnerRecipeCategoryRecipeDetails);
        recipeType = findViewById(R.id.textViewRecipeTypeRecipeDetails);
        recipeTypeSpinner = findViewById(R.id.spinnerRecipeTypeRecipeDetails);
        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsRecipeDetails);

        recipeName.setFocusable(false);
        recipeName.setEnabled(false);
        recipeDescription.setFocusable(false);
        recipeDescription.setEnabled(false);
        recipeInstructions.setFocusable(false);
        recipeInstructions.setEnabled(false);

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
                        RecipeManager.getInstance().editRecipe(this, currentRecipe, recipeName.getText().toString(), recipeDescription.getText().toString(), selectedRecipeCategory, selectedRecipeType, recipeInstructions.getText().toString());
                        getSupportActionBar().setTitle(currentRecipe.getName());
                        Toast.makeText(getApplicationContext(), "Recipe edited",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "No changes made",
                                Toast.LENGTH_LONG).show();
                    }
                    editActive = false;
                    actionbarMenu.findItem(R.id.edit_recipe_button).setIcon(R.drawable.ic_edit_black_24dp);
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
        if (!(recipeInstructions.getText().toString()).equals(currentRecipe.getDescription())) {
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
}
