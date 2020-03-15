package martin.so.foodrecipemanager.model;

import java.io.Serializable;

/**
 * A class representing an ingredient, containing properties that an ingredient should have.
 */
public class Ingredient implements Serializable {
    private String description;

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
