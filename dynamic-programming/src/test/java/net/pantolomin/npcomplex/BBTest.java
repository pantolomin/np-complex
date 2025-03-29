package net.pantolomin.npcomplex;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.pantolomin.npcomplex.DoubleUtil.isEqual;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class BBTest {
    @Test
    void testDP() {
        List<Item> items = List.of(new Item(2, 16), new Item(3, 19), new Item(4, 23), new Item(5, 28));
        BranchAndBound<Item> dp = new BranchAndBound<>(items, i -> i.weight, i -> i.value);
        BranchAndBound.Solution<Item> solution = dp.maximizeValue(7);
        checkCost(solution, 7);
        checkValue(solution, 44);
        checkItems(items, solution, 0, 3);
    }

    @Test
    void testDP2() {
        List<Item> items = List.of(new Item(5, 45), new Item(8, 48), new Item(3, 35));
        BranchAndBound<Item> dp = new BranchAndBound<>(items, i -> i.weight, i -> i.value);
        BranchAndBound.Solution<Item> solution = dp.maximizeValue(10);
        checkCost(solution, 8);
        checkValue(solution, 80);
        checkItems(items, solution, 0, 2);
    }

    private void checkCost(BranchAndBound.Solution<Item> solution, double cost) {
        assertTrue(isEqual(solution.currentCost(), cost), "Cost was " + solution.currentCost());
    }

    private void checkValue(BranchAndBound.Solution<Item> solution, double value) {
        assertTrue(isEqual(solution.currentValue(), value), "Value was " + solution.currentValue());
    }

    private void checkItems(List<Item> items, BranchAndBound.Solution<Item> solution, int... indices) {
        int idx = 0;
        List<Item> expectedItems = Arrays.stream(indices).mapToObj(items::get).collect(Collectors.toList());
        for (Item item : solution.getItems()) {
            assertTrue(expectedItems.remove(item));
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
