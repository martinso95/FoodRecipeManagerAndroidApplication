package martin.so.foodrecipemanager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import martin.so.foodrecipemanager.model.Recipe;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.model.Utils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("TEST", "Permission is granted");
        } else {
            Log.d("TEST", "Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TEST", "Permission: " + permissions[0] + "was " + grantResults[0]);
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
