package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import martin.so.foodrecipemanager.R;

import java.util.List;

/**
 * RecyclerView Adapter class. Handles the recipe card views.
 */
public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    private List<Recipe> recipes;
    private LayoutInflater inflater;
    private Context context;
    private ItemClickListener recipeClickListener;

    public RecipesAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.recipes = recipes;
        Log.d("Test", "Recipe adapter constructor");
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Recipe recipe = recipes.get(position);
        holder.recipeName.setText(recipe.getName());
        holder.recipeCategory.setText(recipe.getCategory());
        holder.recipeType.setText(recipe.getType());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public String getItem(int id) {
        return recipes.get(id).getName();
    }

    /**
     * This determines what data will be displayed on the cards in the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView recipeName;
        TextView recipeCategory;
        TextView recipeType;

        ViewHolder(View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.textViewRecipeNameCard);
            recipeCategory = itemView.findViewById(R.id.textViewRecipeCategoryCard);
            recipeType = itemView.findViewById(R.id.textViewRecipeTypeCard);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (recipeClickListener != null)
                recipeClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener recipeClickListener) {
        this.recipeClickListener = recipeClickListener;
    }

    /**
     * Parent will implement this method to respond to click events.
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}