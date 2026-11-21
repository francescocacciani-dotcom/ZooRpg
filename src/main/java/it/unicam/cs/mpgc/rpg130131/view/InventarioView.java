package it.unicam.cs.mpgc.rpg130131.view;

import it.unicam.cs.mpgc.rpg130131.controller.GameController;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Cibo;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Inventario;
import it.unicam.cs.mpgc.rpg130131.model.inventory.Medicina;
import it.unicam.cs.mpgc.rpg130131.model.inventory.StrumentoCattura;
import it.unicam.cs.mpgc.rpg130131.model.player.Giocatore;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Monete e oggetti posseduti dal giocatore: strumenti, cibi e medicine. */
public final class InventarioView extends VBox implements VistaAggiornabile {

    private final Label monete = new Label();
    private final ListView<String> elenco = new ListView<>();

    public InventarioView() {
        super(4);
        elenco.setPrefHeight(110);
        getChildren().addAll(new Label("Inventario"), monete, elenco);
    }

    @Override
    public void aggiorna(GameController controller) {
        Giocatore giocatore = controller.getGiocatore();
        monete.setText(Messaggi.monete(giocatore.getMonete()));

        Inventario inventario = giocatore.getInventario();
        List<String> righe = new ArrayList<>();
        inventario.getContenutoDi(StrumentoCattura.class)
                .forEach((strumento, quantita) -> righe.add(Messaggi.oggetto(strumento, quantita)));
        inventario.getContenutoDi(Cibo.class)
                .forEach((cibo, quantita) -> righe.add(Messaggi.oggetto(cibo, quantita)));
        inventario.getContenutoDi(Medicina.class)
                .forEach((medicina, quantita) -> righe.add(Messaggi.oggetto(medicina, quantita)));
        if (righe.isEmpty()) {
            righe.add("Vuoto");
        }
        elenco.getItems().setAll(righe);
    }
}