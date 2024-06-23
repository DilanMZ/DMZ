package org.example.eiscuno.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.eiscuno.model.alert.AlertBox;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.machine.unoButtonMonitor;
import org.example.eiscuno.model.observer.Observer;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.unoenum.EISCUnoEnum;
import javafx.scene.control.Button;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameUnoController implements Observer {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private Label logoEiscUno;
    @FXML
    private Label sayOne;

    @FXML
    private Button unoButton;

    @FXML
    private Button takeCardButton;

    @FXML
    public Label plusTwoPlusFourMessage;

    @FXML
    public Label attackMessage;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;
    private boolean controlButton = false;

    private boolean unoButtonPressed=false;
    private final AtomicBoolean unoButtonPressed1 = new AtomicBoolean(false);
    private int posInitCardToShow1;
    private int colorChosen; // 1.Yellow  2.Red  3.Blue  4.Green

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        initVariables();
        setImages();
        this.gameUno.addObserver(this);
        this.gameUno.startGame();

        ImprimeCartaHumana();
        printCartasMaquina();

        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer(), this);
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this, deck, this.gameUno);
        threadPlayMachine.start();
        unoButtonMonitor monitor = new unoButtonMonitor(this.machinePlayer,this);
        monitor.start();
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
        this.posInitCardToShow1 = 0;


    }

    public void setImages() {
        Image imageUno = new Image(getClass().getResourceAsStream(EISCUnoEnum.UNO.getFilePath()));
        ImageView imageViewUno = new ImageView(imageUno);
        imageViewUno.setFitWidth(logoEiscUno.getWidth());
        imageViewUno.setFitHeight(logoEiscUno.getHeight());
        logoEiscUno.setGraphic(imageViewUno);

        Image imageDeckOfCards = new Image(getClass().getResourceAsStream(EISCUnoEnum.DECK_OF_CARDS.getFilePath()));
        ImageView imageViewDeckOfCards = new ImageView(imageDeckOfCards);
        imageViewDeckOfCards.setFitWidth(takeCardButton.getWidth());
        imageViewDeckOfCards.setFitHeight(takeCardButton.getHeight());
        takeCardButton.setGraphic(imageViewDeckOfCards);

        Image imageUnoButton = new Image(getClass().getResourceAsStream(EISCUnoEnum.BUTTON_UNO.getFilePath()));
        ImageView imageViewUnoButton = new ImageView(imageUnoButton);
        imageViewUnoButton.setFitWidth(unoButton.getWidth());
        imageViewUnoButton.setFitHeight(unoButton.getHeight());
        unoButton.setGraphic(imageViewUnoButton);
    }

    private int EncuentraCartaMaquina(String path) {
        Card card;
        for (int i=0; i < machinePlayer.getCardsPlayer().size() ; i++) {
            card = machinePlayer.getCardsPlayer().get(i);
            if (card.getPath().equals(path)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * imprime la carta humana en el grid pane
     */
    private void ImprimeCartaHumana() {

        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);
        if(currentVisibleCardsHumanPlayer.length == 0){
            AlertBox alertBox = new AlertBox();
            alertBox.showMessage("Felicitaciones!", "Has Ganado el juego!", "Sos dios");
        }

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                // Verify if the card can be played on the table
                plusTwoPlusFourMessage.setVisible(false);
                attackMessage.setVisible(false);

                if (table.isEmpty()) {
                    printCartaHumanaByCasos(card);
                    hasPlayerPlayed(true);
                    controlButton = true;

                } else if (card.getPath().contains("2_wild_draw")) {
                    if (table.getCurrentCardOnTheTable().getColor().equals(card.getColor())) {
                        //Two cards are added to the machine´s array of cards
                        int index = EncuentraCartaMaquina(card.getPath());
                        if (index != -1) {
                            printCartaHumanaByCasos(card);
                            machinePlayer.removeCard(index);
                            attackMessage.setText("La maquina ha atacado");
                            plusTwoPlusFourMessage.setText("Tu: +4");
                            plusTwoPlusFourMessage.setVisible(true);
                            attackMessage.setVisible(true);
                            gameUno.eatCard(humanPlayer, 4);
                            hasPlayerPlayed(false);


                        } else {
                            printCartaHumanaByCasos(card);
                            plusTwoPlusFourMessage.setText("Maquina: +2");
                            plusTwoPlusFourMessage.setVisible(true);
                            gameUno.eatCard(machinePlayer, 2);
                            hasPlayerPlayed(true);
                        }
                    }

                } else if (card.getPath().contains("4_wild_draw")) {
                    //Four cards are added to the machine´s array of cards
                    printCartaHumanaByCasos(card);
                    plusTwoPlusFourMessage.setText("Maquina: +4");
                    plusTwoPlusFourMessage.setVisible(true);
                    gameUno.eatCard(machinePlayer, 4);
                    hasPlayerPlayed(true);

                } else if (card.getPath().contains("reserve_")) {
                    if (table.getCurrentCardOnTheTable().getColor().equals(card.getColor())) {
                        //Give the turn to machine on reverse
                        printCartaHumanaByCasos(card);
                        hasPlayerPlayed(true);
                    }

                } else if (card.getPath().contains("skip_")) {
                    if (table.getCurrentCardOnTheTable().getColor().equals(card.getColor())) {
                        //Skip turn to machine - stop
                        printCartaHumanaByCasos(card);
                        attackMessage.setText("Oponente Bloqueado\n       Vuelve a lanzar");
                        attackMessage.setVisible(true);
                        hasPlayerPlayed(false);
                    }

                } else if (card.getPath().contains("wild_change")) {
                    mostrarEleccionDeColor();
                    printCartaHumanaByCasos(deck.getGhostCards().get(colorChosen));
                    hasPlayerPlayed(true);

                } else if (table.getCurrentCardOnTheTable().getPath().contains("4_wild_draw")) {
                    printCartaHumanaByCasos(card);
                    hasPlayerPlayed(true);
                } else if (table.getCurrentCardOnTheTable().getColor().equals(card.getColor()) || table.getCurrentCardOnTheTable().getValue().equals(card.getValue())) {
                    printCartaHumanaByCasos(card);
                    hasPlayerPlayed(true);
                }

            });


            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    public void printCartaHumanaByCasos(Card card) {
        gameUno.playCard(card); //puts the user's card on table's array
        tableImageView.setImage(card.getImage()); //puts the img on the table GUI
        humanPlayer.removeCard(findPosCardsPlayer(humanPlayer, card));
        ImprimeCartaHumana();
    }

    public void hasPlayerPlayed(boolean bool) {
        threadPlayMachine.setHasPlayerPlayed(bool);
    }


    //method used only for setting the Uno type cards on machine's deck
    private void printCartasMaquina() {
        Platform.runLater(() -> {
            this.gridPaneCardsMachine.getChildren().clear();
            Card[] currentVisibleCardsMachinePlayer = this.gameUno.getCurrentVisibleCardsMachinePlayer(this.posInitCardToShow1);

            for (int i = 0; i < currentVisibleCardsMachinePlayer.length; i++) {
                Image cardMachine = new Image(getClass().getResourceAsStream(EISCUnoEnum.CARD_UNO.getFilePath()));
                ImageView cardImageView = new ImageView(cardMachine);

                cardImageView.setY(16);
                cardImageView.setFitHeight(90);
                cardImageView.setFitWidth(70);

                this.gridPaneCardsMachine.add(cardImageView, i, 0);
            }
        });
    }

    /**
     * encuentra la carta del jugador humano
     *
     * @param card la carta a encontrar
     * @return la posicion de la carta
     */
    private Integer findPosCardsPlayer(Player player, Card card) {
        for (int i = 0; i < player.getCardsPlayer().size(); i++) {
            if (player.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Maneja la acción del botón atras para mostrar el mazo anterior.
     *
     * @param event evento de accion
     */
    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            this.gameUno.notifyObservers();
        }
    }

    @FXML
    void onHandleButtonExit(ActionEvent event) {
        System.exit(0);
    }

    /**
     * Maneja el boton siguiente para mostrar el mazo que sigue.
     *
     * @param event evento de accion
     */
    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            this.gameUno.notifyObservers();
        }
    }

    /**
     * Maneja la accion de tomar una carta
     *
     * @param event evento de accion
     */
    @FXML
    void onHandleTakeCard(ActionEvent event) {
        gameUno.eatCard(humanPlayer, 1);
        this.threadSingUNOMachine.setEat(true);
        this.gameUno.notifyObservers();
    }

    /**
     * Maneja la accion de decir Uno.
     *
     * @param event evento de accion
     */
    @FXML
    void onHandleUno(ActionEvent event) {
        setUnoButtonPressed();
        int uno = this.humanPlayer.getCardsPlayer().size();
        int unoMachine = this.machinePlayer.getCardsPlayer().size();
        if (uno == 1) {
            this.threadSingUNOMachine.setEat(false);
        }
        this.gameUno.notifyObservers();
    }

    private void mostrarEleccionDeColor() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Escoja un color");
        alert.setHeaderText("Escoja un color para continuar:");

        ButtonType redButton = new ButtonType("Rojo");
        ButtonType yellowButton = new ButtonType("Amarillo");
        ButtonType greenButton = new ButtonType("Verde");
        ButtonType blueButton = new ButtonType("Azul");

        alert.getButtonTypes().setAll(redButton, yellowButton, greenButton, blueButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == yellowButton) {
                System.out.println("Eligio Amarillo");
                colorChosen = 0;
            } else if (response == redButton) {
                System.out.println("Eligio Rojo");
                colorChosen = 1;
            } else if (response == blueButton) {
                System.out.println("Eligio Azul");
                colorChosen = 2;
            } else if (response == greenButton) {
                System.out.println("Eligio Verde");
                colorChosen = 3;
            }
            // Close the alert
            alert.close();
        });

        // This line ensures the application exits after the dialog is closed
        Platform.exit();
    }

    @FXML
    void saltarTurno(ActionEvent event) {
        if(controlButton){
            hasPlayerPlayed(true);
            attackMessage.setText("Saltaste turno");
            attackMessage.setVisible(true);
        }else{
            AlertBox alertBox = new AlertBox();
            alertBox.showMessage("Upss!","No te apures","Es muy temprano para saltar turno");
        }


    }


    public void diUnoLabel() {
        Platform.runLater(() -> {
            sayOne.setVisible(true);
            unoButtonPressed1.set(false);  // Reset the Uno button state

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), evt -> {
                if (!unoButtonPressed1.get()) {
                    System.out.println("The player did not press Uno in 3 seconds");
                }
                sayOne.setVisible(false);
                this.threadSingUNOMachine.setEat(true);
            }));
            timeline.setCycleCount(1);
            timeline.play();
        });
    }

    public void cantaUno() {
        Platform.runLater(() -> {
            gameUno.eatCard(humanPlayer, 1);
            this.threadSingUNOMachine.setEat(true);
        });
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            ImprimeCartaHumana();
            printCartasMaquina();
        });
    }

    public void muestraMazoMaquina() {
        Platform.runLater(() -> {
            printCartasMaquina();
        });
    }
    public void maquinaComeCartas() {
        Platform.runLater(() -> {
            gameUno.eatCard(machinePlayer,1);
            printCartasMaquina();
        });
    }
    public void setUnoButtonPressed() {
      this.unoButtonPressed=true;

    }
    public void restartButton(){
        this.unoButtonPressed=false;
    }
    public boolean getUnoButtonPressed(){
        return this.unoButtonPressed;
    }

}
