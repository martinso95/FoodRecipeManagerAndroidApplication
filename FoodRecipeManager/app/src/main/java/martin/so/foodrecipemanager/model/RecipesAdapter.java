package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import martin.so.foodrecipemanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter class. Handles the recipe card views.
 */
public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    private List<Recipe> recipes;
    private List<Recipe> recipesCopy;
    private LayoutInflater inflater;
    private Context context;
    private ItemClickListener recipeClickListener;
    private RequestOptions glideRequestOptions;

    public RecipesAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.recipes = recipes;
        recipesCopy = new ArrayList<>(recipes);
        glideRequestOptions = new RequestOptions();
        glideRequestOptions.centerCrop();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Recipe recipe = recipes.get(position);

        // Load the recipe photo from Firebase Storage.
        // If not available, then load placeholder in the form of the temporary local photo (if it exists),
        // or a plain static drawable image.
        if (recipe.getPhotoPath() != null) {
            if (recipe.getPhotoDownloadUri() != null) {
                // Load photo with Glide, which supports caching.
                Glide.with(context).load(recipe.getPhotoDownloadUri()).placeholder(R.drawable.ic_food_placeholder_black_100dp).apply(glideRequestOptions).into(holder.recipePhoto);
            } else if (recipe.getTemporaryLocalPhoto() != null) {
                // If photo has changed, load the local photo copy, because uploading the new download uri can take time.
                holder.recipePhoto.setImageBitmap(recipe.getTemporaryLocalPhoto());
            } else {
                // Only comes to this code if the app is online, and no download uri exists (which might happen when editing recipe photos offline).
                // If it is offline, it will get the glide cache. If photo was changed, it will get the local copy.
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipe.getPhotoPath());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri.toString()).apply(glideRequestOptions).into(holder.recipePhoto);
                        recipe.setPhotoDownloadUri(uri.toString());
                        RecipeManager.getInstance().saveChanges();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.recipePhoto.setImageResource(R.drawable.ic_food_placeholder_black_100dp);
                    }
                });
            }
        } else {
            holder.recipePhoto.setImageResource(R.drawable.ic_add_photo_black_100dp);
        }

        holder.recipeName.setText(recipe.getName());
        holder.recipeCategory.setText(recipe.getCategory());
        holder.recipeType.setText(recipe.getType());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public Recipe getItem(int id) {
        return recipes.get(id);
    }

    /**
     * This determines what data will be displayed on the cards in the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView recipePhoto;
        TextView recipeName;
        TextView recipeCategory;
        TextView recipeType;

        ViewHolder(View itemView) {
            super(itemView);
            recipePhoto = itemView.findViewById(R.id.imageViewThumbnailCard);
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

    /**
     * Filter the recipe list based on the text parameter.
     * Notifies that the recipe list has changed.
     *
     * @param text The text that is to be found in the recipes' names.
     * @return the newly filtered list.
     */
    public List<Recipe> filter(String text) {
        recipes.clear();
        if (text.isEmpty()) {
            recipes.addAll(recipesCopy);
        } else {
            text = text.toLowerCase();
            for (Recipe recipe : recipesCopy) {
                if (recipe.getName().toLowerCase().contains(text)) {
                    recipes.add(recipe);
                }
            }
        }
        notifyDataSetChanged();
        return recipes;
    }

    public void setList(List<Recipe> newRecipes) {
        recipes = newRecipes;
        recipesCopy = new ArrayList<>(recipes);
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