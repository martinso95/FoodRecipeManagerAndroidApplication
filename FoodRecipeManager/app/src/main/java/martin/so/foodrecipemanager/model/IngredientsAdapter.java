package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import martin.so.foodrecipemanager.R;

/**
 * Adapter class. Handles the ingredient card views.
 */
public class IngredientsAdapter extends ArrayAdapter<Ingredient> {

    private Context mContext;
    private List<Ingredient> allIngredients;
    private boolean editModeActive;
    private int initialPortions;
    private double portionMultiplier = 1;

    /**
     * @param context         context of the activity.
     * @param allIngredients  the list of ingredients to be used for this adapter.
     * @param editModeActive  determines whether the ingredients view can be edited or not.
     * @param initialPortions number of portions for the recipe. Should be 0 if editModeActive is true, since it will not be used for edit mode.
     */
    public IngredientsAdapter(Context context, List<Ingredient> allIngredients, boolean editModeActive, int initialPortions) {
        super(context, 0, allIngredients);
        this.mContext = context;
        this.allIngredients = allIngredients;
        this.editModeActive = editModeActive;
        this.initialPortions = initialPortions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            if (editModeActive) {
                v = vi.inflate(R.layout.ingredient_item_edit_mode, null);
            } else {
                v = vi.inflate(R.layout.ingredient_item, null);
            }
        }

        Ingredient ingredient = getItem(position);

        if (ingredient != null) {
            TextView ingredientAmount;
            TextView ingredientUnit;
            TextView ingredientName;
            if (editModeActive) {
                ImageButton deleteIngredientButton = v.findViewById(R.id.imageButtonDeleteIngredientIngredientItemEditMode);
                ingredientAmount = v.findViewById(R.id.textViewIngredientAmountIngredientItemEditMode);
                ingredientUnit = v.findViewById(R.id.textViewIngredientUnitIngredientItemEditMode);
                ingredientName = v.findViewById(R.id.textViewIngredientNameIngredientItemEditMode);

                deleteIngredientButton.setOnClickListener(v1 -> {
                    allIngredients.remove(position);
                    notifyDataSetChanged();
                });

                if (ingredient.getAmount() == 0) {
                    ingredientAmount.setText("");
                } else {
                    ingredientAmount.setText(IngredientManager.formatDecimals(ingredient.getAmount()));
                }
            } else {
                ingredientAmount = v.findViewById(R.id.textViewIngredientAmountIngredientItem);
                ingredientUnit = v.findViewById(R.id.textViewIngredientUnitIngredientItem);
                ingredientName = v.findViewById(R.id.textViewIngredientNameIngredientItem);

                if (ingredient.getAmount() == 0) {
                    ingredientAmount.setText("");
                } else {
                    ingredientAmount.setText(IngredientManager.formatDecimals(ingredient.getAmount() * portionMultiplier));
                }
            }

            if (ingredient.getUnit().equals(Utils.NA)) {
                ingredientUnit.setText("");
            } else {
                ingredientUnit.setText(ingredient.getUnit());
            }
            ingredientName.setText(ingredient.getName());
        }
        return v;
    }

    public void adjustIngredientAmountAccordingToPortions(int portionsAfterAdjusting) {
        portionMultiplier = (double) portionsAfterAdjusting / initialPortions;
        notifyDataSetChanged();
    }

    public void resetAdjustedIngredientPortions() {
        portionMultiplier = 1;
        notifyDataSetChanged();
    }

    public void setInitialPortions(int initialPortions) {
        this.initialPortions = initialPortions;
    }

}