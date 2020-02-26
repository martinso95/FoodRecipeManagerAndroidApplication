package martin.so.foodrecipemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.ui.recipes.RecipesFragment;
import martin.so.foodrecipemanager.ui.tbd1.Tbd1Fragment;
import martin.so.foodrecipemanager.ui.tbd2.Tbd2Fragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private Fragment recipesFragment;
    private Fragment tbd1Fragment;
    private Fragment tbd2Fragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_recipes:
                    fragment = recipesFragment;
                    break;
                case R.id.navigation_tbd1:
                    fragment = tbd1Fragment;
                    break;
                case R.id.navigation_tbd2:
                    fragment = tbd2Fragment;
                    break;
            }
            return loadFragment(fragment);
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener onNavigationItemReselectedListener = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Test", "MAIN ACTIVITY CREATED");
//        SharedPreferences.Editor editor = getSharedPreferences("FoodRecipeManager", MODE_PRIVATE).edit();
//        editor.clear();
//        editor.apply();
//
////         Add dummy recipes:
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Bacon sandwich", "Test", "Test", Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_CATEGORY_MEAT));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Sausage", "Test", "Test", Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_CATEGORY_MEAT));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Ribeye", "Test", "Test", Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_CATEGORY_MEAT));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Meat pie", "Test", "Test", Utils.RECIPE_TYPE_DESSERT, Utils.RECIPE_CATEGORY_MEAT));
//
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Sallad", "Test", "Test", Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_CATEGORY_VEGAN));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Pasta", "Test", "Test", Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_CATEGORY_VEGAN));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Falafel", "Test", "Test", Utils.RECIPE_TYPE_HEAVY_MEAL, Utils.RECIPE_CATEGORY_VEGAN));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Donut", "Test", "Test", Utils.RECIPE_TYPE_DESSERT, Utils.RECIPE_CATEGORY_VEGAN));
//
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Beans", "Test", "Test", Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_CATEGORY_VEGETARIAN));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Vegetarian pizza", "Test", "Test", Utils.RECIPE_TYPE_LIGHT_MEAL, Utils.RECIPE_CATEGORY_VEGETARIAN));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Plant burger", "Test", "Test", Utils.RECIPE_TYPE_BREAKFAST, Utils.RECIPE_CATEGORY_VEGETARIAN));
//        RecipeManager.getInstance().addRecipe(this, new Recipe("Apple pie", "Test", "Test", Utils.RECIPE_TYPE_DESSERT, Utils.RECIPE_CATEGORY_VEGETARIAN));

        RecipeManager.getInstance().loadRecipes(this);

        setContentView(R.layout.activity_main);

        recipesFragment = new RecipesFragment();
        tbd1Fragment = new Tbd1Fragment();
        tbd2Fragment = new Tbd2Fragment();

        loadFragment(recipesFragment);

        BottomNavigationView navigation = findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setOnNavigationItemReselectedListener(onNavigationItemReselectedListener);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Test", "Permission is granted");
        } else {
            Log.d("Test", "Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Test", "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
