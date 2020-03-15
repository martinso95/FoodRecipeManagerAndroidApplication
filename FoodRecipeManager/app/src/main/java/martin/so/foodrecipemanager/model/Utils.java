package martin.so.foodrecipemanager.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Utils {

    public static final String PHOTO_STORAGE_DIRECTORY = "recipeStorageFolder";

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

}
