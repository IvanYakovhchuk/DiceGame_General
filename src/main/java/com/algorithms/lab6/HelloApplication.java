package com.algorithms.lab6;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloApplication extends Application {

    private static AtomicInteger userScore = new AtomicInteger(0);
    private static AtomicInteger computerScore = new AtomicInteger(0);
    private static boolean userTurn = false;
    private static boolean compTurn = false;
    private static List<Integer> rolledDice = new ArrayList<>();
    private static Map<String, Integer> dice = new HashMap<>();
    private static int attemptsLeft = 3;
    private static Button throwDice;
    private static Text myScore;
    private static Text oppScore;
    private static Text myTurn;
    private static Text oppTurn;
    private static Group diceRoot;

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

        myScore = new Text("My Score: 0");
        myScore.setFill(Color.WHITE);
        myScore.setLayoutX(10);
        myScore.setLayoutY(410);
        myScore.setFont(Font.loadFont(getClass().getResourceAsStream("/com/algorithms/lab6/fonts/Pacifico-Regular.ttf"), 24));
        root.getChildren().add(myScore);

        oppScore = new Text("Opponent's Score: 0");
        oppScore.setFill(Color.WHITE);
        oppScore.setLayoutX(410);
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
            throwDice.setDisable(false);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        timeline.play();
    }

    private void showDice() {
        Random rand = new Random();

        rolledDice.clear();

        for (int i = 0; i < 5; i++) {
            int scoreFromDice = rand.nextInt(1, 7); // Генеруємо значення від 1 до 6
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
        myTurn.setVisible(true);
        oppTurn.setVisible(false);
        myTurn.setText(String.format("My Turn (%d throws left)", attemptsLeft));
        rolledDice.clear();
        throwDice(() -> handleUserRoll());
    }

    private void handleUserRoll() {
        attemptsLeft--;
        myTurn.setText(String.format("My Turn (%d throws left)", attemptsLeft));
        List<CheckBox> checkBoxes = new ArrayList<>();

        if (attemptsLeft > 0) {
            for (int i = 0; i < rolledDice.size(); i++) {
                CheckBox checkBox = new CheckBox();
                checkBox.setLayoutX(165 + i * 70);
                checkBox.setLayoutY(250);
                checkBoxes.add(checkBox);
                diceRoot.getChildren().add(checkBox);
            }

            throwDice.setOnAction(e -> {
                rerollDice(checkBoxes);
                diceRoot.getChildren().removeAll(checkBoxes);
                handleUserRoll();
            });

        } else {
            int totalScore = rolledDice.stream()
                    .reduce(Integer::sum)
                    .orElse(0);

            userScore.addAndGet(totalScore);
            myScore.setText("My Score: " + userScore);

            rolledDice.clear();
            throwDice.setDisable(false);

            startComputerTurn();
        }
    }

    private void rerollDice(List<CheckBox> checkBoxes) {
        Random rand = new Random();

        for (int i = 0; i < checkBoxes.size(); i++) {
            final int index = i;
            if (checkBoxes.get(index).isSelected()) {
                int newScore = rand.nextInt(1, 7);
                rolledDice.set(index, newScore);

                Image diceImage = new Image(getClass().getResource(getKeyFromValue(newScore)).toExternalForm());
                ImageView imageView = new ImageView(diceImage);

                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setSmooth(true);
                imageView.setPreserveRatio(true);

                imageView.setLayoutX(150 + index * 70);
                imageView.setLayoutY(185);

                diceRoot.getChildren().set(index, imageView);
            }
        }
    }

    private void startComputerTurn() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            throwDice.setDisable(true);
            computerTurn();
            throwDice.setDisable(false);
        });
        pause.play();

        throwDice.setOnAction(e -> userTurn());
    }

    public void computerTurn() {
        myTurn.setVisible(false);
        oppTurn.setVisible(true);
        oppTurn.setText("Opponent's Turn (1 throws left)");
        throwDice(() -> {
            int totalScore = rolledDice.stream()
                    .reduce(Integer::sum)
                    .orElse(0);

            computerScore.addAndGet(totalScore);
            oppScore.setText("Opponent's Score: " + computerScore);

            rolledDice.clear();
        });
    }

    public void play() {
        throwDice.setOnAction(e -> {
            userTurn();
        });
    }


    public static String getKeyFromValue(int diceValue) {
        for (Map.Entry<String, Integer> entry : dice.entrySet()) {
            if (entry.getValue() == diceValue) {
                return entry.getKey();
            }
        }
        return null;
    }
}