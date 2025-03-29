package net.pantolomin.npcomplex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.ToDoubleFunction;

import static net.pantolomin.npcomplex.DoubleUtil.*;

@RequiredArgsConstructor
@Slf4j
public class DynamicP<I> {
    private final List<I> items;
    private final ToDoubleFunction<I> itemCost;
    private final ToDoubleFunction<I> itemValue;

    public final Solution<I> maximizeValue(double costLimit) {
        List<Solution<I>> solutions = new ArrayList<>();
        int handledItems = 0;
        for (I item : this.items) {
            double cost = this.itemCost.applyAsDouble(item);
            double value = this.itemValue.applyAsDouble(item);
            double costThreshold = costLimit - cost;
            if (ltZero(costThreshold)) {
                // Can't ever take the item
                continue;
            }
            Solution<I> previousSolution = null;
            Solution<I> solutionToAdd = new Solution<>(null, item, cost, value);
            Queue<Solution<I>> addedSolutions = new LinkedList<>();
            int length = solutions.size();
            for (int i = 0; i < length; i++) {
                Solution<I> solution = solutions.get(i);
                while (solutionToAdd != null) {
                    if (gt(solutionToAdd.getCost(), solution.getCost())) {
                        // New solution can't be inserted here
                        break;
                    }
                    if (lt(solutionToAdd.getCost(), solution.getCost())) {
                        if (previousSolution == null || gt(solutionToAdd.getValue(), previousSolution.getValue())) {
                            solutions.add(i++, solutionToAdd);
                            length++;
                            previousSolution = solutionToAdd;
                        } // else: solution worse than what we already have
                    } else if (gt(solutionToAdd.getValue(), solution.getValue())) {
                        // Same cost but better value -> replace
                        solutions.set(i, solutionToAdd);
                    } // else: solution worse than what we already have
                    solutionToAdd = addedSolutions.poll();
                }
                if (le(solution.getCost(), costThreshold)) {
                    Solution<I> toAdd = solution.add(item, cost, value);
                    if (solutionToAdd != null) {
                        addedSolutions.offer(toAdd);
                    } else {
                        solutionToAdd = toAdd;
                    }
                }
                previousSolution = solution;
            }
            while (solutionToAdd != null) {
                if (previousSolution == null || gt(solutionToAdd.getValue(), previousSolution.getValue())) {
                    solutions.add(solutionToAdd);
                    previousSolution = solutionToAdd;
                }
                solutionToAdd = addedSolutions.poll();
            }
            handledItems++;
            if (handledItems % 10 == 0) {
                log.info("Item {}: {}", handledItems, solutions.size());
            }
        }
        return solutions.getLast();
    }

    @RequiredArgsConstructor
    public static final class Solution<I> {
        private final Solution<I> previousSolution;
        private final I item;
        @Getter
        private final double cost;
        @Getter
        private final double value;

        public Solution<I> add(I item, double cost, double value) {
            return new Solution<>(this, item, this.cost + cost, this.value + value);
        }

        public List<I> getItems() {
            List<I> solutionItems = this.previousSolution != null ? this.previousSolution.getItems() : new ArrayList<>();
            solutionItems.add(this.item);
            return solutionItems;
        }
    }
}
