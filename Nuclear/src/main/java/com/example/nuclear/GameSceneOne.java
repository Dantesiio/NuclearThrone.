package com.example.nuclear;

import com.example.nuclear.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class GameSceneOne {
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane fondoNivel;
    private GraphicsContext gc;

    private ArrayList<Level> levels;
    private int currentLevel = 0;

    @FXML
    public void initialize(){
        gc = canvas.getGraphicsContext2D();
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(this::handleKeyPressed);
        canvas.setOnKeyReleased(this::handleKeyReleased);
        canvas.setOnKeyReleased(this::onKeyReleased);
        canvas.setOnMousePressed(this::onMousePressed);
        avatar = new Avatar();
        levels = new ArrayList<>();
        //Generar el primer mapa
        Level l1 = new Level(0);
        Paredes paredes= new Paredes(new Vector(700,150));
        Enemy e = new Enemy(new Vector(400, 100));
        new Thread(e).start();
        l1.getEnemies().add(e);
        l1.getEnemies().add(new Enemy(new Vector(400, 300)));
        levels.add(l1);
        //Generar el segundo mapa
        Level l2 = new Level(1);
        l2.setColor(Color.GRAY);
        l2.getEnemies().add(new Enemy(new Vector(100, 100)));
        l2.getEnemies().add(new Enemy(new Vector(100, 300)));
        l2.getEnemies().add(new Enemy(new Vector(300, 300)));
        levels.add(l2);

        draw();

    }
    private boolean isAlive = true;

    private boolean Apressed = false;
    private boolean Wpressed = false;
    private boolean Spressed = false;
    private boolean Dpressed = false;

    private Avatar avatar;
    private Paredes paredes;

    public void handleKeyPressed(KeyEvent event) {
        System.out.println(event.getCode());
        switch (event.getCode()) {
            case W:
                Wpressed = true;
                avatar.keyPressed("W");
                break;
            case A:
                Apressed = true;
                avatar.keyPressed("A");
                break;
            case S:
                Spressed = true;
                avatar.keyPressed("S");
                break;
            case D:
                Dpressed = true;
                avatar.keyPressed("D");
                break;
            case SPACE:
                avatar.keyPressed("SPACE");
                break;
        }
    }

    public void handleKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case W:
                Wpressed = false;
                break;
            case A:
                Apressed = false;
                break;
            case S:
                Spressed = false;
                break;
            case D:
                Dpressed = false;
                break;
        }
    }

    public void onKeyReleased(KeyEvent event){
        switch (event.getCode()){
            case W: Wpressed = false; break;
            case A: Apressed = false; break;
            case S: Spressed = false; break;
            case D: Dpressed = false; break;
        }
    }

    private void onMousePressed(MouseEvent e) {
        System.out.println("X: " +e.getX() + "Y: "+e.getY());

        double diffX = e.getX() - avatar.pos.getX();
        double diffY = e.getY() - avatar.pos.getY();
        Vector diff = new Vector(diffX, diffY);
        diff.normalize();
        diff.setMag(4);


        levels.get(currentLevel).getBullets().add(
                new Bullet(
                        new Vector(avatar.pos.getX(), avatar.pos.getY()),
                        diff
                )
        );
    }

    public void draw(){
        Thread ae = new Thread(()->{
            while(isAlive){
                //Dibujar en el lienzo
                Level level = levels.get(currentLevel);

                Platform.runLater(()->{
                    gc.setFill(level.getColor());
                    gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
                    avatar.draw(gc);
                    for(int i=0 ; i<level.getBullets().size() ; i++){
                        level.getBullets().get(i).draw(gc);
                        if(isOutside(level.getBullets().get(i).pos.getX(), level.getBullets().get(i).pos.getY())){
                            level.getBullets().remove(i);
                        }
                    }
                    for(int i=0 ; i<level.getEnemies().size() ; i++){
                        level.getEnemies().get(i).draw(gc);
                    }
                });

                //Calculos geometricos

                //Paredes
                if(avatar.pos.getX() < 25){
                    avatar.pos.setX(25);
                }
                if(avatar.pos.getY() > canvas.getHeight() - 25){
                    avatar.pos.setY( canvas.getHeight() -25 );
                }
                if(avatar.pos.getX() > canvas.getWidth() -25){
                    avatar.pos.setX( canvas.getWidth() -25);
                }
                if(avatar.pos.getY() < 0){
                    currentLevel = 1;
                    avatar.pos.setY(canvas.getHeight());
                }

                //Colisiones
                for(int i=0 ; i<level.getBullets().size() ; i++){
                    Bullet bn = level.getBullets().get(i);
                    for(int j=0 ; j<level.getEnemies().size() ; j++){
                        Enemy en = level.getEnemies().get(j);

                        double distance = Math.sqrt(
                                Math.pow(en.pos.getX()-bn.pos.getX(), 2) +
                                        Math.pow(en.pos.getY()-bn.pos.getY(), 2)
                        );

                        if(distance < 5){
                            level.getBullets().remove(i);
                            level.getEnemies().remove(j);
                        }

                    }
                }

                if(Wpressed){
                    avatar.pos.setY(avatar.pos.getY()-3);
                }
                if (Apressed) {
                    avatar.pos.setX(avatar.pos.getX()-3);
                }
                if (Spressed) {
                    avatar.pos.setY(avatar.pos.getY()+3);
                }
                if (Dpressed) {
                    avatar.pos.setX(avatar.pos.getX()+3);
                }

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {e.printStackTrace();}
            }
        });
        ae.start();
    }

    public boolean isOutside(double x, double y){
        return x<-10 || y<-10 || x>canvas.getWidth() || y>canvas.getHeight();
    }
}
