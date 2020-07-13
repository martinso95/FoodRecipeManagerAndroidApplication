package martin.so.foodrecipemanager.model;

/**
 * A class representing an ingredient, containing properties that an ingredient should have.
 */
public class Ingredient {

    private String amount;
    private String name;

    public Ingredient() {
        // Empty constructor in order for Firebase realtime database to work.
    }

    public Ingredient(String amount, String name) {
        this.amount = amount;
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
