package it.unicam.cs.mpgc.rpg130131.view;

import javafx.scene.control.ListView;

/** Registro dei messaggi di gioco, con scorrimento automatico all'ultimo. */
public class LogView extends ListView<String> {

    private static final int MASSIMO_RIGHE = 200;

    public LogView() {
        setPrefHeight(110);
    }

    public void aggiungi(String messaggio) {
        getItems().add(messaggio);
        if (getItems().size() > MASSIMO_RIGHE) {
            getItems().remove(0);
        }
        scrollTo(getItems().size() - 1);
    }

    public void svuota() {
        getItems().clear();
    }
}