package sample;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS =7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String disk_color1 = "#24303E";
    private static final String disk_color2 = "#4CAA88";


    private static String playerOne = "Player One";
    private static String playerTwo = "Player Two";


    private boolean isPlayerOneTurn = true;

    private boolean isAllowedToInsert = true;  // TO avoid the same color disk added by one user

    private Disc insertedDiscsArray[][] = new Disc[ROWS][COLUMNS];  //For the Structural Changes

    @FXML
    public GridPane rootGridPane;

    @FXML
    public Pane insertDiskPane;

    @FXML
    public Label playerNameLabel;

    @FXML
    public TextField playerOneTextField, playerTwoTextField;

    @FXML
    public Button setNameButton;


    public void createPlayGround() {

        Shape ractangleWithHoles = createGameStructuralGrid();
        rootGridPane.add(ractangleWithHoles,0, 1);

        List<Rectangle> rectangleList = createClickableColumns();

        setNameButton.setOnAction(actionEvent -> {
            setPlayers();
        });

        for (Rectangle rectangle:rectangleList){
            rootGridPane.add(rectangle, 0,1);
        }

    }

    private void setPlayers() {


        // TODO TODO TODO TODO

       String inputPlayerOne = playerOneTextField.getText();
       playerOne = inputPlayerOne;

       String inputPlayerTwo = playerTwoTextField.getText();
       playerTwo = inputPlayerTwo;


       try{


            if (inputPlayerOne.isEmpty()){
                warnUser();
           }
            else if (inputPlayerTwo.isEmpty()){
                warnUser();
            }
       }catch (Exception e){
           warnUser();
           return;
       }

    }

    private void warnUser() {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Ouccured!");
        alert.setHeaderText("You Have Enter Name Here!");
        alert.setContentText("Please Enter Your Name");
        alert.show();
    }

    private Shape createGameStructuralGrid(){

        Shape ractangleWithHoles = new Rectangle(CIRCLE_DIAMETER*(COLUMNS+1), CIRCLE_DIAMETER*(ROWS+1));

        for(int row=0 ;row< ROWS ;row++){
            for(int col=0;col<COLUMNS;col++){

                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER/2);
                circle.setCenterX(CIRCLE_DIAMETER/2);
                circle.setCenterY(CIRCLE_DIAMETER/2);
                circle.setSmooth(true);

                circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) +CIRCLE_DIAMETER/4);
                circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

                ractangleWithHoles = Shape.subtract(ractangleWithHoles, circle);

            }
        }

        ractangleWithHoles.setFill(Color.WHITE);

       return ractangleWithHoles;
    }


    private List<Rectangle> createClickableColumns(){

        List<Rectangle> rectangleList = new ArrayList<>();

        for(int col =0;col<COLUMNS;col++) {

            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, CIRCLE_DIAMETER*(ROWS+1));
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

            rectangle.setOnMouseEntered( event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited( event -> rectangle.setFill(Color.TRANSPARENT));

            final int column = col;
            rectangle.setOnMouseClicked(event -> {

                if (isAllowedToInsert) {

                    isAllowedToInsert = false;   // When disc is being dropped then no more discs would be inserted
                    insertDisc(new Disc(isPlayerOneTurn), column);

                }
            });

            rectangleList.add(rectangle);

        }
        return rectangleList;

    }

    private void insertDisc(Disc disc, int column){

        int row = ROWS - 1;  //to place the discs upon a disc in column
        while (row >= 0){

            if ( getDiscPresent(row, column) == null)
              break;

              row--;
        }

        if (row < 0)   // if it is full, we cannot insert anymore disk
            return;


        insertedDiscsArray[row][column] = disc;  //For structural changes : For Developers
        insertDiskPane.getChildren().add(disc); //Visuals : For Player

        disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
        // disc.setTranslateY(5* (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
        int currentRow = row;

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc);
        translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
        translateTransition.setOnFinished(actionEvent -> {

            isAllowedToInsert = true;  // Finally when disc is dropped allow next player to insert disc
           if (gameEnded(currentRow ,column)){

               gameOver();
               return;

           }

            isPlayerOneTurn = !isPlayerOneTurn;  // player 2 turn --> toggle btw players
            playerNameLabel.setText(isPlayerOneTurn? playerOne:playerTwo);


        });
        translateTransition.play();

    }
    private boolean gameEnded(int row, int column){

        // virticle points. A small example : player has inserted his last disc at row= 2 col=3
        // range of row values = 0,1,2,3,4,5
        // index of each element present in column [row][column] = 0.3  1,3  2,3  3,3  4,3  5,3 --> Point2D  x,y

        List<Point2D> virticlePoints = IntStream.rangeClosed(row -3,row +3)  //range of row values = 0,1,2,3,4,5
                .mapToObj(r ->new Point2D(r, column))           //0.3  1,3  2,3  3,3  4,3  5,3 --> Point2D  x,y
                .collect(Collectors.toList());

        List<Point2D> horizontalPoints = IntStream.rangeClosed(column -3,column +3)  //range of row values = 0,1,2,3,4,5
                .mapToObj(col ->new Point2D(row, col))           //0.3  1,3  2,3  3,3  4,3  5,3 --> Point2D  x,y
                .collect(Collectors.toList());

        // Digonal Points

        Point2D startPoint1 = new Point2D(row -3, column +3);
        List<Point2D> digonal1Points = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> startPoint1.add(i, -i))
                .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row -3, column - 3);
        List<Point2D> digonal2Points = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> startPoint2.add(i, i))
                .collect(Collectors.toList());


        boolean isEnded = checkCombinations(virticlePoints) || checkCombinations(horizontalPoints)
                            ||checkCombinations(digonal1Points)|| checkCombinations(digonal2Points);

        return isEnded;

    }
    private boolean checkCombinations(List<Point2D> points){

        int chain=0;

        for (Point2D point: points){


            int rowIndexForArray = (int)point.getX();
            int columnIndexForArray = (int)point.getY();

            Disc disc = getDiscPresent(rowIndexForArray, columnIndexForArray);

            if ( disc !=null && disc.isPlayerOneMove == isPlayerOneTurn){

                chain++;

                if (chain==4){
                    return true;
                }
            }
            else {
                chain = 0;
            }
        }
        return false;
    }

    private Disc getDiscPresent(int row, int column) {   // to prevent arrayIndexOutOfBond

        if (row >=ROWS|| row<0|| column>= COLUMNS || column <0)//  if row or column index is Invalid
            return null;

            return insertedDiscsArray[row][column];


    }
    private void gameOver() {

        String winner = isPlayerOneTurn? playerOne:playerTwo;
        System.out.println("The Winner is : "+winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is: "+winner);
        alert.setContentText("Want to play again? ");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesBtn, noBtn);


        Platform.runLater(()->{   // prevent the exception
                                    // run the code(layout) after closing animation

            Optional<ButtonType> btnClicked = alert.showAndWait();
            if (btnClicked.isPresent() && btnClicked.get() == yesBtn){
                // ... user choose yes to reset game
                resetGame();
            }else {   // no to exit game
                Platform.exit();
                System.exit(0);
            }

        });

    }

    public void resetGame() {

        insertDiskPane.getChildren().clear();  // remove all the disc from Pane

        for (int row = 0; row < insertedDiscsArray.length; row++){          // make all element , structural change -->objects removed
            for (int column = 0; column < insertedDiscsArray[row].length;column++){
                insertedDiscsArray[row][column]=null;
            }
        }
        isPlayerOneTurn = true;
        playerNameLabel.setText(playerOne);

        createPlayGround();  // create a fresh playground
    }


    private static class Disc extends Circle{

        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove){

            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER/2);
            setFill(isPlayerOneMove? Color.valueOf(disk_color1): Color.valueOf(disk_color2));
            setCenterX(CIRCLE_DIAMETER/2);
            setCenterY(CIRCLE_DIAMETER/2);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
