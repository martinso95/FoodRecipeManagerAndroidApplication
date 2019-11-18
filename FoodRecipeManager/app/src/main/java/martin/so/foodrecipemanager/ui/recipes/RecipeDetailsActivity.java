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

public class RecipeDetailsActivity extends AppCompatActivity {

    private TextInputEditText recipeName;
    private TextInputEditText description;

    private Recipe currentRecipe;
    private Menu actionbarMenu;
    boolean editActive = false;

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

        recipeName = findViewById(R.id.textInputLayoutEditRecipeNameRecipeDetails);
        description = findViewById(R.id.textInputLayoutEditDescriptionRecipeDetails);
        recipeName.setFocusable(false);
        recipeName.setEnabled(false);
        description.setFocusable(false);
        description.setEnabled(false);
        recipeName.setText(currentRecipe.getName());
        description.setText(currentRecipe.getDescription());

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
                        currentRecipe.setDescription(description.getText().toString());
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
                    description.setFocusable(false);
                    description.setEnabled(false);
                }
            }
        } else {
            editActive = true;
            actionbarMenu.findItem(R.id.edit_recipe_button).setIcon(R.drawable.ic_save_black_24dp);
            recipeName.setFocusable(true);
            recipeName.setEnabled(true);
            recipeName.setFocusableInTouchMode(true);
            description.setFocusable(true);
            description.setEnabled(true);
            description.setFocusableInTouchMode(true);
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
        if (!(description.getText().toString()).equals(currentRecipe.getDescription())) {
            haveChanged = true;
        }
        return haveChanged;
    }

    private boolean checkFieldsAreNotEmpty() {
        boolean noEmpty = true;
        if (recipeName.getText().toString().isEmpty()) {
            recipeName.setError("Please enter a Recipe name");
            noEmpty = false;
        }
        if (description.getText().toString().isEmpty()) {
            description.setError("Please enter Description");
            noEmpty = false;
        }
        return noEmpty;
    }
}
