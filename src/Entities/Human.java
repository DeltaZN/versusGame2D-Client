package Entities;

import Entities.exceptions.NotAliveException;
import GUI.Main;
import Server.Command;
import ServerCon.ClientCommandHandler;
import World.Location;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Human extends FlowPane implements Moveable, Comparable<Human>, Serializable {
    private String name;
    private Location loc;
    private int hp = 100;
    private Moves lastMove = Moves.BACK;
    private LocalDateTime dateOfCreation;
    private double speedModifier = 1.0;
    private String user = "default";
    private Rectangle col_rec;

    public Moves getLastMove() {
        return lastMove;
    }

    public Rectangle getCol_rec() {
        return col_rec;
    }

    public void setCol_rec(Rectangle col_rec) {
        this.col_rec = col_rec;
    }

    public void setUser(String str) {
        user = str;
    }

    public String getUser() {
        return user;
    }

    public Human(String iName) {
        this.name = iName;
        this.loc = new Location(0, 0);
        dateOfCreation = LocalDateTime.now();
    }

    public Human(String iName, Location iLoc) {
        this.name = iName;
        this.loc = iLoc;
        dateOfCreation = LocalDateTime.now();
    }

    public Human(String iName, Location iLoc, LocalDateTime date) {
        this.name = iName;
        this.loc = iLoc;
        dateOfCreation = date;
        setTranslateY(loc.getY());
        setTranslateX(loc.getX());
    }

    public void show() {

    }

    public void hide() {
        try {
            ClientCommandHandler.mainWindow.getMainController().getGraphics().getChildren().removeAll(this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        getChildren().clear();
    }

    public void moveOther(Moves move) {
        if (isAlive()) {

            lastMove = move;

            setTranslateY(getTranslateY() + move.getY() * speedModifier);
            setTranslateX(getTranslateX() + move.getX() * speedModifier);

            loc.setXY(loc.getX() + move.getX() * speedModifier, loc.getY() + move.getY() * speedModifier);
        }
    }

    public boolean checkIntersects(Human h) {
        if (((Path) Shape.intersect(col_rec, h.col_rec)).getElements().size() > 0) return true;
        else return false;
    }

    public void teleportOther(double x, double y) {
        setTranslateX(x);
        setTranslateY(y);
        loc.setXY(x, y);
    }

    public void teleport(double x, double y) {
        setTranslateX(x);
        setTranslateY(y);
        loc.setXY(x, y);
        ClientCommandHandler.dH.executeCommand(new Command("teleport", x+"", y+""));
    }

    public void move(Moves move) throws NotAliveException {
        if (isAlive()) {
            lastMove = move;

            setTranslateY(getTranslateY() + move.getY() * speedModifier);
            setTranslateX(getTranslateX() + move.getX() * speedModifier);

            boolean intersects = false;
            for (Node n : ClientCommandHandler.mainWindow.getMainController().getGraphics().getChildren()) {
                if (n instanceof Human) {
                    Human h = (Human) n;
                    if (h.col_rec != this.col_rec) {
                        if (checkIntersects(h)) {
                            intersects = true;
                            System.out.println("Касание");
                            setTranslateY(getTranslateY() - move.getY() * speedModifier);
                            setTranslateX(getTranslateX() - move.getX() * speedModifier);
                            while (checkIntersects(h)) {
                                teleport(loc.getX() + 10, loc.getY() + 10);
                            }
                            break;
                        }
                    }
                }
                if (n instanceof Wall) {
                    Wall h = (Wall) n;
                    if(((Path) Shape.intersect(col_rec, h.getWall())).getElements().size() > 0) {
                        intersects = true;
                        System.out.println("Касание");
                        setTranslateY(getTranslateY() - move.getY() * speedModifier);
                        setTranslateX(getTranslateX() - move.getX() * speedModifier);
                        while (((Path) Shape.intersect(col_rec, h.getWall())).getElements().size() > 0) {
                            teleport(loc.getX() + 10, loc.getY() + 10);
                        }
                    }
                }
                if (n instanceof BigWall) {
                    BigWall h = (BigWall) n;
                    if(((Path) Shape.intersect(col_rec, h.getWall())).getElements().size() > 0) {
                        intersects = true;
                        System.out.println("Касание");
                        setTranslateY(getTranslateY() - move.getY() * speedModifier);
                        setTranslateX(getTranslateX() - move.getX() * speedModifier);
                        while (((Path) Shape.intersect(col_rec, h.getWall())).getElements().size() > 0) {
                            teleport(loc.getX() + 10, loc.getY() + 10);
                        }
                    }
                }
            }

            if (!intersects) {
                ClientCommandHandler.dH.executeCommand(new Command("move", move.toString()));
                loc.setXY(loc.getX() + move.getX() * speedModifier, loc.getY() + move.getY() * speedModifier);
                System.out.println("Перемещение " + loc);
            }
        } else System.out.println("Перемещние невозможно");

    }

    public void shootOther() {
        System.out.println("SHOOT");
        final Shape bullet = new Circle(2, Color.ORANGE);
        ClientCommandHandler.mainWindow.getMainController().getGraphics().getChildren().add(bullet);
        final TranslateTransition bulletAnimation = new TranslateTransition(Duration.seconds(2), bullet);

        ((Circle) bullet).setCenterX(getLocation().getX()+16);
        ((Circle) bullet).setCenterY(getLocation().getY()+32);

        bulletAnimation.setToX(getLastMove().getX()*1000);
        bulletAnimation.setToY(getLastMove().getY()*1000);

        bullet.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            Pane fr = ClientCommandHandler.mainWindow.getMainController().getGraphics();
            for (Node n : fr.getChildren()) {
                if (n instanceof Human) {
                    Human h = (Human) n;
                    if (h.getCol_rec() != this.getCol_rec()) {
                        if (((Path)Shape.intersect(bullet, h.getCol_rec())).getElements().size() > 0) {
                            System.out.println("Hit!");
                            h.setHealth(h.getHealth() - 10);
                            if (h == ClientCommandHandler.getPlayerClient()) {
                                double maxHealt = 0;
                                if (h instanceof Spy)
                                    maxHealt = 100;
                                else maxHealt = 150;
                                ClientCommandHandler.getHpBar().setWidth(56.0*(((double)h.getHealth())/maxHealt));
                            }
                            bulletAnimation.stop();
                            fr.getChildren().remove(bullet);
                            break;
                        }
                    }
                }
                if (n instanceof BigWall) {
                    BigWall h = (BigWall) n;
                    if (((Path)Shape.intersect(bullet, h.getWall())).getElements().size() > 0) {
                        System.out.println("Hit!");
                        bulletAnimation.stop();
                        fr.getChildren().remove(bullet);
                        break;
                    }
                }
                if (n instanceof Wall) {
                    Wall h = (Wall) n;
                    if (((Path)Shape.intersect(bullet, h.getWall())).getElements().size() > 0) {
                        System.out.println("Hit!");
                        bulletAnimation.stop();
                        fr.getChildren().remove(bullet);
                        break;
                    }
                }
            }

        });

        bulletAnimation.setOnFinished(event -> ClientCommandHandler.mainWindow.getMainController().getGraphics().getChildren().remove(bullet));
        bulletAnimation.play();
    }


    public void shoot() {
        ClientCommandHandler.dH.executeCommand(new Command("shoot"));
        System.out.println("SHOOT");
        final Shape bullet = new Circle(2, Color.ORANGE);
        ClientCommandHandler.mainWindow.getMainController().getGraphics().getChildren().add(bullet);
        final TranslateTransition bulletAnimation = new TranslateTransition(Duration.seconds(2), bullet);
        ((Circle) bullet).setCenterX(getLocation().getX()+16);
        ((Circle) bullet).setCenterY(getLocation().getY()+32);

        bulletAnimation.setToX(getLastMove().getX()*1000);
        bulletAnimation.setToY(getLastMove().getY()*1000);

        bullet.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            Pane fr = ClientCommandHandler.mainWindow.getMainController().getGraphics();
            for (Node n : fr.getChildren()) {
                if (n instanceof Human) {
                    Human h = (Human) n;
                    if (h.getCol_rec() != this.getCol_rec()) {
                        if (((Path)Shape.intersect(bullet, h.getCol_rec())).getElements().size() > 0) {
                            System.out.println("Hit!");
                            h.setHealth(h.getHealth() - 10);
                            ClientCommandHandler.dH.executeCommand(new Command("hit", h.getName()));
                            bulletAnimation.stop();
                            fr.getChildren().remove(bullet);
                            break;
                        }
                    }
                }
                if (n instanceof BigWall) {
                    BigWall h = (BigWall) n;
                    if (((Path)Shape.intersect(bullet, h.getWall())).getElements().size() > 0) {
                        System.out.println("Hit!");
                        bulletAnimation.stop();
                        fr.getChildren().remove(bullet);
                        break;
                    }
                }
                if (n instanceof Wall) {
                    Wall h = (Wall) n;
                        if (((Path) Shape.intersect(bullet, h.getWall())).getElements().size() > 0) {
                            System.out.println("Hit!");
                            bulletAnimation.stop();
                            fr.getChildren().remove(bullet);
                            break;
                    }
                }
            }

        });

        bulletAnimation.setOnFinished(event -> ClientCommandHandler.mainWindow.getMainController().getGraphics().getChildren().remove(bullet));
        bulletAnimation.play();

    }

    public int getHealth() {
        return hp;
    }

    public Location getLocation() {
        return loc;
    }

    public double getSpeedModifier() {
        return speedModifier;
    }

    public void setSpeedModifier(Double mod) {
        speedModifier = mod;
    }

    public void died(Human human) {
        hp = 0;
        System.out.println(name + " был убит " + human.getName() + "ом");
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return hp > 0 ? true : false;
    }

    @Override
    public String toString() {
        return name + getClass().toString().replace("class Entities.", " ") + " "+ loc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Human)) return false;
        Human human = (Human) o;
        return hp == human.hp &&
                Double.compare(human.speedModifier, speedModifier) == 0 &&
                Objects.equals(name, human.name) &&
                Objects.equals(loc, human.loc) &&
                lastMove == human.lastMove &&
                Objects.equals(dateOfCreation, human.dateOfCreation) &&
                Objects.equals(user, human.user);
    }

    public LocalDateTime getDate() {
        return dateOfCreation;
    }

    public int compareTo(Human human) {
        return human.name.length() - name.length();
    }

    public void setHealth(int health) {
        hp = health;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, loc, hp);
    }

    public double distance(Moveable moveable) {
        return Math.sqrt(Math.pow(getLocation().getY()-moveable.getLocation().getY(), 2.0)
                + Math.pow((getLocation().getX()-moveable.getLocation().getX()), 2.0));
    }


}