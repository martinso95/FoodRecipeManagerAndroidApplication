package martin.so.foodrecipemanager.model;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a recipe, containing properties that a recipe should have.
 */
public class Recipe {
    private String photoPath = null;
    private String photoDownloadUri = null;
    @Exclude
    private Bitmap temporaryLocalPhoto = null;
    private String name;
    private String type;
    private String category;
    private int timeHours = -1; // Negative means no time is set.
    private int timeMinutes = -1; // Negative means no time is set.
    private List<Ingredient> ingredients;
    private String instructions;

    public Recipe() {
        // Empty constructor in order for Firebase realtime database to work.
    }

    /**
     * A recipe's necessary properties...
     *
     * @param photoPath    Is null if the recipe does not have a photo.
     *                     If it has, then the photo's path is a random UUID.
     * @param name         A unique name of the recipe.
     * @param category     Category of the recipe. (Meat, Vegetarian, Vegan).
     * @param type         Type of the recipe. (All, Breakfast, Light meal, Heavy meal, Dessert).
     * @param timeHours    Number of hours it takes for this recipe. Negative if no time exists for this recipe.
     * @param timeMinutes  Number of minutes it takes for this recipe. Negative if no time exists for this recipe.
     * @param ingredients  List of ingredients, type Ingredient.
     * @param instructions Full instructions on how to cook the recipe.
     */
    public Recipe(String photoPath, String name, String category, String type, int timeHours, int timeMinutes, List<Ingredient> ingredients, String instructions) {
        this.photoPath = photoPath;
        this.name = name;
        this.type = type;
        this.category = category;
        this.timeHours = timeHours;
        this.timeMinutes = timeMinutes;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getPhotoDownloadUri() {
        return photoDownloadUri;
    }

    public void setPhotoDownloadUri(String photoDownloadUri) {
        this.photoDownloadUri = photoDownloadUri;
    }

    @Exclude
    public Bitmap getTemporaryLocalPhoto() {
        return temporaryLocalPhoto;
    }

    @Exclude
    public void setTemporaryLocalPhoto(Bitmap temporaryLocalPhoto) {
        this.temporaryLocalPhoto = temporaryLocalPhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTimeHours() {
        return timeHours;
    }

    public void setTimeHours(int timeHours) {
        this.timeHours = timeHours;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    public void setTimeMinutes(int timeMinutes) {
        this.timeMinutes = timeMinutes;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

}
