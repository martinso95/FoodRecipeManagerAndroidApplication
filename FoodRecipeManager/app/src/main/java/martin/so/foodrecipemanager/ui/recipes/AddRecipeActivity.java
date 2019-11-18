package martin.so.foodrecipemanager.ui.recipes;

import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class AddRecipeActivity extends AppCompatActivity {

    private TextInputEditText recipeName;
    private TextInputEditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Recipe");
        }

        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameAddRecipe);
        description = findViewById(R.id.textInputLayoutEditDescriptionAddRecipe);

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
                    Recipe recipe = new Recipe(recipeName.getText().toString(), description.getText().toString());
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
            recipeName.setError("Please enter an Recipe name");
            noEmpty = false;
        }
        if (description.getText().toString().isEmpty()) {
            description.setError("Please enter Description");
            noEmpty = false;
        }
        return noEmpty;
    }
}
