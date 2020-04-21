package martin.so.foodrecipemanager.model;

/**
 * A class representing an ingredient, containing properties that an ingredient should have.
 */
public class Ingredient {
    private String description;

    public Ingredient() {
        // Empty constructor in order for Firebase realtime database to work.
    }

    public Ingredient(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
