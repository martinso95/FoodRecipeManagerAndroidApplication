package martin.so.foodrecipemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import martin.so.foodrecipemanager.model.FirebaseStorageOfflineHandler;
import martin.so.foodrecipemanager.model.RecipeManager;
import martin.so.foodrecipemanager.ui.recipes.RecipesFragment;
import martin.so.foodrecipemanager.ui.tbd1.Tbd1Fragment;
import martin.so.foodrecipemanager.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private Fragment recipesFragment;
    private Fragment tbd1Fragment;
    private Fragment profileFragment;

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
                    fragment = profileFragment;
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

        RecipeManager.getInstance().initializeRecipeManager();

        setContentView(R.layout.activity_main);

        recipesFragment = new RecipesFragment();
        tbd1Fragment = new Tbd1Fragment();
        profileFragment = new ProfileFragment();

        loadFragment(recipesFragment);

        BottomNavigationView navigation = findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setOnNavigationItemReselectedListener(onNavigationItemReselectedListener);

        FirebaseStorageOfflineHandler.getInstance().initializeFirebaseStorageOfflineHandler(this);
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
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
