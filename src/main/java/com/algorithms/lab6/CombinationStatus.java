package com.algorithms.lab6;

import java.util.Map;

import static com.algorithms.lab6.HelloApplication.computerUsedCombinations;
import static com.algorithms.lab6.HelloApplication.userUsedCombinations;

public class CombinationStatus {
    private final String combination;
    private final String userStatus;
    private final String computerStatus;

    public CombinationStatus(String combination, String userStatus, String computerStatus) {
        this.combination = combination;
        this.userStatus = userStatus;
        this.computerStatus = computerStatus;
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

    static String getStatus(String combination, boolean isUser) {
        Map<String, Boolean> usedCombinations = isUser ? userUsedCombinations : computerUsedCombinations;
        return usedCombinations.getOrDefault(combination, false) ? "Used" : "Available";
    }

}