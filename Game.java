import javafx.application.Application; 
import javafx.stage.Stage;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.input.KeyEvent;
import java.awt.MouseInfo;
import java.awt.Point;
import javafx.animation.AnimationTimer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Text;

public class Game extends Application {
    private static int timer = 0;
    private static int globalTimer = 1;

    private static float delayDefault = 40;
    private static float speedDefault = 2;
    private static float delay = delayDefault;
    private static float speed = speedDefault;

    private boolean mousePressed = false;

    private Text score;
    private Point mouse;
    private Circle player;
    private static Pane pane;

    private static int highscore;

    private static ArrayList<Circle> enemies = new ArrayList<Circle>();
    private static ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();

    public static void main() {
        Application.launch();
    }

    @Override
    public void start(Stage primary) {
        //Load Highscore
        try {
            FileReader fileReader = new FileReader("data.dat");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            highscore = Integer.parseInt(bufferedReader.readLine());
        } catch (IOException error) {
            highscore = 0;
        }

        //Window Setup
        primary.setWidth(1350);
        primary.setHeight(700);
        primary.centerOnScreen();
        
        String title = "Dodge";
        pane = new Pane();

        //Background
        Rectangle background = new Rectangle();
        background.setX(0);
        background.setY(0);
        background.setWidth(1350);
        background.setHeight(700);
        background.setFill(Color.RED);

        pane.getChildren().add(background);

        //Player
        player = new Circle();
        player.setRadius(50);

        //PlayerShadow
        Circle playerShadow = new Circle();
        playerShadow.setRadius(50);
        playerShadow.setFill(Color.BLACK);

        //Score
        score = new Text();
        score.setFill(Color.WHITE);
        score.setFont(Font.font("Courier"
            ,FontWeight.BOLD
            ,250
            ));
        score.translateXProperty().bind(pane.widthProperty().divide(2).subtract(100));
        score.translateYProperty().bind(pane.heightProperty().divide(2));
        score.setTextAlignment(TextAlignment.CENTER);

        //Score Effects
        Text scoreEffectsText = new Text();
        scoreEffectsText.setFill(Color.LIGHTGREEN);
        scoreEffectsText.setFont(Font.font("Courier"
            ,FontWeight.BOLD
            ,250
            ));
        scoreEffectsText.translateXProperty().bind(pane.widthProperty().divide(2).subtract(100));
        scoreEffectsText.translateYProperty().bind(pane.heightProperty().divide(2));
        scoreEffectsText.setTextAlignment(TextAlignment.CENTER);
        scoreEffectsText.setOpacity(0);

        //Highscore
        Text highscoreText = new Text();
        highscoreText.setFill(Color.WHITE);
        highscoreText.setFont(Font.font("Courier"
            ,FontWeight.BOLD
            ,25
            ));
        highscoreText.setX(20);
        highscoreText.setY(35);
        highscoreText.setTextAlignment(TextAlignment.CENTER);

        //Floor rectangles
        for (int x = 0; x < 1350; x+=50) {
            for (int y = 0; y < 700; y+=50) {
                Rectangle r = new Rectangle();
                r.setX(x);
                r.setY(y);
                r.setWidth(50);
                r.setHeight(50);
                r.setFill(Color.LIGHTBLUE);
                r.setStroke(Color.DARKGRAY);
                r.setStrokeWidth(0.2);
                rectangles.add(r);
                pane.getChildren().add(r);
            }
        }

        //Animation
        new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                if (mousePressed) {
                    player.setFill(Color.CRIMSON);
                    playerShadow.setOpacity(0);
                } else {
                    player.setFill(Color.RED);
                    playerShadow.setOpacity(0.7);
                }

                //Setting Player Position
                mouse = MouseInfo.getPointerInfo().getLocation();

                player.setCenterX(mouse.getX());
                playerShadow.setCenterX(mouse.getX()+4);
                player.setCenterY(mouse.getY());
                playerShadow.setCenterY(mouse.getY()+4);
                if (player.getCenterY() > 700) {
                    player.setCenterY(700);
                    playerShadow.setCenterY(700+4);
                }
                if (player.getCenterY() < 0) {
                    player.setCenterY(0);
                    playerShadow.setCenterY(0+4);
                }
                if (player.getCenterX() < 0) {
                    player.setCenterX(0);
                    playerShadow.setCenterX(0+4);
                }
                if (player.getCenterX() > 1350) {
                    player.setCenterX(1350);
                    playerShadow.setCenterX(1350+4);
                }

                highscoreText.setText("Highscore: "+highscore);

                //Spawn enemies
                if (timer > delay) {   
                    Circle enemy = new Circle();

                    enemy.setCenterX(-20);
                    enemy.setCenterY(randomGenerator((int) now+10000));
                    enemy.setRadius(20);
                    enemy.setFill(Color.BLACK);

                    enemies.add(enemy);
                    pane.getChildren().add(enemy);

                    //Difficulty Management
                    if ((globalTimer/60) < 40) {
                        delay-=0.7;
                        speed+=0.1;
                        if (delay < 11) {
                            delay = 11;
                        }
                        if (speed > 5) {
                            speed = 5;
                        }
                    } else {
                        delay-=0.025;
                        speed+=0.025;
                        if (delay < 5) {
                            delay = 5;
                        }
                        if (speed > 8) {
                            speed = 8;
                        }
                    }
                    timer = 0;
                }

                //Flash score green if beaten highscore
                if ((globalTimer/60) >= highscore && (globalTimer/60) <= highscore+1) {
                    scoreEffectsText.setOpacity(((float)Math.abs(globalTimer - (highscore+2)*60))/240);
                } else {
                    scoreEffectsText.setOpacity(0);
                }

                //Enemy Management
                for (Circle enemy: enemies) {
                    enemy.setCenterX(enemy.getCenterX()+speed);
                    if (circleCircleCollision(player.getCenterX(), player.getCenterY(), enemy.getCenterX(), enemy.getCenterY(), player.getRadius(), enemy.getRadius())) {
                        if (!mousePressed) {
                            enemy.setCenterX(4000);
                            die();
                        }
                    }
                }

                //Floor Tile Management
                for (Rectangle rectangle: rectangles) {
                    if (mousePressed) {
                        if (rectangleCircleCollision(rectangle,player)) {
                            rectangle.setOpacity(rectangle.getOpacity()-0.075);
                            if (rectangle.getOpacity() < 0) {
                                rectangle.setOpacity(0);
                                die();
                            }
                        }
                    }
                }

                //Update Score
                score.setText(""+(globalTimer/60));
                scoreEffectsText.setText(""+(globalTimer/60));

                //Write Highscore
                if ((globalTimer/60) > highscore) {
                    highscore = (int) (globalTimer/60);

                    try {
                        File file = new File("data.dat");
                        file.createNewFile();

                        FileWriter fileWriter = new FileWriter("data.dat");
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.write(""+((int) (globalTimer/60)));
                        bufferedWriter.close();
                    } catch(IOException error) {}
                }

                timer++;
                globalTimer++;
            }
        }.start();

        //Apply to scene
        pane.getChildren().add(score);
        pane.getChildren().add(scoreEffectsText);
        pane.getChildren().add(playerShadow);
        pane.getChildren().add(player);
        pane.getChildren().add(highscoreText);

        Scene scene = new Scene(pane, 200, 200);
        primary.setTitle(title);
        primary.setScene(scene);
        primary.show();

        scene.setOnMouseReleased(event -> this.handleMouseRelease());
        scene.setOnMousePressed(event -> this.handleMousePress());

        scene.setCursor(Cursor.NONE);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primary.setX(bounds.getMinX());
        primary.setY(bounds.getMinY());
        primary.setWidth(bounds.getWidth());
        primary.setHeight(bounds.getHeight());

    }

    private void handleMousePress() {
        mousePressed = true;
    }

    private void handleMouseRelease() {
        mousePressed = false;
    }

    private static int randomGenerator(int seed) {
        Random generator = new Random(seed);
        return generator.nextInt(675 + 1);
    }

    private static boolean circleCircleCollision(double x1, double y1, double x2, double y2, double r1, double r2) {
        double xDif = x1 - x2;
        double yDif = y1 - y2;
        double distanceSquared = xDif * xDif + yDif * yDif;
        return distanceSquared < (r1 + r2) * (r1 + r2);
    }

    public static boolean rectangleCircleCollision(Rectangle r, Circle c) {
        float cx = Math.abs((float)c.getCenterX() - (float)r.getX() - (float)(r.getWidth()/2));
        float xDist = (float)(r.getWidth()/2) + (float)c.getRadius();
        if (cx > xDist) {
            return false;
        }
        float cy = Math.abs((float)c.getCenterY() - (float)r.getY() - (float)(r.getHeight()/2));
        float yDist = (float)(r.getHeight()/2) + (float)c.getRadius();
        if (cy > yDist) {
            return false;
        }
        if (cx <= (float)(r.getWidth()/2) || cy <= (float)(r.getHeight()/2)) {
            return true;
        }
        float xCornerDist = cx - (float)(r.getWidth()/2);
        float yCornerDist = cy - (float)(r.getHeight()/2);
        float xCornerDistSq = xCornerDist * xCornerDist;
        float yCornerDistSq = yCornerDist * yCornerDist;
        float maxCornerDistSq = (float)c.getRadius() * (float)c.getRadius();
        return xCornerDistSq + yCornerDistSq <= maxCornerDistSq;
    }

    private static void die() {
        speed = speedDefault;
        delay = delayDefault;
        globalTimer = 1;
        timer = 0;

        for (Circle enemy: enemies) {
            enemy.setCenterX(4000);
        }
        enemies = new ArrayList<Circle>();

        for (Rectangle tile: rectangles) {
            tile.setOpacity(1);
        }
    }
}