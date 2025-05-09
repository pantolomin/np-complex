package net.pantolomin.npcomplex;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.pantolomin.npcomplex.DoubleUtil.isEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class DynamicPTest {
    @Test
    void testDP() {
        List<Item> items = List.of(new Item(2, 16), new Item(3, 19), new Item(4, 23), new Item(5, 28));
        DynamicP<Item> dp = new DynamicP<>(items, i -> i.weight, i -> i.value);
        DynamicP.Solution<Item> solution = dp.maximizeValue(7);
        checkCost(solution, 7);
        checkValue(solution, 44);
        checkItems(items, solution, 0, 3);
    }

    @Test
    void testDP2() {
        List<Item> items = List.of(new Item(5, 45), new Item(8, 48), new Item(3, 35));
        DynamicP<Item> dp = new DynamicP<>(items, i -> i.weight, i -> i.value);
        DynamicP.Solution<Item> solution = dp.maximizeValue(10);
        checkCost(solution, 8);
        checkValue(solution, 80);
        checkItems(items, solution, 0, 2);
    }

    private void checkCost(DynamicP.Solution<Item> solution, double cost) {
        assertTrue(isEqual(solution.getCost(), cost), "Cost was " + solution.getCost());
    }

    private void checkValue(DynamicP.Solution<Item> solution, double value) {
        assertTrue(isEqual(solution.getValue(), value), "Value was " + solution.getValue());
    }

    private void checkItems(List<Item> items, DynamicP.Solution<Item> solution, int... indices) {
        int idx = 0;
        for (Item item : solution.getItems()) {
            Item expectedItem = items.get(indices[idx++]);
            assertEquals(expectedItem, item);
        }
    }

    private record Item(int weight, int value) {
        @Override
        public String toString() {
            return "Item{" +
                    "weight=" + weight +
                    ", value=" + value +
                    '}';
        }
    }
}
