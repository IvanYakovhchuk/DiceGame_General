package com.algorithms.lab6;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.algorithms.lab6.HelloApplication.computerUsedCombinations;
import static com.algorithms.lab6.HelloApplication.isStreet;

public class DiceCombinationChecker {

    public static boolean[] determineChanges(List<Integer> dice, int roundsLeft) {
        List<Integer> currentDice = new ArrayList<>(dice);
        String bestCombination = null;
        int minChanges = Integer.MAX_VALUE;
        boolean[] bestChanges = new boolean[5];

        for (int rerollAttempt = 1; rerollAttempt <= 2; rerollAttempt++) {
            if (shouldMaximizeSingleDice(dice, rerollAttempt, roundsLeft)) {
                bestChanges = maximizeSingleDice(dice);
            } else {
                List<List<Integer>> possibleRolls = generatePossibleRolls(currentDice);

                for (List<Integer> roll : possibleRolls) {
                    String combination = determineCombination(roll);
                    if (combination != null && (!computerUsedCombinations.containsKey(combination) || !computerUsedCombinations.get(combination))) {
                        int changes = countChanges(dice, roll);
                        if (changes < minChanges) {
                            minChanges = changes;
                            bestCombination = combination;
                            currentDice = new ArrayList<>(roll);
                            bestChanges = calculateChanges(dice, roll);
                        }
                    }
                }

                if (("Four of a Kind".equals(bestCombination)) && rerollAttempt < 2) {
                    List<Integer> attemptToGeneral = tryToImproveToGeneral(currentDice);
                    if (attemptToGeneral != null) {
                        bestChanges = calculateChanges(dice, attemptToGeneral);
                        break;
                    }
                }

                if (bestCombination != null) {
                    break;
                }
            }
        }

        return bestChanges;
    }

    private static List<Integer> tryToImproveToGeneral(List<Integer> dice) {
        Map<Integer, Long> frequencies = dice.stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        int targetNumber = frequencies.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .map(Map.Entry::getKey)
                .findFirst().orElse(-1);

        if (targetNumber == -1) return null;

        List<Integer> newDice = new ArrayList<>(dice);
        for (int i = 0; i < newDice.size(); i++) {
            if (newDice.get(i) != targetNumber) {
                newDice.set(i, targetNumber);
            }
        }

        if (determineCombination(newDice).equals("General")) {
            return newDice;
        }

        return null;
    }

    private static List<List<Integer>> generatePossibleRolls(List<Integer> dice) {
        List<List<Integer>> allRolls = new ArrayList<>();

        for (int i = 1; i <= dice.size(); i++) {
            List<int[]> subsets = generateSubsets(dice.size(), i);
            for (int[] subset : subsets) {
                generateRollsForSubset(dice, subset, allRolls);
            }
        }

        return allRolls;
    }

    private static List<int[]> generateSubsets(int n, int k) {
        List<int[]> subsets = new ArrayList<>();
        int[] subset = new int[k];
        generateSubsetsRecursive(subsets, subset, 0, 0, n, k);
        return subsets;
    }

    private static void generateSubsetsRecursive(List<int[]> subsets, int[] subset, int subsetIndex, int nextIndex, int n, int k) {
        if (subsetIndex == k) {
            subsets.add(Arrays.copyOf(subset, subset.length));
            return;
        }
        for (int i = nextIndex; i < n; i++) {
            subset[subsetIndex] = i;
            generateSubsetsRecursive(subsets, subset, subsetIndex + 1, i + 1, n, k);
        }
    }

    private static void generateRollsForSubset(List<Integer> dice, int[] subset, List<List<Integer>> allRolls) {
        int subsetSize = subset.length;
        int[] values = new int[subsetSize];

        generateRollsRecursive(dice, subset, values, 0, allRolls);
    }

    private static void generateRollsRecursive(List<Integer> dice, int[] subset, int[] values, int index, List<List<Integer>> allRolls) {
        if (index == values.length) {
            List<Integer> roll = new ArrayList<>(dice);
            for (int i = 0; i < subset.length; i++) {
                roll.set(subset[i], values[i]);
            }
            allRolls.add(roll);
            return;
        }
        for (int i = 1; i <= 6; i++) {
            values[index] = i;
            generateRollsRecursive(dice, subset, values, index + 1, allRolls);
        }
    }

    private static String determineCombination(List<Integer> dice) {
        Map<Integer, Long> frequencies = dice.stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        if (frequencies.containsValue(5L)) {
            return "General";
        } else if (frequencies.containsValue(4L)) {
            return "Four of a Kind";
        } else if (frequencies.containsValue(3L) && frequencies.containsValue(2L)) {
            return "Fullhouse";
        } else if (isStreet(dice)) {
            return "Street";
        }
        return null;
    }

    private static int countChanges(List<Integer> original, List<Integer> target) {
        int changes = 0;
        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).equals(target.get(i))) {
                changes++;
            }
        }
        return changes;
    }

    private static boolean[] calculateChanges(List<Integer> original, List<Integer> target) {
        boolean[] changes = new boolean[original.size()];
        for (int i = 0; i < original.size(); i++) {
            changes[i] = !original.get(i).equals(target.get(i));
        }
        return changes;
    }

    private static boolean shouldMaximizeSingleDice(List<Integer> dice, int rerollAttempt, int roundsLeft) {
        int threshold = rerollAttempt == 1 ? 4 : 3;
        long countToReroll = dice.stream().filter(num -> Collections.frequency(dice, num) < threshold).count();

        boolean hasMaximizableValue = IntStream.rangeClosed(1, 6)
                .anyMatch(value -> !computerUsedCombinations.containsKey(String.valueOf(value)));

        return roundsLeft < 5 && countToReroll >= threshold && hasMaximizableValue;
    }

    private static boolean[] maximizeSingleDice(List<Integer> dice) {
        Map<Integer, Long> frequencies = dice.stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        int targetNumber = frequencies.keySet().stream()
                .filter(num -> !computerUsedCombinations.containsKey("" + num))
                .max(Comparator.naturalOrder())
                .orElse(dice.get(0));

        boolean[] changes = new boolean[dice.size()];
        for (int i = 0; i < dice.size(); i++) {
            if (!dice.get(i).equals(targetNumber)) {
                changes[i] = true;
            }
        }
        return changes;
    }
}