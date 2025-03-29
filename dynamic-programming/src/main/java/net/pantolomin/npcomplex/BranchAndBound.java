package net.pantolomin.npcomplex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

import static net.pantolomin.npcomplex.DoubleUtil.gt;
import static net.pantolomin.npcomplex.DoubleUtil.leZero;

@RequiredArgsConstructor
public class BranchAndBound<I> {
    private final List<I> items;
    private final ToDoubleFunction<I> itemCost;
    private final ToDoubleFunction<I> itemValue;

    private static <I> boolean isFinished(SearchContext<I> searchCtx, int index) {
        if (index >= searchCtx.getOrderedItems().length) {
            Solution<I> currentSolution = searchCtx.getCurrentSolution();
            if (searchCtx.getBestSolution() == null
                    || gt(currentSolution.currentValue(), searchCtx.getBestSolution().currentValue())) {
                searchCtx.setBestSolution(currentSolution);
            } // else: solution is worse than our best till now
            return true;
        }
        return false;
    }

    public final Solution<I> maximizeValue(double costLimit) {
        WeightedItem<I>[] orderedItems = this.items.stream()
                .map(item -> new WeightedItem<>(item, this.itemValue.applyAsDouble(item) / this.itemCost.applyAsDouble(item)))
                .sorted()
                .toArray(WeightedItem[]::new);
        SearchContext<I> searchCtx = new SearchContext<>(orderedItems, costLimit);
        explore(searchCtx, 0);
        return searchCtx.getBestSolution();
    }

    private void explore(SearchContext<I> searchCtx, int index) {
        exploreLeftSide(searchCtx, index);
        exploreRightSide(searchCtx, index + 1);
    }

    private void exploreLeftSide(SearchContext<I> searchCtx, int index) {
        Solution<I> currentSolution = searchCtx.getCurrentSolution();
        // Take it -> explore the left side of the tree
        Solution<I> newSolution = add(searchCtx, searchCtx.getOrderedItems()[index].item());
        if (newSolution == null) {
            // "currentSolution" is a solution
            if (searchCtx.getBestSolution() == null
                    || gt(currentSolution.currentValue(), searchCtx.getBestSolution().currentValue())) {
                searchCtx.setBestSolution(currentSolution);
            } // else: solution is worse than our best till now
            return;
        }
        searchCtx.setCurrentSolution(newSolution);
        int nextIndex = index + 1;
        try {
            if (isFinished(searchCtx, nextIndex)) {
                return;
            }
            explore(searchCtx, nextIndex);
        } finally {
            searchCtx.setCurrentSolution(currentSolution);
        }
    }

    private void exploreRightSide(SearchContext<I> searchCtx, int index) {
        // Don't take it -> explore the right side of the tree
        Solution<I> currentSolution = searchCtx.getCurrentSolution();
        double maxValue = estimateValue(searchCtx.getOrderedItems(), index, searchCtx.getCostLimit() - currentSolution.currentCost())
                + currentSolution.currentValue();
        if (gt(maxValue, searchCtx.getBestSolution().currentValue())) {
            explore(searchCtx, index);
        } // else: will never obtain a better solution
    }

    private Solution<I> add(SearchContext<I> searchCtx, I item) {
        Solution<I> currentSolution = searchCtx.getCurrentSolution();
        double cost = itemCost.applyAsDouble(item);
        double value = itemValue.applyAsDouble(item);
        double updatedCost = currentSolution.currentCost() + cost;
        if (gt(updatedCost, searchCtx.getCostLimit())) {
            return null;
        }
        return new Solution<>(currentSolution, item, updatedCost, currentSolution.currentValue() + value);
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

    @RequiredArgsConstructor
    @Getter
    @Setter
    private static final class SearchContext<I> {
        private final WeightedItem<I>[] orderedItems;
        private final double costLimit;
        private Solution<I> currentSolution = new Solution<>(null, null, 0d, 0d);
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
