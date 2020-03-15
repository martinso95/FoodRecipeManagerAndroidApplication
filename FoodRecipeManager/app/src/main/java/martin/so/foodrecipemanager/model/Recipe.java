package martin.so.foodrecipemanager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a recipe, containing properties that a recipe should have.
 */
public class Recipe implements Serializable {
    private String name;
    private String description;
    private String type;
    private String category;
    private List<Ingredient> ingredients;
    private String instructions;

    public Recipe(String name, String description, String type, String category, List<Ingredient> ingredients, String instructions) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
