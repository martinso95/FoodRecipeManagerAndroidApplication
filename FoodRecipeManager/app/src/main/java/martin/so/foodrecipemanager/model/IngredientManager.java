package martin.so.foodrecipemanager.model;

import java.util.Collections;
import java.util.List;

public class IngredientManager {

    /**
     * Adds an ingredient in alphabetical order, based on the ingredient's name.
     *
     * @param ingredientList the ingredient list that the ingredient will be added in.
     * @param ingredient     the ingredient to be added.
     */
    public static void addIngredient(List<Ingredient> ingredientList, Ingredient ingredient) {
        if (ingredient != null) {
            int index = Collections.binarySearch(ingredientList, ingredient,
                    (ingredient1, ingredient2) -> ingredient1.getName().compareToIgnoreCase(ingredient2.getName()));
            if (index < 0) {
                index = (index * -1) - 1;
            }
            ingredientList.add(index, ingredient);
        }
    }
}
