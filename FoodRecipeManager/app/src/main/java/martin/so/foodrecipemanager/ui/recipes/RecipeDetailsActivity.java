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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        Bundle data = getIntent().getExtras();
        int recipePosition = data.getInt("recipePosition");
        currentRecipe = RecipeManager.getInstance().getAllRecipes().get(recipePosition);

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
                        currentRecipe.setName(recipeName.getText().toString());
                        currentRecipe.setDescription(recipeDescription.getText().toString());
                        currentRecipe.setCategory(selectedRecipeCategory);
                        currentRecipe.setType(selectedRecipeType);
                        currentRecipe.setInstructions(recipeInstructions.getText().toString());
                        RecipeManager.getInstance().saveChanges(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_details, menu);
        actionbarMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.edit_recipe_button) {
            editRecipe();
            return true;

        } else if (id == R.id.delete_recipe_button) {
            RecipeManager.getInstance().removeRecipe(currentRecipe);
            Toast.makeText(getApplicationContext(), "Recipe " + recipeName.getText().toString() + " deleted",
                    Toast.LENGTH_LONG).show();
            RecipeManager.getInstance().saveChanges(this);
            finish();
            return true;

        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    private int getSpinnerPosition(String[] spinnerValues, String targetValue) {
        for (int i = 0; i < spinnerValues.length; i++) {
            if (spinnerValues[i].equals(targetValue)) {
                return i;
            }
        }
        return 404;
    }
}
