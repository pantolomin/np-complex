package net.pantolomin.npcomplex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Knapsack {
    private static final String RESOURCE = "data";

    @Test
    void testKnapsack() throws IOException {
        File problemsDir = new File(getClass().getClassLoader().getResource(RESOURCE).getFile());
        for (File f : problemsDir.listFiles()) {
            Problem problem = readProblem(f);
            if (problem.getItems().size() >= 100) {
                continue;
            }
            log.info("Problem {}: capacity: {} - items: {}",
                    f.getName(), problem.getCapacity(), problem.getItems().size());
            DynamicP<Item> dp = new DynamicP<>(problem.getItems(), i -> i.getWeight(), i -> i.getValue());
            DynamicP.Solution<Item> solution = dp.maximizeValue(problem.getCapacity());
            log.info("Solution: weight {} - value {} - items: {}",
                    solution.getCost(), solution.getValue(), solution.getItems().size());
        }
    }

    private Problem readProblem(File file) throws IOException {
        List<Item> itemList = null;
        int nbItems = 0;
        int capacity = 0;
        try (FileReader reader = new FileReader(file); BufferedReader input = new BufferedReader(reader)) {
            String line;
            while ((line = input.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (itemList == null) {
                    nbItems = Integer.parseInt(parts[0]);
                    capacity = Integer.parseInt(parts[1]);
                    itemList = new ArrayList<>(nbItems);
                } else if (itemList.size() < nbItems) {
                    itemList.add(new Item(Integer.parseInt(parts[1]), Integer.parseInt(parts[0])));
                }
            }
        }
        return new Problem(capacity, itemList);
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Problem {
        private final int capacity;
        private final List<Item> items;
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static final class Item {
        private final int weight;
        private final int value;
    }
}
