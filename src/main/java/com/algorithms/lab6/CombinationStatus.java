package com.algorithms.lab6;

import java.util.Map;

import static com.algorithms.lab6.DiceGameGeneralApplication.computerUsedCombinations;
import static com.algorithms.lab6.DiceGameGeneralApplication.userUsedCombinations;

public class CombinationStatus {
    private final String combination;
    private final String userStatus;
    private final String computerStatus;
    private final String combinationValue;

    public CombinationStatus(String combination, String userStatus, String computerStatus, String combinationValue) {
        this.combination = combination;
        this.userStatus = userStatus;
        this.computerStatus = computerStatus;
        this.combinationValue = combinationValue;
    }

    public String getCombination() {
        return combination;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public String getComputerStatus() {
        return computerStatus;
    }

    public String getCombinationValue() {
        return combinationValue;
    }

    static String getStatus(String combination, boolean isUser) {
        Map<String, Boolean> usedCombinations = isUser ? userUsedCombinations : computerUsedCombinations;
        return usedCombinations.getOrDefault(combination, false) ? "Used" : "Available";
    }

}