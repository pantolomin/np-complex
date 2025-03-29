package net.pantolomin.npcomplex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.ToDoubleFunction;

import static net.pantolomin.npcomplex.DoubleUtil.gt;
import static net.pantolomin.npcomplex.DoubleUtil.leZero;

@RequiredArgsConstructor
@Slf4j
public class BranchAndBound<I> {
    private final List<I> items;
    private final ToDoubleFunction<I> itemCost;
    private final ToDoubleFunction<I> itemValue;

    public final Solution<I> maximizeValue(double costLimit) {
        WeightedItem<I>[] orderedItems = this.items.stream()
                .map(item -> new WeightedItem<>(item, this.itemValue.applyAsDouble(item) / this.itemCost.applyAsDouble(item)))
                .sorted()
                .toArray(WeightedItem[]::new);
        SearchContext<I> searchCtx = new SearchContext<>(orderedItems, costLimit);
        Stack<State<I>> stack = new Stack<>();
        int maxStackSize = 0;
        int iterations = 0;
        stack.push(new State<>(0, Side.LEFT, new Solution<>(null, null, 0d, 0d)));
        while (!stack.isEmpty()) {
            State<I> state = stack.pop();
            switch (state.getSide()) {
                case LEFT:
                    exploreLeftSide(searchCtx, stack, state);
                    break;
                case RIGHT:
                    exploreRightSide(searchCtx, stack, state);
                    break;
            }
            maxStackSize = Math.max(maxStackSize, stack.size());
            iterations++;
        }
        log.info("Stack size: {} - Iterations: {}", maxStackSize, iterations);
        return searchCtx.getBestSolution();
    }

    private void exploreLeftSide(SearchContext<I> searchCtx, Stack<State<I>> stack, State<I> state) {
        Solution<I> currentSolution = state.getCurrentSolution();
        // Take it -> explore the left side of the tree
        Solution<I> newSolution = addItem(searchCtx, searchCtx.getOrderedItems()[state.getIndex()].item(), currentSolution);
        if (newSolution == null) {
            // "currentSolution" is a solution
            if (searchCtx.getBestSolution() == null
                    || gt(currentSolution.currentValue(), searchCtx.getBestSolution().currentValue())) {
                searchCtx.setBestSolution(currentSolution);
            } // else: solution is worse than our best till now
            stack.push(new State<>(state.getIndex(), Side.RIGHT, currentSolution));
        } else {
            int nextIndex = state.getIndex() + 1;
            if (nextIndex >= searchCtx.getOrderedItems().length) {
                if (searchCtx.getBestSolution() == null
                        || gt(newSolution.currentValue(), searchCtx.getBestSolution().currentValue())) {
                    searchCtx.setBestSolution(newSolution);
                } // else: solution is worse than our best till now
            } else {
                stack.push(new State<>(state.getIndex(), Side.RIGHT, currentSolution));
                stack.push(new State<>(nextIndex, Side.LEFT, newSolution));
            }
        }
    }

    private Solution<I> addItem(SearchContext<I> searchCtx, I item, Solution<I> currentSolution) {
        double cost = itemCost.applyAsDouble(item);
        double value = itemValue.applyAsDouble(item);
        double updatedCost = currentSolution.currentCost() + cost;
        if (gt(updatedCost, searchCtx.getCostLimit())) {
            return null;
        }
        return new Solution<>(currentSolution, item, updatedCost, currentSolution.currentValue() + value);
    }

    private void exploreRightSide(SearchContext<I> searchCtx, Stack<State<I>> stack, State<I> state) {
        Solution<I> currentSolution = state.getCurrentSolution();
        int nextIndex = state.getIndex() + 1;
        // Don't take it -> explore the right side of the tree
        double maxValue = estimateValue(searchCtx.getOrderedItems(), nextIndex, searchCtx.getCostLimit() - currentSolution.currentCost())
                + currentSolution.currentValue();
        if (gt(maxValue, searchCtx.getBestSolution().currentValue())) {
            stack.push(new State<>(nextIndex, Side.LEFT, state.getCurrentSolution()));
        }
    }

    /**
     * Estimate the maximum achievable value
     *
     * @param orderedItems the items in weight order
     * @param index        the current index
     * @param costLimit    the limit in cost
     * @return the max value
     */
    private double estimateValue(WeightedItem<I>[] orderedItems, int index, double costLimit) {
        double maxValue = 0d;
        for (int i = index; i < orderedItems.length; i++) {
            I item = orderedItems[index].item();
            double cost = this.itemCost.applyAsDouble(item);
            double value = this.itemValue.applyAsDouble(item);
            costLimit -= cost;
            maxValue += value;
            if (leZero(costLimit)) {
                return maxValue + costLimit * value / cost;
            }
        }
        return maxValue;
    }

    private enum Side {LEFT, RIGHT}

    @AllArgsConstructor
    @Getter
    @Setter
    private static final class State<I> {
        private final int index;
        private Side side;
        private Solution<I> currentSolution;
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    private static final class SearchContext<I> {
        private final WeightedItem<I>[] orderedItems;
        private final double costLimit;
        //private Solution<I> currentSolution = new Solution<>(null, null, 0d, 0d);
        private Solution<I> bestSolution;
    }

    public record Solution<I>(Solution<I> previous, I item, double currentCost, double currentValue) {
        public List<I> getItems() {
            List<I> solutionItems = this.previous != null ? this.previous.getItems() : new ArrayList<>();
            if (this.item != null) {
                solutionItems.add(this.item);
            }
            return solutionItems;
        }
    }

    private record WeightedItem<I>(I item, double weight) implements Comparable<WeightedItem<I>> {
        @Override
        public int compareTo(WeightedItem o) {
            return Double.compare(this.weight, o.weight);
        }
    }
}
