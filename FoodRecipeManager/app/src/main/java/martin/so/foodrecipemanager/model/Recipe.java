package martin.so.foodrecipemanager.model;

public class Recipe {
    private String name;
    private String description;

    public Recipe(String name, String content) {
        this.name = name;
        this.description = content;
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

}
