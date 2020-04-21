package martin.so.foodrecipemanager.ui.recipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.RecipesAdapter;
import martin.so.foodrecipemanager.model.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class RecipeSearchActivity extends AppCompatActivity implements RecipesAdapter.ItemClickListener, SearchView.OnQueryTextListener {

    private RecipesAdapter recipesAdapter;
    private List<Recipe> allRecipesList;
    private String textToFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewRecipeSearch);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allRecipesList = new ArrayList<>(RecipeManager.getInstance().getAllRecipes());
        recipesAdapter = new RecipesAdapter(this, allRecipesList);
        recipesAdapter.setClickListener(this);
        recyclerView.setAdapter(recipesAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent recipeDetailsActivity = new Intent(this, RecipeDetailsActivity.class);
        recipeDetailsActivity.putExtra("recipeAdapterPosition", position);
        recipeDetailsActivity.putExtra("recipeType", Utils.RECIPE_TYPE_ALL);
        startActivity(recipeDetailsActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.recipe_search_bar);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconified(false);
        searchView.setQueryHint("Search recipes...");
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Method for checking when text is being changed in the search view.
     * Calls the recipe adapter's filter method on change.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        textToFilter = newText;
        recipesAdapter.filter(newText);
        return false;
    }

    /**
     * Method for checking when text is being submitted in the search view.
     * Calls the recipe adapter's filter method on submit.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        textToFilter = query;
        recipesAdapter.filter(query);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Every time the Activity is getting focus (resumed),
     * the recipe list will be updated, in case recipes have been edited,
     * or anything has been deleted, from another view.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d("Test", "Recipe Search Activity RESUMED");
        recipesAdapter.setList(new ArrayList<>(RecipeManager.getInstance().getAllRecipes()));
        recipesAdapter.filter(textToFilter);
    }
}
