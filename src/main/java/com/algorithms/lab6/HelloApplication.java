package com.algorithms.lab6;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HelloApplication extends Application {

    private static final int ROUNDS = 10;
    private static int roundNumber = 1;
    private static final int FIRST_ATTEMPT_GENERAL_SCORE = Integer.MAX_VALUE;
    private static final int SECOND_ATTEMPT_GENERAL_SCORE = 60;
    private static final int FIRST_ATTEMPT_FOUR_SCORE = 45;
    private static final int SECOND_ATTEMPT_FOUR_SCORE = 40;
    private static final int FIRST_ATTEMPT_FULLHOUSE_SCORE = 35;
    private static final int SECOND_ATTEMPT_FULLHOUSE_SCORE = 30;
    private static final int FIRST_ATTEMPT_STREET_SCORE = 25;
    private static final int SECOND_ATTEMPT_STREET_SCORE = 20;
    private static final AtomicInteger userScore = new AtomicInteger(0);
    private static final AtomicInteger computerScore = new AtomicInteger(0);
    private static final List<Integer> rolledDice = new ArrayList<>();
    private static final Map<String, Integer> dice = new HashMap<>();
    private static int attemptsLeft = 3;
    private static Button throwDice;
    private static Button restartButton;
    private static Text myScore;
    private static Text oppScore;
    private static Text myTurn;
    private static Text oppTurn;
    private static Text roundText;
    private static Group diceRoot;
    static Map<String, Boolean> userUsedCombinations = new HashMap<>();
    static Map<String, Boolean> computerUsedCombinations = new HashMap<>();

    static {
        dice.put("/com/algorithms/lab6/images/dice-six-faces-one.png", 1);
        dice.put("/com/algorithms/lab6/images/dice-six-faces-two.png", 2);
        dice.put("/com/algorithms/lab6/images/dice-six-faces-three.png", 3);
        dice.put("/com/algorithms/lab6/images/dice-six-faces-four.png", 4);
        dice.put("/com/algorithms/lab6/images/dice-six-faces-five.png", 5);
        dice.put("/com/algorithms/lab6/images/dice-six-faces-six.png", 6);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        AnchorPane root = fxmlLoader.load();
        diceRoot = new Group();
        root.getChildren().add(diceRoot);
        root.setStyle("-fx-background-image: url('" + getClass().getResource("/com/algorithms/lab6/images/background.jpg").toExternalForm() + "'); -fx-background-size: cover;");

        Scene scene = new Scene(root);
        scene.setFill(Color.YELLOW);
        stage.setTitle("General");
        stage.setScene(scene);
        stage.setResizable(false);

        throwDice = new Button("Throw Dice");
        throwDice.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        throwDice.setLayoutX(270);
        throwDice.setLayoutY(370);
        throwDice.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/LobsterTwo-Regular.ttf"), 18));
        root.getChildren().add(throwDice);

        restartButton = new Button("Restart");
        restartButton.setVisible(false);
        restartButton.setDisable(true);
        restartButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        restartButton.setLayoutX(280);
        restartButton.setLayoutY(370);
        restartButton.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/LobsterTwo-Regular.ttf"), 18));
        root.getChildren().add(restartButton);

        myScore = new Text("My Score: 0");
        myScore.setFill(Color.WHITE);
        myScore.setLayoutX(10);
        myScore.setLayoutY(410);
        myScore.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/Pacifico-Regular.ttf"), 24));
        root.getChildren().add(myScore);

        oppScore = new Text("Opponent's Score: 0");
        oppScore.setFill(Color.WHITE);
        oppScore.setLayoutX(400);
        oppScore.setLayoutY(30);
        oppScore.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/Pacifico-Regular.ttf"), 24));
        root.getChildren().add(oppScore);

        myTurn = new Text();
        myTurn.setVisible(false);
        myTurn.setFill(Color.LIGHTGREEN);
        myTurn.setLayoutX(225);
        myTurn.setLayoutY(170);
        myTurn.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/LobsterTwo-Regular.ttf"), 22));
        root.getChildren().add(myTurn);

        oppTurn = new Text();
        oppTurn.setVisible(false);
        oppTurn.setFill(Color.RED);
        oppTurn.setLayoutX(195);
        oppTurn.setLayoutY(170);
        oppTurn.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/LobsterTwo-Regular.ttf"), 22));
        root.getChildren().add(oppTurn);

        roundText = new Text();
        roundText.setVisible(true);
        roundText.setFill(Color.LIGHTGREEN);
        roundText.setLayoutX(275);
        roundText.setLayoutY(60);
        roundText.setFont(Font.font("Arial", 25));
        root.getChildren().add(roundText);

        Button showCombinationsButton = new Button();
        ImageView infoImageView = new ImageView(getClass().getResource("/com/algorithms/lab6/images/info.png").toExternalForm());
        infoImageView.setFitWidth(20);
        infoImageView.setFitHeight(20);
        showCombinationsButton.setGraphic(infoImageView);
        root.getChildren().add(showCombinationsButton);


        VBox tableContainer = new VBox();
        tableContainer.setSpacing(10);
        tableContainer.setVisible(false);
        TableView<CombinationStatus> tableView = new TableView<>();
        TableColumn<CombinationStatus, String> combinationColumn = new TableColumn<>("Combination");
        combinationColumn.setCellValueFactory(new PropertyValueFactory<>("combination"));
        TableColumn<CombinationStatus, String> userStatusColumn = new TableColumn<>("User Status");
        userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("userStatus"));
        TableColumn<CombinationStatus, String> computerStatusColumn = new TableColumn<>("Computer Status");
        computerStatusColumn.setCellValueFactory(new PropertyValueFactory<>("computerStatus"));
        tableView.getColumns().addAll(combinationColumn, userStatusColumn, computerStatusColumn);
        tableContainer.getChildren().add(tableView);
        showCombinationsButton.setOnAction(e -> {
            if (!tableContainer.isVisible()) {
                ObservableList<CombinationStatus> data = FXCollections.observableArrayList(
                        new CombinationStatus("General", CombinationStatus.getStatus("General", true), CombinationStatus.getStatus("General", false)),
                        new CombinationStatus("Four of a Kind", CombinationStatus.getStatus("Four of a Kind", true), CombinationStatus.getStatus("Four of a Kind", false)),
                        new CombinationStatus("Fullhouse", CombinationStatus.getStatus("Fullhouse", true), CombinationStatus.getStatus("Fullhouse", false)),
                        new CombinationStatus("Street", CombinationStatus.getStatus("Street", true), CombinationStatus.getStatus("Street", false)),
                        new CombinationStatus("6", CombinationStatus.getStatus(String.valueOf(6), true), CombinationStatus.getStatus(String.valueOf(6), false)),
                        new CombinationStatus("5", CombinationStatus.getStatus(String.valueOf(5), true), CombinationStatus.getStatus(String.valueOf(5), false)),
                        new CombinationStatus("4", CombinationStatus.getStatus(String.valueOf(4), true), CombinationStatus.getStatus(String.valueOf(4), false)),
                        new CombinationStatus("3", CombinationStatus.getStatus(String.valueOf(3), true), CombinationStatus.getStatus(String.valueOf(3), false)),
                        new CombinationStatus("2", CombinationStatus.getStatus(String.valueOf(2), true), CombinationStatus.getStatus(String.valueOf(2), false)),
                        new CombinationStatus("1", CombinationStatus.getStatus(String.valueOf(1), true), CombinationStatus.getStatus(String.valueOf(1), false))
                );
                tableView.setItems(data);
                tableContainer.setVisible(true);
            } else {
                tableContainer.setVisible(false);
            }
        });
        root.getChildren().add(tableContainer);

        root.setOnMouseClicked(event -> {
            if (tableContainer.isVisible() && !tableContainer.getBoundsInParent().contains(event.getX(), event.getY())) {
                tableContainer.setVisible(false);
            }
        });

        stage.show();

        play();
    }

    public static void main(String[] args) {
        launch();
    }

    private void throwDice(Runnable onFinish) {
        throwDice.setDisable(true);

        Timeline timeline = new Timeline();
        for (int i = 0; i < 10; i++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 300), event -> {
                diceRoot.getChildren().clear();
                showDice();
            }));
        }

        timeline.setOnFinished(event -> {
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(3));
            pauseTransition.setOnFinished(e -> {
                throwDice.setDisable(false);
                if (onFinish != null) {
                    onFinish.run();
                }
            });
            pauseTransition.play();
        });

        timeline.play();
    }

    private void showDice() {
        Random rand = new Random();
        rolledDice.clear();

        for (int i = 0; i < 5; i++) {
            int scoreFromDice = rand.nextInt(1, 7);
            Image diceImage = new Image(getClass().getResource(getKeyFromValue(scoreFromDice)).toExternalForm());
            ImageView imageView = new ImageView(diceImage);

            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(true);

            imageView.setLayoutX(150 + i * 70);
            imageView.setLayoutY(185);

            rolledDice.add(scoreFromDice);
            diceRoot.getChildren().add(imageView);
        }
    }

    public void userTurn() {
        attemptsLeft = 3;
        roundText.setText("Round " + roundNumber);
        myTurn.setVisible(true);
        oppTurn.setVisible(false);
        myTurn.setText(String.format("My Turn (%d throws left)", attemptsLeft));
        rolledDice.clear();
        throwDice(() -> handleUserRoll(new AtomicBoolean(true)));
    }

    private void handleUserRoll(AtomicBoolean isFirstAttempt) {
        attemptsLeft--;
        myTurn.setText(String.format("My Turn (%d throws left)", attemptsLeft));
        List<CheckBox> checkBoxes = new ArrayList<>();

        diceRoot.getChildren().removeIf(node -> node instanceof CheckBox);

        if (attemptsLeft > 0) {
            for (int i = 0; i < rolledDice.size(); i++) {
                CheckBox checkBox = new CheckBox();
                checkBox.setLayoutX(165 + i * 70);
                checkBox.setLayoutY(250);
                checkBoxes.add(checkBox);
                diceRoot.getChildren().add(checkBox);
            }

            throwDice.setOnAction(e -> {
                rerollDice(checkBoxes, () -> {
                    if (checkBoxes.stream().anyMatch(CheckBox::isSelected)) {
                        isFirstAttempt.set(false);
                    }
                    handleUserRoll(isFirstAttempt);
                });
            });

        } else {
            rerollDice(new ArrayList<>(), () -> {
                throwDice.setDisable(true);
                int totalScore = getCombinationScore(rolledDice, isFirstAttempt.get(), true);
                if (totalScore == Integer.MAX_VALUE) {
                    roundText.setText("You win!");
                    throwDice.setDisable(true);
                    throwDice.setVisible(false);
                    restartButton.setVisible(true);
                    restartButton.setDisable(false);
                    restartButton.setOnAction(e -> {
                        restartButton.setVisible(false);
                        restartButton.setDisable(true);
                        computerScore.set(0);
                        userScore.set(0);
                        roundNumber = 1;
                        roundText.setText("Round " + roundNumber);
                        oppScore.setText("Opponent's Score: " + computerScore.get());
                        myScore.setText("My Score: " + userScore.get());
                        userUsedCombinations.clear();
                        computerUsedCombinations.clear();
                        rolledDice.clear();
                        throwDice.setDisable(false);
                        throwDice.setVisible(true);
                        play();
                    });
                }
                else if (totalScore == 0) {
                    List<RadioButton> radioButtons = new ArrayList<>();
                    ToggleGroup toggleGroup = new ToggleGroup();

                    for (int i = 0; i < rolledDice.size(); i++) {
                        int value = rolledDice.get(i);
                            RadioButton radioButton = new RadioButton();
                            radioButton.setLayoutX(165 + i * 70);
                            radioButton.setLayoutY(250);
                            radioButton.setToggleGroup(toggleGroup);
                            radioButtons.add(radioButton);
                        if (!userUsedCombinations.containsKey(String.valueOf(value))) {
                            diceRoot.getChildren().add(radioButton);
                        }
                    }

                    throwDice.setVisible(false);
                    Button confirmButton = new Button("Confirm");
                    confirmButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    confirmButton.setLayoutX(280);
                    confirmButton.setLayoutY(370);
                    confirmButton.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/LobsterTwo-Regular.ttf"), 18));
                    diceRoot.getChildren().add(confirmButton);

                    confirmButton.setOnAction(e -> {
                        RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
                        if (selectedRadioButton != null) {
                            int selectedIndex = radioButtons.indexOf(selectedRadioButton);
                            int selectedValue = rolledDice.get(selectedIndex);

                            int selectedScore = (int) rolledDice.stream()
                                    .filter(value -> value == selectedValue)
                                    .count() * selectedValue;

                            userScore.addAndGet(selectedScore);
                            myScore.setText("My Score: " + userScore);

                            diceRoot.getChildren().removeAll(radioButtons);

                            userUsedCombinations.put(String.valueOf(selectedValue), true);
                        }

                        diceRoot.getChildren().remove(confirmButton);
                        rolledDice.clear();
                        startComputerTurn();
                        throwDice.setVisible(true);
                    });

                } else {
                    userScore.addAndGet(totalScore);
                    myScore.setText("My Score: " + userScore);

                    rolledDice.clear();
                    startComputerTurn();
                }
            });
        }
    }

    private void rerollDice(List<CheckBox> checkBoxes, Runnable onFinish) {
        Random rand = new Random();
        throwDice.setDisable(true);

        Timeline timeline = new Timeline();
        for (int i = 0; i < 10; i++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 300), event -> {
                for (int j = 0; j < checkBoxes.size(); j++) {
                    if (checkBoxes.get(j).isSelected()) {
                        int newScore = rand.nextInt(1, 7);
                        rolledDice.set(j, newScore);

                        String imagePath = getKeyFromValue(newScore);
                        Image diceImage = new Image(getClass().getResource(imagePath).toExternalForm());
                        ImageView imageView = new ImageView(diceImage);

                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        imageView.setSmooth(true);
                        imageView.setPreserveRatio(true);

                        imageView.setLayoutX(150 + j * 70);
                        imageView.setLayoutY(185);

                        diceRoot.getChildren().set(j, imageView);
                    }
                }
            }));
        }

        timeline.setOnFinished(e -> {
            throwDice.setDisable(false);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        timeline.play();
    }

    private void startComputerTurn() {
        computerTurn();
    }

    public void computerTurn() {
        attemptsLeft = 3;
        myTurn.setVisible(false);
        oppTurn.setVisible(true);
        oppTurn.setText(String.format("Opponent's Turn (%d throws left)", attemptsLeft));
        rolledDice.clear();
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> throwDice(() -> handleComputerRoll(new AtomicBoolean(true))));
        pause.play();
    }

    public void handleComputerRoll(AtomicBoolean isFirstAttempt) {
        attemptsLeft--;
        oppTurn.setText(String.format("Opponent's Turn (%d throws left)", attemptsLeft));
        boolean[] diceToRoll;

        if (attemptsLeft > 0) {
            List<Integer> tempDice = new ArrayList<>(rolledDice);
            diceToRoll = DiceCombinationChecker.determineChanges(rolledDice, ROUNDS - roundNumber);
            rerollDice(diceToRoll);
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                if (!rolledDice.equals(tempDice)) {
                    isFirstAttempt.set(false);
                }
                handleComputerRoll(isFirstAttempt);
            });

            pause.play();

        } else {
            int totalScore = getCombinationScore(rolledDice, isFirstAttempt.get(), false);
            if (totalScore == Integer.MAX_VALUE) {
                roundText.setText("You lose!");
                throwDice.setDisable(true);
                throwDice.setVisible(false);
                restartButton.setVisible(true);
                restartButton.setDisable(false);
                restartButton.setOnAction(e -> {
                    restartButton.setVisible(false);
                    restartButton.setDisable(true);
                    computerScore.set(0);
                    userScore.set(0);
                    roundNumber = 1;
                    roundText.setText("Round " + roundNumber);
                    oppScore.setText("Opponent's Score: " + computerScore.get());
                    myScore.setText("My Score: " + userScore.get());
                    userUsedCombinations.clear();
                    computerUsedCombinations.clear();
                    rolledDice.clear();
                    throwDice.setDisable(false);
                    throwDice.setVisible(true);
                    play();
                });
            }
            else if (totalScore == 0) {
                int maxScore = 0;
                int bestValue = 0;
                Map<Integer, Long> frequency = rolledDice.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                boolean lowValuesExcluded = rolledDice.stream()
                        .filter(v -> v == 1 || v == 2 || v == 3)
                        .allMatch(v -> computerUsedCombinations.containsKey(String.valueOf(v)));

                for (Map.Entry<Integer, Long> entry : frequency.entrySet()) {
                    int value = entry.getKey();
                    long count = entry.getValue();

                    if (!computerUsedCombinations.containsKey(String.valueOf(value))) {
                        int score = value * (int) count;

                        if (roundNumber <= 5 && (value == 4 || value == 5 || value == 6) && count < 3 && !lowValuesExcluded) {
                            continue;
                        }

                        if (score > maxScore) {
                            maxScore = score;
                            bestValue = value;
                        } else if (score == maxScore) {
                            if (count > frequency.get(bestValue)) {
                                bestValue = value;
                            } else if (count == frequency.get(bestValue) && value < bestValue) {
                                bestValue = value;
                            }
                        }
                    }
                }

                if (bestValue > 0) {
                    computerUsedCombinations.put(String.valueOf(bestValue), true);
                    computerScore.addAndGet(maxScore);
                    oppScore.setText("Opponent's Score: " + computerScore);
                }

                rolledDice.clear();
                throwDice.setDisable(false);
            } else {
                computerScore.addAndGet(totalScore);
                oppScore.setText("Opponent's Score: " + computerScore);
                rolledDice.clear();
                throwDice.setDisable(false);
            }

            throwDice.setOnAction(event -> {
                if (roundNumber == 10) {
                    if (computerScore.get() > userScore.get()) {
                        roundText.setText("You lose!");
                    } else if (computerScore.get() < userScore.get()){
                        roundText.setText("You win!");
                    } else {
                        roundText.setText("It's a draw!");
                    }
                    throwDice.setDisable(true);
                    throwDice.setVisible(false);
                    restartButton.setVisible(true);
                    restartButton.setDisable(false);
                    restartButton.setOnAction(e -> {
                        restartButton.setVisible(false);
                        restartButton.setDisable(true);
                        computerScore.set(0);
                        userScore.set(0);
                        roundNumber = 1;
                        roundText.setText("Round " + roundNumber);
                        oppScore.setText("Opponent's Score: " + computerScore.get());
                        myScore.setText("My Score: " + userScore.get());
                        userUsedCombinations.clear();
                        computerUsedCombinations.clear();
                        rolledDice.clear();
                        throwDice.setDisable(false);
                        throwDice.setVisible(true);
                        play();
                    });
                }
                else {
                    roundNumber++;
                    userTurn();
                }
            });
        }
    }

    public void rerollDice(boolean[] diceToRoll) {
        Random rand = new Random();
        throwDice.setDisable(true);

        Timeline timeline = new Timeline();
        for (int i = 0; i < 10; i++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 300), event -> {
                for (int j = 0; j < diceToRoll.length; j++) {
                    if (diceToRoll[j]) {
                        int randomValue = rand.nextInt(1, 7);
                        Image diceImage = new Image(getClass().getResource(getKeyFromValue(randomValue)).toExternalForm());
                        ImageView imageView = new ImageView(diceImage);

                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        imageView.setSmooth(true);
                        imageView.setPreserveRatio(true);

                        imageView.setLayoutX(150 + j * 70);
                        imageView.setLayoutY(185);

                        diceRoot.getChildren().set(j, imageView);
                    }
                }
            }));
        }

        timeline.setOnFinished(event -> {
            for (int j = 0; j < diceToRoll.length; j++) {
                if (diceToRoll[j]) {
                    int newScore = rand.nextInt(1, 7);
                    rolledDice.set(j, newScore);

                    Image diceImage = new Image(getClass().getResource(getKeyFromValue(newScore)).toExternalForm());
                    ImageView imageView = new ImageView(diceImage);

                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setSmooth(true);
                    imageView.setPreserveRatio(true);

                    imageView.setLayoutX(150 + j * 70);
                    imageView.setLayoutY(185);

                    diceRoot.getChildren().set(j, imageView);
                }
            }
        });

        timeline.play();
    }

    public void play() {
        throwDice.setOnAction(e -> userTurn());
    }

    public static String getKeyFromValue(int diceValue) {
        for (Map.Entry<String, Integer> entry : dice.entrySet()) {
            if (entry.getValue() == diceValue) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getCombinationScore(List<Integer> diceValues, boolean isFirstAttempt, boolean isUser) {
        Map<Integer, Long> frequencyMap = diceValues.stream()
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()));
        if (frequencyMap.containsValue(5L)) {
            if (isUser) {
                if (!userUsedCombinations.getOrDefault("General", false)) {
                    userUsedCombinations.put("General", true);
                }
                else {
                    return 0;
                }
            }
            else {
                if (!computerUsedCombinations.getOrDefault("General", false)) {
                    computerUsedCombinations.put("General", true);
                }
                else {
                    return 0;
                }
            }
            return isFirstAttempt ? FIRST_ATTEMPT_GENERAL_SCORE : SECOND_ATTEMPT_GENERAL_SCORE;
        }
        if (frequencyMap.containsValue(4L)) {
            if (isUser) {
                if (!userUsedCombinations.getOrDefault("Four of a Kind", false)) {
                    userUsedCombinations.put("Four of a Kind", true);
                }
                else {
                    return 0;
                }
            }
            else {
                if (!computerUsedCombinations.getOrDefault("Four of a Kind", false)) {
                    computerUsedCombinations.put("Four of a Kind", true);
                }
                else {
                    return 0;
                }
            }
            return isFirstAttempt ? FIRST_ATTEMPT_FOUR_SCORE : SECOND_ATTEMPT_FOUR_SCORE;
        }
        if (frequencyMap.containsValue(3L) && frequencyMap.containsValue(2L)) {
            if (isUser) {
                if (!userUsedCombinations.getOrDefault("Fullhouse", false)) {
                    userUsedCombinations.put("Fullhouse", true);
                }
                else {
                    return 0;
                }
            }
            else {
                if (!computerUsedCombinations.getOrDefault("Fullhouse", false)) {
                    computerUsedCombinations.put("Fullhouse", true);
                }
                else {
                    return 0;
                }
            }
            return isFirstAttempt ? FIRST_ATTEMPT_FULLHOUSE_SCORE : SECOND_ATTEMPT_FULLHOUSE_SCORE;
        }
        if (isStreet(diceValues)) {
            if (isUser) {
                if (!userUsedCombinations.getOrDefault("Street", false)) {
                    userUsedCombinations.put("Street", true);
                }
                else {
                    return 0;
                }
            }
            else {
                if (!computerUsedCombinations.getOrDefault("Street", false)) {
                    computerUsedCombinations.put("Street", true);
                }
                else {
                    return 0;
                }
            }
            return isFirstAttempt ? FIRST_ATTEMPT_STREET_SCORE : SECOND_ATTEMPT_STREET_SCORE;
        }
        return 0;
    }

    static boolean isStreet(List<Integer> diceValues) {
        return new HashSet<>(diceValues).containsAll(List.of(1, 2, 3, 4, 5)) ||
                new HashSet<>(diceValues).containsAll(List.of(2, 3, 4, 5, 6)) ||
                new HashSet<>(diceValues).containsAll(List.of(1, 3, 4, 5, 6)) ||
                new HashSet<>(diceValues).containsAll(List.of(1, 1, 3, 4, 5));
    }
}