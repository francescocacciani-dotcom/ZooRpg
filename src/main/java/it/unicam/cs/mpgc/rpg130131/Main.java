package it.unicam.cs.mpgc.rpg130131;

import it.unicam.cs.mpgc.rpg130131.view.GameApp;
import javafx.application.Application;

/** Punto di ingresso. Non estende Application: serve a poter avviare JavaFX da Gradle e IDE. */
public final class Main {

    public static void main(String[] args) {
        Application.launch(GameApp.class, args);
    }
}