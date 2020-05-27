package martin.so.foodrecipemanager.ui.recipes;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.RecipesAdapter;
import martin.so.foodrecipemanager.model.Utils;

/**
 * Fragment for creating the tab pages.
 * Each page contains a recipe list.
 */
public class PageRecipesFragment extends Fragment implements RecipesAdapter.ItemClickListener {

    private static final String ARG_COUNT = "param1";
    private Integer counter;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference fireBaseDatabaseReference;
    private List<Recipe> recipeList;

    private RecipesAdapter recipesAdapter;

    public PageRecipesFragment() {
        // Required empty public constructor.
    }

    public static PageRecipesFragment newInstance(Integer counter) {
        PageRecipesFragment fragment = new PageRecipesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, counter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            counter = getArguments().getInt(ARG_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_recipes, container, false);
        Log.d("Test", "PAGE " + counter + " Recipe Fragment CREATED");
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPageRecipes);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recipeList = getRecipeTypeList(Utils.RECIPE_TYPES[counter]);
        recipesAdapter = new RecipesAdapter(getContext(), recipeList);
        loadRecipes(recipeList);
        recipesAdapter.setClickListener(this);
        recyclerView.setAdapter(recipesAdapter);
        return view;
    }

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

    /**
     * Clicking on a list item, a new activity will start,
     * that represents a detailed view of the recipe.
     * The position of the item will be passed. The reason is to be able to identify the recipe,
     * with the help of the index.
     */
    @Override
    public void onItemClick(View view, int position) {
        Intent recipeDetailsActivity = new Intent(getActivity(), RecipeDetailsActivity.class);
        recipeDetailsActivity.putExtra("recipeAdapterPosition", position);
        recipeDetailsActivity.putExtra("recipeType", Utils.RECIPE_TYPES[counter]);
        startActivity(recipeDetailsActivity);
    }

    /**
     * Load the recipes from Firebase realtime database.
     */
    public void loadRecipes(List<Recipe> recipeList) {
        firebaseAuth = FirebaseAuth.getInstance();
        Query fireBaseDatabaseReference = FirebaseDatabase.getInstance().getReference(Utils.FIREBASE_RECIPES_PATH).child(firebaseAuth.getUid());
        if (!Utils.RECIPE_TYPES[counter].equals(Utils.RECIPE_TYPE_ALL)) {
            fireBaseDatabaseReference = fireBaseDatabaseReference.orderByChild("type").equalTo(Utils.RECIPE_TYPES[counter]);
        }

        fireBaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    recipeList.clear();
                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                        Recipe recipe = dss.getValue(Recipe.class);
                        recipeList.add(recipe);
                    }
                    Log.d("Test", "added for: " + Utils.RECIPE_TYPES[counter]);
                    recipesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("Test", "Failed to read the recipe list from Firebase.");
                // TODO: Handle failure of loading the recipe list from Firebase.
                // Ex. show "Something went wrong, reload please..." in the Recipe list view.
            }
        });
    }

    /**
     * Every time the Fragment is getting focus (resumed),
     * the recipe list will be updated, in case new recipes have been added,
     * or anything has been deleted, from another view.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d("Test", "PAGE " + counter + " Recipe Fragment RESUMED");
        recipesAdapter.notifyDataSetChanged();
    }
}