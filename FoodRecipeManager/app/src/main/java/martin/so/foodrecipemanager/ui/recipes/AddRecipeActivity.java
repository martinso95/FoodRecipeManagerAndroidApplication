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

public class AddRecipeActivity extends AppCompatActivity {

    private TextView temporaryPhotoPlaceholder;
    private TextInputEditText recipeName;
    private TextInputEditText recipeDescription;
    private Spinner recipeCategory;
    private String selectedRecipeCategory = Utils.RECIPE_CATEGORY;
    private Spinner recipeType;
    private String selectedRecipeType = Utils.RECIPE_TYPE;
    private TextInputEditText recipeInstructions;

    final String[] recipeCategories = {Utils.RECIPE_CATEGORY, Utils.RECIPE_CATEGORY_MEAT, Utils.RECIPE_CATEGORY_VEGETARIAN, Utils.RECIPE_CATEGORY_VEGAN};
    final String[] recipeTypes = {Utils.RECIPE_TYPE, Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_TYPE_DESSERT};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Recipe");
        }

        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameAddRecipe);
        recipeDescription = findViewById(R.id.textInputLayoutEditRecipeDescriptionAddRecipe);
        recipeCategory = findViewById(R.id.spinnerRecipeCategoryAddRecipe);
        recipeType = findViewById(R.id.spinnerRecipeTypeAddRecipe);
        recipeInstructions = findViewById(R.id.textInputLayoutEditRecipeInstructionsAddRecipe);

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_added_recipe, menu);
        return true;
    }

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
                    Recipe recipe = new Recipe(recipeName.getText().toString(), recipeDescription.getText().toString(), recipeInstructions.getText().toString(), selectedRecipeType, selectedRecipeCategory);
                    RecipeManager.getInstance().addRecipe(recipe);
                    RecipeManager.getInstance().saveChanges(getApplicationContext());

                    Toast.makeText(getApplicationContext(), "Recipe added: " + recipeName.getText().toString(),
                            Toast.LENGTH_LONG).show();
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
        return noEmpty;
    }
}
