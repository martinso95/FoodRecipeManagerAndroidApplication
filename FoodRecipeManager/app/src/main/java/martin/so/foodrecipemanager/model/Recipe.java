package martin.so.foodrecipemanager.model;

import java.io.Serializable;

/**
 * A class representing a recipe, containing properties that a recipe should have.
 */
public class Recipe implements Serializable {
    private String name;
    private String description;
    private String instructions;
    private String type;
    private String category;

    public Recipe(String name, String description, String instructions, String type, String category) {
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.type = type;
        this.category = category;
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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

}
