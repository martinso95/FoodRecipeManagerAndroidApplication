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

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.Utils;

import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Fragment containing the recipes in a Tab Layout.
 * The recipes are presented in a RecyclerView, vertical list.
 * Each item in the list is in a form of a card.
 */
public class RecipesFragment extends Fragment {

    private View view = null;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("Test", "MAIN Recipe Fragment START");
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_recipes, container, false);

        tabLayout = view.findViewById(R.id.tabLayoutRecipesFragment);

        viewPager = view.findViewById(R.id.viewPagerRecipesFragment);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getActivity());

        viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText("" + Utils.RECIPE_TYPES[position]);
                    }
                }).attach();

        int limit = (viewPagerAdapter.getItemCount() > 1 ? viewPagerAdapter.getItemCount() - 1 : 1);

        viewPager.setOffscreenPageLimit(limit);
        Log.d("Test", "MAIN Recipe Fragment CREATED");

        return view;
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_recipe_button) {
            Intent addRecipeActivity = new Intent(getActivity(), AddRecipeActivity.class);
            startActivity(addRecipeActivity);
        } else if (id == R.id.search_recipe_button) {
            Intent searchRecipesActivity = new Intent(getActivity(), RecipeSearchActivity.class);
            startActivity(searchRecipesActivity);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Test", "MAIN Recipe Fragment RESUMED");
    }

}