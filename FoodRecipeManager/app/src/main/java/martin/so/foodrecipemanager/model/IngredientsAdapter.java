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

    public IngredientsAdapter(Context context, List<Ingredient> allIngredients, boolean editModeActive) {
        super(context, 0, allIngredients);
        this.mContext = context;
        this.allIngredients = allIngredients;
        this.editModeActive = editModeActive;
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
            if (editModeActive) {
                ImageButton deleteIngredientButton = v.findViewById(R.id.imageButtonDeleteIngredientIngredientItemEditMode);
                TextView ingredientAmount = v.findViewById(R.id.textViewIngredientAmountIngredientItemEditMode);
                TextView ingredientName = v.findViewById(R.id.textViewIngredientNameIngredientItemEditMode);
                ingredientAmount.setText(ingredient.getAmount());
                ingredientName.setText(ingredient.getName());
                deleteIngredientButton.setOnClickListener(v1 -> {
                    allIngredients.remove(position);
                    notifyDataSetChanged();
                });
            } else {
                TextView ingredientAmount = v.findViewById(R.id.textViewIngredientAmountIngredientItem);
                TextView ingredientName = v.findViewById(R.id.textViewIngredientNameIngredientItem);
                ingredientAmount.setText(ingredient.getAmount());
                ingredientName.setText(ingredient.getName());
            }
        }
        return v;
    }

}