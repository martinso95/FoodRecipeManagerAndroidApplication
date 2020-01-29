package martin.so.foodrecipemanager.ui.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.RecipesAdapter;

/**
 * Fragment containing the recipes.
 * The recipes are presented in a RecyclerView, vertical list.
 * Each item in the list is in a form of a card.
 */
public class RecipesFragment extends Fragment implements RecipesAdapter.ItemClickListener {

    private RecipesAdapter recipesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("Test", "Recipe Fragment start");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipesAdapter = new RecipesAdapter(getContext(), RecipeManager.getInstance().getAllRecipes());
        recipesAdapter.setClickListener(this);
        recyclerView.setAdapter(recipesAdapter);

        return view;
    }

    /**
     * Clicking on a list item, a new activity will start,
     * that represents a detailed view of the recipe.
     * The position of the item will be passed. The reason is to be able to identify the recipe,
     * with the help of the index.
     */
    @Override
    public void onItemClick(View view, int position) {
        Intent recipeDetailsActivity = new Intent(getActivity(), RecipeDetailsActivity.class);
        recipeDetailsActivity.putExtra("recipePosition", position);
        startActivity(recipeDetailsActivity);
    }

    /**
     * A method for the "Add recipe"-button.
     * Clicking on the "Add recipe"-button will start a new activity,
     * where it is possible to add a new recipe with details.
     */
    private void addRecipe() {
        Intent addRecipeActivity = new Intent(getActivity(), AddRecipeActivity.class);
        startActivity(addRecipeActivity);
    }

    /**
     * Adds the "Add recipe"-button to the Fragment.
     * Position: Top right.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar.
        inflater.inflate(R.menu.fragment_recipe_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Clicking on the "Add recipe"-button will call addRecipe().
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_recipe_button) {
            addRecipe();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Every time the Fragment is getting focus (resumed),
     * the recipe list will be updated, in case new recipes have been added,
     * or anything has been deleted, from another view.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d("Test", "Resumed RecipeFragment");
        recipesAdapter.notifyDataSetChanged();
    }
}