package martin.so.foodrecipemanager.model;

/**
 * A class representing an ingredient, containing properties that an ingredient should have.
 */
public class Ingredient {

    private double amount;
    private String unit;
    private String name;

    public Ingredient() {
        // Empty constructor in order for Firebase realtime database to work.
    }

    /**
     * Properties of an ingredient.
     *
     * @param amount amount of an ingredient.
     * @param unit   unit of an ingredient.
     * @param name   name of an ingredient.
     */
    public Ingredient(double amount, String unit, String name) {
        this.amount = amount;
        this.unit = unit;
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}