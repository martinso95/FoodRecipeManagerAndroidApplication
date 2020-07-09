package martin.so.foodrecipemanager.model;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static final String RECIPE_CATEGORY = "Category";
    public static final String RECIPE_CATEGORY_MEAT = "Meat";
    public static final String RECIPE_CATEGORY_VEGETARIAN = "Vegetarian";
    public static final String RECIPE_CATEGORY_VEGAN = "Vegan";

    public static final String RECIPE_TYPE = "Type";
    public static final String RECIPE_TYPE_ALL = "All";
    public static final String RECIPE_TYPE_BREAKFAST = "Breakfast";
    public static final String RECIPE_TYPE_LIGHT_MEAL = "Light meal";
    public static final String RECIPE_TYPE_HEAVY_MEAL = "Heavy meal";
    public static final String RECIPE_TYPE_DESSERT = "Dessert";

    public static final String[] RECIPE_TYPES = {"All", "Breakfast", "Light meal", "Heavy meal", "Dessert"};

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter mAdapter = listView.getAdapter();
        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    // Firebase:
    public static final String FIREBASE_RECIPES_PATH = "recipes";
    public static final String FIREBASE_IMAGES_PATH = "images";

    // FileProvider usage: Photos taken with the camera will temporarily be stored locally,
    // in order to upload full image to Firebase.
    public static final String FILE_PROVIDER_AUTHORITY = "martin.so.fileprovider";

    /**
     * Create an empty temporary photo in local file system.
     * Used for storing photo taken with the camera.
     *
     * @param context context of the application.
     * @return the created photo file.
     * @throws IOException handle IOException.
     */
    public static File createTemporaryPhoto(Context context) throws IOException {
        File storageDir = context.getFilesDir();
        // Each file created will have a unique name.
        return File.createTempFile("temporaryPhoto_", ".jpg", storageDir);
    }

    /**
     * Hides the keyboard when it is up.
     *
     * @param activity must be an activity.
     */
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}